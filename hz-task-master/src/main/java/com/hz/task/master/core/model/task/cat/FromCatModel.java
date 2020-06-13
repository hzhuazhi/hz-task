package com.hz.task.master.core.model.task.cat;

import java.io.Serializable;

/**
 * @Description 可爱猫原始数据外层的实体属性Bean
 * @Author yoko
 * @Date 2020/6/6 16:05
 * @Version 1.0
 */
public class FromCatModel implements Serializable {
    private static final long   serialVersionUID = 1203203201101L;

    private String final_from_name;
    private String final_from_wxid;
    private String from_name;
    private String from_wxid;
    private String msg;
    private String msg_type;
    private String rid;
    private String robot_wxid;
    private String time;
    private String type;


    public FromCatModel(){

    }


    public String getFinal_from_name() {
        return final_from_name;
    }

    public void setFinal_from_name(String final_from_name) {
        this.final_from_name = final_from_name;
    }

    public String getFinal_from_wxid() {
        return final_from_wxid;
    }

    public void setFinal_from_wxid(String final_from_wxid) {
        this.final_from_wxid = final_from_wxid;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getFrom_wxid() {
        return from_wxid;
    }

    public void setFrom_wxid(String from_wxid) {
        this.from_wxid = from_wxid;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getRobot_wxid() {
        return robot_wxid;
    }

    public void setRobot_wxid(String robot_wxid) {
        this.robot_wxid = robot_wxid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
