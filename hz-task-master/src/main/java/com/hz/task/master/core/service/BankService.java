package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.model.strategy.StrategyBankLimit;
import com.hz.task.master.core.model.strategy.StrategyData;

import java.util.List;
import java.util.Map;

/**
 * @Description 银行的Service层
 * @Author yoko
 * @Date 2020/5/18 19:06
 * @Version 1.0
 */
public interface BankService<T> extends BaseService<T> {

    /**
     * @Description: 筛选银行卡
     * @param bankList - 银行卡信息
     * @param strategyBankLimitList - 银行卡流水日月总规则
     * @param strategyMoneyAddSubtractList - 订单金额加减范围列表
     * @param orderMoney - 订单充值的金额
     * @return Map - 返回筛选确认的银行卡以及选择的金额
     * @author yoko
     * @date 2020/5/20 14:45
    */
    public Map<String, Object> screenBank(List<BankModel> bankList, List<StrategyBankLimit> strategyBankLimitList,
                                          List<StrategyData> strategyMoneyAddSubtractList, String orderMoney) throws Exception;


    /**
     * @Description: 修改银行卡的使用状态
     * @param bankModel - 银行卡信息
     * @return
     * @author yoko
     * @date 2020/6/3 16:31
    */
    public int upUseStatus(BankModel bankModel);
}
