package com.hz.task.master.core.model.task.cat;

import java.io.Serializable;

/**
 * @Description 可爱猫数据：剔除成员的属性字段
 * @Author yoko
 * @Date 2020/7/23 20:22
 * @Version 1.0
 */
public class CatMember implements Serializable {
    private static final long   serialVersionUID = 1203203211135L;

    /**
     * 被移出的成员的原始微信ID
     */
    public String member_wxid;

    /**
     * 被移出的成员的微信昵称
     */
    public String member_nickname;

    /**
     * 被哪个微信群ID移出的
     */
    public String group_wxid;

    /**
     * 被哪个微信群名称移出的
     */
    public String group_name;

    public CatMember(){

    }

    public String getMember_wxid() {
        return member_wxid;
    }

    public void setMember_wxid(String member_wxid) {
        this.member_wxid = member_wxid;
    }

    public String getMember_nickname() {
        return member_nickname;
    }

    public void setMember_nickname(String member_nickname) {
        this.member_nickname = member_nickname;
    }

    public String getGroup_wxid() {
        return group_wxid;
    }

    public void setGroup_wxid(String group_wxid) {
        this.group_wxid = group_wxid;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }
}
