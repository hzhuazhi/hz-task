package com.hz.task.master.core.service;


import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidWxSortModel;

/**
 * @Description 用户的微信出码排序的Service层
 * @Author yoko
 * @Date 2020/8/31 16:39
 * @Version 1.0
 */
public interface DidWxSortService<T> extends BaseService<T> {

    /**
     * @Description: 获取最大的排序
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/31 16:32
     */
    public int maxSort(DidWxSortModel model);

    /**
     * @Description: 添加用户的微信出码排序的数据
     * <p>
     *     自动累加排序
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/31 16:38
     */
    public int addBySort(DidWxSortModel model);

    /**
     * @Description: 更新使用中的状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/31 17:05
     */
    public int updateInUse(DidWxSortModel model);
}
