package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;

import java.util.List;

/**
 * @Description task:接单池子正在接单的Service层
 * @Author yoko
 * @Date 2020/8/14 14:54
 * @Version 1.0
 */
public interface TaskPoolOpenService<T> extends BaseService<T> {

    /**
     * @Description: 查询进行中池子中的用户集合
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/19 10:51
     */
    public List<Long> getPoolOpenDidList(Object obj);

    /**
     * @Description: 更新剔除进行中池子的用户
     * @param obj
     * @return
     * @author yoko
     * @date 2020/8/14 15:31
     */
    public int updatePoolOpenYn(Object obj);
}
