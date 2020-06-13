package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 可爱猫回调店员下线：小微旗下店员下线通知；取消与小微绑定关系的信息的Dao层
 * @Author yoko
 * @Date 2020/6/11 13:48
 * @Version 1.0
 */
@Mapper
public interface CatDataOfflineMapper<T> extends BaseDao<T> {

    /**
     * @Description: 可爱猫回调店员下线的基本信息更新
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/11 19:00
     */
    public int updateCatDataOffline(CatDataOfflineModel model);
}
