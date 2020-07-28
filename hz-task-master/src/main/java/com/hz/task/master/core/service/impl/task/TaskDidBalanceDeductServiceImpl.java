package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskDidBalanceDeductMapper;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
import com.hz.task.master.core.service.task.TaskDidBalanceDeductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:用户扣减余额流水的Service层
 * @Author yoko
 * @Date 2020/7/2 20:12
 * @Version 1.0
 */
@Service
public class TaskDidBalanceDeductServiceImpl<T> extends BaseServiceImpl<T> implements TaskDidBalanceDeductService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskDidBalanceDeductMapper taskDidBalanceDeductMapper;

    public BaseDao<T> getDao() {
        return taskDidBalanceDeductMapper;
    }

    @Override
    public List<DidBalanceDeductModel> getDidBalanceDeductList(Object obj) {
        return taskDidBalanceDeductMapper.getDidBalanceDeductList(obj);
    }

    @Override
    public int updateDidBalanceDeductStatus(Object obj) {
        return taskDidBalanceDeductMapper.updateDidBalanceDeductStatus(obj);
    }

    @Override
    public List<Long> getBalanceDeductDidList(Object obj) {
        return taskDidBalanceDeductMapper.getBalanceDeductDidList(obj);
    }
}
