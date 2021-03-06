package com.hz.task.master.core.service.impl;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.exception.ServiceException;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ErrorCode;
import com.hz.task.master.core.mapper.BankMapper;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.model.strategy.StrategyBankLimit;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.service.BankService;
import com.hz.task.master.util.ComponentUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description 银行的Service层的实现层
 * @Author yoko
 * @Date 2020/5/18 19:08
 * @Version 1.0
 */
@Service
public class BankServiceImpl<T> extends BaseServiceImpl<T> implements BankService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * 11分钟.
     */
    public long ELEVEN_MIN = 660;

    public long TWO_HOUR = 2;

    @Autowired
    private BankMapper bankMapper;

    public BaseDao<T> getDao() {
        return bankMapper;
    }

    @Override
    public Map<String, Object> screenBank(List<BankModel> bankList, List<StrategyBankLimit> strategyBankLimitList, List<StrategyData> strategyMoneyAddSubtractList, String orderMoney) throws Exception{
        Map<String, Object> map = new HashMap<>();
        String money = "";
        BankModel dataModel = new BankModel();
        List<BankModel> yesSpecialList = new ArrayList<>();// 优先级高的银行卡
        List<BankModel> noSpecialList = new ArrayList<>();// 不是优先级高的银行卡/普通银行卡
        for (BankModel bankModel : bankList){
            if (bankModel.getSpecialType() == 2){
                yesSpecialList.add(bankModel);
            }else {
                noSpecialList.add(bankModel);
            }
        }

        // 优先消耗
        if (yesSpecialList != null && yesSpecialList.size() > 0){
            map = checkDataAndGetMoney(yesSpecialList, strategyBankLimitList, strategyMoneyAddSubtractList, orderMoney);
        }

        if (map == null){
            map = checkDataAndGetMoney(noSpecialList, strategyBankLimitList, strategyMoneyAddSubtractList, orderMoney);
        }

        if (map != null){
            // 缓存挂单- 表示这个银行卡的这个金额已经给出去了
            String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.HANG_MONEY, map.get("bankId"), map.get("distributionMoney"));
            ComponentUtil.redisService.set(strKeyCache, map.get("distributionMoney").toString(), ELEVEN_MIN);
        }

        return map;
    }

    @Override
    public int upUseStatus(BankModel bankModel) {
        return bankMapper.upUseStatus(bankModel);
    }


    /**
     * @Description: 组装缓存key查询缓存中存在的数据
     * @param cacheKey - 缓存的类型key
     * @param obj - 数据的ID
     * @return
     * @author yoko
     * @date 2020/5/20 14:59
    */
    public String getRedisDataByKey(String cacheKey, Object obj){
        String str = null;
        String strKeyCache = CachedKeyUtils.getCacheKey(cacheKey, obj);
        String strCache = (String) ComponentUtil.redisService.get(strKeyCache);
        if (StringUtils.isBlank(strCache)){
            return str;
        }else{
            str = strCache;
            return str;
        }
    }


    /**
     * @Description: 组装缓存key查询缓存中存在的数据:银行卡挂单金额数据
     * <p>
     *     这里获取银行卡目前是否有挂单的数据金额
     * </p>
     * @param cacheKey - 缓存的类型key
     * @param money - 数据的ID
     * @return
     * @author yoko
     * @date 2020/5/20 14:59
     */
    public String getRedisMoneyDataByKey(String cacheKey, long dataId, String money){
        String str = null;
        String strKeyCache = CachedKeyUtils.getCacheKey(cacheKey, dataId, money);
        String strCache = (String) ComponentUtil.redisService.get(strKeyCache);
        if (StringUtils.isBlank(strCache)){
            return str;
        }else{
            str = strCache;
            return str;
        }
    }


    /**
     * @Description: check金额是否超过上限
     * <p>
     *     这里check的金额上限包括：日月总金额
     * </p>
     * @param cacheKey - 缓存的类型Key
     * @param dataId - 数据的主键ID
     * @param orderMoney - 订单金额
     * @param moneyLimit - 要限制的金额
     * @return boolean
     * @author yoko
     * @date 2020/5/20 16:39
     */
    public boolean checkMoney(String cacheKey, long dataId, String orderMoney, String moneyLimit){
        boolean flag;
        String money = "";
        String redis_money = getRedisDataByKey(cacheKey, dataId);
        if (StringUtils.isBlank(redis_money)){
            money = orderMoney;
        }else{
            // 缓存中的金额加订单金额
            money = StringUtil.getBigDecimalAdd(redis_money, orderMoney);
        }
        flag = StringUtil.getBigDecimalSubtract(moneyLimit, money);
        return flag;
    }


    /**
     * @Description: check次数是否超过上限
     * <p>
     *     这里check的次数上限包括：日月总次数
     * </p>
     * @param cacheKey - 缓存的类型Key
     * @param dataId - 数据的主键ID
     * @param numLimit - 要限制的次数
     * @return boolean
     * @author yoko
     * @date 2020/5/20 16:39
     */
    public boolean checkNum(String cacheKey, long dataId, int numLimit){
        boolean flag;
        int num = 0;
        String redis_num = getRedisDataByKey(cacheKey, dataId);
        if (StringUtils.isBlank(redis_num)){
            num = 1;
        }else{
            // 缓存中的金额加订单金额
            num = 1 + Integer.parseInt(redis_num);
        }
        if (numLimit < num){
            flag = false;
        }else {
            flag = true;
        }
        return flag;
    }


    /**
     * @Description: 根据银行卡，筛选出当前银行卡可用金额
     * @param strategyMoneyAddSubtractList - 订单金额加减范围列表的数据
     * @param cacheKey - 缓存类型= HANG_MONEY
     * @param bankId - 银行卡的ID
     * @param orderMoney - 订单金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/5/20 19:07
     */
    public String getUseMoney(List<StrategyData> strategyMoneyAddSubtractList, String cacheKey, long bankId, String orderMoney){
        String str = null;
        int num = 0;
        // 这个金额可以使用，判断这个金额是否被锁定；
        String lockKey_orderMoney = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MONEY_CENT, bankId, orderMoney);
        boolean flagLock_orderMoney = ComponentUtil.redisIdService.lock(lockKey_orderMoney);
        if (flagLock_orderMoney){
            // 首先判断整数金额是否有挂单金额
            String redis_data = getRedisMoneyDataByKey(cacheKey, bankId, orderMoney);
            if (StringUtils.isBlank(redis_data)) {
                // 表示整数金额目前没有挂单的金额，可以直接给出订单金额
                str = orderMoney;
            }
            num = 1;
        }
        if (StringUtils.isBlank(str)){
            // 表示整数金额已有挂单了：先删除整数金额的锁
            if (num == 1){
                // 解锁
                ComponentUtil.redisIdService.delLock(lockKey_orderMoney);
            }

            // 表示整数金额目前有挂单金额，需要进行订单金额的加减
            List<StrategyData> resList = new ArrayList<StrategyData>();
            resList = strategyMoneyAddSubtractList;
            Iterator<StrategyData> itList = resList.iterator();
            while (itList.hasNext()) {
                StrategyData data = itList.next();
                if (data.getStgValueTwo() == 1){
                    // 订单金额 + 策略补充金额
                    String money = StringUtil.getBigDecimalAdd(orderMoney, data.getStgValue());
                    String res_data = getRedisMoneyDataByKey(cacheKey, bankId, money);
                    if (StringUtils.isBlank(res_data)){
                        // 这个金额可以使用，判断这个金额是否被锁定；
                        String lockKey = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MONEY_CENT, bankId, res_data);
                        boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                        // 但是这里一定要把可用金额进行锁定：不然并发的时候会出大问题
                        if (!flagLock){
                            // 另外一个进程已经锁住这张银行卡的这个金额
                            itList.remove();
                        }

                    }else {
                        itList.remove();
                    }
                }else{
                    // 订单金额 - 策略补充金额
                    String money = StringUtil.getBigDecimalSubtractByStr(orderMoney, data.getStgValue());
                    String res_data = getRedisMoneyDataByKey(cacheKey, bankId, money);
                    if (StringUtils.isBlank(res_data)){
                        System.out.println();
                        // 这个金额可以使用，判断这个金额是否被锁定；
                        String lockKey = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MONEY_CENT, bankId, res_data);
                        boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                        // 但是这里一定要把可用金额进行锁定：不然并发的时候会出大问题
                        if (!flagLock){
                            // 另外一个进程已经锁住这张银行卡的这个金额
                            itList.remove();
                        }
                    }else {
                        itList.remove();
                    }
                }
            }

            // 最后符合金额的列表集合
            if (resList == null || resList.size() <= 0){
                return str;
            }

            // 从符合的金额列表集合中随机选择一个金额
            StrategyData strategyData = new StrategyData();
            if (resList != null && resList.size() > 0){
                int random = new Random().nextInt(resList.size());
                strategyData = resList.get(random);// 被选中的金额规则

                // 解锁其它金额
                for (StrategyData data : resList){
                    if (data.getId() != strategyData.getId()){
                        // 未被选中的
                        if (data.getStgValueTwo() == 1){
                            // 订单金额 + 策略补充金额
                            String money = StringUtil.getBigDecimalAdd(orderMoney, data.getStgValue());
                            String res_data = getRedisMoneyDataByKey(cacheKey, bankId, money);
                            String lockKey = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MONEY_CENT, bankId, res_data);
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey);
                        }else {
                            // 订单金额 - 策略补充金额
                            String money = StringUtil.getBigDecimalSubtractByStr(orderMoney, data.getStgValue());
                            String res_data = getRedisMoneyDataByKey(cacheKey, bankId, money);
                            String lockKey = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MONEY_CENT, bankId, res_data);
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey);
                        }

                    }else{
                        // 被选中的
                        if (data.getStgValueTwo() == 1){
                            // 订单金额 + 策略补充金额
                            str = StringUtil.getBigDecimalAdd(orderMoney, data.getStgValue());

                        }else {
                            // 订单金额 - 策略补充金额
                            str = StringUtil.getBigDecimalSubtractByStr(orderMoney, data.getStgValue());
                        }
                    }
                }
            }
        }

        return str;
    }


    /**
     * @Description: 筛选出最终符合银行卡的金额
     * <p>
     *     这里实现由两部分组成：1.check校验上限
     *     2.具体筛选可用金额
     * </p>
     * @param bankList - 可用的银行卡集合
     * @param strategyBankLimitList - 银行卡日月总限制规则策略
     * @param strategyMoneyAddSubtractList - 订单金额加减范围列表
     * @param orderMoney - 订单金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/5/20 19:30
     */
    public Map<String, Object> checkDataAndGetMoney(List<BankModel> bankList, List<StrategyBankLimit> strategyBankLimitList,
                                       List<StrategyData> strategyMoneyAddSubtractList, String orderMoney) throws Exception{
        return null;

    }


}
