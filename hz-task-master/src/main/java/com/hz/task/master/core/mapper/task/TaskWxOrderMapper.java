package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.wx.WxOrderModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:小微给出订单记录的Dao层
 * @Author yoko
 * @Date 2020/8/8 16:29
 * @Version 1.0
 */
@Mapper
public interface TaskWxOrderMapper<T> extends BaseDao<T> {

    /**
     * @Description: 根据条件获取派发订单里面去重复的小微ID集合
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/19 10:51
     */
    public List<Long> getWxIdByOrderList(WxOrderModel model);

    /**
     * @Description: 根据条件获取小微给出订单记录的集合
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/8 17:18
    */
    public List<WxOrderModel> getWxOrderList(WxOrderModel model);
}
