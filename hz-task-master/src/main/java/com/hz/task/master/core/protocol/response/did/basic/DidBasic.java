package com.hz.task.master.core.protocol.response.did.basic;

import java.io.Serializable;

/**
 * @Description 协议：用户的基本信息
 * @Author yoko
 * @Date 2020/5/29 14:39
 * @Version 1.0
 */
public class DidBasic implements Serializable {
    private static final long   serialVersionUID = 1233023331149L;

    /**
     * 余额
     */
    public String balance;

    /**
     * 锁定/冻结金额
     */
    public String lockMoney;

    /**
     * 总金额（累计充值）
     */
    public String totalMoney;

    /**
     * 总收益
     */
    public String totalProfit;

    /**
     * 今日收益
     * 今日收益：充多少送多少的收益，充值档次的赠送，昨天直推的奖励
     */
    public String todayProfit;

    /**
     * 今日兑换
     * 今日兑换：今日派发订单成功的
     */
    public String todayExchange;

    /**
     * 账号昵称
     */
    public String nickname;

    /**
     * 登录账号
     */
    public String acNum;

    /**
     * 邀请码
     */
    public String icode;

    /**
     * 直推总人数
     */
    public Integer totalDirectNum;

    /**
     * 直推总奖励收益
     */
    public String totalDirectProfit;

    /**
     * 充值总收益：充多少送多少
     */
    public String totalRechargeProfit;

    /**
     * 档次总收益：达到的档次之后的总奖励
     */
    public String totalGradeProfit;

    public DidBasic(){

    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getLockMoney() {
        return lockMoney;
    }

    public void setLockMoney(String lockMoney) {
        this.lockMoney = lockMoney;
    }

    public String getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(String totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(String totalProfit) {
        this.totalProfit = totalProfit;
    }

    public String getTodayProfit() {
        return todayProfit;
    }

    public void setTodayProfit(String todayProfit) {
        this.todayProfit = todayProfit;
    }

    public String getTodayExchange() {
        return todayExchange;
    }

    public void setTodayExchange(String todayExchange) {
        this.todayExchange = todayExchange;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAcNum() {
        return acNum;
    }

    public void setAcNum(String acNum) {
        this.acNum = acNum;
    }

    public String getIcode() {
        return icode;
    }

    public void setIcode(String icode) {
        this.icode = icode;
    }

    public Integer getTotalDirectNum() {
        return totalDirectNum;
    }

    public void setTotalDirectNum(Integer totalDirectNum) {
        this.totalDirectNum = totalDirectNum;
    }

    public String getTotalDirectProfit() {
        return totalDirectProfit;
    }

    public void setTotalDirectProfit(String totalDirectProfit) {
        this.totalDirectProfit = totalDirectProfit;
    }

    public String getTotalRechargeProfit() {
        return totalRechargeProfit;
    }

    public void setTotalRechargeProfit(String totalRechargeProfit) {
        this.totalRechargeProfit = totalRechargeProfit;
    }

    public String getTotalGradeProfit() {
        return totalGradeProfit;
    }

    public void setTotalGradeProfit(String totalGradeProfit) {
        this.totalGradeProfit = totalGradeProfit;
    }
}
