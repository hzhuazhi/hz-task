package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.CatDataAnalysisUnusualMapper;
import com.hz.task.master.core.service.CatDataAnalysisUnusualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 可爱猫数据解析异常的Service层的实现层
 * @Author yoko
 * @Date 2020/7/25 15:38
 * @Version 1.0
 */
@Service
public class CatDataAnalysisUnusualServiceImpl<T> extends BaseServiceImpl<T> implements CatDataAnalysisUnusualService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private CatDataAnalysisUnusualMapper catDataAnalysisUnusualMapper;

    public BaseDao<T> getDao() {
        return catDataAnalysisUnusualMapper;
    }
}
