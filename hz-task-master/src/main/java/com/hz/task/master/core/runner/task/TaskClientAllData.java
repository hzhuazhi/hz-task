package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatAllDataModel;
import com.hz.task.master.core.model.cat.CatDataBindingModel;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.client.ClientAllDataModel;
import com.hz.task.master.core.model.client.ClientCollectionDataModel;
import com.hz.task.master.core.model.client.ClientDataModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.strategy.StrategyZfbMoneyRule;
import com.hz.task.master.core.model.strategy.StrategyZfbRule;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.cat.CatMsg;
import com.hz.task.master.core.model.task.cat.FromCatModel;
import com.hz.task.master.core.model.task.client.ClientModel;
import com.hz.task.master.core.model.wx.WxClerkDataModel;
import com.hz.task.master.core.model.wx.WxClerkModel;
import com.hz.task.master.core.model.wx.WxModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.HodgepodgeMethod;
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
 * @Description task:客户端监听数据回调订单
 * @Author yoko
 * @Date 2020/7/6 18:41
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskClientAllData {
    private final static Logger log = LoggerFactory.getLogger(TaskClientAllData.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: task：解析客户端监听数据回调原始数据
     * <p>
     *     每1每秒运行一次
     *     1.查询出未未解析的客户端监听数据回调原始数据。
     *     2.抓出属于转账支付的数据。
     *     3.把数据录入到客户端监听数据回调订单表里面。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void clientAllData() throws Exception{
//        log.info("----------------------------------TaskClientAllData.clientAllData()----start");

        // 策略：检测定位客户端监听的数据是否属于支付宝转账规则配置
        StrategyModel strategyZfbRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ZFB_RULE.getStgType());
        StrategyModel strategyZfbRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyZfbRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        StrategyZfbRule strategyZfbRule = JSON.parseObject(strategyZfbRuleModel.getStgValue(), StrategyZfbRule.class);

        // 策略：检测截取客户端监听的数据中支付宝转账的具体金额规则配置
        StrategyModel strategyZfbMoneyRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ZFB_MONEY_RULE.getStgType());
        StrategyModel strategyZfbMoneyRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyZfbMoneyRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        StrategyZfbMoneyRule strategyZfbMoneyRule = JSON.parseObject(strategyZfbMoneyRuleModel.getStgValue(), StrategyZfbMoneyRule.class);

        // 获取需要解析的客户端监听数据回调原始数据
        StatusModel statusQuery = TaskMethod.assembleTaskStatusQuery(limitNum);
        List<ClientAllDataModel> synchroList = ComponentUtil.taskClientAllDataService.getClientAllDataList(statusQuery);
        for (ClientAllDataModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_CLIENT_ALL_DATA, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    if (!StringUtils.isBlank(data.getJsonData())){
                        ClientModel clientModel = JSON.parseObject(data.getJsonData(), ClientModel.class);
//                        List<ClientModel> clientList = JSON.parseArray(data.getJsonData(), ClientModel.class);
                        if (clientModel == null || StringUtils.isBlank(clientModel.getContent())){
                            // 更新此次task的状态：更新成失败-json解析后数据为空
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "json解析后数据为空");
                            ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                        }else{
                            boolean flag_client = TaskMethod.checkClientModel(clientModel);
                            if (flag_client){
                                // 判断是否是支付宝收款信息
                                boolean flag_zfb_rule = TaskMethod.checkZfbData(clientModel.getContent(), strategyZfbRule);
                                if (flag_zfb_rule){
                                    //查询此任务是否已添加过
                                    ClientCollectionDataModel clientCollectionDataQuery = TaskMethod.assembleClientCollectionDataByAllId(data.getId());
                                    ClientCollectionDataModel clientCollectionData = (ClientCollectionDataModel) ComponentUtil.clientCollectionDataService.findByObject(clientCollectionDataQuery);
                                    if (clientCollectionData == null || clientCollectionData.getId() <= 0){
                                        // 根据支付宝账号userId查询用户did
                                        DidCollectionAccountModel didCollectionAccountQuery = TaskMethod.assembleDidCollectionAccountByUserIdQuery(clientModel.getToken());
                                        DidCollectionAccountModel didCollectionAccountModel = (DidCollectionAccountModel) ComponentUtil.didCollectionAccountService.findByObject(didCollectionAccountQuery);
                                        if (didCollectionAccountModel != null && didCollectionAccountModel.getId() > 0){
                                            // 添加客户端监听的收款信息的数据
                                            ClientCollectionDataModel clientCollectionDataModel = TaskMethod.assembleClientCollectionDataAdd(data.getId(), clientModel, didCollectionAccountModel.getDid(), data.getJsonData());
                                            ComponentUtil.clientCollectionDataService.add(clientCollectionDataModel);
                                        }
                                    }

                                    // 截取收款金额
                                    String money = TaskMethod.getZfbMoney(clientModel.getContent(), strategyZfbMoneyRule);
                                    if (!StringUtils.isBlank(money)){
                                        // 组装添加客户端监听数据回调订单的数据
                                        ClientDataModel clientDataAdd = TaskMethod.assembleClientDataModel(clientModel, money, data.getId());
                                        num = ComponentUtil.clientDataService.add(clientDataAdd);
                                        if (num > 0){
                                            // 更新此次task的状态：更新成成功
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                            ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                                        }else {
                                            // 更新此次task的状态：更新成失败-添加客户端监听数据回调订单数据响应行为0
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "添加客户端监听数据回调订单数据响应行为0");
                                            ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                                        }
                                    }else {
                                        // 更新此次task的状态：更新成失败-无法截取到金额
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "无法截取到金额");
                                        ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                                    }
                                }else {
                                    // 更新此次task的状态：更新成失败-不属于支付宝收款信息
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "不属于支付宝收款信息");
                                    ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                                }

                            }else {
                                // 更新此次task的状态：更新成失败-必填数据为空
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "必填数据为空");
                                ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                            }
                        }
                    }else{
                        // 更新此次task的状态：更新成失败-json字段值为空
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "json字段值为空");
                        ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskClientAllData.clientAllData()----end");
            }catch (Exception e){
                log.error(String.format("this TaskClientAllData.clientAllData() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败-ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByInfo(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try：可能是json格式与需要的数据格式不一致");
                ComponentUtil.taskClientAllDataService.updateClientAllDataStatus(statusModel);
            }
        }
    }


}
