package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.*;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.operate.OperateModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.cat.CatGuest;
import com.hz.task.master.core.model.task.cat.CatMember;
import com.hz.task.master.core.model.task.cat.CatMsg;
import com.hz.task.master.core.model.task.cat.FromCatModel;
import com.hz.task.master.core.model.task.wx.WxClient;
import com.hz.task.master.core.model.wx.WxAllDataModel;
import com.hz.task.master.core.model.wx.WxClerkDataModel;
import com.hz.task.master.core.model.wx.WxClerkModel;
import com.hz.task.master.core.model.wx.WxModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.TaskMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description task:微信回调原始数据
 * @Author yoko
 * @Date 2020/8/11 19:39
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskWxAllData {

    private final static Logger log = LoggerFactory.getLogger(TaskCatAllData.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：解析微信回调原始数据
     * <p>
     *     每1每秒运行一次
     *     1.查询出未未解析的微信数据。
     *     2.判断类型。
     *     3.把数据录入到可爱猫回调订单表里面。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void wxAllData() throws Exception{
//        log.info("----------------------------------TaskWxAllData.wxAllData()----start");
        // 获取需要解析的微信回调原始数据
        StatusModel statusQuery = TaskMethod.assembleTaskStatusQuery(limitNum);
        List<WxAllDataModel> synchroList = ComponentUtil.taskWxAllDataService.getWxAllDataList(statusQuery);
        for (WxAllDataModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_WX_ALL_DATA, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (!StringUtils.isBlank(data.getJsonData())){
                        WxClient wxClient = JSON.parseObject(data.getJsonData(), WxClient.class);
                        if (wxClient == null || StringUtils.isBlank(wxClient.type)){
                            // 更新此次task的状态：更新成失败-json解析后数据为空
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "json解析后数据为空");
                            ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                        }else{
                            if (!StringUtils.isBlank(wxClient.log_wechatid)){
                                boolean flag_type = true;
                                if (wxClient.type.equals("301")){
                                    flag_type = false;
                                }
                                if (wxClient.type.equals("201")){
                                    flag_type = false;
                                }
                                if (flag_type){
                                    // 根据小微ID查询小微信息
                                    WxModel wxQuery = TaskMethod.assembleWxModel(wxClient.log_wechatid);
                                    WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                                    if (wxModel != null && wxModel.getId() > 0){
                                        OperateModel operateAccount = null;
                                        // 根据微信群名称or微信群ID查询收款账号
                                        long did = 0;
                                        long collectionAccountId = 0;
                                        int collectionAccountType = 0;
                                        DidCollectionAccountModel didCollectionAccountByWxGroupIdOrWxGroupNameAndYnQuery = TaskMethod.assembleDidCollectionAccountQueryByAcNameAndPayee(wxClient.chartid, wxClient.wxid3, 3);
                                        DidCollectionAccountModel didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel = ComponentUtil.didCollectionAccountService.getDidCollectionAccountByWxGroupIdOrWxGroupNameAndYn(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnQuery);
                                        if (didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel != null && didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId() > 0){
                                            did = didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getDid();
                                            collectionAccountId = didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId();
                                            if (didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getYn() == null || didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getYn() == 0){
                                                if (wxClient.wxid3.equals(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getPayee())){
                                                    collectionAccountType = 5;
                                                }else{
                                                    collectionAccountType = 4;
                                                }
                                            }else{
                                                collectionAccountType = 3;
                                            }
                                        }else{
                                            collectionAccountType = 2;
                                        }

                                        if (collectionAccountType == 5){
                                            // 收款账号正常
                                        }else{
                                            // 收款账号不属于正常
                                            if (collectionAccountType == 2){
                                                // 说明小微错误加错群：因为根据微信群名称or微信群ID以及去掉yn没有查到对应的收款账号
                                                operateAccount = new OperateModel();
                                                String remark = "我方小微：" + wxModel.getWxName() + "，需退出群：" + wxClient.wxid3;
                                                operateAccount = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, null, 0, null, 7,
                                                        "说明小微错误加错群：因为根据微信群名称or微信群ID以及去掉yn没有查到对应的收款账号", remark , 2, wxModel.getId(),null);
                                            }else{
                                                // 删除小微旗下店员的关联关系
                                                WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(wxModel.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId());
                                                ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);

                                                // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                                DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId(), "检测：微信群修改名称or微信群被删除（服务数据）");
                                                ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                                operateAccount = new OperateModel();
                                                String remark = "我方小微：" + wxModel.getWxName() + "，需退出群：" + wxClient.wxid3;
                                                operateAccount = TaskMethod.assembleOperateData(data.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel, null, 0, null, 7,
                                                        "说明：微信群修改名称or微信群被删除（服务数据）", remark , 2, wxModel.getId(),null);
                                            }
                                        }

                                        // 添加运营数据
                                        if (operateAccount != null){
                                            ComponentUtil.operateService.add(operateAccount);
                                        }

                                        if (wxClient.type.equals("1")){
                                            // 普通消息：包含2,3,7,8,9的类型
                                            // 具体类型说明：1初始化，《2其它》，《3发送固定指令3表示审核使用》，4加群信息，5发红包，6剔除成员，《7成功收款》，《8收款失败》，《9发送固定指令4表示暂停使用微信群》，10小微登入，11小微登出
                                            int dataType = TaskMethod.getWxDataTypeByOne(wxClient.content);

                                            if (collectionAccountType != 2){

                                                // 判断数据类型dataType=7，dataType=8，dataType=9是否是群主回复的
                                                if(dataType == 7 || dataType == 8 || dataType == 9){
                                                    if (!StringUtils.isBlank(wxClient.wxid1)){
                                                        if (!wxClient.wxid1.equals(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getUserId())){
                                                            // 数据不是群主回复的
                                                            dataType = 2;
                                                        }
                                                    }else {
                                                        dataType = 2;
                                                    }
                                                }

                                                CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisByWxData(wxClient, dataType, data.getId(), wxModel.getId(), did, collectionAccountId, collectionAccountType, 2);
                                                ComponentUtil.catDataAnalysisService.add(catDataAnalysisModel);

                                            }

                                            // 更新此次task的状态：更新成成功
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                            ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                                        }else if (wxClient.type.equals("10000")){
                                            // 系统消息：包含2,4,5,6的类型
                                            // 具体类型说明：1初始化，《2其它》，3发送固定指令3表示审核使用，《4加群信息》，《5发红包》，《6剔除成员》，7成功收款，8收款失败，9发送固定指令4表示暂停使用微信群，10小微登入，11小微登出
                                            int dataType = TaskMethod.getWxDataTypeByTenThousand(wxClient.content);

                                            // 判断是否是发红包的数据类型
                                            if (dataType == 5){
                                                // 需要扣减微信群红包的数量
                                                int isInvalid = 0;// 是否失效：1未失效，2已失效
                                                int packNum = didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getRedPackNum();
                                                int resNum = packNum - 1;
                                                if (resNum <= 0){
                                                    isInvalid = 2;
                                                }
                                                log.info("");
                                                DidCollectionAccountModel updateRedPackNum = TaskMethod.assembleDidCollectionAccountUpdateRedPackNumOrInvalid(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId(),1, isInvalid);
                                                ComponentUtil.didCollectionAccountService.updateDidCollectionAccountRedPackNumOrInvalid(updateRedPackNum);
                                            }else if(dataType == 6){
                                                // 微信数据踢人，这里只能检测到踢我方小微
                                                // 属于我放小微被移出群

                                                // 删除小微旗下店员的关联关系
                                                WxClerkModel wxClerkUpdate = TaskMethod.assembleWxClerkUpdate(wxModel.getId(), didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId());
                                                ComponentUtil.wxClerkService.updateWxClerkIsYn(wxClerkUpdate);

                                                // 根据找到的微信群收款账号，更新此收款账号的审核状态，更新成审核初始化
                                                DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdateCheckDataInfo(didCollectionAccountByWxGroupIdOrWxGroupNameAndYnModel.getId(), "检测：我方小微被剔除群");
                                                ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckData(didCollectionAccountUpdate);

                                            }

                                            CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisByWxData(wxClient, dataType, data.getId(), wxModel.getId(), did, collectionAccountId, collectionAccountType, 2);
                                            ComponentUtil.catDataAnalysisService.add(catDataAnalysisModel);

                                            // 更新此次task的状态：更新成成功
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                            ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);

                                        }else{
                                            // 不属于抓取范围数据
                                            // 更新此次task的状态：不属于抓取范围数据
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "不属于抓取范围数据");
                                            ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                                        }
                                    }else{
                                        // 更新此次task的状态：更新成失败-type等于1，但是根据log_wechatid查询小微账号数据为空
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新成失败-type等于1，但是根据log_wechatid查询小微账号数据为空");
                                        ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                                    }
                                }else{
                                    // 小微登入、小微登出
                                    // 根据小微ID查询小微信息
                                    WxModel wxQuery = TaskMethod.assembleWxModel(wxClient.log_wechatid);
                                    WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                                    if (wxModel != null && wxModel.getId() > 0){
                                        int dataType = 0;
                                        if (wxClient.type.equals("301")){
                                            // 登入
                                            dataType = 10;
                                        }else if (wxClient.type.equals("201")){
                                            // 登出
                                            dataType = 11;
                                        }else {
                                            dataType = 2;
                                        }

                                        CatDataAnalysisModel catDataAnalysisModel = TaskMethod.assembleCatDataAnalysisByWxData(wxClient, dataType, data.getId(), wxModel.getId(), 0, 0, 0, 2);
                                        ComponentUtil.catDataAnalysisService.add(catDataAnalysisModel);

                                        // 更新此次task的状态：更新成成功
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                        ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);

                                    }else{
                                        // 更新此次task的状态：更新成失败-type等于200/201，但是根据log_wechatid查询小微账号数据为空
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "更新成失败-type等于200/201，但是根据log_wechatid查询小微账号数据为空");
                                        ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                                    }

                                }

                            }else{
                                // 更新此次task的状态：更新成失败-小微ID数据为空
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "小微ID数据为空");
                                ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                            }
                        }
                    }else{
                        // 更新此次task的状态：更新成失败-json字段值为空
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "json字段值为空");
                        ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskWxAllData.wxAllData()----end");
            }catch (Exception e){
                log.error(String.format("this TaskWxAllData.wxAllData() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败-ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try：可能是json格式与需要的数据格式不一致");
                ComponentUtil.taskWxAllDataService.updateWxAllDataStatus(statusModel);
            }
        }
    }

}
