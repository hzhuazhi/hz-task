package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.order.OrderModel;

import java.util.List;

/**
 * @Description task:任务订单（平台派发订单）的Service层
 * @Author yoko
 * @Date 2020/6/7 11:32
 * @Version 1.0
 */
public interface TaskOrderService<T> extends BaseService<T> {

    /**
     * @Description: 根据条件获取派单数据-未跑task的派单数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<OrderModel> getOrderList(Object obj);

    /**
     * @Description: 更新派单数据数据的状态-task的状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateOrderStatus(Object obj);


    /**
     * @Description: 获取要同步给下游的订单数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/8 17:38
    */
    public List<OrderModel> getOrderNotifyList(Object obj);
}
