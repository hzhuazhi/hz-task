package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.DidMapper;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.service.DidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 用户Service层的实现层
 * @Author yoko
 * @Date 2020/5/13 18:34
 * @Version 1.0
 */
@Service
public class DidServiceImpl<T> extends BaseServiceImpl<T> implements DidService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private DidMapper didMapper;

    public BaseDao<T> getDao() {
        return didMapper;
    }

    @Override
    public List<DidModel> getEffectiveDidList(DidModel model) {
        return didMapper.getEffectiveDidList(model);
    }

    @Override
    public int updateDidMoneyByRecharge(DidModel model) {
        return didMapper.updateDidMoneyByRecharge(model);
    }

    @Override
    public int updateDidMoneyByReward(DidModel model) {
        return didMapper.updateDidMoneyByReward(model);
    }

    @Override
    public int updateDidMoneyByInvalid(DidModel model) {
        return didMapper.updateDidMoneyByInvalid(model);
    }

    @Override
    public int updateDidMoneyBySuccess(DidModel model) {
        return didMapper.updateDidMoneyBySuccess(model);
    }

    @Override
    public int updateDidBalance(DidModel model) {
        return didMapper.updateDidBalance(model);
    }

    @Override
    public List<Long> getIsTeamDidList(DidModel model) {
        return didMapper.getIsTeamDidList(model);
    }

    @Override
    public int updateDidDeductBalance(DidModel model) {
        return didMapper.updateDidDeductBalance(model);
    }

    @Override
    public int updateDidLockMoney(DidModel model) {
        return didMapper.updateDidLockMoney(model);
    }
}
