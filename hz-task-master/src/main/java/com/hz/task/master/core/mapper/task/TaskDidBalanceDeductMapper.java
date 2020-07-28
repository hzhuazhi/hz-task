package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:用户扣减余额流水的Dao层
 * @Author yoko
 * @Date 2020/7/2 20:08
 * @Version 1.0
 */
@Mapper
public interface TaskDidBalanceDeductMapper<T> extends BaseDao<T> {

    /**
     * @Description: 根据条件获取用户扣减余额流水数据-未跑task的数据
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<DidBalanceDeductModel> getDidBalanceDeductList(Object obj);

    /**
     * @Description: 更新用户扣减余额流水数据的状态、运行状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateDidBalanceDeductStatus(Object obj);

    /**
     * @Description: 查询所有金额是锁定的用户集合
     * @param obj - 查询条件
     * @return
     * @author yoko
     * @date 2020/6/5 18:45
     */
    public List<Long> getBalanceDeductDidList(Object obj);

}
