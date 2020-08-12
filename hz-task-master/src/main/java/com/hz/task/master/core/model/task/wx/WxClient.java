package com.hz.task.master.core.model.task.wx;

import java.io.Serializable;

/**
 * @Description 微信客户端数据的实体属性Bean
 * @Author yoko
 * @Date 2020/8/12 11:17
 * @Version 1.0
 */
public class WxClient implements Serializable {
    private static final long   serialVersionUID = 1203205201101L;

    /**
     * 设备PID
     */
    public String pid;

    /**
     * 数据类型：1普通消息（群消息），10000系统消息（加人，修改群名称，踢人，发红包），200小微登入，201小微登出
     */
    public String type;

    /**
     *消息来源：1是小微，0是其它人
     */
    public String self;

    /**
     * 头文件
     */
    public String head;

    /**
     * 发信息的微信ID
     */
    public String wxid1;

    /**
     * 发信息的微信昵称
     */
    public String wxid2;

    /**
     * 微信群名称
     */
    public String wxid3;

    /**
     * 消息内容
     */
    public String content;

    /**
     * 小微昵称
     */
    public String log_nickname;

    /**
     * 小微的微信ID
     */
    public String log_wechatid;

    /**
     * 微信群ID
     */
    public String chartid;



    public WxClient(){

    }


    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getWxid1() {
        return wxid1;
    }

    public void setWxid1(String wxid1) {
        this.wxid1 = wxid1;
    }

    public String getWxid2() {
        return wxid2;
    }

    public void setWxid2(String wxid2) {
        this.wxid2 = wxid2;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLog_nickname() {
        return log_nickname;
    }

    public void setLog_nickname(String log_nickname) {
        this.log_nickname = log_nickname;
    }

    public String getLog_wechatid() {
        return log_wechatid;
    }

    public void setLog_wechatid(String log_wechatid) {
        this.log_wechatid = log_wechatid;
    }

    public String getChartid() {
        return chartid;
    }

    public void setChartid(String chartid) {
        this.chartid = chartid;
    }

    public String getWxid3() {
        return wxid3;
    }

    public void setWxid3(String wxid3) {
        this.wxid3 = wxid3;
    }
}
