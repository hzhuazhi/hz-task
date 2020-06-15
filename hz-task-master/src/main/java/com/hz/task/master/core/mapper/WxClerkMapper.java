package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.wx.WxClerkModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 小微旗下店员的Dao层
 * @Author yoko
 * @Date 2020/5/25 17:37
 * @Version 1.0
 */
@Mapper
public interface WxClerkMapper<T> extends BaseDao<T> {

    /**
     * @Description: 根据wxid跟收款账号删除关联关系
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/15 17:51
     */
    public int updateWxClerkIsYn(WxClerkModel model);
}
