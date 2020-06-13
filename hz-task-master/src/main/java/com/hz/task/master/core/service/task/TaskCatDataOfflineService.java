package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.order.OrderModel;

import java.util.List;

/**
 * @Description task:可爱猫回调店员下线的Service层
 * @Author yoko
 * @Date 2020/6/11 18:10
 * @Version 1.0
 */
public interface TaskCatDataOfflineService<T> extends BaseService<T> {

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


    /**
     * @Description: 处理小微下线的方法
     * <p>
     *     此方法缘由：匹配到下线的收款账号，并且在下线之前有派单（派单状态：初始化，以及超时）。
     *     具体逻辑：1.把收款账号进行下线。
     *     2.派单的状态强制修改成成功状态，并且修改订单备注：小微下线，强制成功。
     *     3.修改可爱猫的订单信息（订单集合ID赋值，订单金额赋值）。
     *
     * </p>
     * @param didCollectionAccountModel - 用户收款账号信息
     * @param orderModel - 订单信息
     * @param catDataOfflineModel - 可爱猫下线信息
     * @return
     * @author yoko
     * @date 2020/6/12 22:49
    */
    public boolean handleCatDataOfferline(DidCollectionAccountModel didCollectionAccountModel, OrderModel orderModel, CatDataOfflineModel catDataOfflineModel) throws Exception;


}
