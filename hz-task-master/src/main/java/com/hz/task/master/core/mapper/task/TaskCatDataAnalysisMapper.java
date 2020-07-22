package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.cat.CatDataAnalysisModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:可爱猫数据解析的Dao层
 * @Author yoko
 * @Date 2020/7/22 19:14
 * @Version 1.0
 */
@Mapper
public interface TaskCatDataAnalysisMapper<T> extends BaseDao<T> {

    /**
     * @Description: 获取可爱猫数据解析的的数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
     */
    public List<CatDataAnalysisModel> getCatDataAnalysisList(Object obj);


    /**
     * @Description: 更新可爱猫数据解析数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateCatDataAnalysisStatus(Object obj);
}
