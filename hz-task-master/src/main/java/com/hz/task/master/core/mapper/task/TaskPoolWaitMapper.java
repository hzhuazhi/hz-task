package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:接单池子等待接单的Dao层
 * @Author yoko
 * @Date 2020/8/14 14:53
 * @Version 1.0
 */
@Mapper
public interface TaskPoolWaitMapper<T> extends BaseDao<T> {

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
