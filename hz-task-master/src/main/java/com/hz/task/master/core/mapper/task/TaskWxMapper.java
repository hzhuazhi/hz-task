package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.wx.WxModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task：小微的Dao层
 * @Author yoko
 * @Date 2020/8/1 18:29
 * @Version 1.0
 */
@Mapper
public interface TaskWxMapper<T> extends BaseDao<T> {

    /**
     * @Description: 查询小微数据
     * @param obj - 正常使用的
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<WxModel> getWxList(Object obj);

    /**
     * @Description: 更新小微数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateWxStatus(Object obj);

}
