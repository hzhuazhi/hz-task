package com.hz.task.master.core.service;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;

import java.util.List;

/**
 * @Description 用户的收款账号的Service层
 * @Author yoko
 * @Date 2020/5/15 14:01
 * @Version 1.0
 */
public interface DidCollectionAccountService<T> extends BaseService<T> {

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

    /**
     * @Description: 根据微信群ID或者微信群名称来查询收款账号信息
     * <p>
     *     此方法不带yn=0的硬性条件
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/7/23 23:04
    */
    public DidCollectionAccountModel getDidCollectionAccountByWxGroupIdOrWxGroupNameAndYn(DidCollectionAccountModel model);

    /**
     * @Description: 更新微信群收款红包次数以及是否失效
     * <p>
     *     每次更新微信群的红包， 如果红包等于0则修改微信群的失效状态
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/3 10:52
    */
    public int updateDidCollectionAccountRedPackNumOrInvalid(DidCollectionAccountModel model);

    /**
     * @Description: 获取有效的收款账号
     * <p>
     *     获取有效的微信群数据集合
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/8/3 15:50
     */
    public List<DidCollectionAccountModel> getEffectiveDidCollectionAccountList(DidCollectionAccountModel model);

}
