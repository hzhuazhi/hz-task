package com.hz.task.master.core.model.task.cat;

import java.io.Serializable;

/**
 * @Description 可爱猫的具体数据的实体属性Bean
 * @Author yoko
 * @Date 2020/6/6 16:09
 * @Version 1.0
 */
public class CatMsg implements Serializable {
    private static final long   serialVersionUID = 1203203201102L;
    /**
     * 微信店员的to_wxid
     */
    private String to_wxid;

    /**
     * 消息ID
     */
    private Long msgid;

    /**
     * 收款金额的下标位
     */
    private String received_money_index;

    /**
     * 收款金额
     */
    private String money;

    /**
     * 总的收款金额
     */
    private String total_money;

    /**
     * 备注
     */
    private String remark;

    /**
     * 具体收款人
     * 孩子(**龙)
     */
    private String shopowner;

    /**
     *场景描述
     */
    private String scene_desc;

    /**
     * 场景
     */
    private Integer scene;

    /**
     * 时间戳
     */
    private Long timestamp;


    public CatMsg(){

    }

    public String getTo_wxid() {
        return to_wxid;
    }

    public void setTo_wxid(String to_wxid) {
        this.to_wxid = to_wxid;
    }

    public Long getMsgid() {
        return msgid;
    }

    public void setMsgid(Long msgid) {
        this.msgid = msgid;
    }

    public String getReceived_money_index() {
        return received_money_index;
    }

    public void setReceived_money_index(String received_money_index) {
        this.received_money_index = received_money_index;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getTotal_money() {
        return total_money;
    }

    public void setTotal_money(String total_money) {
        this.total_money = total_money;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getShopowner() {
        return shopowner;
    }

    public void setShopowner(String shopowner) {
        this.shopowner = shopowner;
    }

    public String getScene_desc() {
        return scene_desc;
    }

    public void setScene_desc(String scene_desc) {
        this.scene_desc = scene_desc;
    }

    public Integer getScene() {
        return scene;
    }

    public void setScene(Integer scene) {
        this.scene = scene;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
