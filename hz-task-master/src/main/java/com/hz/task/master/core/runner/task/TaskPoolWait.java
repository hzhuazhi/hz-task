package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.did.DidWxMonitorModel;
import com.hz.task.master.core.model.did.DidWxSortModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.pool.PoolOriginModel;
import com.hz.task.master.core.model.pool.PoolWaitModel;
import com.hz.task.master.core.model.strategy.StrategyModel;
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
 * @Description task:接单池子等待接单
 * @Author yoko
 * @Date 2020/8/14 15:22
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskPoolWait {

    private final static Logger log = LoggerFactory.getLogger(TaskPoolWait.class);

    @Value("${task.limit.num}")
    private int limitNum;



    /**
     * 10分钟
     */
    public long TEN_MIN = 10;

    /**
     * 30分钟
     */
    public long THIRTY_MIN = 30;



    /**
     * @Description: 检测等待池中的用户是否满足派单的条件
     * <p>
     *     每1分钟运行一次
     *     1.查询等待池中所有等待用户。
     *     2.for循环校验余额是否地域策略部署的最低余额。
     *     3.校验用户是否拥有有效群。
     *     4.校验是否存在订单已发红包，但是未回复。
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(fixedDelay = 1000) // 每1分钟执行
    @Scheduled(fixedDelay = 60000) // 每1分钟执行
    public void checkPoolWait() throws Exception{
//        log.info("----------------------------------TaskPoolWait.checkPoolWait()----start");
        // 查询策略里面的池子中的用户余额不得低于的保底金额
        String poolMinMoney = "";
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.POOL_MIN_MONEY.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        poolMinMoney = strategyModel.getStgValue();
        // 查询等待池中的用户集合
        List<Long> synchroList = ComponentUtil.taskPoolWaitService.getPoolWaitDidList(new PoolWaitModel());
        for (Long data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_POOL_WAIT_DID, data);
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    int dataType = 0;// 数据类型:1初始化，2其它，3余额不足，4有效群不足，5有订单未回复，6有违规操作，7抛开被限制的微信有效群不足，8没有可使用的微信
                    String origin = "";// 起因
                    PoolOriginModel poolOriginModel = null;
                    boolean flag = false;
                    boolean flag_money = false;
                    boolean flag_group = false;
                    boolean flag_order = false;
                    boolean flag_toWxid = false;
                    boolean flag_toWxid_sort = false;
                    // 查询用户基本信息
                    DidModel didQuery = TaskMethod.assembleDidQueryByDid(data);
                    DidModel didModel = (DidModel) ComponentUtil.didService.findByObject(didQuery);
                    // check校验用户金额是否低于池子中最低保底金额
                    flag_money = TaskMethod.checkDidMoney(didModel, poolMinMoney);
                    if (!flag_money){
                        dataType = 3;
                        origin = "等待池中检测到余额低于保底金额!";
                    }
                    flag = flag_money;
                    if (flag){
                        // 校验用户是否拥有有效群
                        DidCollectionAccountModel didCollectionAccountQuery = TaskMethod.assembleDidCollectionAccountListEffective(didModel.getId(), 3, 1, 3,1,2, 0, null);
                        List<DidCollectionAccountModel> didCollectionAccountList = ComponentUtil.didCollectionAccountService.getEffectiveDidCollectionAccountByWxGroup(didCollectionAccountQuery);
                        flag_group = TaskMethod.checkDidCollectionAccountListEffective(didCollectionAccountList);
                        if (!flag_group){
                            dataType = 4;
                            origin = "等待池中检测到有效群不足!";
                        }
                    }
                    flag = flag_group;
                    if (flag){
                        // 校验用户是否有已发红包的订单，但是未回复
                        OrderModel orderQuery = TaskMethod.assembleOrderByIsReply(data, 3, 1, 2,2);
                        OrderModel orderModel = ComponentUtil.orderService.getOrderByNotIsReply(orderQuery);
                        flag_order = TaskMethod.checkOrderByIsReply(orderModel);
                        if (!flag_order){
                            dataType = 5;
                            origin = "等待池中检测到有订单未回复,微信群名称：《" + orderModel.getWxNickname() + "》";
                        }
                    }
                    flag = flag_order;
                    if (flag){
                        // 校验排除超过金额的微信归属群，查询有效群是否不足

                        // 获取用户的微信收款账号金额监控超过范围的微信ID集合
                        DidWxMonitorModel didWxMonitorQuery = TaskMethod.assembleDidWxMonitorByDidQuery(data, "1");
                        List<String> toWxidList = ComponentUtil.didWxMonitorService.getToWxidList(didWxMonitorQuery);
                        if (toWxidList != null && toWxidList.size() > 0){
                            // 校验排除微信集合的其它微信用户是否拥有有效群
                            DidCollectionAccountModel didCollectionAccountToWxQuery = TaskMethod.assembleDidCollectionAccountListEffective(didModel.getId(), 3, 1, 3,1,2, 0, toWxidList);
                            List<DidCollectionAccountModel> didCollectionAccountToWxList = ComponentUtil.didCollectionAccountService.getEffectiveDidCollectionAccountByWxGroup(didCollectionAccountToWxQuery);
                            flag_toWxid = TaskMethod.checkDidCollectionAccountListEffective(didCollectionAccountToWxList);
                            if (!flag_toWxid){
                                dataType = 7;
                                origin = "等待池子中抛开被限制的微信后，有效群不足";
                            }
                        }else {
                            flag_toWxid = true;
                        }
                    }
                    flag = flag_toWxid;
                    if (flag){
                        // 校验微信排序中是否有正在使用的微信
                        DidWxSortModel didWxSortQuery = TaskMethod.assembleDidWxSortData(0, didModel.getId(), null,
                                0, 2, 0, 0, 0, null, null, null);
                        DidWxSortModel didWxSortModel = (DidWxSortModel)ComponentUtil.didWxSortService.findByObject(didWxSortQuery);
                        flag_toWxid_sort = TaskMethod.checkDidWxSortData(didWxSortModel);
                        if (!flag_toWxid_sort){
                            dataType = 8;
                            origin = "等待池子中用户在微信排序数据中没有可使用的微信";
                        }
                    }
                    flag = flag_toWxid_sort;


                    if (!flag){
                        // 把用户移出等待池
                        PoolWaitModel poolWaitUpdate = TaskMethod.assemblePoolWaitUpdate(0, data, 1);
                        ComponentUtil.taskPoolWaitService.updatePoolWaitYn(poolWaitUpdate);

                        poolOriginModel = TaskMethod.assemblePoolOriginAdd(data, dataType, origin);
                        ComponentUtil.poolOriginService.add(poolOriginModel);

                    }


                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskPoolWait.checkPoolWait()----end");
            }catch (Exception e){
                log.error(String.format("this TaskPoolWait.checkPoolWait() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }
}
