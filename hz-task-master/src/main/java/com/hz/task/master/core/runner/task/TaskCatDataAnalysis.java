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
import com.hz.task.master.core.model.wx.WxFriendModel;
import com.hz.task.master.core.model.wx.WxModel;
import com.hz.task.master.core.model.wx.WxOrderModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.TaskMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
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
                                if (didCollectionAccountModel.getCheckStatus() != 3 && StringUtils.isBlank(didCollectionAccountModel.getUserId())){

                                    // 判断发送指令的收到的小微是否与账号分配的小微是否一致
                                    if (didCollectionAccountModel.getWxId() != data.getWxId()){
                                        DidCollectionAccountModel updateCheck = TaskMethod.assembleDidCollectionAccountUpdateCheckInfo(didCollectionAccountModel.getId(), 0, "分配的管理员与您拉进微信的管理员不符合，请核对！");
                                        ComponentUtil.didCollectionAccountService.update(updateCheck);
                                        // 更新此次task的状态：更新成失败-分配的小微与拉进微信群的小微不一致
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "分配的小微与拉进微信群的小微不一致");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }else{
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
                                    }

                                }else{
                                    // 更新此次task的状态：更新成失败-已审核通过的微信群无需重复发送审核指令
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "已审核通过的微信群无需重复发送审核指令");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }
                            }else {
                                // 更新此次task的状态：更新成失败-根据收款账号ID没有找到对应的收款账号
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据收款账号ID没有找到对应的收款账号");
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
                            if (data.getDataFrom() == 2){
                                // 表示删除了我方小微：需要检查此时此时到之前5分钟是否有订单
                                OrderModel orderQuery = TaskMethod.assembleOrderByCreateTime(data.getDid(), data.getCollectionAccountId());
                                OrderModel orderModel = ComponentUtil.orderService.getOrderByDidAndTime(orderQuery);
                                if (orderModel != null && orderModel.getId() != null && orderModel.getId() > 0){
                                    if (orderModel.getOrderStatus() == 1){
                                        // 需要修改此订单状态，修改成质疑状态
                                        OrderModel upOrderModel = TaskMethod.assembleUpdateOrderById(orderModel.getId(), 3, "检测：处于挂单情况下剔除我方小微");
                                        ComponentUtil.orderService.updateOrderStatusAndRemark(upOrderModel);
                                    }
                                }
                            }
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

                        }else if (data.getDataType() == 9){
                            // 发送固定指令4表示暂停使用微信群
                            int workType = 0; // task的补充结果状态
                            String workRemark = "";// task的补充的备注

                            // 根据发指令的微信ID更新用户群的审核状态
                            if (!StringUtils.isBlank(data.getFinalFromWxid())){
                                DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfoByAcNum(data.getFinalFromWxid(), 3, "检测：微信收款异常");
                                ComponentUtil.didCollectionAccountService.updateCheckByAcNum(didCollectionAccountUpdate);

                                // 根据用户ID修改订单的回复状态：修改回复失败的状态，并且说明备注
                                OrderModel upOrderIsReplyModel = TaskMethod.assembleUpdateIsReplyByDid(didCollectionAccountModel.getDid(), 1, 3, "2", "系统回复失败");
                                ComponentUtil.orderService.updateIsReplyAndRemark(upOrderIsReplyModel);
                                workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                            }else {
                                // 更新此次task的状态：更新成失败
                                workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                workRemark = "微信ID数据为空";
                            }

                            // 更新此次task的状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                        }else if(data.getDataType() == 12){
                            // 发送上传图片使用
                            // 根据微信群名称查询此收款账号信息
                            if (didCollectionAccountModel != null && didCollectionAccountModel.getId() > 0){
                                if (didCollectionAccountModel.getCheckStatus() != 3 && !StringUtils.isBlank(didCollectionAccountModel.getUserId()) && StringUtils.isBlank(didCollectionAccountModel.getDdQrCode())){
                                    // 收款账号不属于已审核状态 & 用户的微信ID不为空 & 用户的微信二维码为空

                                    // 判断收到图片的小微是否与账号分配的小微是否一致
                                    if (didCollectionAccountModel.getWxId() != data.getWxId()){
                                        DidCollectionAccountModel updateCheck = TaskMethod.assembleDidCollectionAccountUpdateCheckInfo(didCollectionAccountModel.getId(), 0, "上传二维码图片时：分配的管理员与您拉进微信的管理员不符合，请核对！");
                                        ComponentUtil.didCollectionAccountService.update(updateCheck);
                                        // 更新此次task的状态：更新成失败-分配的小微与拉进微信群的小微不一致
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "上传二维码图片时：分配的小微与拉进微信群的小微不一致");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }else{

                                        // 更新此次task的状态：更新成成功
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }

                                }else{
                                    // 更新此次task的状态：更新成失败-已审核通过的微信群无需重复上传图片
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "已审核通过的微信群无需重复上传图片");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }
                            }else {
                                // 更新此次task的状态：更新成失败-根据收款账号ID没有找到对应的收款账号
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据收款账号ID没有找到对应的收款账号");
                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                            }
                        }

