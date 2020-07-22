package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.wx.WxModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.TaskMethod;
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

        // 获取需要填充的银行回调数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        List<CatDataModel> synchroList = ComponentUtil.taskCatDataService.getCatDataList(statusQuery);
        for (CatDataModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_WORK_TYPE, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    WxModel wxQuery = TaskMethod.assembleWxModel(data.getToWxid());
                    WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                    if (wxModel == null || wxModel.getId() <= 0){
                        // 更新此次task的状态：更新成失败-根据toWxid没有找到对应的数据
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据toWxid没有找到对应的数据");
                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
                    }else{
                        // 填充数据
                        CatDataModel updateCatData = TaskMethod.assembleCatDataUpdate(data.getId(), wxModel.getId());
                        ComponentUtil.catDataService.updateWxId(updateCatData);

                        // 更新此次task的状态：更新成成功
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
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
                ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
            }
        }
    }




    /**
     * @Description: 可爱猫回调订单的数据与派发订单数据进行匹配
     * <p>
     *     #需要重点测试！
     *     每1每秒运行一次
     *     1.查询出已补充完毕的可爱猫回调数据。
     *     2.根据归属小微管理 + wxName 去派单表中找出未失效的订单数据。
     *     3.for循环比对金额是否一致， 如果一致则表示订单消耗成功。
     *     4.完善《可爱猫回调订单》表的数据，把匹配到的派单信息完善到《可爱猫回调订单》表中。
     *     5.修改《任务订单（平台派发订单）》表的状态，修改成成功状态。
     *
     *     6.当根据小微管理 + wxName 匹配金额没有找到对应数据；则根据 小微管理 + 金额进行匹配，如果有一致的金额，
     *     则需要停掉这段时间金额匹配到收款账号（因为需要及时停止才能不至于造成更多的脏数据，类似于根据wiId +  金额找出来符合的收款账号，然后停用。）
     *
     *
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void catDataMatching() throws Exception{
//        log.info("----------------------------------TaskCatData.catDataMatching()----start");

        // 获取需要填充的可爱猫数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeAndRunStatusQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
        List<CatDataModel> synchroList = ComponentUtil.taskCatDataService.getCatDataList(statusQuery);
        for (CatDataModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    OrderModel orderQuery = TaskMethod.assembleOrderQuery(data.getWxId(), data.getWxName(), 1);
                    List<OrderModel> orderList = ComponentUtil.orderService.getInitOrderList(orderQuery);
                    if (orderList != null && orderList.size() > 0 ){
                        int num = 0;
                        for (OrderModel orderModel : orderList){
                            // 先锁住这个派单的主键ID
                            String lockKey_order = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_BY_ORDER, orderModel.getId());
                            boolean flagLock_order = ComponentUtil.redisIdService.lock(lockKey_order);
                            if (flagLock_order){
                                // 比较金额是否一致
                                String result = StringUtil.getBigDecimalSubtractByStr(data.getOrderMoney(), orderModel.getOrderMoney());
                                if (result.equals("0")){
                                    // 表示金额一致:可以匹配到派单
                                    num = 1;// 执行状态的更改了

                                    // 组装要更新的可爱猫回调订单的数据
                                    CatDataModel catDataModel = TaskMethod.assembleCatDataUpdate(data.getId(), 4, orderModel.getOrderNo());
                                    // 组装要更新的派单的订单状态的数据
                                    OrderModel orderUpdate = TaskMethod.assembleUpdateOrderStatus(orderModel.getId(), 4);
                                    boolean flag = ComponentUtil.taskCatDataService.catDataMatchingOrderSuccess(catDataModel, orderUpdate);
                                    if (flag){
                                        // 更新此次task的状态：更新成成功
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
                                    }else {
                                        // 更新此次task的状态：更新成失败
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "事物：影响行出错");
                                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
                                    }
                                    break;
                                }
                                // 解锁
                                ComponentUtil.redisIdService.delLock(lockKey_order);
                            }
                        }
                        if (num == 0){
                            // 表示task任务没有更改状态：需要补充更新task任务的状态
                            // 更新此次task的状态：更新成失败-wxId+wxName没有匹配到金额一致的派单数据
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "wxId+wxName没有匹配到金额一致的派单数据");
                            ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);

                        }
                    }else{
                        // 根据微信ID 查询订单数据
                        OrderModel orderByWxIdQuery = TaskMethod.assembleOrderQuery(data.getWxId(), "", 1);
                        List<OrderModel> orderByWxIdList = ComponentUtil.orderService.getInitOrderList(orderByWxIdQuery);
                        if (orderByWxIdList != null && orderByWxIdList.size() > 0){
                            int num = 0;
                            for (OrderModel orderModel : orderByWxIdList){
                                // 先锁住这个派单的主键ID
                                String lockKey_order = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_BY_ORDER, orderModel.getId());
                                boolean flagLock_order = ComponentUtil.redisIdService.lock(lockKey_order);
                                if (flagLock_order){
                                    // 比较金额是否一致
                                    String result = StringUtil.getBigDecimalSubtractByStr(data.getOrderMoney(), orderModel.getOrderMoney());

                                    if (result.equals("0")){
                                        // 表示金额一致:可以匹配到派单； 但是这个派单是wxId加订单金额才匹配到的，
                                        // 出现这种数据有两种情况：1用户的微信账号修改了名字，2用户的微信账号修改了名字并且用户的收款码接收了别人的转账并且转账的金额与派单金额一致，
                                        // 出现这样的数据时：需要当时金额的所有收款账号停止掉

                                        //更新小微的使用状态
                                        String remark = "根据wxId+金额匹配到的派单；需要wxId+wxName+金额匹配到；匹配到的派单的ID：" + orderModel.getId() + "，依据可爱猫回调数据ID：" + data.getId();
//                                        WxModel wxUpdate = TaskMethod.assembleWxUpdateUseStatus(data.getWxId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, remark);
//                                        ComponentUtil.wxService.update(wxUpdate);
                                        DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateSwitch(orderModel.getCollectionAccountId(), remark);
                                        ComponentUtil.didCollectionAccountService.updateDidCollectionAccountTotalSwitch(didCollectionAccountUpdate);

                                        // #段峰：待写需要发送邮件或者手机短信通知运营人员小微管理被程序检测异常，停用了

                                        num = 1;// 执行状态的更改了

                                        // 更新此次task的状态：更新成成功-根据wxId+金额匹配到的派单；需要wxId+wxName+金额匹配到
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE,
                                                "根据wxId+金额匹配到的派单；需要wxId+wxName+金额匹配到；匹配到的派单的ID：" + orderModel.getId());
                                        ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
                                        break;
                                    }
                                    // 解锁
                                    ComponentUtil.redisIdService.delLock(lockKey_order);
                                }
                            }
                            if (num == 0){
                                // 表示task任务没有更改状态：需要补充更新task任务的状态
                                // 更新此次task的状态：更新成失败-wxId没有匹配到金额一致的派单数据
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "wxId没有匹配到金额一致的派单数据");
                                ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);

                            }

                        }else{
                            // 可以直接定性：脏数据；订单状态修改成orderStatus=2
                            CatDataModel catDataModel = TaskMethod.assembleCatDataUpdate(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "");
                            ComponentUtil.catDataService.updateCatData(catDataModel);
                            // 更新此次task的状态：更新成成功=这里更新成成功是因为在这个业务逻辑中这条数据属于脏数据，可以直接更新它
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "脏数据");
                            ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
                        }
                    }


                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskCatData.catDataMatching()----end");
            }catch (Exception e){
                log.error(String.format("this TaskCatData.catDataMatching() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskCatDataService.updateCatDataStatus(statusModel);
            }
        }
    }

}
