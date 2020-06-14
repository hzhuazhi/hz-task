package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.model.mobilecard.MobileCardDataModel;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.HodgepodgeMethod;
import com.hz.task.master.util.TaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 手机卡的短信信息处理的task
 * @Author yoko
 * @Date 2020/6/2 17:39
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskMobileCard {
    private final static Logger log = LoggerFactory.getLogger(TaskMobileCard.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;

    /**
     * @Description: 解析所有手机短信信息
     * <p>
     *     每5每秒运行一次
     *     1.拆解短信的类型：1广告短信，2挂失短信，3欠费短信，4普通短信（普通短信里面存在银行卡收款信息）
     *     2.短信类型为挂失短信，则需要对银行卡状态进行停用。
     *     3.当手机属于欠费短信：对手机卡的状态进行停用。
     *     4.当普通短信，则需要把数据添加到：银行卡收款回调数据表中。
     *     5.当广告短信则不作处理。
     *     6.手机号变更，则需要对手机号状态更改
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
    @Scheduled(fixedDelay = 5000) // 每5秒执行
    public void mobileCardData() throws Exception{
//        log.info("----------------------------------TaskMobileCard.mobileCardData()----start");
        // 策略：获取检查手机短信类型规则
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.MOBILE_CARD_TYPE_LIST.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        TaskMethod.checkStrategyByMobileCardTypeList(strategyModel);

        // 解析手机短信类型规则的值
        List<StrategyData> strategyDataList = JSON.parseArray(strategyModel.getStgBigValue(), StrategyData.class);

        // 获取手机短信数据
        StatusModel statusQuery = TaskMethod.assembleTaskByMobileCardDataQuery(limitNum);
        List<MobileCardDataModel> synchroList = ComponentUtil.taskMobileCardService.getMobileCardDataList(statusQuery);
        for (MobileCardDataModel data : synchroList){
            try{

                    // 锁住这个数据流水
                    String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_MOBILE_CARD_DATA, data.getId());
                    boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                    if (flagLock){
                        int type = 0;// 筛选之后的最终类型
                        type = TaskMethod.screenMobileCardDataType(data, strategyDataList);
                        if (type == 1){
                            // 广告短信， 无用处：直接更新状态
                            // 组装更改运行状态的数据：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, 1);
                            ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                        }else if (type == 2){
                            // 银行卡挂失：需要停掉那张银行卡，根据尾号以及端口号
                            // 具体要找出是哪一张银行卡，最好的方式是根据smsNum来把所有银行卡找出来；然后用银行卡部署的尾号，跟短信内容里面的尾号就行比较然后在进行银行卡的状态使用修改
                            // 这里为什么做的这么麻烦呢，因为每个银行发挂失的短信模板内容不同，导致无法精准的抓到短信内容里面的尾号，所以才这样做
                            BankModel bankQuery = TaskMethod.assembleBankQuery(data.getSmsNum());
                            List<BankModel> bankList = ComponentUtil.bankService.findByCondition(bankQuery);
                            if (bankList != null && bankList.size() > 0){
                                BankModel bankModel = TaskMethod.screenBankReportTheLossOf(bankList, data.getSmsContent());
                                if (bankModel == null || bankModel.getId() <= 0){
                                    // 更新此次task的状态：更新成失败：根据归属的短信端口号+短信类容的尾号没找到对应的银行卡
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2);
                                    ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                                }else {
                                    // 正式更新银行卡的使用状态
                                    bankModel.setUseStatus(2);
                                    bankModel.setRemark("检查：被挂失");
                                    int num = ComponentUtil.bankService.upUseStatus(bankModel);
                                    if (num > 0){
                                        // 组装更改运行状态的数据：更新成成功
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, 2);
                                        ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                                    }else {
                                        // 更新此次task的状态：更新成失败：因为没有更改到数据
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2);
                                        ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                                    }


                                }
                            }else {
                                // 更新此次task的状态：更新成失败：因为没找到银行卡
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2);
                                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                            }
                        }else if (type == 3){
                            // 手机卡欠费
                            // 暂停手机卡的使用
                            MobileCardModel mobileCardModel = TaskMethod.assembleUpMobileCardUseStatusData(data.getPhoneNum(), "检查：手机欠费");
                            int num = ComponentUtil.mobileCardService.upUseStatus(mobileCardModel);
                            if (num > 0){
                                // 组装更改运行状态的数据：更新成成功
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, 3);
                                log.info("");
                                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                            }else {
                                // 更新此次task的状态：更新成失败：因为没有更改到数据
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 3);
                                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                            }

                        }else if (type == 4){
                            // 银行卡短信
                            // 把数据组装到银行卡的表
                            BankCollectionDataModel bankCollectionDataModel = TaskMethod.assembleBankCollectionDataModel(data);
                            if (bankCollectionDataModel == null){
                                // 更新此次task的状态：更新成失败：因为必填项没数据
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 4);
                                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                            }else {
                                int num = ComponentUtil.bankCollectionDataService.add(bankCollectionDataModel);
                                if (num > 0){
                                    // 组装更改运行状态的数据：更新成成功
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, 4);
                                    log.info("1");
                                    ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                                }else {
                                    // 更新此次task的状态：更新成失败：因为没有更改到数据
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 4);
                                    ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                                }
                            }
                        }else if (type == 5){
                            // 手机卡变更
                            // 停掉手机卡
                            MobileCardModel mobileCardModel = TaskMethod.assembleUpMobileCardUseStatusData(data.getPhoneNum(), "检查：手机卡变更");
                            int num = ComponentUtil.mobileCardService.upUseStatus(mobileCardModel);
                            if (num > 0){
                                // 组装更改运行状态的数据：更新成成功
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, 5);
                                log.info("");
                                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                            }else {
                                // 更新此次task的状态：更新成失败：因为没有更改到数据
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 5);
                                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                            }
                        }else {
                            // 其它短信， 无用处：直接更新状态
                            // 组装更改运行状态的数据：更新成成功
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, 6);
                            ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
                        }
                        // 解锁
                        ComponentUtil.redisIdService.delLock(lockKey);
                    }


//                log.info("----------------------------------TaskMobileCard.mobileCardData()----end");
            }catch (Exception e){
                log.error(String.format("this TaskMobileCard.mobileCardData() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为必填项没数据
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByMobileCardDataModel(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 1);
                ComponentUtil.taskMobileCardService.updateMobileCardDataStatus(statusModel);
            }
        }

    }


    public static void main(String [] args){
        String str = "赢大奖，赚大钱";
        String keyStr = "赚,赢";
        String[] keyStrArr = keyStr.split(",");
        int num = 0;
        int count = 0;
        for (String key : keyStrArr){

            if (str.indexOf(key) > -1){
                System.out.println("符合");
                count ++;
            }
        }

    }

}
