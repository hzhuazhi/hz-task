package com.hz.task.master.core.model.cat;

import com.hz.task.master.core.protocol.page.BasePage;

import java.io.Serializable;

/**
 * @Description 可爱猫回调店员下线：小微旗下店员下线通知；取消与小微绑定关系的信息的实体属性Bean
 * @Author yoko
 * @Date 2020/6/11 11:32
 * @Version 1.0
 */
public class CatDataOfflineModel extends BasePage implements Serializable {
    private static final long   serialVersionUID = 1203223201109L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 原始数据的ID：对应表tb_fn_cat_all_data的主键ID
     */
    private Long allId;

    /**
     * 归属小微管理的主键ID：对应表tb_fn_wx的主键ID
     */
    private Long wxId;

    /**
     * 可爱猫的to_wxid
     */
    private String toWxid;

    /**
     * 微信名称
     */
    private String wxName;

    /**
     * 用户收款账号ID：对应表tb_fn_did_collection_account的主键ID；也就是具体上下线的账号
     */
    private Long collectionAccountId;

    /**
     * 订单号（归属派单订单号）：如果用户在派单过程中进行下线，则把归属的派单订单号补充进来（并把派单号修改成成功状态）
     */
    private String orderNo;

    /**
     * 订单金额：派单订单的金额
     */
    private String orderMoney;

    /**
     * 创建日期：存的日期格式20160530
     */
    private Integer curday;

    /**
     * 创建所属小时：24小时制
     */
    private Integer curhour;

    /**
     * 创建所属分钟：60分钟制
     */
    private Integer curminute;

    /**
     * 数据匹配的类型：1根据小微ID跟微信昵称不能匹配到收款账号，2根据小微ID跟微信昵称能匹配到收款账号
     */
    private Integer matchingType;

    /**
     * 数据类型：1初始化，2没有找到对应名称的收款账号（需要修改小微使用状态，修改成暂停，并且事后盘点数据时去找超时订单里面收款账号是否有对应的），3找到对应的账号并且有派单的，4找到对应收款账号并且没有派单的
     */
    private Integer dataType;

    /**
     * 备注
     */
    private String remark;

    /**
     *运行计算次数
     */
    private Integer runNum;

    /**
     * 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     */
    private Integer runStatus;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 是否有效：0有效，1无效/删除
     */
    private Integer yn;

    private Integer curdayStart;
    private Integer curdayEnd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAllId() {
        return allId;
    }

    public void setAllId(Long allId) {
        this.allId = allId;
    }

    public Long getWxId() {
        return wxId;
    }

    public void setWxId(Long wxId) {
        this.wxId = wxId;
    }

    public String getToWxid() {
        return toWxid;
    }

    public void setToWxid(String toWxid) {
        this.toWxid = toWxid;
    }

    public String getWxName() {
        return wxName;
    }

    public void setWxName(String wxName) {
        this.wxName = wxName;
    }

    public Long getCollectionAccountId() {
        return collectionAccountId;
    }

    public void setCollectionAccountId(Long collectionAccountId) {
        this.collectionAccountId = collectionAccountId;
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

    public Integer getMatchingType() {
        return matchingType;
    }

    public void setMatchingType(Integer matchingType) {
        this.matchingType = matchingType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getRunNum() {
        return runNum;
    }

    public void setRunNum(Integer runNum) {
        this.runNum = runNum;
    }

    public Integer getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(Integer runStatus) {
        this.runStatus = runStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
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
}
