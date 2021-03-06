package com.hz.task.master.core.protocol.response.order;

import java.io.Serializable;

/**
 * @Description协议：派单信息
 * @Author yoko
 * @Date 2020/5/29 17:16
 * @Version 1.0
 */
public class Order implements Serializable {
    private static final long   serialVersionUID = 1233023331049L;

    /**
     * 收款账户名称：用户备注使用
     */
    public String acName;

    /**
     * 订单号
     */
    public String orderNo;

    /**
     * 订单金额
     */
    public String orderMoney;

    /**
     * 收款账号类型：1微信，2支付宝，3银行卡
     */
    public Integer collectionType;

    /**
     * 订单状态：1初始化，2超时/失败，3有质疑，4成功
     */
    public Integer orderStatus;

    /**
     * 创建时间
     */
    public String createTime;

    /**
     * 创建日期-Int
     */
    public Integer curday;

    /**
     * 创建小时
     */
    public Integer curhour;

    /**
     * 创建分钟
     */
    public Integer curminute;

    public Order(){

    }

    public String getAcName() {
        return acName;
    }

    public void setAcName(String acName) {
        this.acName = acName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(String orderMoney) {
        this.orderMoney = orderMoney;
    }

    public Integer getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(Integer collectionType) {
        this.collectionType = collectionType;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getCurday() {
        return curday;
    }

    public void setCurday(Integer curday) {
        this.curday = curday;
    }

    public Integer getCurhour() {
        return curhour;
    }

    public void setCurhour(Integer curhour) {
        this.curhour = curhour;
    }

    public Integer getCurminute() {
        return curminute;
    }

    public void setCurminute(Integer curminute) {
        this.curminute = curminute;
    }
}
