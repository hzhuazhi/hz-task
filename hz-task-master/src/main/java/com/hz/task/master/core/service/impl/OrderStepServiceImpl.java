package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.OrderStepMapper;
import com.hz.task.master.core.service.OrderStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 订单步骤详情的Service层
 * @Author yoko
 * @Date 2020/7/25 16:23
 * @Version 1.0
 */
@Service
public class OrderStepServiceImpl<T> extends BaseServiceImpl<T> implements OrderStepService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * 11分钟.
     */
    public long ELEVEN_MIN = 660;

    public long TWO_HOUR = 2;

    @Autowired
    private OrderStepMapper orderStepMapper;

    public BaseDao<T> getDao() {
        return orderStepMapper;
    }
}
