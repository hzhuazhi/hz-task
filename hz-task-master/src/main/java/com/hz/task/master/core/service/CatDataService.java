package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.cat.CatDataModel;

/**
 * @Description 可爱猫回调订单的Service层
 * @Author yoko
 * @Date 2020/5/26 10:51
 * @Version 1.0
 */
public interface CatDataService<T> extends BaseService<T> {

    /**
     * @Description: 修改wx_id的值
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/6 20:03
    */
    public int updateWxId(CatDataModel model);

    /**
     * @Description: 可爱猫回调订单的数据修改
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/7 9:06
     */
    public int updateCatData(CatDataModel model);
}