//                        else if (data.getDataType() == 10){
//                            // 小微登入
//                            int workType = 0; // task的补充结果状态
//                            String workRemark = "";// task的补充的备注
//
//
//                            // 组装修改小微登入状态
//                            WxModel wxModel = TaskMethod.assembleWxUpdateByLoginType(data.getWxId(), 2);
//                            ComponentUtil.wxService.update(wxModel);
//
//                            // 组装修改小微旗下收款账号的登入状态
//                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateLoginType(data.getWxId(), 3, 2);
//                            ComponentUtil.didCollectionAccountService.updateLoginType(didCollectionAccountUpdate);
//
//
//                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
//
//                            // 更新此次task的状态
//                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
//                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
//                        }else if (data.getDataType() == 11){
//                            // 小微登出
//                            int workType = 0; // task的补充结果状态
//                            String workRemark = "";// task的补充的备注
//
//
//                            // 组装修改小微登出状态
//                            WxModel wxModel = TaskMethod.assembleWxUpdateByLoginType(data.getWxId(), 1);
//                            ComponentUtil.wxService.update(wxModel);
//
//                            // 组装修改小微旗下收款账号的登出状态
//                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateLoginType(data.getWxId(), 3, 1);
//                            ComponentUtil.didCollectionAccountService.updateLoginType(didCollectionAccountUpdate);
//
//
//                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
//
//                            // 更新此次task的状态
//                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
//                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
//                        }
                    }else{
                        // 收款账号0为， 则判断是否是小微登入、登出
                        if (data.getDataType() == 10){
                            // 小微登入
                            int workType = 0; // task的补充结果状态
                            String workRemark = "";// task的补充的备注


                            // 组装修改小微登入状态
                            WxModel wxModel = TaskMethod.assembleWxUpdateByLoginType(data.getWxId(), 2);
                            ComponentUtil.wxService.update(wxModel);

                            // 组装修改小微旗下收款账号的登入状态
                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateLoginType(data.getWxId(), 3, 2);
                            ComponentUtil.didCollectionAccountService.updateLoginType(didCollectionAccountUpdate);


                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;

                            // 更新此次task的状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                        }else if (data.getDataType() == 11){
                            // 小微登出
                            int workType = 0; // task的补充结果状态
                            String workRemark = "";// task的补充的备注


                            // 组装修改小微登出状态
                            WxModel wxModel = TaskMethod.assembleWxUpdateByLoginType(data.getWxId(), 1);
                            ComponentUtil.wxService.update(wxModel);

                            // 组装修改小微旗下收款账号的登出状态
                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateLoginType(data.getWxId(), 3, 1);
                            ComponentUtil.didCollectionAccountService.updateLoginType(didCollectionAccountUpdate);


                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;

                            // 更新此次task的状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                        }else{
                            // 剩余的所属：work_type =1 并且 dataType =2 的数据
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
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






    /**
     * 段峰
     * @Description: 可爱猫数据解析数据，补充成功数据进行逻辑运算
     * <p>
     *     每1每秒运行一次
     *     1.查询出已补充完毕的可爱猫数据解析数据。
     *     2.根据dataType类型进行执行：类型不等于7的一律修改成执行成功。
     *     3.dataType=7的：更新订单的状态，更新成成功状态
     *
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void catDataAnalysisSucWork() throws Exception{
//        log.info("----------------------------------TaskCatDataAnalysis.catDataAnalysisSucWork()----start");
        int curday = DateUtil.getDayNumber(new Date());
        // 获取已经填充完毕的的可爱猫解析数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeAndRunStatusQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
        List<CatDataAnalysisModel> synchroList = ComponentUtil.taskCatDataAnalysisService.getCatDataAnalysisList(statusQuery);
        for (CatDataAnalysisModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_ANALYSIS_WORK_TYPE_SUC, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    int runStatus = 0;
                    String info = "";

                    if (data.getDataType() == 3){
                        // 查询小微是否已经与微信ID存在关系
                        WxFriendModel wxFriendQuery = TaskMethod.assembleWxFriendAddOrQuery(data.getWxId(),0,
                                0,data.getFinalFromWxid(), 0,null, 1);
                        WxFriendModel wxFriendData = (WxFriendModel) ComponentUtil.wxFriendService.findByObject(wxFriendQuery);
                        if (wxFriendData != null && wxFriendData.getId() > 0){
                            // 表示之前就是好友，无需再次添加
                        }else {
                            // 添加小微加好友记录的数据
                            WxFriendModel wxFriendAdd = TaskMethod.assembleWxFriendAddOrQuery(data.getWxId(),data.getDid(),
                                    data.getCollectionAccountId(),data.getFinalFromWxid(), curday,null, 1);
                            ComponentUtil.wxFriendService.add(wxFriendAdd);
                        }

                        // 查询小微是否已经与微信群存在关系
                        WxFriendModel wxGroupQuery = TaskMethod.assembleWxFriendAddOrQuery(data.getWxId(),0,
                                0,null, 0,data.getFromWxid(), 2);
                        WxFriendModel wxGroupData = (WxFriendModel) ComponentUtil.wxFriendService.findByObject(wxGroupQuery);
                        if (wxGroupData != null && wxGroupData.getId() > 0){
                            // 表示之前就与群有关联关系，无需再次添加
                        }else {
                            // 添加小微与微信群记录的数据
                            WxFriendModel wxGroupAdd = TaskMethod.assembleWxFriendAddOrQuery(data.getWxId(),data.getDid(),
                                    data.getCollectionAccountId(),null, curday,data.getFromWxid(), 2);
                            ComponentUtil.wxFriendService.add(wxGroupAdd);
                        }

                        // 更新此次task的状态：更新成成功状态
                        runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                    }else if(data.getDataType() == 4){
                        // 加群
                        // 更新小微回执信息
                        WxOrderModel wxOrderQuery = TaskMethod.assembleWxOrderQuery(data.getWxId(), data.getCollectionAccountId(), 1);
                        WxOrderModel wxOrderModel = (WxOrderModel) ComponentUtil.wxOrderService.findByObject(wxOrderQuery);
                        if (wxOrderModel != null && wxOrderModel.getId() > 0){
                            // 更新小微订单的回执信息
                            WxOrderModel wxOrderUpdate = TaskMethod.assembleWxOrderUpdate(wxOrderModel.getId(), 2);
                            ComponentUtil.wxOrderService.update(wxOrderUpdate);
                        }
                        // 更新此次task的状态：更新成成功状态
                        runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                    }else if(data.getDataType() == 12){
                        // 更新此次task的状态：更新成失败状态
                        // 因为数据不能让其时时在跑，所以更新成失败状态， 把失败状态变更成成功状态是由接口来控制：此类型有点特殊
                        runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                    }else {
                        // 更新此次task的状态：更新成成功状态
                        runStatus = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                    }

                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), runStatus, info);
                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskCatDataAnalysis.catDataAnalysisSucWork()----end");
            }catch (Exception e){
                log.error(String.format("this TaskCatDataAnalysis.catDataAnalysisSucWork() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
            }
        }
    }

}
