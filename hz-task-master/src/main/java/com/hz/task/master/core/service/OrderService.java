package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.protocol.response.order.Order;

import java.util.List;

/**
 * @Description 任务订单的Service层
 * @Author yoko
 * @Date 2020/5/21 19:34
 * @Version 1.0
 */
public interface OrderService<T> extends BaseService<T> {

    /**
     * @Description: 筛选出要派单的收款账号
     * @param didList - 可以正常使用的did账号集合
     * @param orderMoney - 订单金额
     * @param payType - 支付类型：1微信，2支付宝，3银行卡
     * @return
     * @author yoko
     * @date 2020/5/26 15:58
    */
    public DidCollectionAccountModel screenCollectionAccount(List<DidModel> didList, String orderMoney, int payType);


    /**
     * @Description: 派发订单成功的金额
     * <p>sum查询出来的数据</p>
     * @param model - 根据用户，订单状态，日期查询总派发订单成功金额
     * @return
     * @author yoko
     * @date 2020/5/29 11:23
     */
    public String getProfitByCurday(OrderModel model);

    /**
     * @Description: 获取初始化的订单数据（未超时）
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/6 21:42
    */
    public List<OrderModel> getInitOrderList(OrderModel model);

    /**
     * @Description: 获取初始化的订单数据（未超时）- 支付宝
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/6 21:42
     */
    public List<OrderModel> getInitOrderByZfbList(OrderModel model);

    /**
     * @Description: 更新派单的订单状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/7 9:38
    */
    public int updateOrderStatus(OrderModel model);

    /**
     * @Description: 已经失效超时的订单更新成失效状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/7 12:22
    */
    public int updateOrderStatusByInvalidTime(OrderModel model);


    /**
     * @Description: 根据创建时间获取派单集合数据
     * <p>
     *     创建时间开始时间：系统时间的前10分钟
     *     创建时间结束时间：系统当前时间
     *     订单状态：初始化状态跟超时状态
     *     收款账号ID
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/12 21:43
    */
    public List<OrderModel> getOrderByCreateTime(OrderModel model);


    /**
     * @Description: 根据主键集合修改订单状态
     * @param model - 派单ID的集合
     * @return
     * @author yoko
     * @date 2020/6/12 22:02
    */
    public int updateOrderStatusByIdList(OrderModel model);


    /**
     * @Description: 获取直推用户昨天派单消耗成功的总金额
     * @param model - 用户did集合，日期，订单成功
     * @return
     * @author yoko
     * @date 2020/6/6 11:22
     */
    public String directSumMoney(OrderModel model);


    /**
     * @Description: 获取直推用户所有派单消耗成功的总金额
     * @param model - 用户did集合，订单成功
     * @return
     * @author yoko
     * @date 2020/6/6 11:22
     */
    public String directAllSumMoney(OrderModel model);

    /**
     * @Description: 根据用户ID加收款账号ID获取最新的一条订单数据
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/23 14:41
     */
    public OrderModel getNewestOrder(OrderModel model);


    /**
     * @Description: 更新用户操作状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/23 15:44
     */
    public int updateDidStatus(OrderModel model);

    /**
     * @Description: 根据用户ID查询目前时间到前30分钟的订单数据
     * @param model - 查询条件
     * @return
     * @author yoko
     * @date 2020/7/23 17:41
     */
    public OrderModel getOrderByDidAndTime(OrderModel model);

    /**
     * @Description: 根据订单号更新订单的状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/26 16:46
    */
    public int updateOrderStatusByOrderNo(OrderModel model);

    /**
     * @Description: 更新用户发送红包以及回复的相关信息
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/23 15:44
     */
    public int updateRedPackAndReply(OrderModel model);

    /**
     * @Description: 根据条件查询订单信息
     * <p>
     *     查询已发红包，但是没有回复的订单
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/11 15:49
     */
    public OrderModel getOrderByNotIsReply(OrderModel model);

    /**
     * @Description: 更新订单状态以及订单的备注
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/18 14:53
    */
    public int updateOrderStatusAndRemark(OrderModel model);

    /**
     * @Description: 修改订单的回复状态以及备注
     * <p>
     *     根据用户ID，订单状态修改回复状态以及备注
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/22 21:58
     */
    public int updateIsReplyAndRemark(OrderModel model);

}
