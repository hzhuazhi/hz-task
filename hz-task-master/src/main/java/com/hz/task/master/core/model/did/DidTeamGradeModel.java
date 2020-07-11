package com.hz.task.master.core.model.did;

import com.hz.task.master.core.protocol.page.BasePage;

import java.io.Serializable;

/**
 * @Description 团队长奖励金额等级纪录的实体属性Bean
 * @Author yoko
 * @Date 2020/7/11 21:14
 * @Version 1.0
 */
public class DidTeamGradeModel extends BasePage implements Serializable {
    private static final long   serialVersionUID = 1203223201123L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 奖励归属用户ID：对应表tb_fn_did的主键ID；奖励给哪个用户
     */
    private Long did;

    /**
     * 等级一级别的等级
     */
    private Integer oneGrade;

    /**
     * 等级二级别的等级
     */
    private Integer twoGrade;

    /**
     * 等级三级别的等级
     */
    private Integer threeGrade;

    /**
     * 等级四级别的等级
     */
    private Integer fourGrade;

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

    public Integer getOneGrade() {
        return oneGrade;
    }

    public void setOneGrade(Integer oneGrade) {
        this.oneGrade = oneGrade;
    }

    public Integer getTwoGrade() {
        return twoGrade;
    }

    public void setTwoGrade(Integer twoGrade) {
        this.twoGrade = twoGrade;
    }

    public Integer getThreeGrade() {
        return threeGrade;
    }

    public void setThreeGrade(Integer threeGrade) {
        this.threeGrade = threeGrade;
    }

    public Integer getFourGrade() {
        return fourGrade;
    }

    public void setFourGrade(Integer fourGrade) {
        this.fourGrade = fourGrade;
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
}
