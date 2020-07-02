package com.hz.task.master.core.service;


import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;

/**
 * @Description 用户扣减余额流水的Service层
 * @Author yoko
 * @Date 2020/6/20 12:06
 * @Version 1.0
 */
public interface DidBalanceDeductService<T> extends BaseService<T> {

    /**
     * @Description: 修改用户扣减余额流水的订单状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/2 19:16
     */
    public int updateOrderStatus(DidBalanceDeductModel model);
}
