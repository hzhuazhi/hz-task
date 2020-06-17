package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.cat.CatDataBindingModel;

import java.util.List;

/**
 * @Description task:可爱猫回调店员绑定小微的Service层
 * @Author yoko
 * @Date 2020/6/17 10:03
 * @Version 1.0
 */
public interface TaskCatDataBindingService<T> extends BaseService<T> {

    /**
     * @Description: 获取可爱猫回调店员绑定小微的未补充的数据
     * work_type =1的数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
     */
    public List<CatDataBindingModel> getCatDataBindingList(Object obj);


    /**
     * @Description: 可爱猫回调店员绑定小微的状态、运行状态
     * <p>
     *     修改work_type的状态
     * </p>
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateCatDataBindingStatus(Object obj);
}
