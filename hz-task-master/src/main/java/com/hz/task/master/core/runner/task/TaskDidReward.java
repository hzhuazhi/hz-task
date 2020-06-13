package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.did.DidRewardModel;
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
 * @Description task:跑奖励数据的类
 * @Author yoko
 * @Date 2020/6/6 13:48
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskDidReward {

    private final static Logger log = LoggerFactory.getLogger(TaskDidReward.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：用户奖励数据进行正事奖励给用户
     * <p>
     *     每1每秒运行一次
     *     1.查询出未跑task的奖励数据
     *     2.根据奖励类型，奖励给用户的账号上面
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void didReward() throws Exception{
//        log.info("----------------------------------TaskDidReward.didReward()----start");
        // 获取未跑的奖励数据
        StatusModel statusQuery = TaskMethod.assembleTaskStatusQuery(limitNum);
        List<DidRewardModel> synchroList = ComponentUtil.taskDidRewardService.getDidRewardList(statusQuery);
        for (DidRewardModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_REWARD, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    DidModel didModel = TaskMethod.assembleDidMoneyByReward(data);
                    if (didModel != null && didModel.getId() > 0){
                        // 锁住这个用户
                        String lockKey_did = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                        boolean flagLock_did = ComponentUtil.redisIdService.lock(lockKey_did);
                        if (flagLock_did){
                            num = ComponentUtil.didService.updateDidMoneyByReward(didModel);
                            if (num > 0){
                                // 更新此次task的状态：更新成成功
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
                                ComponentUtil.taskDidRewardService.updateDidRewardStatus(statusModel);
                            }else{
                                // 更新此次task的状态：更新成失败
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                                ComponentUtil.taskDidRewardService.updateDidRewardStatus(statusModel);
                            }

                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_did);
                        }

                    }else{
                        // 更新此次task的状态：更新成失败
                        StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                        ComponentUtil.taskDidRewardService.updateDidRewardStatus(statusModel);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidReward.didReward()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidReward.didReward() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                ComponentUtil.taskDidRewardService.updateDidRewardStatus(statusModel);
            }
        }
    }

}
