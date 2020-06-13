package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.CatDataOfflineMapper;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.service.CatDataOfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 可爱猫回调店员下线：小微旗下店员下线通知；取消与小微绑定关系的信息的Service层的实现层
 * @Author yoko
 * @Date 2020/6/11 14:09
 * @Version 1.0
 */
@Service
public class CatDataOfflineServiceImpl<T> extends BaseServiceImpl<T> implements CatDataOfflineService<T> {

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private CatDataOfflineMapper catDataOfflineMapper;

    public BaseDao<T> getDao() {
        return catDataOfflineMapper;
    }

    @Override
    public int updateCatDataOffline(CatDataOfflineModel model) {
        return catDataOfflineMapper.updateCatDataOffline(model);
    }
}
