package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskClientAllDataMapper;
import com.hz.task.master.core.model.client.ClientAllDataModel;
import com.hz.task.master.core.service.task.TaskClientAllDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:客户端监听数据回调原始数据的Service层的实现层
 * @Author yoko
 * @Date 2020/7/6 18:51
 * @Version 1.0
 */
@Service
public class TaskClientAllDataServiceImpl<T> extends BaseServiceImpl<T> implements TaskClientAllDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskClientAllDataMapper taskClientAllDataMapper;


    public BaseDao<T> getDao() {
        return taskClientAllDataMapper;
    }

    @Override
    public List<ClientAllDataModel> getClientAllDataList(Object obj) {
        return taskClientAllDataMapper.getClientAllDataList(obj);
    }

    @Override
    public int updateClientAllDataStatus(Object obj) {
        return taskClientAllDataMapper.updateClientAllDataStatus(obj);
    }
}
