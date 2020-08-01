package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatDataAnalysisModel;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.operate.OperateModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.order.OrderStepModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.wx.WxClerkModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.TaskMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description task:可爱猫数据解析
 * @Author yoko
 * @Date 2020/7/22 20:19
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskCatDataAnalysis {

    private final static Logger log = LoggerFactory.getLogger(TaskCatDataAnalysis.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: 可爱猫数据解析的数据补充
     * <p>
     *     每1每秒运行一次
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void catDataAnalysisWorkType() throws Exception{
//        log.info("----------------------------------TaskCatDataAnalysis.catDataAnalysisWorkType()----start");

        // 获取需要填充的可爱猫数据解析的数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        List<CatDataAnalysisModel> synchroList = ComponentUtil.taskCatDataAnalysisService.getCatDataAnalysisList(statusQuery);
        for (CatDataAnalysisModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_ANALYSIS_WORK_TYPE, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (data.getCollectionAccountId() > 0){
                        DidCollectionAccountModel didCollectionAccountByIdQuery = TaskMethod.assembleDidCollectionAccountByIdQuery(data.getCollectionAccountId());
                        DidCollectionAccountModel didCollectionAccountModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByIdQuery);
                        if (data.getDataType() == 2){
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                        }else if(data.getDataType() == 3){
                            // 发送固定指令3表示审核使用
                            // 根据微信群名称查询此收款账号信息
                            if (didCollectionAccountModel != null && didCollectionAccountModel.getId() > 0){
                                if (didCollectionAccountModel.getCheckStatus() != 3){
                                    // 查询小微与此收款账号之前是否已建立了关联关系
                                    WxClerkModel wxClerkQuery = TaskMethod.assembleWxClerkAddOrQuery(data.getWxId(), didCollectionAccountModel.getId());
                                    WxClerkModel wxClerkData = (WxClerkModel) ComponentUtil.wxClerkService.findByObject(wxClerkQuery);
                                    if (wxClerkData == null || wxClerkData.getId() <= 0){
                                        // 之前没有建立关系，需要添加小微与收款账号的关联关系
                                        WxClerkModel wxClerkAdd = TaskMethod.assembleWxClerkAddOrQuery(data.getWxId(), didCollectionAccountModel.getId());
                                        ComponentUtil.wxClerkService.add(wxClerkAdd);
                                    }
                                    // 更新微信群收款账号信息
                                    DidCollectionAccountModel updateDidCollectionAccountModel = TaskMethod.assembleDidCollectionAccountUpdateByWxGroup(data, didCollectionAccountModel.getId());
                                    int upNum = ComponentUtil.didCollectionAccountService.updateDidCollectionAccountByWxData(updateDidCollectionAccountModel);
                                    if (upNum > 0){
                                        // 更新此次task的状态：更新成成功
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }else {
                                        // 更新此次task的状态：更新成失败-更新微信群收款账号信息响应行为0
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新微信群收款账号信息响应行为0");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }
                                }else{
                                    // 更新此次task的状态：更新成失败-已审核通过的微信群无需重复发送审核指令
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "已审核通过的微信群无需重复发送审核指令");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }
                            }else {
                                // 更新此次task的状态：更新成失败-根据微信群名称没有找到对应的收款账号
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据微信群名称没有找到对应的收款账号");
                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                            }
                        }else if(data.getDataType() == 4){
                            // 加群信息
                            int workType = 0;
                            String workRemark = "";

                            // 更新此次task的状态：更新成成功
                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);


                        }else if(data.getDataType() == 5){
                            // 发红包
                            OperateModel operateModel = null;
                            int workType = 0;
                            String workRemark = "";

                            // 处理订单逻辑
                            // 查询此账号最新订单数据
                            OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(data.getDid(), data.getCollectionAccountId(), 3, 1);
                            OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                            if (orderModel != null && orderModel.getId() > 0){
                                if (orderModel.getOrderStatus() == 1){
                                    // 更新订单的操作状态-修改成发红包状态
                                    OrderModel orderUpdate = TaskMethod.assembleOrderUpdateRedPackData(orderModel.getId(), 2, DateUtil.getNowPlusTime(), 0, null, null, null, 0, null, null);
                                    ComponentUtil.orderService.updateRedPackAndReply(orderUpdate);

                                    // 填充可爱猫解析数据：填充对应的订单信息
                                    CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                    ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);


                                    // 更新此次task的状态：更新成成功状态
                                    workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;

                                }else if(orderModel.getOrderStatus() == 2){
                                    // 订单已超时

                                    // 更新订单的操作状态-修改成发红包状态
                                    OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 3, 0, "订单超时，支付用户已发红包");
                                    ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                    // 填充可爱猫解析数据：填充对应的订单信息
                                    CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                    ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                    // 更新此次task的状态：更新成失败-订单超时之后才发的红包
                                    workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                    workRemark = "订单超时之后才发的红包";
                                }else{
                                    // 更新此次task的状态：更新成失败-脏数据：订单有质疑或者订单成功了，还有收到红包的信息
                                    workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                    workRemark = "脏数据：订单有质疑或者订单成功了，还有收到红包的信息";
                                }
                            }else{
                                // 没有相关订单信息
                                // 更新此次task的状态：更新成失败-根据用户ID、收款账号没有查询到相关订单信息
                                workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                workRemark = "根据用户ID、收款账号没有查询到相关订单信息";
                            }

                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                        }else if(data.getDataType() == 6){
                            // 剔除成员
                            int workType = 0;
                            String workRemark = "";

                            // 更新此次task的状态：更新成成功状态
                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                        }else if(data.getDataType() == 7){
                            // 成功收款

                            int workType = 0; // task的补充结果状态
                            String workRemark = "";// task的补充的备注
                            OperateModel operateModel = null;

                            String [] fg_msg = data.getMsg().split("#");
                            String sucMoney = fg_msg[1];// 用户上报的成功金额

                            // 查询此账号最新订单数据
                            OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(data.getDid(), data.getCollectionAccountId(), 3, 1);
                            OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                            if (orderModel != null && orderModel.getId() > 0){
                                // 判断此订单用户是否回复过
                                if (orderModel.getIsReply() < 3){
                                    // 没有回复过：才进行后续操作，之前用户已经回复过的不做处理
                                    int moneyFitType = 0;// 金额是否与上报金额一致：1初始化，2少了，3多了，4一致
                                    // 金额相减
                                    String result = StringUtil.getBigDecimalSubtractByStr(sucMoney, orderModel.getOrderMoney());
                                    if (result.equals("0")){
                                        moneyFitType = 4;
                                    }else{
                                        boolean flag_money = StringUtil.getBigDecimalSubtract(orderModel.getOrderMoney(), sucMoney);
                                        if (flag_money){
                                            // 少了
                                            moneyFitType = 2;
                                        }else {
                                            // 多了
                                            moneyFitType = 3;
                                        }
                                    }
                                    String profit = "";
                                    if (moneyFitType == 3){
                                        // 查询策略里面的消耗金额范围内的奖励规则列表
                                        StrategyModel strategyQuery = TaskMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.WX_GROUP_CONSUME_MONEY_LIST.getStgType());
                                        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
                                        // 解析奖励规则的值
                                        List<StrategyData> wxGroupConsumeMoneyList = JSON.parseArray(strategyModel.getStgBigValue(), StrategyData.class);
                                        profit = TaskMethod.getConsumeProfit(wxGroupConsumeMoneyList, sucMoney);
                                    }
                                    // 更新订单的操作状态-修改已回复状态
                                    OrderModel orderUpdate = TaskMethod.assembleOrderUpdateRedPackData(orderModel.getId(), 0, null,
                                            4, data.getMsg(), DateUtil.getNowPlusTime(), sucMoney, moneyFitType, null, profit);
                                    ComponentUtil.orderService.updateRedPackAndReply(orderUpdate);

                                    // 更新此次task的状态：成功状态
                                    workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                                }else{
                                    // 更新此次task的状态：失败状态
                                    workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                    workRemark = "订单号：" + orderModel.getOrderNo() + "，已回复过了；重复回复！";
                                }
                            }else{
                                // 更新此次task的状态：更新成失败
                                workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                workRemark = "没有找到对应的相关订单";
                            }

                            // 更新此次task的状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                        }else if(data.getDataType() == 8){
                            // 收款失败
                            int workType = 0; // task的补充结果状态
                            String workRemark = "";// task的补充的备注


                            // 查询此账号最新订单数据
                            OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(data.getDid(), data.getCollectionAccountId(), 3, 1);
                            OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                            if (orderModel != null && orderModel.getId() > 0){
                                // 判断此订单是否已发过红包
                                if (orderModel.getIsRedPack() == 2) {
                                    if (orderModel.getIsReply() < 3){
                                        // 更新订单的操作状态-修改已回复状态
                                        OrderModel orderUpdate = TaskMethod.assembleOrderUpdateRedPackData(orderModel.getId(), 0, null,
                                                3, data.getMsg(), DateUtil.getNowPlusTime(), null, 0, null, null);
                                        ComponentUtil.orderService.updateRedPackAndReply(orderUpdate);
                                    }else {
                                        // 更新此次task的状态：失败状态
                                        workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                        workRemark = "订单号：" + orderModel.getOrderNo() + "，已回复过了；重复回复！";
                                    }
                                }else{
                                    // 没有发过红包，不做处理
                                    // 更新此次task的状态：更新成失败
                                    workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                    workRemark = "没有发过红包，不做处理";
                                }

                            }

                            // 更新此次task的状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                        }
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskCatDataAnalysis.catDataAnalysisWorkType()----end");
            }catch (Exception e){
                log.error(String.format("this TaskCatDataAnalysis.catDataAnalysisWorkType() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
            }
        }
    }






