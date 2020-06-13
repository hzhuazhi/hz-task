package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.bank.BankModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task:银行卡流水限制的Dao层
 * @Author yoko
 * @Date 2020/6/13 19:07
 * @Version 1.0
 */
@Mapper
public interface TaskBankLimitMapper<T> extends BaseDao<T> {

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
