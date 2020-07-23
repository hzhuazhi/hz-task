package com.hz.task.master.core.mapper;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 用户的收款账号的Dao层
 * @Author yoko
 * @Date 2020/5/15 13:54
 * @Version 1.0
 */
@Mapper
public interface DidCollectionAccountMapper<T> extends BaseDao<T> {


    /**
     * @Description: 修改用户收款账号的基本信息
     * <p>基本信息包括：1收款账号名称：用户备注使用=ac_name</p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/5/18 14:52
     */
    public void updateBasic(DidCollectionAccountModel model);


    /**
     * @Description: 更新用户收款账号信息
     * @param model - 用户收款账号信息
     * @return
     * @author yoko
     * @date 2020/5/18 15:52
     */
    public void updateDidCollectionAccount(DidCollectionAccountModel model);

    /**
     * @Description: 更新用户收款账号的总开关
     * <p>
     *     更新用户收款账号开关，并且说明更新缘由
     * </p>
     * @param model
     * @return 
     * @author yoko
     * @date 2020/6/10 17:18
    */
    public int updateDidCollectionAccountTotalSwitch(DidCollectionAccountModel model);


    /**
     * @Description: 修改用户收款账号的审核信息
     * <p>
     *     由于用户下线了小微，所以用户的收款账号的审核状态变成初始状态
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/12 22:30
     */
    public int updateDidCollectionAccountCheckData(DidCollectionAccountModel model);

    /**
     * @Description: 给出码连续失败次数超过策略部署的失败次数：修改用户收款账号修改成暂停使用状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/19 13:57
    */
    public int updateDidCollectionAccountCheckDataByFail(DidCollectionAccountModel model);

    /**
     * @Description: 根据小微ID以及微信群来确定收款账号
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/22 14:32
     */
    public DidCollectionAccountModel getDidCollectionAccountByWxIdAndWxName(DidCollectionAccountModel model);

    /**
     * @Description: 更新微信群的信息
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/22 15:02
     */
    public int updateDidCollectionAccountByWxData(DidCollectionAccountModel model);

    /**
     * @Description: 根据微信群ID查询收款账号
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/23 17:31
     */
    public DidCollectionAccountModel getDidCollectionAccountByWxGroupId(DidCollectionAccountModel model);

}
