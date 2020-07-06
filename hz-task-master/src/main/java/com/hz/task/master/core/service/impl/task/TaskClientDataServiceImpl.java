package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.exception.ServiceException;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.ClientDataMapper;
import com.hz.task.master.core.mapper.OrderMapper;
import com.hz.task.master.core.mapper.task.TaskClientDataMapper;
import com.hz.task.master.core.model.client.ClientDataModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.service.task.TaskClientDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description task:客户端监听数据回调订单的Service层的实现层
 * @Author yoko
 * @Date 2020/7/6 18:53
 * @Version 1.0
 */
@Service
public class TaskClientDataServiceImpl<T> extends BaseServiceImpl<T> implements TaskClientDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskClientDataMapper taskClientDataMapper;

    @Autowired
    private ClientDataMapper clientDataMapper;

    @Autowired
    private OrderMapper orderMapper;


    public BaseDao<T> getDao() {
        return taskClientDataMapper;
    }

    @Override
    public List<ClientDataModel> getClientDataList(Object obj) {
        return taskClientDataMapper.getClientDataList(obj);
    }

    @Override
    public int updateClientDataStatus(Object obj) {
        return taskClientDataMapper.updateClientDataStatus(obj);
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public boolean clientDataMatchingOrderSuccess(ClientDataModel clientDataModel, OrderModel orderModel) throws Exception{
        int num1 = clientDataMapper.updateClientData(clientDataModel);
        int num2 = orderMapper.updateOrderStatus(orderModel);

        if (num1 > 0 && num2 > 0){
            return true;
        }else {
            throw new ServiceException("clientDataMatchingOrderSuccess", "二个执行更新SQL其中有一个或者两个响应行为0");
        }

    }
}
