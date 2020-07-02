package com.hz.task.master.core.model.task.base;

import java.io.Serializable;
import java.util.List;

/**
 * @Description task的抓取规则的实体Bean
 * @Author yoko
 * @Date 2019/12/6 21:01
 * @Version 1.0
 */
public class StatusModel implements Serializable {
    private static final long   serialVersionUID = 1233223301140L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 状态集合
     */
    private List<Integer> statusList;

    /**
     * 运行次数
     */
    private Integer runNum;

    /**
     * 运行计算状态：0初始化，1锁定，2计算失败，3计算成功
     */
    private Integer runStatus;

    /**
     * 发送次数
     */
    private Integer sendNum;

    /**
     * 发送状态：0初始化，1锁定，2计算失败，3计算成功
     */
    private Integer sendStatus;

    /**
     * 运行计算状态：0初始化，1锁定，2计算失败，3计算成功
     * 当做条件
     */
    private Integer runStatusWhere;

    /**
     * 运行次数
     */
    private Integer handleNum;

    /**
     * 运行计算状态：0初始化，1锁定，2计算失败，3计算成功
     */
    private Integer handleStatus;

    /**
     * 查询多少条数据
     */
    private Integer limitNum;

    /**
     * 日期
     */
    private Integer curday;
    private Integer curdayStart;
    private Integer curdayEnd;

    /**
     * 短信内容的类型：1广告短信，2挂失短信，3欠费短信，4普通短信，5手机变更
     */
    private Integer dataType;

    /**
     * 补充数据的类型：1初始化，2补充数据失败（未匹配到银行卡的数据），3补充数据成功
     */
    private Integer workType;

    /**
     * 订单状态：1初始化，2超时/失败，3成功
     */
    private Integer orderStatus;

    /**
     * 原因：task跑时，可能的一些失败原因的存储
     */
    private String info;

    /**
     * 失效时间
     */
    private String invalidTime;

    /**
     * 订单状态_用户操作的状态：1初始化，2失败，3超时后默认成功，4用户点击成功
     */
    private Integer didStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getLimitNum() {
        return limitNum;
    }

    public void setLimitNum(Integer limitNum) {
        this.limitNum = limitNum;
    }

    public Integer getCurday() {
        return curday;
    }

    public void setCurday(Integer curday) {
        this.curday = curday;
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

    public List<Integer> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<Integer> statusList) {
        this.statusList = statusList;
    }

    public Integer getRunStatusWhere() {
        return runStatusWhere;
    }

    public void setRunStatusWhere(Integer runStatusWhere) {
        this.runStatusWhere = runStatusWhere;
    }

    public Integer getHandleNum() {
        return handleNum;
    }

    public void setHandleNum(Integer handleNum) {
        this.handleNum = handleNum;
    }

    public Integer getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(Integer handleStatus) {
        this.handleStatus = handleStatus;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getWorkType() {
        return workType;
    }

    public void setWorkType(Integer workType) {
        this.workType = workType;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }


    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }


    public String getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(String invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Integer getSendNum() {
        return sendNum;
    }

    public void setSendNum(Integer sendNum) {
        this.sendNum = sendNum;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getDidStatus() {
        return didStatus;
    }

    public void setDidStatus(Integer didStatus) {
        this.didStatus = didStatus;
    }
}
