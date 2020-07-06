package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.ClientAllDataMapper;
import com.hz.task.master.core.service.ClientAllDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 客户端监听数据回调原始数据的Service层的实现层
 * @Author yoko
 * @Date 2020/7/6 16:12
 * @Version 1.0
 */
@Service
public class ClientAllDataServiceImpl<T> extends BaseServiceImpl<T> implements ClientAllDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private ClientAllDataMapper clientAllDataMapper;

    public BaseDao<T> getDao() {
        return clientAllDataMapper;
    }
}
