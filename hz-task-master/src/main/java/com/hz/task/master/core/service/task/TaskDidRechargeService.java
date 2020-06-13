package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.did.DidRechargeModel;
import com.hz.task.master.core.model.did.DidRewardModel;

import java.util.List;

/**
 * @Description task:用户充值订单的Service层
 * @Author yoko
 * @Date 2020/6/4 17:26
 * @Version 1.0
 */
public interface TaskDidRechargeService<T> extends BaseService<T> {

    /**
     * @Description: 查询用户充值订单信息
     * @param obj - 订单状态
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<DidRechargeModel> getDidRechargeList(Object obj);

    /**
     * @Description: 更新用户充值订单的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateDidRechargeStatus(Object obj);


    /**
     * @Description: 用户充值成功的业务处理
     * <p>
     *     1.添加奖励：充多少送多少、档次达到多少的奖励
     *     2.更新用户账号金额
     * </p>
     * @param didRechargeProfit - 奖励：用户充多少送多少
     * @param didGradeProfit - 奖励：用户达到的档次奖励
     * @param upDidMoney - 更新用户账户金额
     * @return
     * @author yoko
     * @date 2020/6/4 19:28
    */
    public boolean didRechargeSuccessOrder(DidRewardModel didRechargeProfit, DidRewardModel didGradeProfit, DidModel upDidMoney) throws Exception;


    /**
     * @Description: 获取昨天充值成功的用户集合数据
     * @param model - 查询条件
     * @return
     * @author yoko
     * @date 2020/6/5 18:45
    */
    public List<Long> getRechargeDidList(DidRechargeModel model);
}
