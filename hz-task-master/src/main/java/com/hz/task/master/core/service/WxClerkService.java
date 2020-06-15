package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.wx.WxClerkModel;

/**
 * @Description 小微旗下店员的Service层
 * @Author yoko
 * @Date 2020/5/25 17:34
 * @Version 1.0
 */
public interface WxClerkService<T> extends BaseService<T> {


    /**
     * @Description: 根据wxid跟收款账号删除关联关系
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/15 17:51
    */
    public int updateWxClerkIsYn(WxClerkModel model);
}
