package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidModel;

import java.util.List;

/**
 * @Description 用户的Service层
 * @Author yoko
 * @Date 2020/5/13 18:34
 * @Version 1.0
 */
public interface DidService<T> extends BaseService<T> {

    /**
     * @Description: 查询出可派单的用户集合
     * @param model
     * @return
     * @author yoko
     * @date 2020/5/25 10:42
    */
    public List<DidModel> getEffectiveDidList(DidModel model);

    /**
     * @Description: 用户充值成功后更新用户的信息
     * <p>
     *     用户充值订单成功后更新的信息有：total_money（总金额/累计充值）+,balance（余额）+，vip_type（修改成充值会员）
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/5 11:47
     */
    public int updateDidMoneyByRecharge(DidModel model);


    /**
     * @Description: 用户奖励数据计算完毕之后更新用户的相关金额
     * @param model - 用户奖励信息
     * @return
     * @author yoko
     * @date 2020/6/6 14:26
     */
    public int updateDidMoneyByReward(DidModel model);


    /**
     * @Description: 更新用户的余额以及冻结金额
     * <p>
     *     派发超时订单，把冻结订单进行解冻
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/8 11:57
     */
    public int updateDidMoneyByInvalid(DidModel model);

    /**
     * @Description: 更新用户的冻结金额
     * <p>
     *     派发订单成功后，把冻结金额与对应的订单金额进行扣减
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/8 14:21
    */
    public int updateDidMoneyBySuccess(DidModel model);

    /**
     * @Description: 更新用户的余额
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/3 13:44
     */
    public int updateDidBalance(DidModel model);

    /**
     * @Description: 获取团队长用户Id集合
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/7 22:04
    */
    public List<Long> getIsTeamDidList(DidModel model);

    /**
     * @Description: 扣减用户余额
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/27 20:17
    */
    public int updateDidDeductBalance(DidModel model);

    /**
     * @Description: 修改用户的锁定金额
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/28 15:42
     */
    public int updateDidLockMoney(DidModel model);

}
