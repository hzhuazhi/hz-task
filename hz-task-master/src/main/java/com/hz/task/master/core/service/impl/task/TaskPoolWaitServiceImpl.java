package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskPoolWaitMapper;
import com.hz.task.master.core.service.task.TaskPoolWaitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:接单池子等待接单的Service层的实现层
 * @Author yoko
 * @Date 2020/8/14 14:56
 * @Version 1.0
 */
@Service
public class TaskPoolWaitServiceImpl<T> extends BaseServiceImpl<T> implements TaskPoolWaitService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskPoolWaitMapper taskPoolWaitMapper;

    public BaseDao<T> getDao() {
        return taskPoolWaitMapper;
    }

    @Override
    public List<Long> getPoolWaitDidList(Object obj) {
        return taskPoolWaitMapper.getPoolWaitDidList(obj);
    }

    @Override
    public int updatePoolWaitYn(Object obj) {
        return taskPoolWaitMapper.updatePoolWaitYn(obj);
    }
}
