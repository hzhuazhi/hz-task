package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.HttpSendUtils;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.*;
import com.hz.task.master.core.model.operate.OperateModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.HodgepodgeMethod;
import com.hz.task.master.util.TaskMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description task:任务订单（平台派发订单）
 * @Author yoko
 * @Date 2020/6/7 12:11
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskOrder {

    private final static Logger log = LoggerFactory.getLogger(TaskOrder.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;



    /**
     * @Description: task：检测派发订单是否失效
     * <p>
     *     每1每秒运行一次
     *     1.检测更新已经失效的派单信息，把订单状态为初始化，并且失效时间已经小于当前系统时间的数据进行更新成失效订单/超时订单
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void orderByInvalidTime() throws Exception{
//        log.info("----------------------------------TaskOrder.orderByInvalidTime()----start");
        try{
//            OrderModel orderModel = new OrderModel();
//            orderModel.setOrderStatus(2);
//            ComponentUtil.orderService.updateOrderStatusByInvalidTime(orderModel);

            // 查询策略里面的用户收到红包的规定回复时间
            StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLY_TIME.getStgType());
            StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);

            // 获取订单为初始化状态，并且失效时间已经小于当前时间的订单
            StatusModel statusQuery = TaskMethod.assembleStatusModelQueryByInvalidTime(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
            List<OrderModel> synchroList = ComponentUtil.taskOrderService.getOrderList(statusQuery);
            for (OrderModel data : synchroList){
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_ORDER_INVALID_START, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    OrderModel orderUpdate = TaskMethod.assembleOrderUpdateStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                    if (data.getCollectionType() == 1){
                        String startTime = data.getInvalidTime();
                        String endTime = DateUtil.getNowPlusTime();
                        boolean flag = DateUtil.beforeData(startTime, endTime);
                        if (flag){
                            ComponentUtil.orderService.updateOrderStatus(orderUpdate);
                        }
                    }else if (data.getCollectionType() == 2){
                        String startTime = data.getInvalidTime();
                        String endTime = DateUtil.getNowPlusTime();
                        boolean flag = DateUtil.beforeData(startTime, endTime);
                        if (flag){
                            if (data.getDidStatus() == 2){
                                ComponentUtil.orderService.updateOrderStatus(orderUpdate);
                            }
                        }

                    }else if (data.getCollectionType() == 3){
                        if (data.getIsRedPack() == 1){
                            // 没发红包
                            String startTime = data.getInvalidTime();
                            String endTime = DateUtil.getNowPlusTime();
                            boolean flag = DateUtil.beforeData(startTime, endTime);
                            if (flag){
                                // 当前时间 > 订单失效时间：则修改订单状态修改成超时状态
                                ComponentUtil.orderService.updateOrderStatus(orderUpdate);
                            }
                        }else{
                            // 发了红包

                            // 判断订单是否已回复
                            if (data.getIsReply() != 1){
                                // 已回复
                                // 判断回复时间是否超过了策略规定时间
                                int differSecond = DateUtil.differSecond(data.getReplyTime(), data.getRedPackTime());
                                if (differSecond >= strategyModel.getStgNumValue()){
                                    /**
                                    // 已经超过回复的规定时间：不管回复什么派单的订单状态修改成 orderStatus = 3
                                    // 1.修改订单状态 orderStatus = 3（有质疑的订单状态）
                                     **/
                                    // 发了红包但是已经超过回复的规定时间：不管回复什么派单的订单状态修改成 orderStatus = 4
                                    // 1.修改订单状态 orderStatus = 4（订单成功）
                                    OrderModel orderUpdateStatus = TaskMethod.assembleOrderUpdateStatus(data.getId(), 4);
                                    ComponentUtil.orderService.updateOrderStatus(orderUpdateStatus);
                                    log.info("");
                                    // 2.修改此订单字段 is_reply = 2 （系统默认回复）
                                    OrderModel orderUpdateReply = TaskMethod.assembleOrderUpdateRedPackData(data.getId(), 0, null,
                                            2, null, null, null, 0, "系统默认成功", null);
                                    ComponentUtil.orderService.updateRedPackAndReply(orderUpdateReply);

//                                    // 3.修改用户扣款流水表《tb_fn_did_balance_deduct》的 orderStatus = 3
//                                    DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), 3);
//                                    ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);
                                }else{
                                    // 未超过回复的规定时间
                                    if (data.getIsReply() == 3){
                                        // 回复失败：在跑超时订单时需要判断 is_reply = 3 ,如果等于3，则用户扣款流水的的订单状态字段值需要修改成 orderStatus = 5
                                        ComponentUtil.orderService.updateOrderStatus(orderUpdate);
                                    }else{
                                        // 回复成功

                                        // 判断回复金额是否一致
                                        if (data.getMoneyFitType() == 4){
                                            // 订单金额一致
                                            // 订单直接修改成成功状态
                                            OrderModel orderUpdateStatus = TaskMethod.assembleOrderUpdateStatus(data.getId(), 4);
                                            ComponentUtil.orderService.updateOrderStatus(orderUpdateStatus);
                                        }else{
                                            // 订单金额不一致
                                            if (data.getMoneyFitType() == 2){
                                                // 订单金额少了

                                                // 查询策略里面的消耗金额范围内的奖励规则列表
                                                String profit = "";
                                                StrategyModel strategyProfitQuery = TaskMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.WX_GROUP_CONSUME_MONEY_LIST.getStgType());
                                                StrategyModel strategyProfitModel = ComponentUtil.strategyService.getStrategyModel(strategyProfitQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
                                                // 解析奖励规则的值
                                                List<StrategyData> wxGroupConsumeMoneyList = JSON.parseArray(strategyProfitModel.getStgBigValue(), StrategyData.class);
                                                profit = TaskMethod.getConsumeProfit(wxGroupConsumeMoneyList, data.getActualMoney());

                                                // 1.补一个新的派单，金额等于实际上报金额
                                                String sgid = ComponentUtil.redisIdService.getNewFineId();
                                                // 组装派发订单的数据
                                                OrderModel orderAdd = TaskMethod.assembleOrderByReplenish(data.getDid(), sgid, data.getActualMoney(), data.getCollectionAccountId(), 3,
                                                        "系统补单：依据原订单号：" + data.getOrderNo() +"，上报金额少了", 2, profit);
                                                ComponentUtil.orderService.add(orderAdd);
                                                // 组装用户扣除余额流水的数据
                                                DidBalanceDeductModel didBalanceDeductModel = TaskMethod.assembleDidBalanceDeductAdd(data.getDid(), sgid, data.getActualMoney(), 30);
                                                ComponentUtil.didBalanceDeductService.add(didBalanceDeductModel);
                                                // 组装扣除用户余额
                                                DidModel updateBalance = TaskMethod.assembleUpdateDidBalance(data.getDid(), data.getActualMoney());
                                                // 锁定这个用户
                                                String lockKey_did_money = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                                                boolean flagLock_did_money = ComponentUtil.redisIdService.lock(lockKey_did_money);
                                                if (flagLock_did_money){
                                                    ComponentUtil.didService.updateDidDeductBalance(updateBalance);
                                                    // 解锁
                                                    ComponentUtil.redisIdService.delLock(lockKey_did_money);
                                                }
                                                // 2.原订单修改成 orderStatus = 4，原订单的run_status =3（这里run_status=3是为了保证不去修改用户扣款流水里面的订单状态）；这里有一个弊端就是因为runStatus=3等于说跑分用户的这笔收益没有奖励
                                                OrderModel orderUpdateStatus = TaskMethod.assembleOrderUpdateOrderStatusAndRunStatus(data.getId(), 4, 3);
                                                ComponentUtil.orderService.updateOrderStatus(orderUpdateStatus);
                                                // 3.用户余额流水流水 orderStatus = 5，也就是说用户的订单金额要锁8小时
                                                DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), 5);
                                                ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);
                                            }else if(data.getMoneyFitType() == 3){
                                                // 订单金额多了

                                                // 判断上报金额是否超过200
                                                boolean flag_money = StringUtil.getBigDecimalSubtract("200", data.getActualMoney());
                                                if (!flag_money){
                                                    // 上报金额超过200
                                                    // 定义惩罚数据
                                                    OperateModel operateModel = new OperateModel();
                                                    String remark = "";
                                                    operateModel = TaskMethod.assembleOperateData(0, null, data, 0, null, 7,
                                                            "上报金额超过200", remark , 2, 0, data.getActualMoney());
                                                    ComponentUtil.operateService.add(operateModel);
                                                }
                                                // 1.补一个新的派单，金额等于实际上报金额 - 订单金额
                                                String sgid = ComponentUtil.redisIdService.getNewFineId();
                                                String money = StringUtil.getBigDecimalSubtractByStr(data.getActualMoney(), data.getOrderMoney());
//                                                // 组装派发订单的数据
//                                                OrderModel orderAdd = TaskMethod.assembleOrderByReplenish(data.getDid(), sgid, money, data.getCollectionAccountId(), 3,
//                                                        "系统补单：依据原订单号：" + data.getOrderNo() +"，上报金额多了", 2);
//                                                ComponentUtil.orderService.add(orderAdd);
                                                // 组装用户扣除余额流水的数据
                                                DidBalanceDeductModel didBalanceDeductModel = TaskMethod.assembleDidBalanceDeductAddByManyMoney(data.getDid(), sgid, money, 30, "补充订单：订单上报金额多了，原订单号：" + data.getOrderNo());
                                                ComponentUtil.didBalanceDeductService.add(didBalanceDeductModel);
                                                // 组装扣除用户余额
                                                DidModel updateBalance = TaskMethod.assembleUpdateDidBalance(data.getDid(), money);
                                                // 锁定这个用户
                                                String lockKey_did_money = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                                                boolean flagLock_did_money = ComponentUtil.redisIdService.lock(lockKey_did_money);
                                                if (flagLock_did_money){
                                                    ComponentUtil.didService.updateDidDeductBalance(updateBalance);
                                                    // 解锁
                                                    ComponentUtil.redisIdService.delLock(lockKey_did_money);
                                                }

                                                // 2.原订单修改成 orderStatus = 4
                                                OrderModel orderUpdateStatus = TaskMethod.assembleOrderUpdateStatus(data.getId(), 4);
                                                ComponentUtil.orderService.updateOrderStatus(orderUpdateStatus);
                                            }
                                        }

                                    }
                                }

                            }else{
                                // 未回复
                                // 判断未回复时间是否超过了策略规定时间
                                int differSecond = DateUtil.differSecond(DateUtil.getNowPlusTime(), data.getRedPackTime());
                                if (differSecond >= strategyModel.getStgNumValue()){
                                    /**
                                    // 已经超过规定时间
                                    // 1.修改订单状态 orderStatus = 3（有质疑的订单状态）
                                     **/
                                    // 发了红包，已经超过规定时间
                                    // 1.修改订单状态 orderStatus = 4（成功状态）
                                    OrderModel orderUpdateStatus = TaskMethod.assembleOrderUpdateStatus(data.getId(), 4);
                                    ComponentUtil.orderService.updateOrderStatus(orderUpdateStatus);
                                    // 2.修改此订单字段 is_reply = 2 （系统默认回复）
                                    OrderModel orderUpdateReply = TaskMethod.assembleOrderUpdateRedPackData(data.getId(), 0, null,
                                            2, null, null, null, 0, "系统默认成功", null);
                                    ComponentUtil.orderService.updateRedPackAndReply(orderUpdateReply);
                                }
                            }

                        }
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }
            }


        }catch (Exception e){
            log.error(String.format("this TaskOrder.orderByInvalidTime() is error "));
            e.printStackTrace();
        }
