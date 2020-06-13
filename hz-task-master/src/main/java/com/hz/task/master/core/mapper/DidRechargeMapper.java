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


    /**
     * @Description: 根据查询条件获取银行卡的充值金额
     * 根据日期：日，月，总；银行卡ID查询充值订单状态1，订单状态3查询充值订单的总金额
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/13 20:30
    */
    public String getRechargeMoney(DidRechargeModel model);
}
