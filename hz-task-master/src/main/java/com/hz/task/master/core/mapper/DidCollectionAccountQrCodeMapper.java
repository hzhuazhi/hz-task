package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.did.DidCollectionAccountQrCodeModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 用户的收款账号二维码的Dao层
 * @Author yoko
 * @Date 2020/6/17 14:53
 * @Version 1.0
 */
@Mapper
public interface DidCollectionAccountQrCodeMapper<T> extends BaseDao<T> {

    /**
     * @Description: 批量更新二维码的使用状态或者批量删除二维码
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/17 15:25
     */
    public int updateBatchStatus(DidCollectionAccountQrCodeModel model);

    /**
     * @Description: 批量添加用户收款账号的二维码信息
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/17 18:06
    */
    public int addBatchDidCollectionAccountQrCode(DidCollectionAccountQrCodeModel model);

    /**
     * @Description: 更新用户收款二维码账号已成功的次数
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/18 20:40
    */
    public int updateIsLimitNum(DidCollectionAccountQrCodeModel model);
}
