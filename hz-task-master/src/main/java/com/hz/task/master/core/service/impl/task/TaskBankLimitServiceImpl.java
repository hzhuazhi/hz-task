package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskBankLimitMapper;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.service.task.TaskBankLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:银行卡流水限制的Service的实现层
 * @Author yoko
 * @Date 2020/6/13 19:14
 * @Version 1.0
 */
@Service
public class TaskBankLimitServiceImpl<T> extends BaseServiceImpl<T> implements TaskBankLimitService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskBankLimitMapper taskBankLimitMapper;



    public BaseDao<T> getDao() {
        return taskBankLimitMapper;
    }

    @Override
    public List<BankModel> getBankDataList(Object obj) {
        return taskBankLimitMapper.getBankDataList(obj);
    }

    @Override
    public int updateBankSwitch(Object obj) {
        return taskBankLimitMapper.updateBankSwitch(obj);
    }
}
