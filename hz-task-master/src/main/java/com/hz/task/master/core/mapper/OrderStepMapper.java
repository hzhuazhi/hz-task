package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 订单步骤详情的Dao层
 * @Author yoko
 * @Date 2020/7/25 16:09
 * @Version 1.0
 */
@Mapper
public interface OrderStepMapper<T> extends BaseDao<T> {
}
