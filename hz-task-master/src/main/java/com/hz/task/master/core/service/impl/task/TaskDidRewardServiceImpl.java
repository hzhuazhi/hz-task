package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskDidRewardMapper;
import com.hz.task.master.core.model.did.DidRewardModel;
import com.hz.task.master.core.service.task.TaskDidRewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task：用户奖励数据的Service的实现层
 * @Author yoko
 * @Date 2020/6/6 14:00
 * @Version 1.0
 */
@Service
public class TaskDidRewardServiceImpl<T> extends BaseServiceImpl<T> implements TaskDidRewardService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskDidRewardMapper taskDidRewardMapper;

    public BaseDao<T> getDao() {
        return taskDidRewardMapper;
    }

    @Override
    public List<DidRewardModel> getDidRewardList(Object obj) {
        return taskDidRewardMapper.getDidRewardList(obj);
    }

    @Override
    public int updateDidRewardStatus(Object obj) {
        return taskDidRewardMapper.updateDidRewardStatus(obj);
    }
}
