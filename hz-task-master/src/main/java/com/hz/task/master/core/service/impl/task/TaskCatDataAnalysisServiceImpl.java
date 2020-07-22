package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.task.TaskCatDataAnalysisMapper;
import com.hz.task.master.core.model.cat.CatDataAnalysisModel;
import com.hz.task.master.core.service.task.TaskCatDataAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description task:可爱猫数据解析的Service层的实现层
 * @Author yoko
 * @Date 2020/7/22 19:18
 * @Version 1.0
 */
@Service
public class TaskCatDataAnalysisServiceImpl<T> extends BaseServiceImpl<T> implements TaskCatDataAnalysisService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskCatDataAnalysisMapper taskCatDataAnalysisMapper;


    public BaseDao<T> getDao() {
        return taskCatDataAnalysisMapper;
    }

    @Override
    public List<CatDataAnalysisModel> getCatDataAnalysisList(Object obj) {
        return taskCatDataAnalysisMapper.getCatDataAnalysisList(obj);
    }

    @Override
    public int updateCatDataAnalysisStatus(Object obj) {
        return taskCatDataAnalysisMapper.updateCatDataAnalysisStatus(obj);
    }
}
