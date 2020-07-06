package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.ClientDataMapper;
import com.hz.task.master.core.model.client.ClientDataModel;
import com.hz.task.master.core.service.ClientDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 客户端监听数据回调订单的Service层的实现层
 * @Author yoko
 * @Date 2020/7/6 17:35
 * @Version 1.0
 */
@Service
public class ClientDataServiceImpl <T> extends BaseServiceImpl<T> implements ClientDataService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private ClientDataMapper clientDataMapper;

    public BaseDao<T> getDao() {
        return clientDataMapper;
    }

    @Override
    public int updateDid(ClientDataModel model) {
        return clientDataMapper.updateDid(model);
    }

    @Override
    public int updateClientData(ClientDataModel model) {
        return clientDataMapper.updateClientData(model);
    }
}
