package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 用户扣减余额流水的Dao层
 * @Author yoko
 * @Date 2020/6/20 12:05
 * @Version 1.0
 */
@Mapper
public interface DidBalanceDeductMapper<T> extends BaseDao<T> {

    /**
     * @Description: 修改用户扣减余额流水的订单状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/2 19:16
    */
    public int updateOrderStatus(DidBalanceDeductModel model);
}
