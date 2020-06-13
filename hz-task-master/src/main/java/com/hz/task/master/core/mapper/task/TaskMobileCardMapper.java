package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.mobilecard.MobileCardDataModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task：手机短信的Dao层
 * @Author yoko
 * @Date 2020/6/2 18:49
 * @Version 1.0
 */
@Mapper
public interface TaskMobileCardMapper<T> extends BaseDao<T> {

    /**
     * @Description: 查询未跑的手机短信信息
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<MobileCardDataModel> getMobileCardDataList(Object obj);

    /**
     * @Description: 更新手机短信信息数据的状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateMobileCardDataStatus(Object obj);
}
