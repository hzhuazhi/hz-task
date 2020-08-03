package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.wx.WxFriendModel;
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

import java.util.Date;
import java.util.List;

/**
 * @Description task:用户的task
 * @Author yoko
 * @Date 2020/8/3 15:31
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskDid {

    private final static Logger log = LoggerFactory.getLogger(TaskDid.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：check用户的有效微信群
     * <p>
     *     每2分钟运行一次
     *     1.查询所有用户。
     *     2.根据用户查询有效微信群。
     *     3.如果有效微信群没有达到策略部署的要求，则关闭个人出码的状态。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(fixedDelay = 1000) // 每秒执行
    @Scheduled(fixedDelay = 6000) // 每1分钟执行
    public void didSwitch() throws Exception{
//        log.info("----------------------------------TaskDid.didSwitch()----start");

        // 策略数据：微信群有效个数才允许正常出码
        StrategyModel strategyQuery = TaskMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.GROUP_NUM.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        int groupNum = strategyModel.getStgNumValue();


        // 获取个人出码未关闭的用户
        DidModel didQuery = TaskMethod.assembleDidBySwitch(1);
        List<DidModel> synchroList = ComponentUtil.taskDidService.findByCondition(didQuery);
        for (DidModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_SWITCH_TYPE, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 查此用户下的所有有效微信群信息
                    DidCollectionAccountModel didCollectionAccountQuery = TaskMethod.assembleDidCollectionAccountByEffective(data.getId(), 3);
                    List<DidCollectionAccountModel> didCollectionAccountList = ComponentUtil.didCollectionAccountService.getEffectiveDidCollectionAccountList(didCollectionAccountQuery);
                    if (didCollectionAccountList != null && didCollectionAccountList.size() > 0){
                        if (didCollectionAccountList.size() < groupNum){
                            // 修改出码状态： 修改成关闭状态
                            DidModel updateGroupOrSwitch = TaskMethod.assembleUpdateGroupOrSwitchData(data.getId(), 0, 2);
                            ComponentUtil.didService.updateDidGroupNumOrSwitchType(updateGroupOrSwitch);
                        }
                    }else {
                        // 修改出码状态： 修改成关闭状态
                        DidModel updateGroupOrSwitch = TaskMethod.assembleUpdateGroupOrSwitchData(data.getId(), 0, 2);
                        ComponentUtil.didService.updateDidGroupNumOrSwitchType(updateGroupOrSwitch);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDid.didSwitch()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDid.didSwitch() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
            }
        }
    }
}
