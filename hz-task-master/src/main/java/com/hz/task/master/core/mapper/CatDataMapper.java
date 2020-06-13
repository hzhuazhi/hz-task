package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.order.OrderModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 可爱猫回调订单的Dao层
 * @Author yoko
 * @Date 2020/5/26 10:49
 * @Version 1.0
 */
@Mapper
public interface CatDataMapper<T> extends BaseDao<T> {

    /**
     * @Description: 修改wx_id的值
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/6 20:03
     */
    public int updateWxId(CatDataModel model);

    /**
     * @Description: 可爱猫回调订单的数据修改
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/7 9:06
     */
    public int updateCatData(CatDataModel model);

}
