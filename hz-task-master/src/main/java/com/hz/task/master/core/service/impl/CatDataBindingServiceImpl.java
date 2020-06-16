package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.CatDataBindingMapper;
import com.hz.task.master.core.service.CatDataBindingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 可爱猫回调店员绑定小微的Service层的实现层
 * @Author yoko
 * @Date 2020/6/16 21:59
 * @Version 1.0
 */
@Service
public class CatDataBindingServiceImpl<T> extends BaseServiceImpl<T> implements CatDataBindingService<T> {

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private CatDataBindingMapper catDataBindingMapper;

    public BaseDao<T> getDao() {
        return catDataBindingMapper;
    }
}
