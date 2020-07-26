package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.order.OrderStepModel;

/**
 * @Description 订单步骤详情的Service层
 * @Author yoko
 * @Date 2020/7/25 16:11
 * @Version 1.0
 */
public interface OrderStepService<T> extends BaseService<T> {

    /**
     * @Description: 添加订单步骤详情
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/25 20:42
    */
    public int addOrderStep(OrderStepModel model);
}
