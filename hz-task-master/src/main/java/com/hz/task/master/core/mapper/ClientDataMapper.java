package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.client.ClientDataModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 客户端监听数据回调订单的Dao层
 * @Author yoko
 * @Date 2020/7/6 16:51
 * @Version 1.0
 */
@Mapper
public interface ClientDataMapper<T> extends BaseDao<T> {

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
