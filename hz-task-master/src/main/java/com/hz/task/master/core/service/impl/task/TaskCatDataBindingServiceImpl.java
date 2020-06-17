package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskCatDataBindingMapper;
import com.hz.task.master.core.model.cat.CatDataBindingModel;
import com.hz.task.master.core.service.task.TaskCatDataBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:可爱猫回调店员绑定小微的Service层的实现层
 * @Author yoko
 * @Date 2020/6/17 10:04
 * @Version 1.0
 */
@Service
public class TaskCatDataBindingServiceImpl<T> extends BaseServiceImpl<T> implements TaskCatDataBindingService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskCatDataBindingMapper taskCatDataBindingMapper;

    public BaseDao<T> getDao() {
        return taskCatDataBindingMapper;
    }

    @Override
    public List<CatDataBindingModel> getCatDataBindingList(Object obj) {
        return taskCatDataBindingMapper.getCatDataBindingList(obj);
    }

    @Override
    public int updateCatDataBindingStatus(Object obj) {
        return taskCatDataBindingMapper.updateCatDataBindingStatus(obj);
    }
}
