package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatAllDataModel;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.cat.CatMsg;
import com.hz.task.master.core.model.task.cat.FromCatModel;
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

/**
 * @Description task：可爱猫回调原始数据
 * @Author yoko
 * @Date 2020/6/6 15:22
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskCatAllData {
    private final static Logger log = LoggerFactory.getLogger(TaskCatAllData.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：解析可爱猫的原始数据
     * <p>
     *     每1每秒运行一次
     *     1.查询出未未解析的可爱猫数据。
     *     2.抓出属于转账支付的数据。
     *     3.把数据录入到可爱猫回调订单表里面。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void catAllData() throws Exception{
//        log.info("----------------------------------TaskCatAllData.catAllData()----start");
        // 获取需要解析的可爱猫原始数据
        StatusModel statusQuery = TaskMethod.assembleTaskStatusQuery(limitNum);
        List<CatAllDataModel> synchroList = ComponentUtil.taskCatAllDataService.getCatAllDataList(statusQuery);
        for (CatAllDataModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CAT_ALL_DATA, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (!StringUtils.isBlank(data.getJsonData())){
//                        FromCatModel fromCatModel = JSON.parseObject(data.getJsonData(), FromCatModel.class);
                        List<FromCatModel> fromCatList = JSON.parseArray(data.getJsonData(), FromCatModel.class);
                        if (fromCatList == null || fromCatList.size() <= 0){
                            // 更新此次task的状态：更新成失败-json解析后数据为空
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "json解析后数据为空");
                            ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                        }else{
                            for (FromCatModel fromCatModel : fromCatList){
                                if (fromCatModel.getType().equals("600")){
                                    if (!StringUtils.isBlank(fromCatModel.getMsg())){
                                        CatMsg catMsg = JSON.parseObject(fromCatModel.getMsg(), CatMsg.class);
                                        // 组装可爱猫回调订单数据
                                        CatDataModel catDataModel = TaskMethod.assembleCatDataAddData(catMsg, data.getId());
                                        if (catDataModel == null){
                                            // 更新此次task的状态：更新成失败-可爱猫原始数据中必要字段为空
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "可爱猫原始数据中必要字段为空");
                                            ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                        }else{
                                            num = ComponentUtil.catDataService.add(catDataModel);
                                            if (num > 0){
                                                // 更新此次task的状态：更新成成功
                                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                                ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                            }else {
                                                // 更新此次task的状态：更新成失败-添加可爱猫回调订单数据响应行为0
                                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "添加可爱猫回调订单数据响应行为0");
                                                ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                            }
                                        }

                                    }else{
                                        // 更新此次task的状态：更新成失败-msg数据为空
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "msg数据为空");
                                        ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                    }
                                }else if(fromCatModel.getType().equals("100")){
                                    if (!StringUtils.isBlank(fromCatModel.getMsg())){
                                        // 获取短信猫回传数据是否符合小微店员下线的通知数据
                                        String wxName = TaskMethod.getWxNameByCatData(fromCatModel.getMsg());
                                        if (!StringUtils.isBlank(wxName)){
                                            // 根据可爱猫的robot_wxid查询小微的主键ID
                                            WxModel wxQuery = TaskMethod.assembleWxModel(fromCatModel.getRobot_wxid());
                                            WxModel wxModel = (WxModel) ComponentUtil.wxService.findByObject(wxQuery);
                                            if (wxModel != null && wxModel.getId() > 0){
                                                long did = 0;// 用户ID
                                                long didCollectionAccountId = 0;// 收款账号ID
                                                // 根据小微主键ID查询小微旗下的收款账号信息
                                                WxClerkModel wxClerkQuery = TaskMethod.assembleWxClerkQuery(wxModel.getId());
                                                List<WxClerkModel> wxClerkList = ComponentUtil.wxClerkService.findByCondition(wxClerkQuery);
                                                if (wxClerkList != null && wxClerkList.size() > 0){
                                                    for (WxClerkModel wxClerkModel : wxClerkList){
                                                        // 循环根据收款账号ID加微信名称查询收款账号信息
                                                        DidCollectionAccountModel didCollectionAccountQuery = TaskMethod.assembleDidCollectionAccountQuery(wxClerkModel.getCollectionAccountId(), wxName);
                                                        DidCollectionAccountModel didCollectionAccountModel = (DidCollectionAccountModel)ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountQuery);
                                                        if (didCollectionAccountModel != null && didCollectionAccountModel.getId() > 0){
                                                            did = didCollectionAccountModel.getDid();
                                                            didCollectionAccountId = didCollectionAccountModel.getId();
                                                            break;
                                                        }
                                                    }
                                                    int matchingType = 0;// 数据匹配的类型：1根据小微ID跟微信昵称不能匹配到收款账号，2根据小微ID跟微信昵称能匹配到收款账号
                                                    if (didCollectionAccountId > 0){
                                                        matchingType = 2;
                                                        // 并且立刻锁住这个收款账号，让其它派单的地方无法使用：无需去解锁
                                                        String lockKey_did_collection_account = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_FOR, didCollectionAccountId);
                                                        ComponentUtil.redisIdService.lock(lockKey_did_collection_account);

                                                        // 添加收款账号的下线纪录
                                                        WxClerkDataModel wxClerkDataModel = TaskMethod.assembleWxClerkData(wxModel.getId(), did, didCollectionAccountId, 3);
                                                        ComponentUtil.wxClerkDataService.add(wxClerkDataModel);

                                                    }else{
                                                        matchingType = 1;
                                                    }



                                                    // 组装可爱猫回调店员下线：小微旗下店员下线通知；取消与小微绑定关系的信息；并且添加数据
                                                    CatDataOfflineModel catDataOfflineModel = TaskMethod.assembleCatDataOffline(data.getId(), wxModel.getId(), wxModel.getToWxid(), wxName, didCollectionAccountId, matchingType);
                                                    int catDataOfflineNum = ComponentUtil.catDataOfflineService.add(catDataOfflineModel);
                                                    if (catDataOfflineNum > 0){
                                                        // 更新此次task的状态：更新成成功
                                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                                        ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                                    }else {
                                                        // 更新此次task的状态：更新成失败-添加可爱猫回调店员下线响应行为0
                                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "添加可爱猫回调店员下线响应行为0");
                                                        ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                                    }

                                                }else {
                                                    // 此小微旗下没有店员
                                                    // 更新此次task的状态：更新成失败-根据小微的wxId查询旗下店员数据为空
                                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据小微的wxId查询旗下店员数据为空");
                                                    ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                                }


                                            }else {
                                                // 更新此次task的状态：更新成失败-根据可爱猫的robot_wxid查询小微数据为空
                                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "可爱猫的robot_wxid查询小微数据为空");
                                                ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                            }


                                            // 更新此次task的状态：更新成成功
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                            ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                        }else {
                                            // 更新此次task的状态：更新成失败-type等于100，但不是下线消息
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "type等于100，但不是下线消息");
                                            ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                        }

                                    }else {
                                        // 更新此次task的状态：更新成失败-type等于100，但是msg数据为空
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "type等于100，但是msg数据为空");
                                        ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                    }

                                }else {
                                    // 更新此次task的状态：更新成失败-type不等于600或者不等于100
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "type不等于600也不等于100");
                                    ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                                }
                            }
                        }
                    }else{
                        // 更新此次task的状态：更新成失败-json字段值为空
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "json字段值为空");
                        ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskCatAllData.catAllData()----end");
            }catch (Exception e){
                log.error(String.format("this TaskCatAllData.catAllData() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败-ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try：可能是json格式与需要的数据格式不一致");
                ComponentUtil.taskCatAllDataService.updateCatAllDataStatus(statusModel);
            }
        }
    }


    public static void main(String [] args) throws Exception{
        String json ="[{\"final_from_name\":\"老只\",\"final_from_wxid\":\"wxid_b3fdfn1xz7ou12\",\"from_name\":\"老只\",\"from_wxid\":\"wxid_b3fdfn1xz7ou12\",\"msg\":\"哈哈哈\",\"msg_type\":\"1\",\"rid\":\"10026\",\"robot_wxid\":\"wxid_b3fdfn1xz7ou12\",\"time\":\"1590832884\",\"type\":\"100\"}]";
//        FromCatModel fromCatModel = JSON.parseObject(json, FromCatModel.class);
//        FromCatModel fromCatModel = JSON.parseObject(json, FromCatModel.class);
        List<FromCatModel> fromCatList = JSON.parseArray(json, FromCatModel.class);
        System.out.println("fromCatList:" + fromCatList.size());

    }

}
