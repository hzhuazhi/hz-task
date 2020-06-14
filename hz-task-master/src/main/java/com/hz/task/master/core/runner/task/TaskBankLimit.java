package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.model.bank.BankTransferModel;
import com.hz.task.master.core.model.did.DidRechargeModel;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.util.ComponentUtil;
import com.hz.task.master.util.TaskMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Description task:银行卡流水限制的类
 * @Author yoko
 * @Date 2020/6/13 17:49
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskBankLimit {

    private final static Logger log = LoggerFactory.getLogger(TaskBankCollection.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * @Description: 计算银行卡日月总的收款金额，转账金额
     * <p>
     *     每1分钟运行一次
     *     1.查询未停止使用的银行卡信息。
     *     2.根据银行卡集合for循环查询充值订单（按照日期，银行卡，订单状态初始化，订单状态成功）查询订单金额总和，然后进行限制金额的比较
     *     3.根据银行卡集合for循环查询银行卡转账记录，然后进行限制金额的比较。
     *     4.当现跑金额（充值金额，转账金额）超过设定的金额，则进行银行卡开关状态的修改。
     *     5.redis删除缓存或者添加缓存（日月总金额）。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
    @Scheduled(fixedDelay = 60000) // 每1分钟执行
//    @Scheduled(fixedDelay = 1000) // 每1分钟执行
    public void bankMoney() throws Exception{
//        log.info("----------------------------------TaskBankLimit.bankMoney()----start");
        int curday = DateUtil.getDayNumber(new Date());// 当天
        int curdayStart = DateUtil.getMinMonthDate();// 月初
        int curdayEnd = DateUtil.getMaxMonthDate();// 月末
        // 获取银行数据
        BankModel bankQuery = TaskMethod.assembleBankQuery();
        List<BankModel> synchroList = ComponentUtil.taskBankLimitService.getBankDataList(bankQuery);
        for (BankModel data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_BANK, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // check日收款，日转账
                    if (!StringUtils.isBlank(data.getInDayMoney()) || !StringUtils.isBlank(data.getOutDayMoney())){
                        int inMoney = 0;// 收款金额是否超过限制：超过了设定的限制则大于0
                        int outMoney = 0;// 转账金额是否超过限制：超过了设定的限制则大于0
                        // 需要check日收款金额
                        if (!StringUtils.isBlank(data.getInDayMoney())){
                            // 收款金额需要check
                            DidRechargeModel rechargeQuery = TaskMethod.assembleDidRechargeByBankIdQuery(data.getId(), curday, 0, 0);
                            String money = ComponentUtil.didRechargeService.getRechargeMoney(rechargeQuery);
                            boolean flag = StringUtil.getBigDecimalSubtract(data.getInDayMoney(), money);//  true =(设定金额 - 已经收款金额) > 0； false = (设定金额 - 已经收款金额) < 0
                            if (!flag){
                                inMoney = 1;
                            }
                        }
                        // 需要check日转账金额
                        if (!StringUtils.isBlank(data.getOutDayMoney())){
                            // 转账金额需要check
                            BankTransferModel transferQuery = TaskMethod.assembleBankTransferByBankIdQuery(data.getId(), curday, 0, 0);
                            String money = ComponentUtil.bankTransferService.getBankTransferMoney(transferQuery);
                            boolean flag = StringUtil.getBigDecimalSubtract(data.getOutDayMoney(), money);//  true =(设定金额 - 转账金额) > 0； false = (设定金额 - 转账金额) < 0
                            if (!flag){
                                outMoney = 1;
                            }
                        }
                        if (inMoney > 0 || outMoney> 0){
                            String limitInfo = "";
                            if (inMoney > 0 && outMoney > 0){
                                limitInfo = "日收款，日转账都已经达到上限";
                                // 缓存设置：设置日收款金额
                                String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_DAY, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_in, data.getInDayMoney() , FIVE_MIN);
                                // 缓存设置：设置日转账金额
                                String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_DAY, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_out, data.getOutDayMoney() , FIVE_MIN);
                            }else if(inMoney > 0 && outMoney == 0){
                                limitInfo = "日收款达到上限";
                                // 缓存设置：设置日收款金额
                                String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_DAY, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_in, data.getInDayMoney() , FIVE_MIN);
                            }else if(inMoney == 0 && outMoney > 0){
                                limitInfo = "日转账达到上限";
                                // 缓存设置：设置日转账金额
                                String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_DAY, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_out, data.getOutDayMoney() , FIVE_MIN);
                            }
                            // 日的开关修改成暂停状态（关闭状态）
                            BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 2,0,0, limitInfo);
                            ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                        }else{
                            // 删除缓存：日收款
                            String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_DAY, data.getId());
                            ComponentUtil.redisService.remove(strKeyCache_in);
                            // 删除缓存：日转账
                            String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_DAY, data.getId());
                            ComponentUtil.redisService.remove(strKeyCache_out);
                            log.info("1");
                            // 日的开关修改成开启状态（使用状态）
                            BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 1,0,0, "");
                            ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                        }
                    }else {
                        // 删除缓存：日收款
                        String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_DAY, data.getId());
                        ComponentUtil.redisService.remove(strKeyCache_in);
                        // 删除缓存：日转账
                        String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_DAY, data.getId());
                        ComponentUtil.redisService.remove(strKeyCache_out);
                        // 日的开关修改成开启状态（使用状态）
                        BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 1,0,0, "");
                        ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                    }


                    // check月收款，月转账
                    if (!StringUtils.isBlank(data.getInMonthMoney()) || !StringUtils.isBlank(data.getOutMonthMoney())){
                        int inMoney = 0;// 收款金额是否超过限制：超过了设定的限制则大于0
                        int outMoney = 0;// 转账金额是否超过限制：超过了设定的限制则大于0
                        // 需要check月收款金额
                        if (!StringUtils.isBlank(data.getInMonthMoney())){
                            // 收款金额需要check
                            DidRechargeModel rechargeQuery = TaskMethod.assembleDidRechargeByBankIdQuery(data.getId(), 0, curdayStart, curdayEnd);
                            String money = ComponentUtil.didRechargeService.getRechargeMoney(rechargeQuery);
                            boolean flag = StringUtil.getBigDecimalSubtract(data.getInMonthMoney(), money);//  true =(设定金额 - 已经收款金额) > 0； false = (设定金额 - 已经收款金额) < 0
                            if (!flag){
                                inMoney = 1;
                            }
                        }
                        // 需要check月转账金额
                        if (!StringUtils.isBlank(data.getOutMonthMoney())){
                            // 转账金额需要check
                            BankTransferModel transferQuery = TaskMethod.assembleBankTransferByBankIdQuery(data.getId(), 0, curdayStart, curdayEnd);
                            String money = ComponentUtil.bankTransferService.getBankTransferMoney(transferQuery);
                            boolean flag = StringUtil.getBigDecimalSubtract(data.getOutMonthMoney(), money);//  true =(设定金额 - 转账金额) > 0； false = (设定金额 - 转账金额) < 0
                            if (!flag){
                                outMoney = 1;
                            }
                        }
                        if (inMoney > 0 || outMoney> 0){
                            String limitInfo = "";
                            if (inMoney > 0 && outMoney > 0){
                                limitInfo = "月收款，月转账都已经达到上限";
                                // 缓存设置：设置月收款金额
                                String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_MONTH, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_in, data.getInMonthMoney() , FIVE_MIN);
                                // 缓存设置：设置月转账金额
                                String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_MONTH, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_out, data.getOutMonthMoney() , FIVE_MIN);
                            }else if(inMoney > 0 && outMoney == 0){
                                limitInfo = "月收款达到上限";
                                // 缓存设置：设置月收款金额
                                String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_MONTH, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_in, data.getInMonthMoney() , FIVE_MIN);
                            }else if(inMoney == 0 && outMoney > 0){
                                limitInfo = "月转账达到上限";
                                // 缓存设置：设置月转账金额
                                String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_MONTH, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_out, data.getOutMonthMoney() , FIVE_MIN);
                            }
                            // 月的开关修改成暂停状态（关闭状态）
                            BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 0,2,0, limitInfo);
                            ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                        }else{
                            // 删除缓存：月收款
                            String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_MONTH, data.getId());
                            ComponentUtil.redisService.remove(strKeyCache_in);
                            // 删除缓存：月转账
                            String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_MONTH, data.getId());
                            log.info("1");
                            ComponentUtil.redisService.remove(strKeyCache_out);
                            // 月的开关修改成开启状态（使用状态）
                            BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 0,1,0, "");
                            ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                        }
                    }else {
                        // 删除缓存：月收款
                        String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_MONTH, data.getId());
                        ComponentUtil.redisService.remove(strKeyCache_in);
                        // 删除缓存：月转账
                        String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_MONTH, data.getId());
                        ComponentUtil.redisService.remove(strKeyCache_out);
                        // 月的开关修改成开启状态（使用状态）
                        BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 0,1,0, "");
                        ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                    }

                    // check总收款，总转账
                    if (!StringUtils.isBlank(data.getInTotalMoney()) || !StringUtils.isBlank(data.getOutTotalMoney())){
                        int inMoney = 0;// 收款金额是否超过限制：超过了设定的限制则大于0
                        int outMoney = 0;// 转账金额是否超过限制：超过了设定的限制则大于0
                        // 需要check总收款金额
                        if (!StringUtils.isBlank(data.getInTotalMoney())){
                            // 收款金额需要check
                            DidRechargeModel rechargeQuery = TaskMethod.assembleDidRechargeByBankIdQuery(data.getId(), 0, 0, 0);
                            String money = ComponentUtil.didRechargeService.getRechargeMoney(rechargeQuery);
                            boolean flag = StringUtil.getBigDecimalSubtract(data.getInTotalMoney(), money);//  true =(设定金额 - 已经收款金额) > 0； false = (设定金额 - 已经收款金额) < 0
                            if (!flag){
                                inMoney = 1;
                            }
                        }
                        // 需要check总转账金额
                        if (!StringUtils.isBlank(data.getOutTotalMoney())){
                            // 转账金额需要check
                            BankTransferModel transferQuery = TaskMethod.assembleBankTransferByBankIdQuery(data.getId(), 0, 0, 0);
                            String money = ComponentUtil.bankTransferService.getBankTransferMoney(transferQuery);
                            boolean flag = StringUtil.getBigDecimalSubtract(data.getOutTotalMoney(), money);//  true =(设定金额 - 转账金额) > 0； false = (设定金额 - 转账金额) < 0
                            if (!flag){
                                outMoney = 1;
                            }
                        }
                        if (inMoney > 0 || outMoney> 0){
                            String limitInfo = "";
                            if (inMoney > 0 && outMoney > 0){
                                limitInfo = "总收款，总转账都已经达到上限";
                                // 缓存设置：设置总收款金额
                                String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_TOTAL, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_in, data.getInTotalMoney() , FIVE_MIN);
                                // 缓存设置：设置总转账金额
                                String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_TOTAL, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_out, data.getOutTotalMoney() , FIVE_MIN);
                            }else if(inMoney > 0 && outMoney == 0){
                                limitInfo = "总收款达到上限";
                                // 缓存设置：设置总收款金额
                                String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_TOTAL, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_in, data.getInTotalMoney() , FIVE_MIN);
                            }else if(inMoney == 0 && outMoney > 0){
                                limitInfo = "总转账达到上限";
                                // 缓存设置：设置总转账金额
                                String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_TOTAL, data.getId());
                                ComponentUtil.redisService.set(strKeyCache_out, data.getOutTotalMoney() , FIVE_MIN);
                            }
                            // 总的开关修改成暂停状态（关闭状态）
                            BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 0,0,2, limitInfo);
                            ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                        }else{
                            // 删除缓存：总收款
                            String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_TOTAL, data.getId());
                            ComponentUtil.redisService.remove(strKeyCache_in);
                            // 删除缓存：总转账
                            String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_TOTAL, data.getId());
                            log.info("1");
                            ComponentUtil.redisService.remove(strKeyCache_out);
                            // 总的开关修改成开启状态（使用状态）
                            BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 0,0,1, "");
                            ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                        }
                    }else {
                        // 删除缓存：总收款
                        String strKeyCache_in = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_IN_MONEY_TOTAL, data.getId());
                        ComponentUtil.redisService.remove(strKeyCache_in);
                        // 删除缓存：总转账
                        String strKeyCache_out = CachedKeyUtils.getCacheKey(CacheKey.SHARE_BANK_OUT_MONEY_TOTAL, data.getId());
                        ComponentUtil.redisService.remove(strKeyCache_out);
                        // 总的开关修改成开启状态（使用状态）
                        BankModel bankUpdateSwitch = TaskMethod.assembleBankSwitchUpdate(data.getId(), 0,0,1, "");
                        ComponentUtil.taskBankLimitService.updateBankSwitch(bankUpdateSwitch);
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskBankLimit.bankMoney()----end");
            }catch (Exception e){
                log.error(String.format("this TaskBankLimit.bankMoney() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
            }
        }
    }






}
