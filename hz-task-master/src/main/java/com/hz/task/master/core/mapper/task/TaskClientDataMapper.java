package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.client.ClientDataModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:客户端监听数据回调订单的Dao层
 * @Author yoko
 * @Date 2020/7/6 18:43
 * @Version 1.0
 */
@Mapper
public interface TaskClientDataMapper<T> extends BaseDao<T> {

    /**
     * @Description: 获取客户端监听数据回调订单的数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
     */
    public List<ClientDataModel> getClientDataList(Object obj);


    /**
     * @Description: 更新客户端监听数据回调订单数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateClientDataStatus(Object obj);
}
