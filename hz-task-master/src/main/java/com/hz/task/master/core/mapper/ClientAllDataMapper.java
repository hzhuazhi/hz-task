package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 客户端监听数据回调原始数据的Dao层
 * @Author yoko
 * @Date 2020/7/6 16:10
 * @Version 1.0
 */
@Mapper
public interface ClientAllDataMapper<T> extends BaseDao<T> {
}
