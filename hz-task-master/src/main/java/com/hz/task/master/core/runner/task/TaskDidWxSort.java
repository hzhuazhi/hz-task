package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidWxSortModel;
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
import java.util.Set;

/**
 * @Description 用户的微信出码排序的task
 * @Author yoko
 * @Date 2020/9/3 11:11
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskDidWxSort {

    private final static Logger log = LoggerFactory.getLogger(TaskDidWxSort.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：跑用户微信的出码排序
     * <p>
     *     每2秒运行一次
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
    @Scheduled(fixedDelay = 2000) // 每2秒执行
    public void didWxLimit() throws Exception{
//        log.info("----------------------------------TaskDidWxSort.didWxLimit()----start");
        // 获取要触发用户微信排序的redis的key
        String prefix = CachedKeyUtils.getCacheKey(CacheKey.DID_WX_SORT, "*");
        // 获取所有的key
//        Set<String> data = redisTemplate.keys(prefix);
        Set<String> synchroList = ComponentUtil.redisService.prefixKeys(prefix);
        for (String data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_WX_SORT, data);
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    String strCache = (String) ComponentUtil.redisService.get(data);
                    if (!StringUtils.isBlank(strCache)) {
                        // 从缓存里面获取数据
                        DidWxSortModel redis_didWxSortModel = null;
                        redis_didWxSortModel = JSON.parseObject(strCache, DidWxSortModel.class);
                        if (redis_didWxSortModel != null && redis_didWxSortModel.getDid() != null && redis_didWxSortModel.getDid() > 0){
                            long did = redis_didWxSortModel.getDid();
                            String toWxid = redis_didWxSortModel.getToWxid();
                            String delayTime = redis_didWxSortModel.getDelayTime();
                            log.info("strKeyCache:" + data + ",strCache:" + strCache);
                            // 根据条件查询此排序的信息
                            DidWxSortModel didWxSortQuery = TaskMethod.assembleDidWxSortData(0, did, toWxid,
                                    0, 0, 0, 0, 0, null, null, null);
                            DidWxSortModel didWxSortModel = (DidWxSortModel)ComponentUtil.didWxSortService.findByObject(didWxSortQuery);
                            if (didWxSortModel != null && didWxSortModel.getId() != null && didWxSortModel.getId() > 0){
                                // 把此用户的微信进行延迟使用的更新
                                DidWxSortModel didWxSortUpdate = TaskMethod.assembleDidWxSortData(didWxSortModel.getId(), 0, null,
                                        0, 0, 1, 0, 0, null, null, delayTime);
                                ComponentUtil.didWxSortService.update(didWxSortUpdate);

                                // 怕同时出现两个正在使用的微信，统一更新此用户的所有微信更新成未使用状态
                                DidWxSortModel didWxSortUpInUseByDid = TaskMethod.assembleDidWxSortData(0, did, null,
                                        0, 0, 1, 0, 0, null, null, null);
                                ComponentUtil.didWxSortService.updateInUse(didWxSortUpInUseByDid);

                                // 根据刚刚更新的微信，找出刚刚更新的微信的排序的下一个微信的排序的微信出来:这里还有一个字段要注意，就是delayTime一定要小于系统时间
                                DidWxSortModel didWxSortNextQuery = TaskMethod.assembleDidWxSortData(0, did, null,
                                        0, 1, 0, didWxSortModel.getSort(), 0, null, "1", null);
                                DidWxSortModel didWxSortNextModel = (DidWxSortModel)ComponentUtil.didWxSortService.findByObject(didWxSortNextQuery);
                                if (didWxSortNextModel != null && didWxSortNextModel.getId() != null && didWxSortNextModel.getId() > 0){
                                    // 把此微信更新成使用状态
                                    DidWxSortModel didWxSortUpInUseIng = TaskMethod.assembleDidWxSortData(didWxSortNextModel.getId(), 0, null,
                                            0, 0, 2, 0, 0, null, null, null);
                                    ComponentUtil.didWxSortService.updateInUse(didWxSortUpInUseIng);
                                }else {
                                    // 说明上一个的微信的排序已经是最后一个微信了：需要重新轮询了
                                    // 重新轮询：查询出此用户位置最小的一个微信
                                    DidWxSortModel didWxSortMinQuery = TaskMethod.assembleDidWxSortData(0, did, null,
                                            0, 1, 0, 0, 0, null, "1", null);
                                    DidWxSortModel didWxSortMinModel = (DidWxSortModel)ComponentUtil.didWxSortService.findByObject(didWxSortMinQuery);
                                    if (didWxSortMinModel != null && didWxSortMinModel.getId() != null && didWxSortMinModel.getId() > 0){

                                        // 这里怕上面要停用的那个微信跟查的顺序最小的微信是同一个微信：这样就代表用户只有一个微信在跑，如果只有一个微信在跑，则不能更新
                                        if (didWxSortMinModel.getId() != didWxSortModel.getId()){
                                            // 把此微信更新成使用状态
                                            DidWxSortModel didWxSortUpInUseIng = TaskMethod.assembleDidWxSortData(didWxSortMinModel.getId(), 0, null,
                                                    0, 0, 2, 0, 0, null, null, null);
                                            ComponentUtil.didWxSortService.updateInUse(didWxSortUpInUseIng);
                                        }

                                    }
                                }
                            }
                        }
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidWxSort.didWxLimit()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidWxSort.didWxLimit() is error , the dataId=%s !", data));
                e.printStackTrace();
            }

            // 删除此redis
            ComponentUtil.redisService.remove(data);
        }
    }
}
