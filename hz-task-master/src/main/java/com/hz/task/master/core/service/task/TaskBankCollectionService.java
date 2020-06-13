package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;

import java.util.List;

/**
 * @Description task：银行卡回传信息的Service层
 * @Author yoko
 * @Date 2020/6/3 19:11
 * @Version 1.0
 */
public interface TaskBankCollectionService<T> extends BaseService<T> {

    /**
     * @Description: 查询未跑的银行卡回传信息
     * @param obj
     * @return
     * @author yoko
     * @date 2020/6/3 13:53
     */
    public List<BankCollectionDataModel> getBankCollectionDataList(Object obj);

    /**
     * @Description: 更新银行卡回传信息数据的状态
     * @param obj
     * @return
     * @author yoko
     * @date 2020/1/11 16:30
     */
    public int updateBankCollectionDataStatus(Object obj);
}
