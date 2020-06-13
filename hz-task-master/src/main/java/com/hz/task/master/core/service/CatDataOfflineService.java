package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;

/**
 * @Description 可爱猫回调店员下线：小微旗下店员下线通知；取消与小微绑定关系的信息的Service层
 * @Author yoko
 * @Date 2020/6/11 14:07
 * @Version 1.0
 */
public interface CatDataOfflineService<T> extends BaseService<T> {

    /**
     * @Description: 可爱猫回调店员下线的基本信息更新
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/11 19:00
     */
    public int updateCatDataOffline(CatDataOfflineModel model);
}
