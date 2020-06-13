package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidRewardModel;

import java.util.List;

/**
 * @Description task：用户奖励数据的Service层
 * @Author yoko
 * @Date 2020/6/6 13:59
 * @Version 1.0
 */
public interface TaskDidRewardService<T> extends BaseService<T> {

    /**
     * @Description: 查询用户奖励数据
     * @param obj - run_status
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<DidRewardModel> getDidRewardList(Object obj);

    /**
     * @Description: 更新用户奖励数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateDidRewardStatus(Object obj);
}
