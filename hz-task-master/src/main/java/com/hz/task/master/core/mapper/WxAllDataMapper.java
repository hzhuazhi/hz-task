package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 微信回调原始数据的Dao层
 * @Author yoko
 * @Date 2020/8/11 17:49
 * @Version 1.0
 */
@Mapper
public interface WxAllDataMapper<T> extends BaseDao<T> {
}
