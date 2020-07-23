package com.hz.task.master.core.model.task.cat;

import java.io.Serializable;

/**
 * @Description 可爱猫：加群成员属性
 * @Author yoko
 * @Date 2020/7/23 10:39
 * @Version 1.0
 */
public class CatGuest implements Serializable {
    private static final long   serialVersionUID = 1203203201135L;

    /**
     * 进群用户的wxid
     */
    public String wxid;

    /**
     * 进群用户微信昵称
     */
    public String nickname;

    public CatGuest(){

    }

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
