package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatDataAnalysisModel;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.operate.OperateModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.cat.CatGuest;
import com.hz.task.master.core.model.task.cat.CatMember;
import com.hz.task.master.core.model.wx.WxClerkModel;
import com.hz.task.master.core.model.wx.WxModel;
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
//    @Scheduled(fixedDelay = 1000) // 每秒执行
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

                    if (data.getDataType() == 2){
                        // 更新此次task的状态：更新成成功
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                    }else if(data.getDataType() == 3){
                        // 发送固定指令3表示审核使用
                        // 根据微信群名称查询此收款账号信息
                        DidCollectionAccountModel didCollectionAccountByPayeeQuery = TaskMethod.assembleDidCollectionAccountByPayee(data.getFromName(), 3);
                        DidCollectionAccountModel didCollectionAccountByPayeeData = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByPayeeQuery);
                        if (didCollectionAccountByPayeeData != null && didCollectionAccountByPayeeData.getId() > 0){
                            if (didCollectionAccountByPayeeData.getCheckStatus() != 3){
                                // 更新微信群收款账号信息
                                DidCollectionAccountModel updateDidCollectionAccountModel = TaskMethod.assembleDidCollectionAccountUpdateByWxGroup(data, didCollectionAccountByPayeeData.getId());
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

                        // 校验加群信息
                        boolean flag_guest = TaskMethod.checkCatGuest(data.getGuest());
                        if (flag_guest){
                            // 校验加群信息的成员是否是我方小微
                            List<CatGuest> catGuestList = JSON.parseArray(data.getGuest(), CatGuest.class);
                            CatGuest catGuest = catGuestList.get(0);
                            WxModel wxQuery = TaskMethod.assembleWxModel(catGuest.wxid);
                            WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                            if (wxModel == null || wxModel.getId() <= 0){
                                // 不属于我方小微

                                // 根据微信群群名称查询
                                DidCollectionAccountModel didCollectionAccountByPayeeQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee("", data.getFromName(), 3);
                                DidCollectionAccountModel didCollectionAccountByPayeeModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByPayeeQuery);
                                if (didCollectionAccountByPayeeModel != null && didCollectionAccountByPayeeModel.getId() > 0){
                                    // 能匹配此收款账号

                                    // 判断此收款账号是否有订单正在进行中
                                    OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(didCollectionAccountByPayeeModel.getDid(), didCollectionAccountByPayeeModel.getId(), 3);
                                    OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                                    if (orderModel != null && orderModel.getId() > 0){
                                        if (orderModel.getOrderStatus() == 1){
                                            // 订单是初始化状态

                                            // 更新订单的操作状态
                                            OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 2, 2, "");
                                            ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                            // 更新此次task的状态：更新成成功
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                                        }else {
                                            // 订单不是初始化状态，加群已是无效


                                            // 订单不是初始化状态，加群已是无效
                                            String remark = "";
                                            if (!StringUtils.isBlank(catGuest.getNickname())){
                                                remark = "闲杂人微信昵称：" + catGuest.getNickname();
                                            }
                                            OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, null, 0, null, 3,
                                                    "订单不是初始化状态，加群已是无效", remark , 1, 0, null);
                                            ComponentUtil.operateService.add(operateModel);


                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                            // 更新此次task的状态：更新成失败-订单不是初始化状态，加群已是无效
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "订单不是初始化状态，加群已是无效");
                                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                                        }

                                    }else {
                                        // 没有订单在进行中：加群又不属于我方小微且没有指派订单，属于闲杂人加群；需让用户剔除此成员
                                        String remark = "";
                                        if (!StringUtils.isBlank(catGuest.getNickname())){
                                            remark = "闲杂人微信昵称：" + catGuest.getNickname();
                                        }
                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, null, 0, null, 3,
                                                "加群又不属于我方小微且没有指派订单，属于闲杂人加群；需让用户剔除此成员", remark , 1, 0, null);
                                        ComponentUtil.operateService.add(operateModel);

                                        // 更新此次task的状态：更新成失败-加群又不属于我方小微且没有指派订单，属于闲杂人加群
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "加群又不属于我方小微且没有指派订单，属于闲杂人加群");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }


                                }else {
                                    // 说明监控的微信群修改了微信群名称
                                    // 需要根据微信群ID找出对应的微信群收款账号
                                    DidCollectionAccountModel didCollectionAccountByAcNameQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), "", 3);
                                    DidCollectionAccountModel didCollectionAccountByAcNameModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByAcNameQuery);
                                    if (didCollectionAccountByAcNameModel != null && didCollectionAccountByAcNameModel.getId() > 0){
                                        // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                        DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByAcNameModel.getId(), "检测：微信群名称被修改");
                                        ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                        // 删除小微旗下店员的关联关系
                                        WxModel wxByWxIdQuery = TaskMethod.assembleWxModel(data.getRobotWxid());
                                        WxModel wxByWxIdModel = (WxModel) ComponentUtil.wxService.findByObject(wxByWxIdQuery);

                                        WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(wxByWxIdModel.getId(), didCollectionAccountByAcNameModel.getId());
                                        ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);


                                        // 说明监控的微信群修改了微信群名称：监控微信群已修改了群名称
                                        String remark = "我方小微：" + wxByWxIdModel.getWxName() + "需退出群：" + data.getFromName();
                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, null, 0, null, 3,
                                                "监控微信群已修改了群名称：原群名称《" + didCollectionAccountByAcNameModel.getPayee() + "》，现群名称：《" + data.getFromName()+ "》", remark , 2, wxByWxIdModel.getId(),null);
                                        ComponentUtil.operateService.add(operateModel);

                                        // 更新此次task的状态：更新成失败-微信群名称被修改
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "微信群名称被修改");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                                    }else {
                                        // 说明监控的跑分用户已经删除微信群（服务数据），但是我方小微没有退出微信群
                                        // 查询我方小微信息
                                        WxModel wxByWxIdQuery = TaskMethod.assembleWxModel(data.getRobotWxid());
                                        WxModel wxByWxIdModel = (WxModel) ComponentUtil.wxService.findByObject(wxByWxIdQuery);

                                        // 说明监控的微信群修改了微信群名称：监控微信群已修改了群名称
                                        String remark = "我方小微：" + wxByWxIdModel.getWxName() + "，需退出群：" + data.getFromName();
                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, null, 0, null, 3,
                                                "跑分用户已经删除微信群（服务数据），但是我方小微没有退出微信群", remark , 2, wxByWxIdModel.getId(),null);
                                        ComponentUtil.operateService.add(operateModel);


                                        // 更新此次task的状态：更新成失败-根据微信群、微信ID都没有找到收款账号的相关信息
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据微信群、微信ID都没有找到收款账号的相关信息");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }
                                }

                            }else {
                                // 属于我方小微
                                // 更新此次task的状态：更新成失败-微信加群信息解析后属于我方小微加群信息
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "微信加群信息解析后属于我方小微加群信息");
                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                            }
                        }else {
                            // 更新此次task的状态：更新成失败-微信加群信息的内容不符规格
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "微信加群信息的内容不符规格");
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                        }

                    }else if(data.getDataType() == 5){
                        // 发红包

                        // 发红包-根据微信群群名称查询
                        DidCollectionAccountModel didCollectionAccountByPayeeQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee("", data.getFromName(), 3);
                        DidCollectionAccountModel didCollectionAccountByPayeeModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByPayeeQuery);
                        if (didCollectionAccountByPayeeModel != null && didCollectionAccountByPayeeModel.getId() > 0){
                            // 处理订单逻辑
                            // 查询此账号最新订单数据
                            OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(didCollectionAccountByPayeeModel.getDid(), didCollectionAccountByPayeeModel.getId(), 3);
                            OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                            if (orderModel != null && orderModel.getId() > 0){
                                if (orderModel.getOrderStatus() == 1){
                                    // 更新订单的操作状态-修改成发红包状态
                                    OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 3, 0, "");
                                    ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                    // 填充可爱猫解析数据：填充对应的订单信息
                                    CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                    ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                    // 更新此次task的状态：更新成成功状态
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }else if(orderModel.getOrderStatus() == 2){
                                    // 订单已超时

                                    // 更新订单的操作状态-修改成发红包状态
                                    OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 3, 0, "订单超时，支付用户已发红包");
                                    ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                    // 填充可爱猫解析数据：填充对应的订单信息
                                    CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                    ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                    // 更新此次task的状态：更新成失败-订单超时之后才发的红包
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "订单超时之后才发的红包");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }else{
                                    // 更新此次task的状态：更新成失败-脏数据：订单有质疑或者订单成功了，还有收到红包的信息
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "脏数据：订单有质疑或者订单成功了，还有收到红包的信息");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }
                            }else{
                                // 没有相关订单信息
                                // 更新此次task的状态：更新成失败-根据用户ID、收款账号没有查询到相关订单信息
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据用户ID、收款账号没有查询到相关订单信息");
                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                            }

                        }else{
                            // 发红包-说明监控的微信群修改了微信群名称
                            // 发红包-需要根据微信群ID找出对应的微信群收款账号
                            DidCollectionAccountModel didCollectionAccountByAcNameQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), "", 3);
                            DidCollectionAccountModel didCollectionAccountByAcNameModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByAcNameQuery);
                            if (didCollectionAccountByAcNameModel != null && didCollectionAccountByAcNameModel.getId() > 0){
                                // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByAcNameModel.getId(), "检测：微信群名称被修改");
                                ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                // 删除小微旗下店员的关联关系
                                WxModel wxByWxIdQuery = TaskMethod.assembleWxModel(data.getRobotWxid());
                                WxModel wxByWxIdModel = (WxModel) ComponentUtil.wxService.findByObject(wxByWxIdQuery);

                                WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(wxByWxIdModel.getId(), didCollectionAccountByAcNameModel.getId());
                                ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);

                                // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByAcNameModel.getDid(), didCollectionAccountByAcNameModel.getId());
                                OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                    if (orderByTimeData.getOrderStatus() <= 2){
                                        // 在30分钟之内有超时订单，直接罚款此用户
                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 4,
                                                "微信群名称被修改，但是在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, wxByWxIdModel.getId(),null);
                                        ComponentUtil.operateService.add(operateModel);

                                        // 填充可爱猫解析数据：填充对应的订单信息
                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                        ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                    }
                                }



                                // 发红包-说明监控的微信群修改了微信群名称：监控微信群已修改了群名称
                                String remark = "我方小微：" + wxByWxIdModel.getWxName() + "需退出群：" + data.getFromName();
                                OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, null, 0, null, 4,
                                        "监控微信群已修改了群名称：原群名称《" + didCollectionAccountByAcNameModel.getPayee() + "》，现群名称：《" + data.getFromName()+ "》", remark , 2, wxByWxIdModel.getId(),null);
                                ComponentUtil.operateService.add(operateModel);

                                // 更新此次task的状态：更新成失败-微信群名称被修改
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "微信群名称被修改");
                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                            }else {
                                // 发红包-说明监控的跑分用户已经删除微信群（服务数据），但是我方小微没有退出微信群

                                // 查询我方小微信息
                                WxModel wxByWxIdQuery = TaskMethod.assembleWxModel(data.getRobotWxid());
                                WxModel wxByWxIdModel = (WxModel) ComponentUtil.wxService.findByObject(wxByWxIdQuery);

                                // 不加yn=0的条件查询收款账号信息
                                DidCollectionAccountModel didCollectionAccountByAcNameYnQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), "", 3);
                                DidCollectionAccountModel didCollectionAccountByAcNameYnData = ComponentUtil.didCollectionAccountService.getDidCollectionAccountByWxGroupId(didCollectionAccountByAcNameYnQuery);
                                if (didCollectionAccountByAcNameYnData != null && didCollectionAccountByAcNameYnData.getId() > 0){
                                    // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                    OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByAcNameYnData.getDid(), didCollectionAccountByAcNameYnData.getId());
                                    OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                    if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                        if (orderByTimeData.getOrderStatus() <= 2){
                                            // 在30分钟之内有超时订单，直接罚款此用户
                                            OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 4,
                                                    "跑分用户已经删除微信群（服务数据），但是在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, wxByWxIdModel.getId(),null);
                                            ComponentUtil.operateService.add(operateModel);

                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                        }
                                    }
                                }


                                // 发红包-说明监控的微信群修改了微信群名称：监控微信群已修改了群名称
                                String remark = "我方小微：" + wxByWxIdModel.getWxName() + "，需退出群：" + data.getFromName() +"；另外需要人工查看此时用户是否有挂单，如果有挂单，需强制修改订单状态！";
                                OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, null, 0, null, 4,
                                        "跑分用户已经删除微信群（服务数据），但是我方小微没有退出微信群", remark , 2, wxByWxIdModel.getId(),null);
                                ComponentUtil.operateService.add(operateModel);


                                // 更新此次task的状态：更新成失败-根据微信群、微信ID都没有找到收款账号的相关信息
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据微信群、微信ID都没有找到收款账号的相关信息");
                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                            }
                        }

                    }else if(data.getDataType() == 6){
                        // 剔除成员

                        // 判断剔除信息是否是《你被""移出群聊》
                        boolean flag = TaskMethod.checkCatMember(data.getMsg());
                        if (flag){
                            // 判断是否是我方小微被移出
                            CatMember catMember = JSON.parseObject(data.getMsg(), CatMember.class);
                            // 根据小微ID查询小微信息
                            WxModel wxQuery = TaskMethod.assembleWxModel(catMember.member_wxid);
                            WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                            if (wxModel == null || wxModel.getId() <= 0){
                                // 属于其他支付用户

                                // 根据监控robot_wxid找出我方小微
                                WxModel wxQueryByRobotWxid = TaskMethod.assembleWxModel(data.getRobotWxid());
                                WxModel myWxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQueryByRobotWxid);
                                if (myWxModel != null && myWxModel.getId() > 0){
                                    // start
                                    // 剔除成员-根据微信群名称or微信群ID查询收款账号
                                    DidCollectionAccountModel didCollectionAccountByWxGroupIdOrWxGroupNameAndYnQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), data.getFromName(), 3);
                                    DidCollectionAccountModel didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel = ComponentUtil.didCollectionAccountService.getDidCollectionAccountByWxGroupIdOrWxGroupNameAndYn(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnQuery);
                                    if (didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel != null && didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId() > 0){
                                        if (didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getYn() == null || didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getYn() == 0){
                                            if (data.getFromName().equals(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getPayee())){
                                                // 剔除成员-说明监控的微信群没有修改微信群名称

                                                // 处理订单逻辑
                                                // 查询此账号最新订单数据
                                                OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getDid(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId(), 3);
                                                OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                                                if (orderModel != null && orderModel.getId() > 0){
                                                    if (orderModel.getOrderStatus() == 1){
                                                        // 更新订单的操作状态-修改成删除成员状态
                                                        OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 4, 3, "");
                                                        ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                                        // 填充可爱猫解析数据：填充对应的订单信息
                                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                                        ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                                        // 更新此次task的状态：更新成成功状态
                                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                                    }else if(orderModel.getOrderStatus() == 2){
                                                        // 订单已超时

                                                        // 更新订单的操作状态-修改成剔除成员状态
                                                        OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 4, 3, "订单超时，支付用户被剔除");
                                                        ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                                        // 填充可爱猫解析数据：填充对应的订单信息
                                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, null);
                                                        ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                                        // 更新此次task的状态：更新成失败-订单超时之后才剔除支付用户的
                                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "订单超时之后才剔除支付用户的");
                                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                                    }else{
                                                        // 更新订单的操作状态-修改成剔除成员状态
                                                        OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 4, 3, "订单成功状态时，支付用户被剔除");
                                                        ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                                        // 更新此次task的状态：更新成失败-脏数据：订单有质疑或者订单成功了，才踢除支付用户
                                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "脏数据：订单有质疑或者订单成功了，才踢除支付用户");
                                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                                    }
                                                }else{
                                                    // 没有相关订单信息
                                                    // 更新此次task的状态：更新成失败-根据用户ID、收款账号没有查询到相关订单信息
                                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据用户ID、收款账号没有查询到相关订单信息");
                                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                                }


                                            }else{
                                                // 剔除成员-说明监控的微信群修改了微信群名称

                                                // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                                DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId(), "检测：微信群名称被修改");
                                                ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                                // 删除小微旗下店员的关联关系
                                                WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(myWxModel.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId());
                                                ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);

                                                // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                                OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getDid(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId());
                                                OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                                if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                                    if (orderByTimeData.getOrderStatus() <= 2 && orderByTimeData.getDidStatus() > 1){
                                                        // 查询此订单是否被罚过
                                                        OperateModel operateQuery = TaskMethod.assembleOperateQuery(0,0,0,null,null, orderByTimeData.getOrderNo(), 2);
                                                        OperateModel operateData = (OperateModel) ComponentUtil.operateService.findByObject(operateQuery);
                                                        if (operateData == null || operateData.getId() <= 0){
                                                            // 没有被处罚过
                                                            // 在30分钟之内有超时订单，直接罚款此用户
                                                            OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 6,
                                                                    "微信群名称被修改，但是在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, myWxModel.getId(),null);
                                                            ComponentUtil.operateService.add(operateModel);
                                                        }

                                                        // 填充可爱猫解析数据：填充对应的订单信息
                                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                                        ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                                    }
                                                }

                                                // 删除成员-说明监控的微信群修改了微信群名称：监控微信群已修改了群名称
                                                String remark = "我方小微：" + myWxModel.getWxName() + "需退出群：" + data.getFromName();
                                                OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, null, 0, null, 6,
                                                        "监控微信群已修改了群名称：原群名称《" + didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getPayee() + "》，现群名称：《" + data.getFromName()+ "》", remark , 2, myWxModel.getId(),null);
                                                ComponentUtil.operateService.add(operateModel);

                                                // 更新此次task的状态：更新成失败-微信群名称被修改
                                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "微信群名称被修改");
                                                ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                                            }
                                        }else {
                                            // 表示此微信群收款账号已被用户删除（服务数据）
                                            // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                            OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getDid(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId());
                                            OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                            if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                                if (orderByTimeData.getOrderStatus() <= 2 && orderByTimeData.getDidStatus() > 1){
                                                    // 查询此订单是否被罚过
                                                    OperateModel operateQuery = TaskMethod.assembleOperateQuery(0,0,0,null,null, orderByTimeData.getOrderNo(), 2);
                                                    OperateModel operateData = (OperateModel) ComponentUtil.operateService.findByObject(operateQuery);
                                                    if (operateData == null || operateData.getId() <= 0){
                                                        // 没有被处罚过
                                                        // 在30分钟之内有超时订单，直接罚款此用户
                                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 6,
                                                                "踢出微信支付用户时，并且删除微信群（服务数据），并且此订单有支付用户进入，而且在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, myWxModel.getId(),null);
                                                        ComponentUtil.operateService.add(operateModel);
                                                    }

                                                    // 填充可爱猫解析数据：填充对应的订单信息
                                                    CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                                    ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                                }
                                            }
                                            // 更新此次task的状态：更新成失败状态：支付用户被剔除微信群，并且微信群被删（服务数据）
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "支付用户被剔除微信群，并且微信群被删（服务数据）");
                                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                        }
                                    }else {
                                        // 根据微信群名称or微信群ID查询收款账号-没有找到对应的收款账号

                                        // 剔除成员-说明小微错误加错群：因为根据微信群名称or微信群ID以及去掉yn没有查到对应的收款账号
                                        String remark = "我方小微：" + myWxModel.getWxName() + "，需退出群：" + data.getFromName() ;
                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, null, 0, null, 6,
                                                "说明小微错误加错群：因为根据微信群名称or微信群ID以及去掉yn没有查到对应的收款账号", remark , 2, myWxModel.getId(),null);
                                        ComponentUtil.operateService.add(operateModel);

                                        // 更新此次task的状态：更新成失败-剔除成员：根据微信群名称or微信群ID查询收款账号-没有找到对应的收款账号
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "剔除成员：根据微信群名称or微信群ID查询收款账号-没有找到对应的收款账号");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }
                                    // end
                                }else{
                                    // 更新此次task的状态：更新成失败-剔除成员：根据上报的监控的小微ID没有找到我方对应的小微数据
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "剔除成员：根据上报的监控的小微ID没有找到我方对应的小微数据");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }



                            }else {
                                // 属于我方小微账号

                                // 剔除成员-根据微信群群名称查询
                                DidCollectionAccountModel didCollectionAccountByPayeeQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee("", data.getFromName(), 3);
                                DidCollectionAccountModel didCollectionAccountByPayeeModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByPayeeQuery);
                                if (didCollectionAccountByPayeeModel != null && didCollectionAccountByPayeeModel.getId() > 0){
                                    // 删除小微旗下店员的关联关系
                                    WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(wxModel.getId(), didCollectionAccountByPayeeModel.getId());
                                    ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);

                                    // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                    DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByPayeeModel.getId(), "检测：我方小微被剔除群");
                                    ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                    // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                    OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByPayeeModel.getDid(), didCollectionAccountByPayeeModel.getId());
                                    OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                    if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                        if (orderByTimeData.getOrderStatus() <= 2 && orderByTimeData.getDidStatus() > 1){
                                            // 查询此订单是否被罚过
                                            OperateModel operateQuery = TaskMethod.assembleOperateQuery(0,0,0,null,null, orderByTimeData.getOrderNo(), 2);
                                            OperateModel operateData = (OperateModel) ComponentUtil.operateService.findByObject(operateQuery);
                                            if (operateData == null || operateData.getId() <= 0){
                                                // 没有被处罚过
                                                // 在30分钟之内有超时订单，直接罚款此用户
                                                OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 5,
                                                        "删除我方小微，并且此订单有支付用户进入，而且在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, wxModel.getId(),null);
                                                ComponentUtil.operateService.add(operateModel);
                                            }
                                        }
                                        // 填充可爱猫解析数据：填充对应的订单信息
                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                        ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                    }
                                    // 更新此次task的状态：更新成失败状态：我方小微被剔除微信群
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "我方小微被剔除微信群");
                                    ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                }else{
                                    // 剔除成员-说明监控的微信群修改了微信群名称
                                    // 剔除成员-需要根据微信群ID找出对应的微信群收款账号
                                    DidCollectionAccountModel didCollectionAccountByAcNameQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), "", 3);
                                    DidCollectionAccountModel didCollectionAccountByAcNameModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountByAcNameQuery);
                                    if (didCollectionAccountByAcNameModel != null && didCollectionAccountByAcNameModel.getId() > 0){
                                        // 删除小微旗下店员的关联关系
                                        WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(wxModel.getId(), didCollectionAccountByPayeeModel.getId());
                                        ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);

                                        // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                        DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByPayeeModel.getId(), "检测：我方小微被剔除群");
                                        ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                        // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                        OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByPayeeModel.getDid(), didCollectionAccountByPayeeModel.getId());
                                        OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                        if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                            if (orderByTimeData.getOrderStatus() <= 2 && orderByTimeData.getDidStatus() > 1){
                                                // 查询此订单是否被罚过
                                                OperateModel operateQuery = TaskMethod.assembleOperateQuery(0,0,0,null,null, orderByTimeData.getOrderNo(), 2);
                                                OperateModel operateData = (OperateModel) ComponentUtil.operateService.findByObject(operateQuery);
                                                if (operateData == null || operateData.getId() <= 0){
                                                    // 没有被处罚过
                                                    // 在30分钟之内有超时订单，直接罚款此用户
                                                    OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 5,
                                                            "删除我方小微，并且修改微信群名称，并且此订单有支付用户进入，而且在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, wxModel.getId(),null);
                                                    ComponentUtil.operateService.add(operateModel);
                                                }
                                            }
                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                        }
                                        // 更新此次task的状态：更新成失败状态：我方小微被剔除微信群，并且微信群名称被修改
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "我方小微被剔除微信群，并且微信群名称被修改");
                                        ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                    }else {
                                        // 剔除成员-小微被剔除-微信群被删（服务数据）
                                        // 不加yn=0的条件查询收款账号信息
                                        DidCollectionAccountModel didCollectionAccountByAcNameYnQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), "", 3);
                                        DidCollectionAccountModel didCollectionAccountByAcNameYnData = ComponentUtil.didCollectionAccountService.getDidCollectionAccountByWxGroupId(didCollectionAccountByAcNameYnQuery);
                                        if (didCollectionAccountByAcNameYnData != null && didCollectionAccountByAcNameYnData.getId() > 0){
                                            // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                            OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByAcNameYnData.getDid(), didCollectionAccountByAcNameYnData.getId());
                                            OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                            if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                                if (orderByTimeData.getOrderStatus() <= 2 && orderByTimeData.getDidStatus() > 1){
                                                    // 查询此订单是否被罚过
                                                    OperateModel operateQuery = TaskMethod.assembleOperateQuery(0,0,0,null,null, orderByTimeData.getOrderNo(), 2);
                                                    OperateModel operateData = (OperateModel) ComponentUtil.operateService.findByObject(operateQuery);
                                                    if (operateData == null || operateData.getId() <= 0){
                                                        // 没有被处罚过
                                                        // 在30分钟之内有超时订单，直接罚款此用户
                                                        OperateModel operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByPayeeModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 5,
                                                                "删除我方小微，并且删除微信群（服务数据），并且此订单有支付用户进入，而且在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, wxModel.getId(),null);
                                                        ComponentUtil.operateService.add(operateModel);
                                                    }

                                                    // 填充可爱猫解析数据：填充对应的订单信息
                                                    CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                                    ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                                }
                                            }
                                            // 更新此次task的状态：更新成失败状态：我方小微被剔除微信群，并且微信群被删（服务数据）
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "我方小微被剔除微信群，并且微信群被删（服务数据）");
                                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                        }else{
                                            // 更新此次task的状态：更新成失败状态：我方小微被剔除微信群
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "我方小微被剔除微信群");
                                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                                        }
                                    }
                                }
                            }

                        }else{
                            // 表示是《你被""移出群聊》

                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "你被移出群聊");
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);

                        }

                    }else if(data.getDataType() == 7){
                        // 成功收款
                        // 根据监控robot_wxid找出我方小微
                        WxModel wxQueryByRobotWxid = TaskMethod.assembleWxModel(data.getRobotWxid());
                        WxModel myWxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQueryByRobotWxid);
                        if (myWxModel != null && myWxModel.getId() > 0){
                            int actionType = 0; // 1表示收款账号正常，2表示收款账号名称被更改，3收款账号被删除（服务数据），4没找到对应的收款账号
                            // 成功收款-根据微信群名称or微信群ID查询收款账号
                            DidCollectionAccountModel didCollectionAccountByWxGroupIdOrWxGroupNameAndYnQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(data.getFromWxid(), data.getFromName(), 3);
                            DidCollectionAccountModel didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel = ComponentUtil.didCollectionAccountService.getDidCollectionAccountByWxGroupIdOrWxGroupNameAndYn(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnQuery);
                            if (didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel != null && didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId() > 0){
                                if (didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getYn() == null || didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getYn() == 0){
                                    if (data.getFromName().equals(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getPayee())){
                                        // 1表示收款账号正常
                                        actionType = 1;
                                    }else{
                                        // 2表示收款账号名称被更改
                                        actionType = 2;
                                    }
                                }else {
                                    // 3收款账号被删除（服务数据）
                                    actionType = 3;
                                }
                            }else{
                                // 4没找到对应的收款账号
                                actionType = 4;
                            }
                            int workType = 0; // task的补充结果状态
                            String workRemark = "";// task的补充的备注
                            OperateModel operateModel = null;
                            if (actionType == 1){
                                String [] fg_msg = data.getMsg().split("#");
                                String sucMoney = fg_msg[1];// 用户上报的成功金额

                                // 查询此账号最新订单数据
                                OrderModel orderQuery = TaskMethod.assembleOrderByNewestQuery(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getDid(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId(), 3);
                                OrderModel orderModel = ComponentUtil.orderService.getNewestOrder(orderQuery);
                                if (orderModel != null && orderModel.getId() > 0){
                                    if (orderModel.getOrderStatus() == 1){
                                        // 比较金额是否一致
                                        String result = StringUtil.getBigDecimalSubtractByStr(sucMoney, orderModel.getOrderMoney());
                                        if (result.equals("0")){
                                            // 金额一致
                                            // 修改订单操作状态修改：收款成功状态
                                            OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 7, 0, null);
                                            ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, sucMoney);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                                        }else {
                                            // 金额不一致
                                            // 修改订单操作状态修改：收款部分（跟订单金额不相同）
                                            OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 6, 0, sucMoney);
                                            ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, sucMoney);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                            // 定义补充状态
                                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                            workRemark = "收款部分（跟订单金额不相同），具体金额：" + sucMoney;

                                            // 定义惩罚数据
                                            operateModel = new OperateModel();
                                            String remark = "";
                                            operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, orderModel, 2, sucMoney, 7,
                                                    "上报金额与订单金额不匹配：支付用户金额支付错误", remark , 2, myWxModel.getId(),sucMoney);
                                        }

                                    }else if (orderModel.getOrderStatus() == 2){
                                        // 订单超时才进行成功数据上报
                                        // 比较金额是否一致
                                        String result = StringUtil.getBigDecimalSubtractByStr(sucMoney, orderModel.getOrderMoney());
                                        if (result.equals("0")){
                                            // 金额一致
                                            // 修改订单操作状态修改：收款成功状态
                                            OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 7, 0, null);
                                            ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, sucMoney);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE;
                                        }else {
                                            // 金额不一致
                                            // 修改订单操作状态修改：收款部分（跟订单金额不相同）
                                            OrderModel orderUpdate = TaskMethod.assembleOrderUpdateDidStatus(orderModel.getId(), 6, 0, sucMoney);
                                            ComponentUtil.orderService.updateDidStatus(orderUpdate);

                                            // 填充可爱猫解析数据：填充对应的订单信息
                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderModel, sucMoney);
                                            ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);

                                            // 定义补充状态
                                            workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                            workRemark = "收款部分（跟订单金额不相同），具体金额：" + sucMoney;

                                            // 定义惩罚数据
                                            operateModel = new OperateModel();
                                            String remark = "";
                                            operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, orderModel, 2, sucMoney, 7,
                                                    "上报金额与订单金额不匹配：支付用户金额支付错误", remark , 2, myWxModel.getId(),sucMoney);
                                        }



                                    }else {

                                    }
                                }


                            }else if (actionType == 2){

                            }else if (actionType == 3){
                                // 根据用户ID查询加目前时间前三十分钟查询订单信息
                                OrderModel orderByTimeQuery = TaskMethod.assembleOrderByCreateTime(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getDid(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId());
                                OrderModel orderByTimeData = ComponentUtil.orderService.getOrderByDidAndTime(orderByTimeQuery);
                                if (orderByTimeData != null && orderByTimeData.getId() > 0){
                                    if (orderByTimeData.getOrderStatus() <= 2 && orderByTimeData.getDidStatus() > 1){
                                        // 查询此订单是否被罚过
                                        OperateModel operateQuery = TaskMethod.assembleOperateQuery(0,0,0,null,null, orderByTimeData.getOrderNo(), 2);
                                        OperateModel operateData = (OperateModel) ComponentUtil.operateService.findByObject(operateQuery);
                                        if (operateData == null || operateData.getId() <= 0){
                                            // 没有被处罚过
                                            // 在30分钟之内有超时订单，直接罚款此用户
                                            operateModel = new OperateModel();
                                            operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, orderByTimeData, 2, orderByTimeData.getOrderMoney(), 7,
                                                    "用户收款成功时，并且删除微信群（服务数据），并且此订单有支付用户进入，而且在30分钟之内有初始化或超时订单，直接罚款此用户", null , 1, myWxModel.getId(),null);
                                        }

                                        // 填充可爱猫解析数据：填充对应的订单信息
                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisUpdate(data.getId(), orderByTimeData, null);
                                        ComponentUtil.catDataAnalysisService.update(catDataAnalysisModel);
                                    }
                                }
                                // 补充写数据
                            }else if (actionType == 4){
                                // 成功收款-说明小微错误加错群：因为根据微信群名称or微信群ID以及去掉yn没有查到对应的收款账号
                                operateModel = new OperateModel();
                                String remark = "我方小微：" + myWxModel.getWxName() + "，需退出群：" + data.getFromName() ;
                                operateModel = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, null, 0, null, 7,
                                        "说明小微错误加错群：因为根据微信群名称or微信群ID以及去掉yn没有查到对应的收款账号", remark , 2, myWxModel.getId(),null);

                                workType = ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO;
                                workRemark = "收款成功：根据微信群名称or微信群ID查询收款账号-没有找到对应的收款账号";
                            }

                            // 添加运营数据
                            if (operateModel != null){
                                ComponentUtil.operateService.add(operateModel);
                            }
                            // 更新此次task的状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), workType, workRemark);
                            ComponentUtil.taskCatDataAnalysisService.updateCatDataAnalysisStatus(statusModel);
                        }else {
                            // 此信息不属于我方小微的数据，异常脏数据
                            // 更新此次task的状态：更新成失败状态：脏数据：此信息不属于我方小微上报的数据
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "脏数据：此信息不属于我方小微上报的数据");
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
//     * @Description: 可爱猫回调订单的数据与派发订单数据进行匹配
//     * <p>
//     *     #需要重点测试！
//     *     每1每秒运行一次
//     *     1.查询出已补充完毕的可爱猫回调数据。
//     *     2.根据归属小微管理 + wxName 去派单表中找出未失效的订单数据。
//     *     3.for循环比对金额是否一致， 如果一致则表示订单消耗成功。
//     *     4.完善《可爱猫回调订单》表的数据，把匹配到的派单信息完善到《可爱猫回调订单》表中。
//     *     5.修改《任务订单（平台派发订单）》表的状态，修改成成功状态。
//     *
//     *     6.当根据小微管理 + wxName 匹配金额没有找到对应数据；则根据 小微管理 + 金额进行匹配，如果有一致的金额，
//     *     则需要停掉这段时间金额匹配到收款账号（因为需要及时停止才能不至于造成更多的脏数据，类似于根据wiId +  金额找出来符合的收款账号，然后停用。）
//     *
//     *
//     *
//     * </p>
//     * @author yoko
//     * @date 2019/12/6 20:25
//     */
////    @Scheduled(cron = "5 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每秒执行
//    public void catDataMatching() throws Exception{
////        log.info("----------------------------------TaskCatData.catDataMatching()----start");
//
//        // 获取需要填充的可爱猫数据
//        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeAndRunStatusQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
//        List<CatDataModel> synchroList = ComponentUtil.taskCatDataService.getCatDataList(statusQuery);
//        for (CatDataModel data : synchroList){
//            try{
//                // 锁住这个数据流水
//                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA, data.getId());
//                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
//                if (flagLock){
//                    OrderModel orderQuery = TaskMethod.assembleOrderQuery(data.getWxId(), data.getWxName(), 1);
//                    List<OrderModel> orderList = ComponentUtil.orderService.getInitOrderList(orderQuery);
//                    if (orderList != null && orderList.size() > 0 ){
//                        int num = 0;
//                        for (OrderModel orderModel : orderList){
//                            // 先锁住这个派单的主键ID
//                            String lockKey_order = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_BY_ORDER, orderModel.getId());
//                            boolean flagLock_order = ComponentUtil.redisIdService.lock(lockKey_order);
//                            if (flagLock_order){
//                                // 比较金额是否一致
//                                String result = StringUtil.getBigDecimalSubtractByStr(data.getOrderMoney(), orderModel.getOrderMoney());
//                                if (result.equals("0")){
//                                    // 表示金额一致:可以匹配到派单
//                                    num = 1;// 执行状态的更改了
//
//                                    // 组装要更新的可爱猫回调订单的数据
//                                    CatDataModel catDataModel = TaskMethod.assembleCatDataUpdate(data.getId(), 4, orderModel.getOrderNo());
//                                    // 组装要更新的派单的订单状态的数据
//                                    OrderModel orderUpdate = TaskMethod.assembleUpdateOrderStatus(orderModel.getId(), 4);
//                                    boolean flag = ComponentUtil.taskCatDataService.catDataMatchingOrderSuccess(catDataModel, orderUpdate);
//                                    if (flag){
//                                        // 更新此次task的状态：更新成成功
//                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
//                                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//                                    }else {
//                                        // 更新此次task的状态：更新成失败
//                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "事物：影响行出错");
//                                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//                                    }
//                                    break;
//                                }
//                                // 解锁
//                                ComponentUtil.redisIdService.delLock(lockKey_order);
//                            }
//                        }
//                        if (num == 0){
//                            // 表示task任务没有更改状态：需要补充更新task任务的状态
//                            // 更新此次task的状态：更新成失败-wxId+wxName没有匹配到金额一致的派单数据
//                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "wxId+wxName没有匹配到金额一致的派单数据");
//                            ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//
//                        }
//                    }else{
//                        // 根据微信ID 查询订单数据
//                        OrderModel orderByWxIdQuery = TaskMethod.assembleOrderQuery(data.getWxId(), "", 1);
//                        List<OrderModel> orderByWxIdList = ComponentUtil.orderService.getInitOrderList(orderByWxIdQuery);
//                        if (orderByWxIdList != null && orderByWxIdList.size() > 0){
//                            int num = 0;
//                            for (OrderModel orderModel : orderByWxIdList){
//                                // 先锁住这个派单的主键ID
//                                String lockKey_order = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_BY_ORDER, orderModel.getId());
//                                boolean flagLock_order = ComponentUtil.redisIdService.lock(lockKey_order);
//                                if (flagLock_order){
//                                    // 比较金额是否一致
//                                    String result = StringUtil.getBigDecimalSubtractByStr(data.getOrderMoney(), orderModel.getOrderMoney());
//
//                                    if (result.equals("0")){
//                                        // 表示金额一致:可以匹配到派单； 但是这个派单是wxId加订单金额才匹配到的，
//                                        // 出现这种数据有两种情况：1用户的微信账号修改了名字，2用户的微信账号修改了名字并且用户的收款码接收了别人的转账并且转账的金额与派单金额一致，
//                                        // 出现这样的数据时：需要当时金额的所有收款账号停止掉
//
//                                        //更新小微的使用状态
//                                        String remark = "根据wxId+金额匹配到的派单；需要wxId+wxName+金额匹配到；匹配到的派单的ID：" + orderModel.getId() + "，依据可爱猫回调数据ID：" + data.getId();
////                                        WxModel wxUpdate = TaskMethod.assembleWxUpdateUseStatus(data.getWxId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, remark);
////                                        ComponentUtil.wxService.update(wxUpdate);
//                                        DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateSwitch(orderModel.getCollectionAccountId(), remark);
//                                        ComponentUtil.didCollectionAccountService.updateDidCollectionAccountTotalSwitch(didCollectionAccountUpdate);
//
//                                        // #段峰：待写需要发送邮件或者手机短信通知运营人员小微管理被程序检测异常，停用了
//
//                                        num = 1;// 执行状态的更改了
//
//                                        // 更新此次task的状态：更新成成功-根据wxId+金额匹配到的派单；需要wxId+wxName+金额匹配到
//                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE,
//                                                "根据wxId+金额匹配到的派单；需要wxId+wxName+金额匹配到；匹配到的派单的ID：" + orderModel.getId());
//                                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//                                        break;
//                                    }
//                                    // 解锁
//                                    ComponentUtil.redisIdService.delLock(lockKey_order);
//                                }
//                            }
//                            if (num == 0){
//                                // 表示task任务没有更改状态：需要补充更新task任务的状态
//                                // 更新此次task的状态：更新成失败-wxId没有匹配到金额一致的派单数据
//                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "wxId没有匹配到金额一致的派单数据");
//                                ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//
//                            }
//
//                        }else{
//                            // 可以直接定性：脏数据；订单状态修改成orderStatus=2
//                            CatDataModel catDataModel = TaskMethod.assembleCatDataUpdate(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "");
//                            ComponentUtil.catDataService.updateCatData(catDataModel);
//                            // 更新此次task的状态：更新成成功=这里更新成成功是因为在这个业务逻辑中这条数据属于脏数据，可以直接更新它
//                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "脏数据");
//                            ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//                        }
//                    }
//
//
//                    // 解锁
//                    ComponentUtil.redisIdService.delLock(lockKey);
//                }
//
////                log.info("----------------------------------TaskCatData.catDataMatching()----end");
//            }catch (Exception e){
//                log.error(String.format("this TaskCatData.catDataMatching() is error , the dataId=%s !", data.getId()));
//                e.printStackTrace();
//                // 更新此次task的状态：更新成失败
//                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
//                ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
//            }
//        }
//    }

}
