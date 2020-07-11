package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.DidTeamGradeMapper;
import com.hz.task.master.core.service.DidTeamGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 团队长奖励金额等级纪录的Service层的实现层
 * @Author yoko
 * @Date 2020/7/11 21:30
 * @Version 1.0
 */
@Service
public class DidTeamGradeServiceImpl<T> extends BaseServiceImpl<T> implements DidTeamGradeService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private DidTeamGradeMapper didTeamGradeMapper;

    public BaseDao<T> getDao() {
        return didTeamGradeMapper;
    }
}
