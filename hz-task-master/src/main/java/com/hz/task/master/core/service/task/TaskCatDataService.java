package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.order.OrderModel;

import java.util.List;

/**
 * @Description task:可爱猫回调订单的Service层
 * @Author yoko
 * @Date 2020/6/6 17:47
 * @Version 1.0
 */
public interface TaskCatDataService<T> extends BaseService<T> {

    /**
     * @Description: 获取可爱猫回调订单的数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
     */
    public List<CatDataModel> getCatDataList(Object obj);


    /**
     * @Description: 更新可爱猫回调订单数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateCatDataStatus(Object obj);
    

    /**
     * @Description: 可爱猫回调订单正常匹配到派单信息的处理方法
     * <p>
     *     加上事物；
     *     1.更新可爱猫匹配成功的状态，并且填充匹配的订单号
     *     2.更新派单的订单状态，更新成成功状态
     * </p>
     * @param catDataModel - 可爱猫回调订单数据
     * @param orderModel - 派单信息
     * @return
     * @author yoko
     * @date 2020/6/7 9:49
    */
    public boolean catDataMatchingOrderSuccess(CatDataModel catDataModel, OrderModel orderModel) throws Exception;


}
