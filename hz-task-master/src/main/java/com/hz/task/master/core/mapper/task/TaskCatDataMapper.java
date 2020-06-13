package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.cat.CatDataModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:可爱猫回调订单的Dao层
 * @Author yoko
 * @Date 2020/6/6 17:35
 * @Version 1.0
 */
@Mapper
public interface TaskCatDataMapper<T> extends BaseDao<T> {

    /**
     * @Description: 获取可爱猫回调订单的数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
    */
    public List<CatDataModel> getCatDataList(Object obj);


    /**
     * @Description: 更新可爱猫回调订单数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateCatDataStatus(Object obj);



}
