package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.client.ClientDataModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.task.base.StatusModel;
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
 * @Description task:客户端监听数据回调订单
 * @Author yoko
 * @Date 2020/7/6 20:59
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskClientData {

    private final static Logger log = LoggerFactory.getLogger(TaskClientData.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: 客户端监听数据回调订单的数据补充
     * <p>
     *     每1每秒运行一次
     *     1.根据支付宝账号userId找出对应的did，并且填充
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void clientDataWorkType() throws Exception{
//        log.info("----------------------------------TaskClientData.clientDataWorkType()----start");

        // 获取需要填充的客户端监听数据回调订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        List<ClientDataModel> synchroList = ComponentUtil.taskClientDataService.getClientDataList(statusQuery);
        for (ClientDataModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CLIENT_DATA_WORK_TYPE, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    DidCollectionAccountModel didCollectionAccountQuery = TaskMethod.assembleDidCollectionAccountByUserIdQuery(data.getUserId());
                    DidCollectionAccountModel didCollectionAccountModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountQuery);
                    if (didCollectionAccountModel == null || didCollectionAccountModel.getId() <= 0){
                        // 更新此次task的状态：更新成失败-根据userId没有找到对应收款账号的数据
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据userId没有找到对应收款账号的数据");
                        ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
                    }else{
                        // 填充数据
                        ClientDataModel updateClientData = TaskMethod.assembleClientDataUpdate(data.getId(), didCollectionAccountModel.getDid());
                        int num = ComponentUtil.clientDataService.updateDid(updateClientData);
                        if (num > 0){
                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
                        }else {

                        }
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskClientData.clientDataWorkType()----end");
            }catch (Exception e){
                log.error(String.format("this TaskClientData.clientDataWorkType() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
            }
        }
    }




    /**
     * @Description: 客户端监听数据回调订单与派发订单数据进行匹配
     * <p>
     *     #需要重点测试！
     *     每1每秒运行一次
     *     1.查询出已补充完毕的客户端监听数据回调订单数据。
     *     2.根据did + userid 去派单表中找出未失效的订单数据。
     *     3.for循环比对金额是否一致， 如果一致则表示订单消耗成功。
     *     4.完善《客户端监听数据回调订单》表的数据，把匹配到的派单信息完善到《客户端监听数据回调订单》表中。
     *     5.修改《任务订单（平台派发订单）》表的状态，修改成成功状态。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void clientDataMatching() throws Exception{
//        log.info("----------------------------------TaskClientData.clientDataMatching()----start");

        // 获取需要填充的客户端监听数据回调订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeAndRunStatusQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
        List<ClientDataModel> synchroList = ComponentUtil.taskClientDataService.getClientDataList(statusQuery);
        for (ClientDataModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CLIENT_DATA, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    OrderModel orderQuery = TaskMethod.assembleOrderByZfbQuery(data.getDid(), data.getUserId(), 2);
                    List<OrderModel> orderList = ComponentUtil.orderService.getInitOrderByZfbList(orderQuery);
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
                                    ClientDataModel clientDataModel = TaskMethod.assembleClientDataUpdate(data.getId(), 4, orderModel.getOrderNo());
                                    // 组装要更新的派单的订单状态的数据
                                    OrderModel orderUpdate = TaskMethod.assembleUpdateOrderStatus(orderModel.getId(), 4);
                                    boolean flag = ComponentUtil.taskClientDataService.clientDataMatchingOrderSuccess(clientDataModel, orderUpdate);
                                    if (flag){
                                        // 更新此次task的状态：更新成成功
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                        ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
                                    }else {
                                        // 更新此次task的状态：更新成失败
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "事物：影响行出错");
                                        ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
                                    }
                                    break;
                                }
                                // 解锁
                                ComponentUtil.redisIdService.delLock(lockKey_order);
                            }
                        }
                        if (num == 0){
                            // 表示task任务没有更改状态：需要补充更新task任务的状态
                            // 更新此次task的状态：更新成失败-did + userId没有匹配到金额一致的派单数据
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "did + userId没有匹配到金额一致的派单数据");
                            ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);

                        }
                    }else{
                        // 更新此次task的状态：更新成失败-did + userId没有匹配到派单数据
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "脏数据：did + userId没有匹配到派单数据");
                        ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
                    }


                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskClientData.clientDataMatching()----end");
            }catch (Exception e){
                log.error(String.format("this TaskClientData.clientDataMatching() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskClientDataService.updateClientDataStatus(statusModel);
            }
        }
    }

}
