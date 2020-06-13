package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.exception.ServiceException;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.CatDataMapper;
import com.hz.task.master.core.mapper.OrderMapper;
import com.hz.task.master.core.mapper.task.TaskCatDataMapper;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.service.task.TaskCatDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description task:可爱猫回调订单的Service层的实现层
 * @Author yoko
 * @Date 2020/6/6 17:49
 * @Version 1.0
 */
@Service
public class TaskCatDataServiceImpl<T> extends BaseServiceImpl<T> implements TaskCatDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskCatDataMapper taskCatDataMapper;

    @Autowired
    private CatDataMapper catDataMapper;

    @Autowired
    private OrderMapper orderMapper;


    public BaseDao<T> getDao() {
        return taskCatDataMapper;
    }

    @Override
    public List<CatDataModel> getCatDataList(Object obj) {
        return taskCatDataMapper.getCatDataList(obj);
    }

    @Override
    public int updateCatDataStatus(Object obj) {
        return taskCatDataMapper.updateCatDataStatus(obj);
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public boolean catDataMatchingOrderSuccess(CatDataModel catDataModel, OrderModel orderModel) throws Exception{
        int num1 = catDataMapper.updateCatData(catDataModel);
        int num2 = orderMapper.updateOrderStatus(orderModel);

        if (num1 > 0 && num2 > 0){
            return true;
        }else {
            throw new ServiceException("catDataMatchingOrderSuccess", "二个执行更新SQL其中有一个或者两个响应行为0");
        }

    }

}
