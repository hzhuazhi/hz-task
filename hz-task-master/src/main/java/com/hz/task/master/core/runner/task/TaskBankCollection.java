package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;
import com.hz.task.master.core.model.bank.BankCollectionModel;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.model.did.DidRechargeModel;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.TaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description 银行卡回调信息处理的task
 * @Author yoko
 * @Date 2020/6/3 19:25
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskBankCollection {

    private final static Logger log = LoggerFactory.getLogger(TaskBankCollection.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;

    /**
     * @Description: 解析所有银行卡回调信息-进行数据的填补
     * <p>
     *     每1每秒运行一次
     *     1.通过手机号，确定出来手机号的主键ID。
     *     2.确定出来是哪张银行卡：通过手机号的主键ID，短信端口(sms_num)查询出这一类的银行卡；
     *     然后把银行卡集合for循环与短信内容中的银行卡尾号进行匹配，匹配上了，就能把银行卡确定出来。
     *     3.通过步骤2确定出来了具体的银行卡，然后根据银行卡的附带属性（sms_content）里面的关键字与《银行卡收款回调数据表》的短信内容（sms_content）进行关键字匹配；
     *     如果匹配的上，就代表此次的短信确认是“收款短信”；在继续根据银行卡的附带属性（start_key,end_key）来把《银行卡收款回调数据表》的短信内容（sms_content）的收款“金额”给截取出来；
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void bankCollectionDataWorkType() throws Exception{
//        log.info("----------------------------------TaskBankCollection.bankCollectionDataWorkType()----start");

        // 获取需要填充的银行回调数据
        StatusModel statusQuery = TaskMethod.assembleTaskBankCollectionDataByWorkTypeQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        List<BankCollectionDataModel> synchroList = ComponentUtil.taskBankCollectionService.getBankCollectionDataList(statusQuery);
        for (BankCollectionDataModel data : synchroList){
            try{
                    // 锁住这个数据流水
                    String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_BANK_COLLECTION_DATA_WORK_TYPE, data.getId());
                    boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                    if (flagLock){
                        MobileCardModel mobileCardQuery = TaskMethod.assembleMobileCardQueryByPhoneNum(data.getPhoneNum());
                        MobileCardModel mobileCardModel  = ComponentUtil.mobileCardService.getMobileCard(mobileCardQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
                        if (mobileCardModel == null || mobileCardModel.getId() <= 0){
                            // 没有查询到相对应的手机号：修改workType=2
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2, "没有查询到相对应的手机号");
                            ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                        }else {
                            BankModel bankQuery = TaskMethod.assembleBankByMobileCardAndSmsNumQuery(mobileCardModel.getId(), data.getSmsNum());
                            List<BankModel> bankList = ComponentUtil.bankService.findByCondition(bankQuery);
                            if (bankList == null || bankList.size() <= 0){
                                // 没有查询到相对应的银行卡：修改workType=2
                                StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2, "没有查询到相对应的银行卡");
                                ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                            }else {
                                // 进行银行卡数据匹配
                                BankModel bankModel = TaskMethod.bankMatchingByLastNum(bankList, data.getSmsContent());
                                if (bankModel == null || bankModel.getId() <= 0){
                                    // 根据银行卡尾号匹配短信内容中的尾号没有对应的银行卡数据：修改workType=2
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2, "根据银行卡尾号匹配短信内容中的尾号没有对应的银行卡数据");
                                    ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                                }else{
                                    // 银行卡回传数据填充
                                    BankCollectionDataModel bankCollectionDataModel = TaskMethod.assembleBankCollectionData(bankModel, data, mobileCardModel.getId());
                                    if (bankCollectionDataModel == null || bankCollectionDataModel.getId() <= 0){
                                        // 没有匹配到想要的补充数据：修改workType=2
                                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 2, "没有匹配到想要的补充数据");
                                        ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                                    }else {
                                        int num = ComponentUtil.bankCollectionDataService.update(bankCollectionDataModel);
                                        if (num > 0){
                                            // 组装更改运行状态的数据：更新成成功：修改workType=3
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, 3, "");
                                            ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                                        }else {
                                            // 更新此次task的状态：更新成失败：因为没有更改到数据
                                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 1, "因为没有更改到数据 update响应影响行为0");
                                            ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                                        }
                                    }
                                }
                            }
                        }
                        // 解锁
                        ComponentUtil.redisIdService.delLock(lockKey);
                    }

//                log.info("----------------------------------TaskBankCollection.bankCollectionDataWorkType()----end");
            }catch (Exception e){
                log.error(String.format("this TaskBankCollection.bankCollectionDataWorkType() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为没有更改到数据
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataWorkType(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, 1, "异常失败try!");
                ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
            }
        }
    }






    /**
     * @Description: task：根据银行卡已补充完毕的数据进行匹配用户充值订单
     * <p>
     *     每1每秒运行一次
     *     1.根据银行卡回调数据的银行卡ID（bank_id）去找出用户发起的充值订单有效期内的订单数据。
     *     2.for循环用户有效期内的订单数据，匹配金额是否与银行回调数据的金额一致。
     *     3.如果银行卡以及金额一致，修改用户充值订单的状态。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每秒执行
    @Scheduled(fixedDelay = 60000) // 每分钟执行
    public void bankCollectionDataByOrder() throws Exception{
//        log.info("----------------------------------TaskBankCollection.bankCollectionDataByOrder()----start");
        // 获取需要填充的银行回调数据
        StatusModel statusQuery = TaskMethod.assembleTaskBankCollectionDataByOrderQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
        List<BankCollectionDataModel> synchroList = ComponentUtil.taskBankCollectionService.getBankCollectionDataList(statusQuery);
        for (BankCollectionDataModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_BANK_COLLECTION_DATA_WORK_TYPE_IS_OK, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    DidRechargeModel didRechargeModel = TaskMethod.assembleDidRechargeQuery(data.getBankId(), 2);
                    List<DidRechargeModel> didRechargeList = ComponentUtil.didRechargeService.findByCondition(didRechargeModel);

                    if (didRechargeList == null || didRechargeList.size() <= 0){
                        // 根据银行卡订单状态有效时间查询用户充值数据为空：修改成失败状态
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataByOrder(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据银行卡订单状态有效时间查询用户充值数据为空");
                        ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                    }else {
                        for (DidRechargeModel dataModel : didRechargeList){
                            // 符合条件的充值订单循环比较银行收款金额一致的数据
                            // A.首先加锁
                            String lockKey_did_recharge = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_RECHARGE, dataModel.getId());
                            boolean flagLock_did_recharge = ComponentUtil.redisIdService.lock(lockKey_did_recharge);
                            if (flagLock_did_recharge){
                                // 银行卡回调数据的金额与用户充值的金额进行相减，得出结果是否等于0
                                String result = StringUtil.getBigDecimalSubtractByStr(dataModel.getDistributionMoney(), data.getMoney());
                                if (result.equals("0")){
                                    // 表示金额匹配：1.修改用户充值的订单状态：修改成成功
                                    // 2.把银行卡收款信息录入到表tb_fn_bank_collection中（当然这里稍微会有漏的，但是可以忽略不计；漏的地方是：确实是收款金额，但是没有匹配到用户充值订单）
                                    // 3.把匹配的订单号修改到tb_fn_bank_collection_data表中
                                    DidRechargeModel didRechargeUpdate = TaskMethod.assembleDidRechargeUpdateStatus(dataModel.getId(), 3);
                                    ComponentUtil.didRechargeService.update(didRechargeUpdate);

                                    BankCollectionModel bankCollectionModel = TaskMethod.assembleBankCollectionAdd(dataModel.getBankId(), dataModel.getOrderNo(), dataModel.getDistributionMoney());
                                    ComponentUtil.bankCollectionService.add(bankCollectionModel);

                                    BankCollectionDataModel bankCollectionDataUpdate = TaskMethod.assembleBankCollectionDataUpdate(data.getId(), dataModel.getOrderNo());
                                    ComponentUtil.bankCollectionDataService.update(bankCollectionDataUpdate);
                                    num = 1;
                                    break;
                                }
                            }

                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_did_recharge);
                        }

                        if (num > 0){
                            // 有匹配到订单：修改成成功状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataByOrder(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                            ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                        }else{
                            // 没有匹配到订单：修改成失败状态
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataByOrder(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "没有匹配到用户充值订单");
                            ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
                        }
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskBankCollection.bankCollectionDataWorkType()----end");
            }catch (Exception e){
                log.error(String.format("this TaskBankCollection.bankCollectionDataWorkType() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为没有更改到数据
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByBankCollectionDataByOrder(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO,"异常失败try!");
                ComponentUtil.taskBankCollectionService.updateBankCollectionDataStatus(statusModel);
            }
        }
    }



}
