package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatDataBindingModel;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.wx.WxClerkModel;
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
 * @Description task:可爱猫回调店员绑定小微的类
 * @Author yoko
 * @Date 2020/6/16 22:32
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskCatDataBinding {

    private final static Logger log = LoggerFactory.getLogger(TaskCatDataBinding.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: 可爱猫回调店员绑定小微的数据填充的task
     * <p>
     *     每1每秒运行一次
     *     1.填充可爱猫店员绑定小微的数据
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void catDataBindingWork() throws Exception{
//        log.info("----------------------------------TaskCatDataBinding.catDataBindingWork()----start");

        // 获取未填充可爱猫回调店员绑定小微的数据
        StatusModel statusQuery = TaskMethod.assembleTaskByWorkTypeQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        List<CatDataBindingModel> synchroList = ComponentUtil.taskCatDataBindingService.getCatDataBindingList(statusQuery);
        for (CatDataBindingModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_DATA_BINDING, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 根据可爱猫的robot_wxid查询小微的主键ID
                    WxModel wxQuery = TaskMethod.assembleWxModel(data.getToWxid());
                    WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                    if (wxModel != null && wxModel.getId() > 0){
                        // 根据微信昵称找到对应的微信收款账号
                        DidCollectionAccountModel didCollectionAccountModel = TaskMethod.assembleDidCollectionAccountQueryByPayee(data.getWxName());
                        List<DidCollectionAccountModel> didCollectionAccountList = ComponentUtil.didCollectionAccountService.findByCondition(didCollectionAccountModel);
                        if (didCollectionAccountList == null || didCollectionAccountList.size() <= 0){
                            // 可爱猫回调店员绑定小微的数据更新成未能匹配到收款账号
                            CatDataBindingModel catDataBindingUpdate = TaskMethod.assembleCatDataBindingUpdate(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE, wxModel.getId(), 0);
                            ComponentUtil.catDataBindingService.update(catDataBindingUpdate);

                            // 更新此次task的状态：更新成失败-根据微信昵称没有匹配到收款账号
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据微信昵称没有匹配到收款账号!");
                            ComponentUtil.taskCatDataBindingService.updateCatDataBindingStatus(statusModel);
                        }else if (didCollectionAccountList.size() == 1){
                            // 可爱猫回调店员绑定小微的数据更新成能匹配到收款账号；并且填充wxid，收款账号ID
                            CatDataBindingModel catDataBindingUpdate = TaskMethod.assembleCatDataBindingUpdate(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, wxModel.getId(), didCollectionAccountList.get(0).getId());
                            ComponentUtil.catDataBindingService.update(catDataBindingUpdate);

                            // 更新此次task的状态：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskCatDataBindingService.updateCatDataBindingStatus(statusModel);

                        }else {
                            // 可爱猫回调店员绑定小微的数据更新成未能匹配到收款账号
                            CatDataBindingModel catDataBindingUpdate = TaskMethod.assembleCatDataBindingUpdate(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE, wxModel.getId(), 0);
                            ComponentUtil.catDataBindingService.update(catDataBindingUpdate);

                            // 更新此次task的状态：更新成失败-根据微信昵称匹配到多个微信昵称相同的收款账号
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据微信昵称匹配到多个微信昵称相同的收款账号!");
                            ComponentUtil.taskCatDataBindingService.updateCatDataBindingStatus(statusModel);
                        }
                    }else{
                        // 更新此次task的状态：更新成失败-根据toWxid没有找到对应的小微信息
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据toWxid没有找到对应的小微信息!");
                        ComponentUtil.taskCatDataBindingService.updateCatDataBindingStatus(statusModel);
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskCatDataBinding.catDataBindingWork()----end");
            }catch (Exception e){
                log.error(String.format("this TaskCatDataBinding.catDataBindingWork() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try!");
                ComponentUtil.taskCatDataBindingService.updateCatDataBindingStatus(statusModel);
            }
        }
    }
}
