package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskBankCollectionMapper;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;
import com.hz.task.master.core.service.task.TaskBankCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task：银行卡回传信息的Service的实现层
 * @Author yoko
 * @Date 2020/6/3 19:12
 * @Version 1.0
 */
@Service
public class TaskBankCollectionServiceImpl <T> extends BaseServiceImpl<T> implements TaskBankCollectionService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskBankCollectionMapper taskBankCollectionMapper;

    public BaseDao<T> getDao() {
        return taskBankCollectionMapper;
    }

    @Override
    public List<BankCollectionDataModel> getBankCollectionDataList(Object obj) {
        return taskBankCollectionMapper.getBankCollectionDataList(obj);
    }

    @Override
    public int updateBankCollectionDataStatus(Object obj) {
        return taskBankCollectionMapper.updateBankCollectionDataStatus(obj);
    }
}
