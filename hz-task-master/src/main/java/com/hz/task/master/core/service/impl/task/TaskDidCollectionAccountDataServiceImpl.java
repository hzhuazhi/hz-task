package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskDidCollectionAccountDataMapper;
import com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel;
import com.hz.task.master.core.service.task.TaskDidCollectionAccountDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task：检测用户收款账号给出码以及成功的信息的Service层的实现层
 * @Author yoko
 * @Date 2020/6/19 10:32
 * @Version 1.0
 */
@Service
public class TaskDidCollectionAccountDataServiceImpl<T> extends BaseServiceImpl<T> implements TaskDidCollectionAccountDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskDidCollectionAccountDataMapper taskDidCollectionAccountDataMapper;


    public BaseDao<T> getDao() {
        return taskDidCollectionAccountDataMapper;
    }

    @Override
    public List<Long> getDidCollectionAccountList(TaskDidCollectionAccountDataModel model) {
        return taskDidCollectionAccountDataMapper.getDidCollectionAccountList(model);
    }

    @Override
    public int countLimitNum(TaskDidCollectionAccountDataModel model) {
        return taskDidCollectionAccountDataMapper.countLimitNum(model);
    }

    @Override
    public TaskDidCollectionAccountDataModel getSucLimitNumAndMoney(TaskDidCollectionAccountDataModel model) {
        return taskDidCollectionAccountDataMapper.getSucLimitNumAndMoney(model);
    }

    @Override
    public List<TaskDidCollectionAccountDataModel> getOrderStatusByDidCollectionAccount(TaskDidCollectionAccountDataModel model) {
        return taskDidCollectionAccountDataMapper.getOrderStatusByDidCollectionAccount(model);
    }
}
