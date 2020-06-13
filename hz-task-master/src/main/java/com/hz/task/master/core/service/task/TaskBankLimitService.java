package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.bank.BankModel;

import java.util.List;

/**
 * @Description task:银行卡流水限制的Service层
 * @Author yoko
 * @Date 2020/6/13 19:13
 * @Version 1.0
 */
public interface TaskBankLimitService<T> extends BaseService<T> {

    /**
     * @Description: 获取银行卡的信息
     * <p>
     *     获取未被暂停使用的，并且未删除的银行卡数据集合
     * </p>
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/6 17:42
     */
    public List<BankModel> getBankDataList(Object obj);


    /**
     * @Description: 更新银行卡的三个开关
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateBankSwitch(Object obj);
}
