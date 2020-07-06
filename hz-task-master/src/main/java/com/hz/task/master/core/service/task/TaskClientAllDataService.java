package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.client.ClientAllDataModel;

import java.util.List;

/**
 * @Description task:客户端监听数据回调原始数据的Service层
 * @Author yoko
 * @Date 2020/7/6 18:48
 * @Version 1.0
 */
public interface TaskClientAllDataService<T> extends BaseService<T>{

    /**
     * @Description: 查询未解析的客户端监听数据回调原始数据
     * @param obj - run_status
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<ClientAllDataModel> getClientAllDataList(Object obj);

    /**
     * @Description: 更新客户端监听数据回调原始数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateClientAllDataStatus(Object obj);
}
