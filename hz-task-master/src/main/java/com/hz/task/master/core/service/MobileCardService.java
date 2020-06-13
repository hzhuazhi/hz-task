package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;

/**
 * @Description 手机卡的Service层
 * @Author yoko
 * @Date 2020/5/18 17:22
 * @Version 1.0
 */
public interface MobileCardService<T> extends BaseService<T> {

    /**
     * @Description: 更新手机卡的使用状态
     * @param model - 手机号或者ID
     * @return
     * @author yoko
     * @date 2020/6/3 17:24
     */
    public int upUseStatus(MobileCardModel model);


    /**
     * @Description: 根据条件查询手机号数据
     * @param model - 查询条件
     * @param isCache - 是否通过缓存查询：0需要通过缓存查询，1直接查询数据库
     * @return
     * @author yoko
     * @date 2019/11/21 19:26
     */
    public MobileCardModel getMobileCard(MobileCardModel model, int isCache) throws Exception;
}
