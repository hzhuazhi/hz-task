package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskDidMapper;
import com.hz.task.master.core.service.task.TaskDidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description task:用户的Service层的实现层
 * @Author yoko
 * @Date 2020/8/3 15:13
 * @Version 1.0
 */
@Service
public class TaskDidServiceImpl<T> extends BaseServiceImpl<T> implements TaskDidService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskDidMapper taskDidMapper;


    public BaseDao<T> getDao() {
        return taskDidMapper;
    }
}
