package com.hz.task.master.core.model.order;

import com.hz.task.master.core.protocol.page.BasePage;

import java.io.Serializable;
import java.util.List;

/**
 * @Description 订单步骤详情的实体属性Bean
 * @Author yoko
 * @Date 2020/7/25 15:46
 * @Version 1.0
 */
public class OrderStepModel extends BasePage implements Serializable {
    private static final long   serialVersionUID = 1203223201821L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 奖励归属用户ID：对应表tb_fn_did的主键ID；奖励给哪个用户
     */
    private Long did;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单金额
     */
    private String orderMoney;

    /**
     * 用户收款账号ID：对应表tb_fn_did_collection_account的主键ID
     */
    private Long collectionAccountId;

    /**
     * 收款账号是否正常：1正常，2异常（修改名称，被删除）
     */
    private Integer isOkCollectionAccount;

    /**
     * 订单状态：1初始化，2超时/失败，3有质疑，4成功
     */
    private Integer orderStatus;

    /**
     * 用户成功收款上报的金额
     */
    private String money;

    /**
     * 失效时间
     */
    private String invalidTime;

    /**
     * 支付用户发红包是否超时：1初始化，2已超时，3未超时
     */
    private Integer redPackInvalidType;

    /**
     * 金额是否与上报金额一致：1初始化，2少了，3多了，4一致
     */
    private Integer moneyFitType;

    /**
     * 回复成功or失败是否超时：1初始化，2已超时，3未超时
     */
    private Integer replyInvalidType;

    private String replyTime;

    /**
     * 剔除成员类型：1初始化，2需要剔除成员，3已剔除支付用户成员
     */
    private Integer eliminateType;

    /**
     * 发红包的时间
     */
    private String redPackTime;

    /**
     * 操作了几步：目前操作了几步
     */
    private Integer stepNum;

    /**
     * 总共需要操作几步：总共要操作几步
     */
    private Integer totalStepNum;

    /**
     * 订单是否处于完结状态：1未完结，2完结
     */
    private Integer endStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 补充数据的类型：1初始化，2补充数据失败（其它原因等..），3补充数据成功
     */
    private Integer workType;

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

    private String invalidTimeBig;
    private String invalidTimeSmall;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDid() {
        return did;
    }

    public void setDid(Long did) {
        this.did = did;
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

    public Long getCollectionAccountId() {
        return collectionAccountId;
    }

    public void setCollectionAccountId(Long collectionAccountId) {
        this.collectionAccountId = collectionAccountId;
    }

    public Integer getIsOkCollectionAccount() {
        return isOkCollectionAccount;
    }

    public void setIsOkCollectionAccount(Integer isOkCollectionAccount) {
        this.isOkCollectionAccount = isOkCollectionAccount;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(String invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Integer getRedPackInvalidType() {
        return redPackInvalidType;
    }

    public void setRedPackInvalidType(Integer redPackInvalidType) {
        this.redPackInvalidType = redPackInvalidType;
    }

    public Integer getMoneyFitType() {
        return moneyFitType;
    }

    public void setMoneyFitType(Integer moneyFitType) {
        this.moneyFitType = moneyFitType;
    }

    public Integer getReplyInvalidType() {
        return replyInvalidType;
    }

    public void setReplyInvalidType(Integer replyInvalidType) {
        this.replyInvalidType = replyInvalidType;
    }

    public String getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(String replyTime) {
        this.replyTime = replyTime;
    }

    public Integer getEliminateType() {
        return eliminateType;
    }

    public void setEliminateType(Integer eliminateType) {
        this.eliminateType = eliminateType;
    }

    public String getRedPackTime() {
        return redPackTime;
    }

    public void setRedPackTime(String redPackTime) {
        this.redPackTime = redPackTime;
    }

    public Integer getStepNum() {
        return stepNum;
    }

    public void setStepNum(Integer stepNum) {
        this.stepNum = stepNum;
    }

    public Integer getTotalStepNum() {
        return totalStepNum;
    }

    public void setTotalStepNum(Integer totalStepNum) {
        this.totalStepNum = totalStepNum;
    }

    public Integer getEndStatus() {
        return endStatus;
    }

    public void setEndStatus(Integer endStatus) {
        this.endStatus = endStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getWorkType() {
        return workType;
    }

    public void setWorkType(Integer workType) {
        this.workType = workType;
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

    public String getInvalidTimeBig() {
        return invalidTimeBig;
    }

    public void setInvalidTimeBig(String invalidTimeBig) {
        this.invalidTimeBig = invalidTimeBig;
    }

    public String getInvalidTimeSmall() {
        return invalidTimeSmall;
    }

    public void setInvalidTimeSmall(String invalidTimeSmall) {
        this.invalidTimeSmall = invalidTimeSmall;
    }
}
