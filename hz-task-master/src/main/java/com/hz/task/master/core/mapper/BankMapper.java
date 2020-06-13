package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.bank.BankModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 银行的Dao层
 * @Author yoko
 * @Date 2020/5/18 19:04
 * @Version 1.0
 */
@Mapper
public interface BankMapper<T> extends BaseDao<T> {

    /**
     * @Description: 修改银行卡的使用状态
     * @param bankModel - 银行卡信息
     * @return
     * @author yoko
     * @date 2020/6/3 16:31
     */
    public int upUseStatus(BankModel bankModel);
}
