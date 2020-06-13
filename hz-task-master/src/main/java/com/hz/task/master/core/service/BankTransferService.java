package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.bank.BankTransferModel;

/**
 * @Description 银行转账信息的Service层
 * @Author yoko
 * @Date 2020/5/18 19:48
 * @Version 1.0
 */
public interface BankTransferService<T> extends BaseService<T> {

    /**
     * @Description: 根据查询条件获取银行卡的转账金额
     * 根据日期：日，月，总；银行卡ID查询转账金额总和
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/13 20:30
     */
    public String getBankTransferMoney(BankTransferModel model);
}
