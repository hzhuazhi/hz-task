package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.cat.CatAllDataModel;

import java.util.List;

/**
 * @Description task：可爱猫回调原始数据的Service层
 * @Author yoko
 * @Date 2020/6/6 15:31
 * @Version 1.0
 */
public interface TaskCatAllDataService<T> extends BaseService<T> {

    /**
     * @Description: 查询未解析的可爱猫原始数据
     * @param obj - run_status
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<CatAllDataModel> getCatAllDataList(Object obj);

    /**
     * @Description: 更新可爱猫原始数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateCatAllDataStatus(Object obj);
}
