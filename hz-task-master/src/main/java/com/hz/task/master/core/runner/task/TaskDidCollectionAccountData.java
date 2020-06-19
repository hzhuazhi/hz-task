package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.cat.CatDataBindingModel;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.did.DidCollectionAccountQrCodeModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description task：检测用户收款账号给出码以及成功的信息
 * @Author yoko
 * @Date 2020/6/19 10:27
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskDidCollectionAccountData {

    private final static Logger log = LoggerFactory.getLogger(TaskDidCollectionAccountData.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;


    /**
     * @Description: 查询所有给出的收款账号的次数
     * <p>
     *     每1分钟运行一次
     *     1.查询今日所有给出码的收款码集合
     *     2.for循环查询每个收款账号给出的次数
     *     3.根据策略部署数据，进行次数比较，如果给码次数已经超过部署的策略的给码次数，则纪录redis缓存；
     *     redis缓存Key：LOCK_DID_COLLECTION_ACCOUNT_DAY_LIMIT_NUM
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每1分钟执行
    @Scheduled(fixedDelay = 60000) // 每1分钟执行
    public void countNum() throws Exception{
//        log.info("----------------------------------TaskDidCollectionAccountData.countNum()----start");
        int curday = DateUtil.getDayNumber(new Date());
        // 查询策略里面的微信收款二维码日限制规则列表
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.QC_CODE_LIMIT_LIST.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        // 解析微信收款二维码日限制规则列表的值
        List<StrategyData> qcCodeLimitList = JSON.parseArray(strategyModel.getStgBigValue(), StrategyData.class);
        // 获取未填充可爱猫回调店员绑定小微的数据
        TaskDidCollectionAccountDataModel taskDidCollectionAccountDataQuery = TaskMethod.assembleTaskDidCollectionAccountData(0, 0, curday, 0, 0);
        List<Long> synchroList = ComponentUtil.taskDidCollectionAccountDataService.getDidCollectionAccountList(taskDidCollectionAccountDataQuery);
        for (Long data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_COLLECTION_ACCOUNT_LIMIT_NUM, data);
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 查询此收款码已给出次数
                    TaskDidCollectionAccountDataModel limitNumQuery = TaskMethod.assembleTaskDidCollectionAccountData(0, data, curday, 0, 0);
                    int num = ComponentUtil.taskDidCollectionAccountDataService.countLimitNum(limitNumQuery);
//                    num = 100;
                    // 查询这个收款账号的二维码信息
                    DidCollectionAccountQrCodeModel didCollectionAccountQrCodeModel = TaskMethod.assembleDidCollectionAccountQrCode(data);
                    List<DidCollectionAccountQrCodeModel> didCollectionAccountQrCodeList = ComponentUtil.didCollectionAccountQrCodeService.findByCondition(didCollectionAccountQrCodeModel);
                    if (didCollectionAccountQrCodeList != null && didCollectionAccountQrCodeList.size() > 0){
                        int checkNum = 0;
                        if (didCollectionAccountQrCodeList.size() == 1){
                            for (StrategyData strategyData : qcCodeLimitList){
                                if (strategyData.getStgKey() == Long.parseLong(String.valueOf(didCollectionAccountQrCodeList.get(0).getDataType()))){
                                    int stgLimitNum = strategyData.getStgValueThree();
                                    if(num > stgLimitNum){
                                        checkNum = 1;
                                        break;
                                    }
                                }
                            }
                        }else if (didCollectionAccountQrCodeList.size() > 1){
                            // #这里可能要有改动哦，目前做法：表示多个普通收款二维码，属于固定类型1
                            for (StrategyData strategyData : qcCodeLimitList){
                                if (strategyData.getStgKey() == 1){
                                    int stgLimitNum = strategyData.getStgValueThree();
                                    if(num > stgLimitNum){
                                        checkNum = 1;
                                        break;
                                    }
                                }
                            }
                        }

                        if (checkNum > 0){
                            // 表示已超过日给出出码上限次数：需要存入缓存中；redis缓存的有效时间：到今天的凌晨0点失效
                            String strKeyCache_check_lock_did_collection_account_day_limit_num = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_DAY_LIMIT_NUM, data);
                            String strCache_check_lock_did_collection_account_day_limit_num = (String) ComponentUtil.redisService.get(strKeyCache_check_lock_did_collection_account_day_limit_num);
                            if (StringUtils.isBlank(strCache_check_lock_did_collection_account_day_limit_num)){
//                                long time = DateUtil.getTomorrowMinute() * 60;
                                long time = DateUtil.getTomorrowMinute();
                                ComponentUtil.redisService.set(strKeyCache_check_lock_did_collection_account_day_limit_num, String.valueOf(data), time);
                            }

                        }
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidCollectionAccountData.countNum()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidCollectionAccountData.countNum() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }



    /**
     * @Description: 查询所有给出的收款账号成功收款的总次数，成功收款的金额总和
     * <p>
     *     每1分钟运行一次
     *     1.查询今日所有给出码的收款码集合
     *     2.for循环查询每个成功收款账号给出的次数以及金额
     *     3.根据策略部署数据，进行次数比较，成功金额比较；如果给码成功次数或者成功金额已经超过部署的策略的数据，则纪录redis缓存；
     *     redis缓存Key：LOCK_DID_COLLECTION_ACCOUNT_DAY_SUC_MONEY，LOCK_DID_COLLECTION_ACCOUNT_DAY_SUC_LIMIT_NUM
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每1分钟执行
    @Scheduled(fixedDelay = 60000) // 每1分钟执行
    public void countSuccessData() throws Exception{
//        log.info("----------------------------------TaskDidCollectionAccountData.countSuccessData()----start");
        int curday = DateUtil.getDayNumber(new Date());
        // 查询策略里面的微信收款二维码日限制规则列表
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.QC_CODE_LIMIT_LIST.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        // 解析微信收款二维码日限制规则列表的值
        List<StrategyData> qcCodeLimitList = JSON.parseArray(strategyModel.getStgBigValue(), StrategyData.class);
        // 获取未填充可爱猫回调店员绑定小微的数据
        TaskDidCollectionAccountDataModel taskDidCollectionAccountDataQuery = TaskMethod.assembleTaskDidCollectionAccountData(4, 0, curday, 0, 0);
        List<Long> synchroList = ComponentUtil.taskDidCollectionAccountDataService.getDidCollectionAccountList(taskDidCollectionAccountDataQuery);
        for (Long data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_COLLECTION_ACCOUNT_LIMIT_SUC_NUM, data);
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 查询此收款派单成功状态的订单金额总和，次数总和
                    TaskDidCollectionAccountDataModel limitQuery = TaskMethod.assembleTaskDidCollectionAccountData(4, data, curday, 0, 0);
                    TaskDidCollectionAccountDataModel taskDidCollectionAccountDataModel = ComponentUtil.taskDidCollectionAccountDataService.getSucLimitNumAndMoney(limitQuery);
                    if (taskDidCollectionAccountDataModel != null && taskDidCollectionAccountDataModel.getIsLimitNum() != null && taskDidCollectionAccountDataModel.getIsLimitNum() > 0){
//                        taskDidCollectionAccountDataModel.setIsLimitNum(100);
//                        taskDidCollectionAccountDataModel.setMoney("500000.00");
                        // 查询这个收款账号的二维码信息
                        DidCollectionAccountQrCodeModel didCollectionAccountQrCodeModel = TaskMethod.assembleDidCollectionAccountQrCode(data);
                        List<DidCollectionAccountQrCodeModel> didCollectionAccountQrCodeList = ComponentUtil.didCollectionAccountQrCodeService.findByCondition(didCollectionAccountQrCodeModel);
                        if (didCollectionAccountQrCodeList != null && didCollectionAccountQrCodeList.size() > 0){
                            int checkNum = 0;
                            if (didCollectionAccountQrCodeList.size() == 1){
                                for (StrategyData strategyData : qcCodeLimitList){
                                    if (strategyData.getStgKey() == Long.parseLong(String.valueOf(didCollectionAccountQrCodeList.get(0).getDataType()))){
                                        int stgLimitNum = strategyData.getStgValueTwo();
                                        if(taskDidCollectionAccountDataModel.getIsLimitNum() > stgLimitNum){
                                            checkNum = 1;
                                            break;
                                        }
                                        boolean flag = StringUtil.getBigDecimalSubtract(strategyData.getStgValue(), taskDidCollectionAccountDataModel.getMoney());
                                        if (!flag){
                                            checkNum = 2;
                                            break;
                                        }
                                    }
                                }
                            }else if (didCollectionAccountQrCodeList.size() > 1){
                                // #这里可能要有改动哦，目前做法：表示多个普通收款二维码，属于固定类型1
                                for (StrategyData strategyData : qcCodeLimitList){
                                    if (strategyData.getStgKey() == 1){
                                        int stgLimitNum = strategyData.getStgValueTwo();
                                        if(taskDidCollectionAccountDataModel.getIsLimitNum() > stgLimitNum){
                                            checkNum = 1;
                                            break;
                                        }
                                        boolean flag = StringUtil.getBigDecimalSubtract(strategyData.getStgValue(), taskDidCollectionAccountDataModel.getMoney());
                                        if (!flag){
                                            checkNum = 2;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (checkNum > 0){
                                if (checkNum == 1){
                                    // 表示已超过日给出出码成功上限次数：需要存入缓存中；redis缓存的有效时间：到今天的凌晨0点失效
                                    String strKeyCache_check_lock_did_collection_account_day_suc_limit_num = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_DAY_SUC_LIMIT_NUM, data);
                                    String strCache_check_lock_did_collection_account_day_suc_limit_num = (String) ComponentUtil.redisService.get(strKeyCache_check_lock_did_collection_account_day_suc_limit_num);
                                    if (StringUtils.isBlank(strCache_check_lock_did_collection_account_day_suc_limit_num)){
                                        long time = DateUtil.getTomorrowMinute();
                                        ComponentUtil.redisService.set(strKeyCache_check_lock_did_collection_account_day_suc_limit_num, String.valueOf(data), time);
                                    }
                                }
                                if (checkNum == 2){
                                    // 表示已超过日收款账号成功收款金额超过上限：需要存入缓存中；redis缓存的有效时间：到今天的凌晨0点失效
                                    String strKeyCache_check_lock_did_collection_account_day_suc_money = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_DAY_SUC_MONEY, data);
                                    String strCache_check_lock_did_collection_account_day_suc_money = (String) ComponentUtil.redisService.get(strKeyCache_check_lock_did_collection_account_day_suc_money);
                                    if (StringUtils.isBlank(strCache_check_lock_did_collection_account_day_suc_money)){
                                        long time = DateUtil.getTomorrowMinute();
                                        ComponentUtil.redisService.set(strKeyCache_check_lock_did_collection_account_day_suc_money, String.valueOf(data), time);
                                    }
                                }

                            }
                        }
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidCollectionAccountData.countSuccessData()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidCollectionAccountData.countSuccessData() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }



    /**
     * @Description: 查询所有给出码最近的几次派单数据,判断用户收款账号是否异常
     * <p>
     *     每1分钟运行一次
     *     1.查询今日所有给出码的收款码集合
     *     2.for循环查询每个收款账号派单的订单状态大于1的派单数据：这里可以理解为查询超时订单以及成功订单
     *     3.根据策略部署数据，进行次数失败次数比较，如果派单连续失败次数已经超过部署的策略的失败次数，则修改此收款账号的使用状态，修改成暂停状态
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每1分钟执行
    @Scheduled(fixedDelay = 60000) // 每1分钟执行
    public void countFailNum() throws Exception{
//        log.info("----------------------------------TaskDidCollectionAccountData.countFailNum()----start");
        int curday = DateUtil.getDayNumber(new Date());
        // 查询策略里面的二维码连续给出失败次数
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.QC_CODE_FAIL_NUM.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        int failNum = strategyModel.getStgNumValue();
        // 获取未填充可爱猫回调店员绑定小微的数据
        TaskDidCollectionAccountDataModel taskDidCollectionAccountDataQuery = TaskMethod.assembleTaskDidCollectionAccountData(0, 0, curday, 0, 0);
        List<Long> synchroList = ComponentUtil.taskDidCollectionAccountDataService.getDidCollectionAccountList(taskDidCollectionAccountDataQuery);
        for (Long data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_COLLECTION_ACCOUNT_FAIL_NUM, data);
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 查询此收款码最近派给他的订单数据，并且订单状态要大于1的派单集合
                    TaskDidCollectionAccountDataModel fileQuery = TaskMethod.assembleTaskDidCollectionAccountData(0, data, curday, failNum, 1);
                    List<TaskDidCollectionAccountDataModel> dataList = ComponentUtil.taskDidCollectionAccountDataService.getOrderStatusByDidCollectionAccount(fileQuery);
                    int checkNum = 0;
                    if (dataList != null && dataList.size() > 0){
                        if (dataList.size() == failNum){
                            for (TaskDidCollectionAccountDataModel dataModel : dataList){
                                if (dataModel.getOrderStatus() == 4){
                                    break;
                                }else{
                                    checkNum ++;
                                }
                            }
                        }
                    }

                    if (checkNum != 0 && checkNum >= failNum) {
                        //判断之前是否在2小时以内处理过这个收款账号（不能一直去修改这个账号）
                        String strKeyCache_check_lock_did_collection_account_fail = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_COLLECTION_ACCOUNT_FAIL, data);
                        String strCache_check_lock_did_collection_account_fail = (String) ComponentUtil.redisService.get(strKeyCache_check_lock_did_collection_account_fail);
                        if (StringUtils.isBlank(strCache_check_lock_did_collection_account_fail)) {
                            // 连续失败超过策略部署的上限次数；需要把这个账号更新成暂停使用状态
                            DidCollectionAccountModel didCollectionAccountUpdate = TaskMethod.assembleDidCollectionAccountUpdate(data, "检查：连续给码收款未成功，请检查您的收款账号");
                            ComponentUtil.didCollectionAccountService.updateDidCollectionAccountCheckDataByFail(didCollectionAccountUpdate);
                            ComponentUtil.redisService.set(strKeyCache_check_lock_did_collection_account_fail, String.valueOf(data), 30, TimeUnit.MINUTES);
                        }
                    }
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidCollectionAccountData.countFailNum()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidCollectionAccountData.countFailNum() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }



    public static void main(String [] args){
        int num = DateUtil.getTomorrowMinute();
        System.out.println(num);
    }
}
