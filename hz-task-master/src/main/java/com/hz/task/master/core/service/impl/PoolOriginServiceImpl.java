package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.PoolOriginMapper;
import com.hz.task.master.core.service.PoolOriginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 接单池子被移出的起源的Service层的实现层
 * @Author yoko
 * @Date 2020/8/13 15:28
 * @Version 1.0
 */
@Service
public class PoolOriginServiceImpl<T> extends BaseServiceImpl<T> implements PoolOriginService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private PoolOriginMapper poolOriginMapper;

    public BaseDao<T> getDao() {
        return poolOriginMapper;
    }
}
