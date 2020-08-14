package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description task:接单池子正在接单的Dao层
 * @Author yoko
 * @Date 2020/8/14 14:52
 * @Version 1.0
 */
@Mapper
public interface TaskPoolOpenMapper<T> extends BaseDao<T> {
}