//    /**
//     * @Description: 可爱猫数据解析数据，支付成功的数据进行更新订单状态
//     * <p>
//     *     每1每秒运行一次
//     *     1.查询出已补充完毕的可爱猫数据解析数据。
//     *     2.根据dataType类型进行执行：类型不等于7的一律修改成执行成功。
//     *     3.dataType=7的：更新订单的状态，更新成成功状态
//     *
//     *
//     * </p>
//     * @author yoko
//     * @date 2019/12/6 20:25
//     */
////    @Scheduled(cron = "5 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每秒执行
//    public void catDataAnalysisSucWork() throws Exception{
////        log.info("----------------------------------TaskCatDataAnalysis.catDataAnalysisSucWork()----start");
//
//        // 获取需要填充的可爱猫数据
//        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeAndRunStatusQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
//        List<CatDataAnalysisModel> synchroList = ComponentUtil.taskCatDataAnalysisService.getCatDataAnalysisList(statusQuery);
//        for (CatDataAnalysisModel data : synchroList){
//            try{
//                // 锁住这个数据流水
//                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_ANALYSIS_WORK_TYPE_SUC, data.getId());
//                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
//                if (flagLock){
//                    int runStatus = 0;
//                    String info = "";
//
//                    if (data.getDataType() == 7){
//                        // 更新订单状态：更新成成功状态
//                        OrderModel orderUpdate = TaskMethod.assembleOrderUpdateStatusByOrderNo(data.getOrderNo(), 4);
//                        int num = ComponentUtil.orderService.updateOrderStatusByOrderNo(orderUpdate);
//                        if (num > 0){
//                            // 更新此次task的状态：更新成成功状态
//                            runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
//                        }else {
//                            // 更新此次task的状态：更新成失败状态
//                            runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
//                            info = "更新订单状态的响应行为0";
//                        }
//                    }else {
//                        // 更新此次task的状态：更新成成功状态
//                        runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
//                    }
//
//                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), runStatus, info);
//                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
//                    // 解锁
//                    ComponentUtil.redisIdService.delLock(lockKey);
//                }
//
////                log.info("----------------------------------TaskCatDataAnalysis.catDataAnalysisSucWork()----end");
//            }catch (Exception e){
//                log.error(String.format("this TaskCatDataAnalysis.catDataAnalysisSucWork() is error , the dataId=%s !", data.getId()));
//                e.printStackTrace();
//                // 更新此次task的状态：更新成失败
//                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
//                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
//            }
//        }
//    }

}
