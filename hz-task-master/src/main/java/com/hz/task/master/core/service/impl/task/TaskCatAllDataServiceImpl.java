package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskCatAllDataMapper;
import com.hz.task.master.core.model.cat.CatAllDataModel;
import com.hz.task.master.core.service.task.TaskCatAllDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task：可爱猫回调原始数据的Service的实现层
 * @Author yoko
 * @Date 2020/6/6 15:33
 * @Version 1.0
 */
@Service
public class TaskCatAllDataServiceImpl<T> extends BaseServiceImpl<T> implements TaskCatAllDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskCatAllDataMapper taskCatAllDataMapper;


    public BaseDao<T> getDao() {
        return taskCatAllDataMapper;
    }

    @Override
    public List<CatAllDataModel> getCatAllDataList(Object obj) {
        return taskCatAllDataMapper.getCatAllDataList(obj);
    }

    @Override
    public int updateCatAllDataStatus(Object obj) {
        return taskCatAllDataMapper.updateCatAllDataStatus(obj);
    }
}
