package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.exception.ServiceException;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.DidCollectionAccountMapper;
import com.hz.task.master.core.mapper.OrderMapper;
import com.hz.task.master.core.mapper.task.TaskCatDataOfflineMapper;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.service.task.TaskCatDataOfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description TODO
 * @Author yoko
 * @Date 2020/6/11 18:12
 * @Version 1.0
 */
@Service
public class TaskCatDataOfflineServiceImpl <T> extends BaseServiceImpl<T> implements TaskCatDataOfflineService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskCatDataOfflineMapper taskCatDataOfflineMapper;

    @Autowired
    private DidCollectionAccountMapper didCollectionAccountMapper;

    @Autowired
    private OrderMapper orderMapper;


    public BaseDao<T> getDao() {
        return taskCatDataOfflineMapper;
    }

    @Override
    public List<CatDataOfflineModel> getCatDataOfflineList(Object obj) {
        return taskCatDataOfflineMapper.getCatDataOfflineList(obj);
    }

    @Override
    public int updateCatDataOfflineStatus(Object obj) {
        return taskCatDataOfflineMapper.updateCatDataOfflineStatus(obj);
    }

    @Override
    public int updateCatDataOfflineOrderInfo(CatDataOfflineModel model) {
        return taskCatDataOfflineMapper.updateCatDataOfflineOrderInfo(model);
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public boolean handleCatDataOfferline(DidCollectionAccountModel didCollectionAccountModel, OrderModel orderModel, CatDataOfflineModel catDataOfflineModel) throws Exception{
        int num1 = didCollectionAccountMapper.updateDidCollectionAccountCheckData(didCollectionAccountModel);
        int num2 = orderMapper.updateOrderStatusByIdList(orderModel);
        int num3 = taskCatDataOfflineMapper.updateCatDataOfflineOrderInfo(catDataOfflineModel);

        if (num1 > 0 && num2 > 0 && num3 > 0 && num2 == orderModel.getIdList().size()){
            return true;
        }else{
            throw new ServiceException("handleCatDataOfferline", "三个执行更新SQL其中有一个或者多个响应行为0");
        }

    }
}
