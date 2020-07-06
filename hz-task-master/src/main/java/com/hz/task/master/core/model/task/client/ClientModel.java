package com.hz.task.master.core.model.task.client;

import java.io.Serializable;

/**
 * @Description 客户端监听数据的实体属性Bean
 * @Author yoko
 * @Date 2020/7/6 19:14
 * @Version 1.0
 */
public class ClientModel implements Serializable {
    private static final long   serialVersionUID = 1213203201101L;

    /**
     * 具体内容
     */
    private String content;

    /**
     * 信息下标位
     */
    private String number;

    /**
     * 时间
     */
    private String time;

    /**
     * token值
     */
    private String token;

    /**
     * 支付宝账号ID
     */
    private String userId;

    public ClientModel(){

    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
