package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskPoolOpenMapper;
import com.hz.task.master.core.service.task.TaskPoolOpenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:接单池子正在接单的Service层的实现层
 * @Author yoko
 * @Date 2020/8/14 14:56
 * @Version 1.0
 */
@Service
public class TaskPoolOpenServiceImpl<T> extends BaseServiceImpl<T> implements TaskPoolOpenService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskPoolOpenMapper taskPoolOpenMapper;

    public BaseDao<T> getDao() {
        return taskPoolOpenMapper;
    }

    @Override
    public List<Long> getPoolOpenDidList(Object obj) {
        return taskPoolOpenMapper.getPoolOpenDidList(obj);
    }

    @Override
    public int updatePoolOpenYn(Object obj) {
        return taskPoolOpenMapper.updatePoolOpenYn(obj);
    }
}
