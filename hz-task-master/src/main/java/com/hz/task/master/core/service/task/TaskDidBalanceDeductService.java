package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidBalanceDeductModel;

import java.util.List;

/**
 * @Description task:用户扣减余额流水的Service层
 * @Author yoko
 * @Date 2020/7/2 20:11
 * @Version 1.0
 */
public interface TaskDidBalanceDeductService<T> extends BaseService<T> {

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
}
