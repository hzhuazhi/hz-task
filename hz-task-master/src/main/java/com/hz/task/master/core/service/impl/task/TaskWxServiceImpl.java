package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskWxMapper;
import com.hz.task.master.core.model.wx.WxModel;
import com.hz.task.master.core.service.task.TaskWxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task：小微的Service层的实现层
 * @Author yoko
 * @Date 2020/8/2 9:16
 * @Version 1.0
 */
@Service
public class TaskWxServiceImpl<T> extends BaseServiceImpl<T> implements TaskWxService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskWxMapper taskWxMapper;


    public BaseDao<T> getDao() {
        return taskWxMapper;
    }

    @Override
    public List<WxModel> getWxList(Object obj) {
        return taskWxMapper.getWxList(obj);
    }

    @Override
    public int updateWxStatus(Object obj) {
        return taskWxMapper.updateWxStatus(obj);
    }
}
