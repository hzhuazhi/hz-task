package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.CatDataMapper;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.service.CatDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 可爱猫回调订单的Service层的实现层
 * @Author yoko
 * @Date 2020/5/26 11:03
 * @Version 1.0
 */
@Service
public class CatDataServiceImpl <T> extends BaseServiceImpl<T> implements CatDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private CatDataMapper catDataMapper;

    public BaseDao<T> getDao() {
        return catDataMapper;
    }

    @Override
    public int updateWxId(CatDataModel model) {
        return catDataMapper.updateWxId(model);
    }

    @Override
    public int updateCatData(CatDataModel model) {
        return catDataMapper.updateCatData(model);
    }
}
