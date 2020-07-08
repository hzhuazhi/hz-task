package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.HttpSendUtils;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
import com.hz.task.master.core.model.did.DidCollectionAccountQrCodeModel;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.util.ComponentUtil;
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
            OrderModel orderModel = new OrderModel();
            orderModel.setOrderStatus(2);
            ComponentUtil.orderService.updateOrderStatusByInvalidTime(orderModel);
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
                        DidBalanceDeductModel didBalanceDeductUpdate = TaskMethod.assembleDidBalanceDeductUpdate(data.getOrderNo(), 4);
                        num = ComponentUtil.didBalanceDeductService.updateOrderStatus(didBalanceDeductUpdate);

                        // 删除此用户名下的挂单
                        String strKeyCache_lock_did_order_ing = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_ORDER_ING, data.getDid());
                        ComponentUtil.redisService.remove(strKeyCache_lock_did_order_ing);
                        if (num > 0){
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskOrderService.updateOrderStatus(statusModel);
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
