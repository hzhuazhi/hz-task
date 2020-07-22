package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.DidCollectionAccountMapper;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.service.DidCollectionAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 用户的收款账号的Service层的实现层
 * @Author yoko
 * @Date 2020/5/15 14:02
 * @Version 1.0
 */
@Service
public class DidCollectionAccountServiceImpl<T> extends BaseServiceImpl<T> implements DidCollectionAccountService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private DidCollectionAccountMapper didCollectionAccountMapper;

    public BaseDao<T> getDao() {
        return didCollectionAccountMapper;
    }

    @Override
    public void updateBasic(DidCollectionAccountModel model) {
        didCollectionAccountMapper.updateBasic(model);
    }

    @Override
    public void updateDidCollectionAccount(DidCollectionAccountModel model) {
        didCollectionAccountMapper.updateDidCollectionAccount(model);
    }

    @Override
    public int updateDidCollectionAccountTotalSwitch(DidCollectionAccountModel model) {
        return didCollectionAccountMapper.updateDidCollectionAccountTotalSwitch(model);
    }

    @Override
    public int updateDidCollectionAccountCheckData(DidCollectionAccountModel model) {
        return didCollectionAccountMapper.updateDidCollectionAccountCheckData(model);
    }

    @Override
    public int updateDidCollectionAccountCheckDataByFail(DidCollectionAccountModel model) {
        return didCollectionAccountMapper.updateDidCollectionAccountCheckDataByFail(model);
    }

    @Override
    public DidCollectionAccountModel getDidCollectionAccountByWxIdAndWxName(DidCollectionAccountModel model) {
        return didCollectionAccountMapper.getDidCollectionAccountByWxIdAndWxName(model);
    }

    @Override
    public int updateDidCollectionAccountByWxData(DidCollectionAccountModel model) {
        return didCollectionAccountMapper.updateDidCollectionAccountByWxData(model);
    }
}
