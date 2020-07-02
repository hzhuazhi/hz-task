package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.DidBalanceDeductMapper;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
import com.hz.task.master.core.service.DidBalanceDeductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 用户扣减余额流水的Service层的实现层
 * @Author yoko
 * @Date 2020/6/20 12:06
 * @Version 1.0
 */
@Service
public class DidBalanceDeductServiceImpl<T> extends BaseServiceImpl<T> implements DidBalanceDeductService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private DidBalanceDeductMapper didBalanceDeductMapper;

    public BaseDao<T> getDao() {
        return didBalanceDeductMapper;
    }

    @Override
    public int updateOrderStatus(DidBalanceDeductModel model) {
        return didBalanceDeductMapper.updateOrderStatus(model);
    }
}
