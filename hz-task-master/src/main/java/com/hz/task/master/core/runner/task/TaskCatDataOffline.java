package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
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
 * @Description task:可爱猫回调店员下线的类
 * @Author yoko
 * @Date 2020/6/11 18:04
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskCatDataOffline {

    private final static Logger log = LoggerFactory.getLogger(TaskCatDataOffline.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: 可爱猫回调店员下线的task
     * <p>
     *     每1每秒运行一次
     *     1.判断matching_type的类型，如果matching_type = 1（1根据小微ID跟微信昵称不能匹配到收款账号），
     *     则需要：停掉此小微账号，删除此小微旗下店员的数据，旗下所有收款账号的审核状态都修改成初始化状态；
     *     后续如果查询出是具体哪个收款账号，还需要查看这个账号是否在下线那段时间是否给出过订单，如果查到给出过订单，则需要把那个订单修改成成功状态。
     *     以上的类型matching_type = 1作废，不予采纳， 重新给出的方式：当matching_type = 1的数据不做任何处理（这个由派单5次，如果没有一次成功的来进行检测甄别出来）
     *
     *     2.判断matching_type的类型，如果matching_type = 2（2根据小微ID跟微信昵称能匹配到收款账号），
     *     则需要：修改收款账号的审核信息（状态修改成审核初始化，审核原因：小微下线）；
     *     查询在下线期间，是否有派单：如果有派单，则强制把那个派单进行修改成成功状态。
     *
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void catDataOffline() throws Exception{
//        log.info("----------------------------------TaskCatDataOffline.catDataOffline()----start");

        // 获取未跑的可爱猫回调店员下线数据
        StatusModel statusQuery = TaskMethod.assembleTaskStatusQuery(limitNum);
        List<CatDataOfflineModel> synchroList = ComponentUtil.taskCatDataOfflineService.getCatDataOfflineList(statusQuery);
        for (CatDataOfflineModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_OFFLINE, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (data.getMatchingType() == 1){
                        // 直接修改成执行成功
                        // 更新此次task的状态：更新成成功
                        StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusByCatDataOffline(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, 2, "");
                        ComponentUtil.taskCatDataOfflineService.updateCatDataOfflineStatus(statusModel);
                    }else{
                        // 根据小微ID跟微信昵称能匹配到收款账号

                        // 查询从现在到十分钟之前是否有派单数据
                        OrderModel orderModel = TaskMethod.assembleOrderByCreateTimeQuery(data.getCollectionAccountId(), data.getCreateTime());
                        List<OrderModel> orderList = ComponentUtil.orderService.getOrderByCreateTime(orderModel);
                        if (orderList == null || orderList.size() <= 0){
                            // 只需要修改用户的收款账号：修改成审核初始化状态
                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckData(data.getCollectionAccountId());
                            int num = ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);
                            if (num > 0){
                                // 更新此次task的状态：更新成成功
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusByCatDataOffline(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, 4, "");
                                ComponentUtil.taskCatDataOfflineService.updateCatDataOfflineStatus(statusModel);
                            }else {
                                // 更新此次task的状态：更新成失败-修改用户收款账号审核信息响应行为0
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusByCatDataOffline(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 4, "修改用户收款账号审核信息响应行为0");
                                ComponentUtil.taskCatDataOfflineService.updateCatDataOfflineStatus(statusModel);
                            }
                        }else{
                            // 有派单数据时
                            // 组装要更改订单状态的数据
                            OrderModel orderUpdate = TaskMethod.assembleUpdateOrderStatus(orderList);
                            // 组装要更该可爱猫回调店员下线的订单信息数据
                            CatDataOfflineModel catDataOfflineModel = TaskMethod.assembleCatDataOffline(data.getId(), orderList);
                            // 组装用户收款账号要重新审核的数据
                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckData(data.getCollectionAccountId());
                            boolean flag = ComponentUtil.taskCatDataOfflineService.handleCatDataOfferline(didCollectionAccountUpdate, orderUpdate, catDataOfflineModel);
                            if (flag){
                                // 更新此次task的状态：更新成成功
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusByCatDataOffline(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, 3, "");
                                ComponentUtil.taskCatDataOfflineService.updateCatDataOfflineStatus(statusModel);
                            }else {
                                // 更新此次task的状态：更新成失败-找到对应的账号并且有派单的SQL响应数据行数对应不上
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusByCatDataOffline(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 3, "找到对应的账号并且有派单的SQL响应数据行数对应不上");
                                ComponentUtil.taskCatDataOfflineService.updateCatDataOfflineStatus(statusModel);
                            }
                        }

                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskCatDataOffline.catDataOffline()----end");
            }catch (Exception e){
                log.error(String.format("this TaskCatDataOffline.catDataOffline() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusByCatDataOffline(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 0, "异常失败try!");
                ComponentUtil.taskCatDataOfflineService.updateCatDataOfflineStatus(statusModel);
            }
        }
    }
}
