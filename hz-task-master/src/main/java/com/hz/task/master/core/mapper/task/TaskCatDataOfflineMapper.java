package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:可爱猫回调店员下线的Dao层
 * @Author yoko
 * @Date 2020/6/11 18:08
 * @Version 1.0
 */
@Mapper
public interface TaskCatDataOfflineMapper<T> extends BaseDao<T> {

    /**
     * @Description: 获取未跑的可爱猫回调店员下线数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
     */
    public List<CatDataOfflineModel> getCatDataOfflineList(Object obj);


    /**
     * @Description: 更新可爱猫回调店员下线数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateCatDataOfflineStatus(Object obj);

    /**
     * @Description: 修改可爱猫回调店员下线的订单以及订单金额
     * <p>
     *     当小微下线，查询在下线之前有派发订单，则把派发订单集合的ID以英文逗号隔开修改赋值到order_no的字段；
     *     把订单集合的总金额修改赋值到order_money的字段里面
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/12 22:39
     */
    public int updateCatDataOfflineOrderInfo(CatDataOfflineModel model);

}