//        log.info("----------------------------------TaskOrder.orderByInvalidTime()----end");
    }



    /**
     * @Description: task：执行派单失效/超时订单的逻辑运算
     * <p>
     *     每1每秒运行一次
     *     微信订单：
     *      1.查询出已失效的订单数据。
     *      2.根据订单信息，修改用户账号金额数据：具体修改金额：余额 =  余额 + 订单金额， 冻结金额 = 冻结金额 - 订单金额。
     *     支付宝订单：
     *      1.查询出已失效的订单数据。
     *      2.修改用户余额流水的订单状态。
     *
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void orderByInvalid() throws Exception{
//        log.info("----------------------------------TaskOrder.orderByInvalid()----start");
        // 获取已失效的订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskByOrderStatusQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
        List<OrderModel> synchroList = ComponentUtil.taskOrderService.getOrderList(statusQuery);
        for (OrderModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_ORDER_INVALID, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (data.getCollectionType() == 1){
                        // 微信支付订单的处理逻辑
                        // 锁住这个用户
                        String lockKey_did = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                        boolean flagLock_did = ComponentUtil.redisIdService.lock(lockKey_did);
                        if (flagLock_did){
                            DidModel didUpdateMoney = TaskMethod.assembleUpdateDidMoneyByInvalid(data.getDid(), data.getOrderMoney());
                            num = ComponentUtil.didService.updateDidMoneyByInvalid(didUpdateMoney);
                            if (num > 0){
                                // 更新此次task的状态：更新成成功
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                            }else{
                                // 更新此次task的状态：更新成失败
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新的影响行为0");
                                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                            }
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_did);
                        }
                    }else if(data.getCollectionType() == 2){
                        // 支付宝订单的逻辑处理
                        DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), 2);
                        num = ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);
                        if (num > 0){
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                        }else{
                            // 更新此次task的状态：更新成失败
                            log.info("");
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新的影响行为0");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                        }
                    }else if(data.getCollectionType() == 3){
                        int orderStatus = 0;
                        if (data.getIsRedPack() == 1){
                            // 未发红包，并且订单已超时
                            orderStatus = 2;
                        }else{
                            // 发了红包
                            if(data.getIsReply() == 3){
                                // 未超过回复的规定时间，并且回复失败
                                // 回复失败：在跑超时订单时需要判断 is_reply = 3 ,如果等于3，则用户扣款流水的的订单状态字段值需要修改成 orderStatus = 5
                                orderStatus = 5;
                            }else if(data.getIsReply() == 4){
                                if (data.getMoneyFitType() == 2){
                                    // 回复的金额少了，原订单需要锁8小时
                                    orderStatus = 5;
                                }
                            }
                        }
                        DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), orderStatus);
                        num = ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);

                        int runStatus = 0;
                        String info = "";
                        if (num > 0){
                            // 更新此次task的状态：更新成成功
                            runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                        }else{
                            // 更新此次task的状态：更新成失败
                            runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                            info = "更新的影响行为0";
                        }
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), runStatus, info);
                        ComponentUtil.taskOrderService.updateOrderStatus(statusModel);

                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskOrder.orderByInvalid()----end");
            }catch (Exception e){
                log.error(String.format("this TaskOrder.orderByInvalid() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try");
                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
            }
        }
    }



    /**
     * @Description: task：执行已经超过有失效时间的订单，并且用户操作状态属于初始化状态的逻辑
     * <p>
     *     每1每秒运行一次
     *     固定支付宝订单：
     *      1.查询出已超过失效时间，并且用户操作状态属于初始化状态的订单数据。
     *      2.修改此订单在《用户扣减余额流水表》中的订单状态，修改成order_status=5
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void orderByInvalidAndInitDidStatus() throws Exception{
//        log.info("----------------------------------TaskOrder.orderByInvalidAndInitDidStatus()----start");
        // 获取已经超过有失效时间的订单，并且用户操作状态属于初始化状态的订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskStatusQuery(limitNum);
        List<OrderModel> synchroList = ComponentUtil.taskOrderService.getOrderListByInvalidTime(statusQuery);
        for (OrderModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_ORDER_INVALID_BY_DID_STATUS_INIT, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 支付宝订单的逻辑处理
                    DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), 5);
                    num = ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);
                    if (num > 0){
                        // 更新此次task的状态：更新成成功
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByOrderStatusAndInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, 2,"");
                        ComponentUtil.taskOrderService.updateOrderStatusById(statusModel);
                    }else{
                        // 更新此次task的状态：更新成失败
                        log.info("");
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByOrderStatusAndInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 0,"更新的影响行为0");
                        ComponentUtil.taskOrderService.updateOrderStatusById(statusModel);
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskOrder.orderByInvalidAndInitDidStatus()----end");
            }catch (Exception e){
                log.error(String.format("this TaskOrder.orderByInvalidAndInitDidStatus() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByOrderStatusAndInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 0, "异常失败try");
                ComponentUtil.taskOrderService.updateOrderStatusById(statusModel);
            }
        }
    }





    /**
     * @Description: task：执行派单成功订单的逻辑运算
     * <p>
     *     每1每秒运行一次
     *     1.查询出未订单处于成功的状态数据。
     *     2.扣除用户账号相对应的冻结金额。
     *     3.清除缓存：LOCK_DID_COLLECTION_ACCOUNT_MONEY
     *
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void orderBySuccessOrder() throws Exception{
//        log.info("----------------------------------TaskOrder.orderBySuccessOrder()----start");
        // 查询策略里面的团队长奖励固定比例数据
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TEAM_CONSUME_REWARD.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);

        // 查询策略里面的团队长奖励固定比例数据-微信群
        StrategyModel strategy_Wx_Query = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TEAM_CONSUME_REWARD_WX_GROUP.getStgType());
        StrategyModel strategy_Wx_Model = ComponentUtil.strategyService.getStrategyModel(strategy_Wx_Query, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);

        String ratioReward = strategyModel.getStgValue();// 团队长奖励固定比例数据
        String ratio_Wx_Reward = strategy_Wx_Model.getStgValue();// 团队长奖励固定比例数据-微信群
        // 获取已成功的订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskByOrderDidStatusQuery(limitNum, 3);
        List<OrderModel> synchroList = ComponentUtil.taskOrderService.getOrderList(statusQuery);
        for (OrderModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_ORDER_SUCCESS, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (data.getCollectionType() == 1){
                        // 微信支付处理逻辑
                        // 更新这个收款二维码成功收款的次数
                        DidCollectionAccountQrCodeModel didCollectionAccountQrCodeModel = TaskMethod.assembleDidCollectionAccountQrCode(data.getQrCodeId(), 1);
                        ComponentUtil.didCollectionAccountQrCodeService.updateIsLimitNum(didCollectionAccountQrCodeModel);

                        // 锁住这个用户
                        String lockKey_did = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                        boolean flagLock_did = ComponentUtil.redisIdService.lock(lockKey_did);
                        if (flagLock_did){
                            DidModel didUpdateMoney = TaskMethod.assembleUpdateDidMoneyBySuccess(data.getDid(), data.getOrderMoney());
                            num = ComponentUtil.didService.updateDidMoneyBySuccess(didUpdateMoney);
                            if (num > 0){

//                            // 删除要删除的redis
//                            String strKeyCache_did_collection_account_money = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_MONEY, data.getCollectionAccountId(), data.getOrderMoney());
//                            ComponentUtil.redisService.remove(strKeyCache_did_collection_account_money);
                                log.info("");
                                // 更新此次task的状态：更新成成功
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                            }else{
                                // 更新此次task的状态：更新成失败
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新的影响行为0");
                                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                            }
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_did);
                        }
                    }else if(data.getCollectionType() == 2){
                        // 支付宝支付处理逻辑
                        DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), data.getOrderStatus());
                        num = ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);

                        // 删除此用户名下的挂单
                        String strKeyCache_lock_did_order_ing = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_ORDER_ING, data.getDid());
                        ComponentUtil.redisService.remove(strKeyCache_lock_did_order_ing);
                        if (num > 0){
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);

                            // 只有用户手动点击成功的订单状态团队长才有奖励
                            if (data.getOrderStatus() == 4){
                                // 添加团队长奖励数据-start

                                // 获取此用户的上级用户ID
                                DidLevelModel didLevelQuery = TaskMethod.assembleDidSuperiorQuery(data.getDid(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
                                DidLevelModel didLevelModel = (DidLevelModel) ComponentUtil.didLevelService.findByObject(didLevelQuery);
                                if (didLevelModel != null && didLevelModel.getId() > 0){
                                    // 根据用户ID查询此用户是否属于团队长
                                    DidModel didByIdQuery = TaskMethod.assembleDidQueryByDid(didLevelModel.getLevelDid());
                                    DidModel didModel = (DidModel) ComponentUtil.didService.findByObject(didByIdQuery);
                                    if (didModel.getIsTeam() == 2){
                                        // 属于团队长属性:计算需要给与每单的奖励的金额 = 订单金额 * 奖励比例
                                        String moneyReward = StringUtil.getMultiply(data.getOrderMoney(), ratioReward);
                                        if (!StringUtils.isBlank(moneyReward) && !moneyReward.equals("0.00")){
                                            DidRewardModel didRewardModel = TaskMethod.assembleTeamDirectConsumeProfit(10, didModel.getId(), moneyReward, data);
                                            ComponentUtil.didRewardService.add(didRewardModel);
                                        }

                                    }
                                }

                                // 添加团队长奖励数据-end
                            }


                        }else{
                            // 更新此次task的状态：更新成失败
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新的影响行为0");
                            log.info("");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                        }

                    }else if(data.getCollectionType() == 3){
                        // 微信群支付处理逻辑
                        DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), data.getOrderStatus());
                        num = ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);

//                        // 删除此用户名下的挂单
//                        String strKeyCache_lock_did_order_ing = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_ORDER_ING, data.getDid());
//                        ComponentUtil.redisService.remove(strKeyCache_lock_did_order_ing);
                        // 删除此收款账号下的挂单
                        String strKeyCache_lock_did_collection_account_order_ing = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_ORDER_ING, data.getCollectionAccountId());
                        ComponentUtil.redisService.remove(strKeyCache_lock_did_collection_account_order_ing);
                        if (num > 0){
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);

//                            // 用户自己奖励
//                            if (!StringUtils.isBlank(data.getProfit()) && data.getOrderStatus() != 3){
//
//                            }

                            DidRewardModel didRewardMyModel = TaskMethod.assembleTeamDirectConsumeProfit(6, data.getDid(), data.getProfit(), data);
                            ComponentUtil.didRewardService.add(didRewardMyModel);


                            // 添加团队长奖励数据-start
                            // 获取此用户的上级用户ID
                            DidLevelModel didLevelQuery = TaskMethod.assembleDidSuperiorQuery(data.getDid(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
                            DidLevelModel didLevelModel = (DidLevelModel) ComponentUtil.didLevelService.findByObject(didLevelQuery);
                            if (didLevelModel != null && didLevelModel.getId() > 0){
                                // 根据用户ID查询此用户是否属于团队长
                                DidModel didByIdQuery = TaskMethod.assembleDidQueryByDid(didLevelModel.getLevelDid());
                                DidModel didModel = (DidModel) ComponentUtil.didService.findByObject(didByIdQuery);
                                if (didModel.getIsTeam() == 2){
                                    // 属于团队长属性:计算需要给与每单的奖励的金额 = 订单金额 * 奖励比例
                                    String moneyReward = StringUtil.getMultiply(data.getOrderMoney(), ratio_Wx_Reward);
                                    log.info("");
                                    if (!StringUtils.isBlank(moneyReward) && !moneyReward.equals("0.00")){
                                        DidRewardModel didRewardModel = TaskMethod.assembleTeamDirectConsumeProfit(10, didModel.getId(), moneyReward, data);
                                        ComponentUtil.didRewardService.add(didRewardModel);
                                    }

                                }
                            }
                            // 添加团队长奖励数据-end




                        }else{
                            // 更新此次task的状态：更新成失败
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新的影响行为0");
                            log.info("");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                        }
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskOrder.orderBySuccessOrder()----end");
            }catch (Exception e){
                log.error(String.format("this TaskOrder.orderBySuccessOrder() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try");
                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
            }
        }
    }



    /**
     * @Description: task：执行每条成功订单的微信在当时成功金额是否超过策略中的上限
     * <p>
     *     每1每秒运行一次
     *     1.检测收款类型collection_type=3的微信ID在当前时间与策略部署的前1个小时的金额总和是否超过部署上限或者在部署范围。
     *     2.如果超过部署上限测添加数据到表tb_fn_did_wx_monitor中。
     *     3.如果在部署范围：则存缓存（金额在2000-2500之间的，存缓存；以便此微信ID的收款账号每次给码只能给出一个）
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void orderByToWxidMoney() throws Exception{
//        log.info("----------------------------------TaskOrder.orderByToWxidMoney()----start");
        // 查询策略里面的微信原始ID收款金额时间范围值
        int toWxidTime = 0;
        StrategyModel strategyToWxidTimeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TO_WXID_TIME.getStgType());
        StrategyModel strategyToWxidTimeModel = ComponentUtil.strategyService.getStrategyModel(strategyToWxidTimeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        toWxidTime = strategyToWxidTimeModel.getStgNumValue();

        // 查询策略里面的微信每次只出一个群码的金额范围
        String toWxidRangeMoney = "";
        StrategyModel strategyToWxidRangeMoneyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TO_WXID_RANGE_MONEY.getStgType());
        StrategyModel strategyToWxidRangeMoneyModel = ComponentUtil.strategyService.getStrategyModel(strategyToWxidRangeMoneyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        toWxidRangeMoney = strategyToWxidRangeMoneyModel.getStgValue();

        // 查询策略里面的微信在时间范围内最大收款金额
        String toWxidMaxMoney = "";
        StrategyModel strategyToWxidMaxMoneyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TO_WXID_MAX_MONEY.getStgType());
        StrategyModel strategyToWxidMaxMoneyModel = ComponentUtil.strategyService.getStrategyModel(strategyToWxidMaxMoneyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        toWxidMaxMoney = strategyToWxidMaxMoneyModel.getStgValue();

        // 查询策略里面的微信收款金额超上限的解控时间
        int toWxidRelieveTime = 0;
        StrategyModel strategyToWxidRelieveTimeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TO_WXID_RELIEVE_TIME.getStgType());
        StrategyModel strategyToWxidRelieveTimeModel = ComponentUtil.strategyService.getStrategyModel(strategyToWxidRelieveTimeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        toWxidRelieveTime = strategyToWxidRelieveTimeModel.getStgNumValue();

        // 获取支付类型为3，订单成功，并且没有进行数据填充的订单数据
        StatusModel statusQuery = TaskMethod.assembleOrderByToWxidMoneyQuery(3, 3, 1, limitNum);
        List<OrderModel> synchroList = ComponentUtil.taskOrderService.getOrderList(statusQuery);
        for (OrderModel data : synchroList){
            try{
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_ORDER_WORK_TYPE, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 判断当前时间与订单时间的间隔是否已经超过策略的时间范围
                    boolean flag_time = TaskMethod.checkToWxidTimeExceed(toWxidTime, data.getCreateTime());
                    if (flag_time){
                        OrderModel orderQuery = TaskMethod.assembleOrderSumMoneyByToWxid(3, 3, data.getUserId(), data.getCreateTime(), toWxidTime);
                        String money = ComponentUtil.orderService.sucMoneyByTowxid(orderQuery);
                        // 获取监控类型
                        int monitorType = TaskMethod.getMonitorType(money, toWxidRangeMoney, toWxidMaxMoney);// 类型1表示没有符合金额限定的范围，2符合金额范围，3符合最大收款金额
                        if (monitorType == 2){
                            // 存储到redis中
                            String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.TO_WXID_RANGE_MONEY_TIME, data.getId(), data.getUserId());
                            ComponentUtil.redisService.set(strKeyCache, data.getUserId() + "," + money, toWxidTime, TimeUnit.MINUTES);
                        }else if (monitorType == 3){
                            // 存储到数据表中

                            // 获取此微信的微信昵称
                            String wxNickname = "";
                            DidCollectionAccountModel didCollectionAccountQuery = TaskMethod.assembleDidCollectionAccountByUserId(data.getUserId(), 3);
                            DidCollectionAccountModel didCollectionAccountData = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountQuery);
                            if (didCollectionAccountData != null && didCollectionAccountData.getId() != null && didCollectionAccountData.getId() > 0){
                                if (!StringUtils.isBlank(didCollectionAccountData.getBankName())){
                                    wxNickname = didCollectionAccountData.getBankName();
                                }
                            }
                            // 添加用户的微信收款账号金额监控数据
                            DidWxMonitorModel didWxMonitorModel = TaskMethod.assembleDidWxMonitorAdd(data.getDid(), wxNickname, data.getUserId(), toWxidRelieveTime);
                            ComponentUtil.didWxMonitorService.add(didWxMonitorModel);
                        }
                    }
                    // 更新订单补充数据的状态
                    // 更新此次task的状态
                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                    ComponentUtil.taskOrderService.updateWorkType(statusModel);
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskOrder.orderByToWxidMoney()----end");
            }catch (Exception e){
                log.error(String.format("this TaskOrder.orderByToWxidMoney() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskOrderService.updateWorkType(statusModel);
            }
        }
    }



    /**
     * @Description: task：执行派单成功订单的数据同步
     * <p>
     *     每1每秒运行一次
     *     1.查询出已处理的派单成功的订单数据数据。
     *     2.根据同步地址进行数据同步。
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void orderNotify() throws Exception{
//        log.info("----------------------------------TaskOrder.orderNotify()----start");

        // 获取已成功的订单数据，并且为同步给下游的数据
        StatusModel statusQuery = TaskMethod.assembleTaskByOrderNotifyQuery(limitNum);
        List<OrderModel> synchroList = ComponentUtil.taskOrderService.getOrderNotifyList(statusQuery);
        for (OrderModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_ORDER_NOTIFY, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 进行数据同步

//                    String sendData = "total_amount=" + data.getOrderMoney() + "&" + "out_trade_no=" + data.getOutTradeNo() + "&" + "trade_status=" + 1
//                            + "&" + "trade_no=" + data.getOrderNo() + "&" + "trade_time=" + data.getCreateTime();
                    Map<String, Object> sendMap = new HashMap<>();
                    sendMap.put("total_amount", data.getOrderMoney());
                    sendMap.put("out_trade_no", data.getOutTradeNo());
                    sendMap.put("trade_status", 1);
                    sendMap.put("trade_no", data.getOrderNo());
                    sendMap.put("trade_time", data.getCreateTime());
                    if (data.getMoneyFitType() != 4){
                        if (!StringUtils.isBlank(data.getActualMoney())){
                            sendMap.put("pay_amount", data.getActualMoney());
                        }else{
                            // 发了红包，未回复的订单
                            sendMap.put("pay_amount", data.getOrderMoney());
                        }
                    }else {
                        sendMap.put("pay_amount", data.getOrderMoney());
                    }

                    String sendUrl = "";
                    if (!StringUtils.isBlank(data.getNotifyUrl())){
                        sendUrl = data.getNotifyUrl();
                    }else {
                        sendUrl = ComponentUtil.loadConstant.defaultNotifyUrl;
                    }
//                    sendUrl = "http://localhost:8085/pay/data/fine";
//                    String resp = HttpSendUtils.sendGet(sendUrl + "?" + URLEncoder.encode(sendData,"UTF-8"), null, null);
//                    String resp = HttpSendUtils.sendGet(sendUrl + "?" + sendData, null, null);
                    String resp = HttpSendUtils.sendPostAppJson(sendUrl , JSON.toJSONString(sendMap));
                    if (resp.equals("ok")){
                        // 成功
                        // 更新此次task的状态：更新成成功
                        StatusModel statusModel = TaskMethod.assembleUpdateSendStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
                        ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                    }else {
                        // 更新此次task的状态：更新成失败
                        StatusModel statusModel = TaskMethod.assembleUpdateSendStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                        ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskOrder.orderNotify()----end");
            }catch (Exception e){
                log.error(String.format("this TaskOrder.orderNotify() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateSendStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
            }
        }
    }

}
