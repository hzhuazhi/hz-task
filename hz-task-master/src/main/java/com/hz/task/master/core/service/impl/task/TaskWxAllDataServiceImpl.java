package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskWxAllDataMapper;
import com.hz.task.master.core.model.wx.WxAllDataModel;
import com.hz.task.master.core.service.task.TaskWxAllDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:微信回调原始数据的Service层的实现层
 * @Author yoko
 * @Date 2020/8/11 19:08
 * @Version 1.0
 */
@Service
public class TaskWxAllDataServiceImpl<T> extends BaseServiceImpl<T> implements TaskWxAllDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskWxAllDataMapper taskWxAllDataMapper;


    public BaseDao<T> getDao() {
        return taskWxAllDataMapper;
    }

    @Override
    public List<WxAllDataModel> getWxAllDataList(Object obj) {
        return taskWxAllDataMapper.getWxAllDataList(obj);
    }

    @Override
    public int updateWxAllDataStatus(Object obj) {
        return taskWxAllDataMapper.updateWxAllDataStatus(obj);
    }
}
