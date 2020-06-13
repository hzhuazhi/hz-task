package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.bank.BankTransferModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 银行转账信息的Dao层
 * @Author yoko
 * @Date 2020/5/18 19:47
 * @Version 1.0
 */
@Mapper
public interface BankTransferMapper<T> extends BaseDao<T> {

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
