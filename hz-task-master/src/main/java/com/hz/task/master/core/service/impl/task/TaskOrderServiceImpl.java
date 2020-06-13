package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskOrderMapper;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.service.task.TaskOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:任务订单（平台派发订单）的Service层的实现层
 * @Author yoko
 * @Date 2020/6/7 11:34
 * @Version 1.0
 */
@Service
public class TaskOrderServiceImpl<T> extends BaseServiceImpl<T> implements TaskOrderService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskOrderMapper taskOrderMapper;

    public BaseDao<T> getDao() {
        return taskOrderMapper;
    }

    @Override
    public List<OrderModel> getOrderList(Object obj) {
        return taskOrderMapper.getOrderList(obj);
    }

    @Override
    public int updateOrderStatus(Object obj) {
        return taskOrderMapper.updateOrderStatus(obj);
    }

    @Override
    public List<OrderModel> getOrderNotifyList(Object obj) {
        return taskOrderMapper.getOrderNotifyList(obj);
    }
}
