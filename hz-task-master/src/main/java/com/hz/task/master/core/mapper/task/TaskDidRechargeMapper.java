package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.did.DidRechargeModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:用户充值订单的Dao层
 * @Author yoko
 * @Date 2020/6/4 17:24
 * @Version 1.0
 */
@Mapper
public interface TaskDidRechargeMapper<T> extends BaseDao<T> {

    /**
     * @Description: 查询用户充值订单信息
     * @param obj - 订单状态
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<DidRechargeModel> getDidRechargeList(Object obj);

    /**
     * @Description: 更新用户充值订单的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateDidRechargeStatus(Object obj);

    /**
     * @Description: 获取昨天充值成功的用户集合数据
     * @param model - 查询条件
     * @return
     * @author yoko
     * @date 2020/6/5 18:45
     */
    public List<Long> getRechargeDidList(DidRechargeModel model);

}
