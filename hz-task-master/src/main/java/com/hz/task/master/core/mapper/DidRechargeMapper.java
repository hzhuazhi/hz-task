package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.did.DidRechargeModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 用户充值记录的Dao层
 * @Author yoko
 * @Date 2020/5/19 14:47
 * @Version 1.0
 */
@Mapper
public interface DidRechargeMapper<T> extends BaseDao<T> {

    /**
     * @Description: 直推的总金额
     * @param model - 用户did集合，日期，订单成功
     * @return
     * @author yoko
     * @date 2020/6/6 11:22
     */
    public String directSumMoney(DidRechargeModel model);
}
