package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.did.DidRewardModel;
import com.hz.task.master.core.model.task.base.StatusModel;
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
 * @Description task：小微
 * @Author yoko
 * @Date 2020/8/2 9:33
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskWx {

    private final static Logger log = LoggerFactory.getLogger(TaskWx.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：check校验小微加好友是否上限
     * <p>
     *     每2分钟运行一次
     *     1.查询出未所有小微未超过总加好友的数量的小微数据。
     *     2.统计小微好友流水总好友是否超过上限，当日添加好友是否超过上限。
     *     3.如果总好友超过上限，则is_ok修改成2。
     *     4.如果当日超过当日设定的上限，则添加redis缓存（redis失效时间：当日凌晨自动失效）。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(fixedDelay = 1000) // 每秒执行
    @Scheduled(fixedDelay = 12000) // 每2分钟执行
    public void wxLimitNum() throws Exception{
//        log.info("----------------------------------TaskWx.wxLimitNum()----start");
        // 获取小微未超过总上限的数据
        WxModel wxQuery = TaskMethod.assembleWxByIsOk(1);
        List<WxModel> synchroList = ComponentUtil.taskWxService.getWxList(wxQuery);
        for (WxModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_WX_IS_OK, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 查所有加好友的数量
                    WxFriendModel wxFriendAllQuery = TaskMethod.assembleWxFriendQuery(data.getId(), 0);
                    int dataNum = ComponentUtil.wxFriendService.queryByCount(wxFriendAllQuery);
                    if (dataNum > 0){
                        int isOk = 0;
                        if (dataNum >= data.getDataNum()){
                            // 更新状态:已经超过规定的限制
                            isOk = 2;
                        }else {
                            //未超过规定的限制
                            isOk = 1;
                        }
                        WxModel wxUpdate = TaskMethod.assembleWxUpdate(data.getId(), isOk, dataNum);
                        ComponentUtil.taskWxService.updateWxStatus(wxUpdate);
                    }


                    // 查当日加好友的数量

                    // 先查看缓存中是否有当日已经超的数据，如果有，则无需执行
                    String strKeyCache_wx_day_num = CachedKeyUtils.getCacheKey(CacheKey.WX_DAY_NUM, data.getId());
                    String strCache_wx_day_num = (String) ComponentUtil.redisService.get(strKeyCache_wx_day_num);
                    if (StringUtils.isBlank(strCache_wx_day_num)){
                        // 表示缓存中没有数据，则需要执行
                        int curday = DateUtil.getDayNumber(new Date());
                        WxFriendModel wxFriendAdyQuery = TaskMethod.assembleWxFriendQuery(data.getId(), curday);
                        int dayNum = ComponentUtil.wxFriendService.queryByCount(wxFriendAdyQuery);// 当日加好友数量
                        if (dayNum >= data.getDayNum()){
                            // 缓存设置：设置日新增好友已到达上限-失效时间当日的凌晨零点
                            long time = DateUtil.getTomorrowMinute();
                            String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.WX_DAY_NUM, data.getId());
                            ComponentUtil.redisService.set(strKeyCache, String.valueOf(dayNum) , time);
                        }

                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskWx.wxLimitNum()----end");
            }catch (Exception e){
                log.error(String.format("this TaskWx.wxLimitNum() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
            }
        }
    }
}
