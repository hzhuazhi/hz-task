package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
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
 * @Description task:用户扣减余额流水的处理类
 * @Author yoko
 * @Date 2020/7/2 20:25
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskDidBalanceDeduct {

    private final static Logger log = LoggerFactory.getLogger(TaskDidBalanceDeduct.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：用户扣减余额流水的处理
     * <p>
     *     每1每秒运行一次
     *     1.查询出未跑task的扣减余额流水
     *     2.当订单状态等于成功的，则直接修改这个task的流水状态，修改成run_status=3
     *     3.当订单状态等于超时状态，把此流水扣减余额的金额进行累加到用户的余额。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void didBalanceDeduct() throws Exception{
//        log.info("----------------------------------TaskDidBalanceDeduct.didBalanceDeduct()----start");
        // 获取未跑的用户扣减余额流水的数据
        StatusModel statusQuery = TaskMethod.assembleTaskByOrderStatusQuery(limitNum, 1);
        List<DidBalanceDeductModel> synchroList = ComponentUtil.taskDidBalanceDeductService.getDidBalanceDeductList(statusQuery);
        for (DidBalanceDeductModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_BALANCE_DEDUCT, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (data.getOrderStatus() == 4){
                        // 成功订单
                        // 更新此次task的状态：更新成成功
                        StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
                        ComponentUtil.taskDidBalanceDeductService.updateDidBalanceDeductStatus(statusModel);
                    }else if (data.getOrderStatus() == 2){
                        // 失效订单

                        // 锁住这个用户
                        String lockKey_did = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                        boolean flagLock_did = ComponentUtil.redisIdService.lock(lockKey_did);
                        if (flagLock_did){
                            // 更新用户的余额
                            DidModel didModel = TaskMethod.assembleDidUpdateBalance(data.getDid(), data.getMoney());
                            num = ComponentUtil.didService.updateDidBalance(didModel);
                            if (num > 0){
                                // 更新此次task的状态：更新成成功
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
                                ComponentUtil.taskDidBalanceDeductService.updateDidBalanceDeductStatus(statusModel);
                            }else{
                                // 更新此次task的状态：更新成失败
                                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                                ComponentUtil.taskDidBalanceDeductService.updateDidBalanceDeductStatus(statusModel);
                            }
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_did);
                        }
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidBalanceDeduct.didBalanceDeduct()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidBalanceDeduct.didBalanceDeduct() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败
                StatusModel statusModel = TaskMethod.assembleTaskUpdateStatusModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
                ComponentUtil.taskDidRewardService.updateDidRewardStatus(statusModel);
            }
        }
    }
}
