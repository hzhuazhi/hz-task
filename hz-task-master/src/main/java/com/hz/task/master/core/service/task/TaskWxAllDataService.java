package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.wx.WxAllDataModel;

import java.util.List;

/**
 * @Description task:微信回调原始数据的Service层
 * @Author yoko
 * @Date 2020/8/11 19:06
 * @Version 1.0
 */
public interface TaskWxAllDataService<T> extends BaseService<T> {

    /**
     * @Description: 查询未解析的微信回调原始数据
     * @param obj - run_status
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<WxAllDataModel> getWxAllDataList(Object obj);

    /**
     * @Description: 更新微信回调原始数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateWxAllDataStatus(Object obj);

}
