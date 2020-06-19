package com.hz.task.master.core.model.task.did;

import com.hz.task.master.core.protocol.page.BasePage;

import java.io.Serializable;

/**
 * @Description task：检测用户收款账号给出码以及成功的信息的实体属性Bean
 * @Author yoko
 * @Date 2020/6/19 10:30
 * @Version 1.0
 */
public class TaskDidCollectionAccountDataModel extends BasePage implements Serializable {
    private static final long   serialVersionUID = 1233223401145L;

    /**
     * 查询数据的条数：SQL:limit
     */
    private Integer dataLimitNum;

    /**
     * 查询订单状态的另外一个查询条件
     */
    private Integer whereOrderStatus;

    /**
     * 收款账号给出码的次数
     */
    private Integer limitNum;

    /**
     * 收款账号给出码，订单成功的次数
     */
    private Integer isLimitNum;

    /**
     * 收款账号成功收款的金额
     */
    private String money;

    /**
     * 用户收款账号ID：对应表tb_fn_did_collection_account的主键ID；也就是具体上下线的账号
     */
    private Long collectionAccountId;

    /**
     *订单状态：1初始化，2超时/失败，3有质疑，4成功
     */
    private Integer orderStatus;

    /**
     * 日期
     */
    private Integer curday;

    /**
     *运行计算次数
     */
    private Integer runNum;

    /**
     * 是否有效：0有效，1无效/删除
     */
    private Integer yn;

    private Integer curdayStart;
    private Integer curdayEnd;

    public Integer getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(Integer limitNum) {
        this.limitNum = limitNum;
    }

    public Integer getIsLimitNum() {
        return isLimitNum;
    }

    public void setIsLimitNum(Integer isLimitNum) {
        this.isLimitNum = isLimitNum;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public Long getCollectionAccountId() {
        return collectionAccountId;
    }

    public void setCollectionAccountId(Long collectionAccountId) {
        this.collectionAccountId = collectionAccountId;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getCurday() {
        return curday;
    }

    public void setCurday(Integer curday) {
        this.curday = curday;
    }

    public Integer getRunNum() {
        return runNum;
    }

    public void setRunNum(Integer runNum) {
        this.runNum = runNum;
    }

    public Integer getYn() {
        return yn;
    }

    public void setYn(Integer yn) {
        this.yn = yn;
    }

    public Integer getCurdayStart() {
        return curdayStart;
    }

    public void setCurdayStart(Integer curdayStart) {
        this.curdayStart = curdayStart;
    }

    public Integer getCurdayEnd() {
        return curdayEnd;
    }

    public void setCurdayEnd(Integer curdayEnd) {
        this.curdayEnd = curdayEnd;
    }

    public Integer getDataLimitNum() {
        return dataLimitNum;
    }

    public void setDataLimitNum(Integer dataLimitNum) {
        this.dataLimitNum = dataLimitNum;
    }

    public Integer getWhereOrderStatus() {
        return whereOrderStatus;
    }

    public void setWhereOrderStatus(Integer whereOrderStatus) {
        this.whereOrderStatus = whereOrderStatus;
    }
}
