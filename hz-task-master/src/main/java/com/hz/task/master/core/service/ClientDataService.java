package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.client.ClientDataModel;

/**
 * @Description 客户端监听数据回调订单的
 * @Author yoko
 * @Date 2020/7/6 16:52
 * @Version 1.0
 */
public interface ClientDataService<T> extends BaseService<T> {

    /**
     * @Description: 修改did的值
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/6 20:03
     */
    public int updateDid(ClientDataModel model);

    /**
     * @Description: 客户端监听数据回调订单的数据修改
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/7 9:06
     */
    public int updateClientData(ClientDataModel model);
}
