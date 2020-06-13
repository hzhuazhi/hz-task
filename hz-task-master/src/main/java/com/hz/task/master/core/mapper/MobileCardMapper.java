package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 手机卡的Dao层
 * @Author yoko
 * @Date 2020/5/18 17:22
 * @Version 1.0
 */
@Mapper
public interface MobileCardMapper<T> extends BaseDao<T> {

    /**
     * @Description: 更新手机卡的使用状态
     * @param model - 手机号或者ID
     * @return
     * @author yoko
     * @date 2020/6/3 17:24
    */
    public int upUseStatus(MobileCardModel model);
}
