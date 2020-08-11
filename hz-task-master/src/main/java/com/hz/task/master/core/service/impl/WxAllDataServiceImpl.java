package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.WxAllDataMapper;
import com.hz.task.master.core.service.WxAllDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 微信回调原始数据的Service层的实现层
 * @Author yoko
 * @Date 2020/8/11 17:53
 * @Version 1.0
 */
@Service
public class WxAllDataServiceImpl<T> extends BaseServiceImpl<T> implements WxAllDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private WxAllDataMapper wxAllDataMapper;

    public BaseDao<T> getDao() {
        return wxAllDataMapper;
    }
}
