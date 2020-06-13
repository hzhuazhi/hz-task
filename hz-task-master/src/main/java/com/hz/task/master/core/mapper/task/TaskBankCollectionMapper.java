package com.hz.task.master.core.mapper.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description task：银行卡回传数据的Dao层
 * @Author yoko
 * @Date 2020/6/3 19:09
 * @Version 1.0
 */
@Mapper
public interface TaskBankCollectionMapper<T> extends BaseDao<T> {

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
