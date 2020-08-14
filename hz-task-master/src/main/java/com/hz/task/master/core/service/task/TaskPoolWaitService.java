package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;

import java.util.List;

/**
 * @Description task:接单池子等待接单的Service层
 * @Author yoko
 * @Date 2020/8/14 14:55
 * @Version 1.0
 */
public interface TaskPoolWaitService<T> extends BaseService<T> {

    /**
     * @Description: 查询等待池中的用户集合
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/19 10:51
     */
    public List<Long> getPoolWaitDidList(Object obj);

    /**
     * @Description: 更新剔除等待池的用户
     * @param obj
     * @return
     * @author yoko
     * @date 2020/8/14 15:31
    */
    public int updatePoolWaitYn(Object obj);
}
