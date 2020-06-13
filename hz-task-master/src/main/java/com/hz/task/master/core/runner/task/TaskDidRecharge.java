package com.hz.task.master.core.runner.task;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidLevelModel;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.did.DidRechargeModel;
import com.hz.task.master.core.model.did.DidRewardModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
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
import java.util.stream.Collectors;

/**
 * @Description task:用户充值订单处理
 * <p>
 *     1.检测失效订单
 *     2.检测成功订单：A.给与用户奖励. B.删除必要的redis限制
 * </p>
 * @Author yoko
 * @Date 2020/6/4 17:01
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskDidRecharge {
    private final static Logger log = LoggerFactory.getLogger(TaskDidRecharge.class);

    @Value("${task.limit.num}")
    private int limitNum;

    /**
     * 10分钟
     */
    public long TEN_MIN = 10;



    /**
     * @Description: task：检测已出失效订单
     * <p>
     *     每1每秒运行一次
     *     1.根据订单状态属于初始状态的，检测失效时间是否已经小于系统时间。
     *     2.小于系统时间，则进行订单状态更新：更新 成失效订单
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void didRechargeByInvalidTime() throws Exception{
//        log.info("----------------------------------TaskDidRecharge.didRechargeByInvalidTime()----start");
        // 获取已失效的订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskDidRechargeByInvalidTimeQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE, "1");
        List<DidRechargeModel> synchroList = ComponentUtil.taskDidRechargeService.getDidRechargeList(statusQuery);
        for (DidRechargeModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_RECHARGE_INVALID, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeByOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, 2);
                    ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidRecharge.didRechargeByInvalidTime()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidRecharge.didRechargeByInvalidTime() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为没有更改到数据
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeByOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO,0);
                ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
            }
        }
    }




    /**
     * @Description: task：执行用户充值订单成功的逻辑运算
     * <p>
     *     每1每秒运行一次
     *     1.查询出未执行并且用户充值订单是成功状态的数据
     *     2.计算用户充值奖励-并把数据添加到用户奖励表。
     *     3.计算用户档次奖励-并把数据添加到用户奖励表。
     *     4.修改用户涉及金额的字段：total_money（总金额/累计充值）+,balance（余额）+，vip_type（修改成充值会员）
     *     5.删除redis：HANG_MONEY、LOCK_DID_ORDER_INVALID_TIME
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "1 * * * * ?")
    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void didRechargeBySuccessOrder() throws Exception{
//        log.info("----------------------------------TaskDidRecharge.didRechargeBySuccessOrder()----start");

        // 查询策略里面的金额列表
        StrategyModel strategyByMoneyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.MONEY.getStgType());
        StrategyModel strategyByMoneyModel = ComponentUtil.strategyService.getStrategyModel(strategyByMoneyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        // 解析金额列表的值
        List<StrategyData> moneyList = JSON.parseArray(strategyByMoneyModel.getStgBigValue(), StrategyData.class);

        // 查询策略里面的总金额充值档次奖励列表
        StrategyModel strategyMoneyGradeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.MONEY_GRADE_LIST.getStgType());
        StrategyModel strategyMoneyGradeModel = ComponentUtil.strategyService.getStrategyModel(strategyMoneyGradeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        // 解析金额列表的值
        List<StrategyData> moneyGradeList = JSON.parseArray(strategyMoneyGradeModel.getStgBigValue(), StrategyData.class);


        // 获取已成功的订单数据
        StatusModel statusQuery = TaskMethod.assembleTaskDidRechargeBySuccessOrderQuery(limitNum, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE);
        List<DidRechargeModel> synchroList = ComponentUtil.taskDidRechargeService.getDidRechargeList(statusQuery);
        for (DidRechargeModel data : synchroList){
            try{
                int num = 0;
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_DID_RECHARGE_SUCCESS_ORDER, data.getId());
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 计算用户充多少送多少的具体金额
                    String rechargeProfit = TaskMethod.getRechargeProfit(moneyList, data.getMoneyId());
                    if (StringUtils.isBlank(rechargeProfit)){
                        // #这里会有一个漏洞，可能没有按照规则充多少送多少，会导致漏洞：但是我们制定的规则就是冲多少送多少的规则，所以如果用户没按照规则来就属于脏数据这里只是备注一下
                        StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeBySuccessOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "计算充多少送多少的收益值为空");
                        ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
                    }else{
                        // 计算用户档次奖励

                        // 查询用户总充值 ; 总充值 = 之前总充值 + 这次的充值
                        DidModel didQuery = TaskMethod.assembleDidQueryByDid(data.getDid());
                        DidModel didModel = (DidModel) ComponentUtil.didService.findByObject(didQuery);
                        if (didModel == null || didModel.getId() <= 0){
                            // 更新此次task的状态：更新成失败：根据用户ID查询用户数据为空
                            StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeBySuccessOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "根据用户ID查询用户数据为空");
                            ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
                        }else {
                            // 奖励：组装用户充多少从多少的数据
                            DidRewardModel didRechargeProfit = TaskMethod.assembleDidRechargeProfit(data, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE, rechargeProfit);

                            // 累计充值
                            String totalRechargeMoney = "";
                            if (StringUtils.isBlank(didModel.getTotalMoney())){
                                totalRechargeMoney = "0.00";
                            }else{
                                totalRechargeMoney = StringUtil.getBigDecimalAdd(didModel.getTotalMoney(), data.getOrderMoney());
                            }

                            // 计算用户的档次奖励金额
                            Map<String, String> gradeProfitMap = TaskMethod.getGradeProfit(moneyGradeList, totalRechargeMoney, data.getOrderMoney());
                            DidRewardModel didGradeProfit = null;
                            if (gradeProfitMap != null && gradeProfitMap.size() > 0){
                                didGradeProfit = new DidRewardModel();
                                didGradeProfit = TaskMethod.assembleDidGradeProfit(data, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, gradeProfitMap.get("gradeProfit"), gradeProfitMap.get("stgGradeProfit"));
                            }

                            // 组装更新用户金额信息的数据
                            DidModel upDidMoney = TaskMethod.assembleUpdateDidMoneyByRecharge(data.getDid(), data.getOrderMoney());

                            // 锁住这个用户
                            String lockKey_did = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_MONEY, data.getDid());
                            boolean flagLock_did = ComponentUtil.redisIdService.lock(lockKey_did);
                            if (flagLock_did){
                                boolean flag = ComponentUtil.taskDidRechargeService.didRechargeSuccessOrder(didRechargeProfit, didGradeProfit, upDidMoney);
                                if (flag){

                                    // 删除要删除的redis
                                    String strKeyCache_HANG_MONEY = CachedKeyUtils.getCacheKey(CacheKey.HANG_MONEY, data.getBankId(), data.getDistributionMoney());// 银行卡具体的挂单金额
                                    String strKeyCache_LOCK_DID_ORDER_INVALID_TIME = CachedKeyUtils.getCacheKey(CacheKey.LOCK_DID_ORDER_INVALID_TIME, data.getDid());// 用户调起充值订单的失效时间
                                    ComponentUtil.redisService.remove(strKeyCache_HANG_MONEY);
                                    ComponentUtil.redisService.remove(strKeyCache_LOCK_DID_ORDER_INVALID_TIME);

                                    // 更新此次task的状态：更新成成功
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeBySuccessOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_THREE, "");
                                    ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
                                }else {
                                    // 更新此次task的状态：更新成失败：执行SQL操作更新用户信息出错
                                    StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeBySuccessOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "执行SQL操作更新用户信息出错");
                                    ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
                                }

                                // 解锁
                                ComponentUtil.redisIdService.delLock(lockKey_did);
                            }
                        }
                    }

                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskDidRecharge.didRechargeBySuccessOrder()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidRecharge.didRechargeBySuccessOrder() is error , the dataId=%s !", data.getId()));
                e.printStackTrace();
                // 更新此次task的状态：更新成失败：因为ERROR
                StatusModel statusModel = TaskMethod.assembleUpdateStatusByDidRechargeBySuccessOrderStatus(data.getId(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO, "异常失败try");
                ComponentUtil.taskDidRechargeService.updateDidRechargeStatus(statusModel);
            }
        }
    }







    /**
     * @Description: task：执行用户直推充值奖励的逻辑运算
     * <p>
     *     每凌晨1点运行一次
     *     1.查询系统时间昨天的充值成功的所有用户
     *     2.根据已昨天已充值的用户找出这些用户下面直推的用户。
     *     3.把直推用户的所有昨天的充值成功的订单查询出来。
     *     4.每笔订单进行奖励数据添加。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */

    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void didRechargeByDirect() throws Exception{
        log.info("----------------------------------TaskDidRecharge.didRechargeByDirect()----start");

        // 查询策略里面的直推奖励规则
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.DIRECT_REWARD.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);



        // 获取昨天充值成功的数据
        DidRechargeModel didRechargeQuery = TaskMethod.assembleTaskDidRechargeByDirectQuery();
        List<Long> synchroList = ComponentUtil.taskDidRechargeService.getRechargeDidList(didRechargeQuery);
        for (Long data : synchroList){
            try{
                // 循环查询这些 用户的直推用户
                DidLevelModel didLevelQuery = TaskMethod.assembleDidLevelQuery(data, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
                List<DidLevelModel> didLevelList = ComponentUtil.didLevelService.findByCondition(didLevelQuery);
                for (DidLevelModel didLevelModel : didLevelList){
                    // 循环查询错昨天这些直推用户充值成功的订单
                    DidRechargeModel didRechargeDirectQuery = TaskMethod.assembleDidRechargeByDidQuery(didLevelModel.getDid());
                    List<DidRechargeModel> directList = ComponentUtil.didRechargeService.findByCondition(didRechargeDirectQuery);
                    for (DidRechargeModel didRechargeModel : directList){

                        // 计算出直推收益： 直推收益 = 订单金额 * 策略中直推规则
                        String directProfit = StringUtil.getMultiply(didRechargeModel.getOrderMoney(), strategyModel.getStgValue());

                        // 获取用户的账号
                        DidModel didQuery = TaskMethod.assembleDidQueryByDid(didRechargeModel.getDid());
                        DidModel didModel = (DidModel) ComponentUtil.didService.findByObject(didQuery);

                        // 组装直推用户奖励数据
                        DidRewardModel didRewardModel = TaskMethod.assembleDidDirectProfit(didRechargeModel, ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE, data, didModel.getAcNum(), directProfit);
                        ComponentUtil.didRewardService.add(didRewardModel);
                    }
                }
                log.info("----------------------------------TaskDidRecharge.didRechargeByDirect()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidRecharge.didRechargeByDirect() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }






    /**
     * @Description: task：执行用户团队奖励的逻辑运算
     * <p>
     *     每凌晨1点运行一次
     *     1.查询系统时间昨天的充值成功的所有用户
     *     2.根据已昨天已充值的用户找出这些用户下面直推的用户。
     *     3.求和直推用户的总充值金额。
     *     4.判断计算与策略中团队奖励规则是否达标：如果达标进行奖励金额计算。
     *
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(fixedDelay = 1000) // 每秒执行
    public void didRechargeByTeam() throws Exception{
        log.info("----------------------------------TaskDidRecharge.didRechargeByTeam()----start");

        // 查询策略里面的团队日充值累计总额奖励规则列表
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.TEAM_REWARD.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        // 解析奖励规则的值
        List<StrategyData> teamRewardList = JSON.parseArray(strategyModel.getStgBigValue(), StrategyData.class);



        // 获取昨天充值成功的数据
        DidRechargeModel didRechargeQuery = TaskMethod.assembleTaskDidRechargeByDirectQuery();
        List<Long> synchroList = ComponentUtil.taskDidRechargeService.getRechargeDidList(didRechargeQuery);
        for (Long data : synchroList){
            try{
                // 循环查询这些 用户的直推用户
                DidLevelModel didLevelQuery = TaskMethod.assembleDidLevelQuery(data, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
                List<DidLevelModel> didLevelList = ComponentUtil.didLevelService.findByCondition(didLevelQuery);

                if (didLevelList != null && didLevelList.size() > 0){
                    // 直推的用户ID集合
                    List<Long> didList = didLevelList.stream().map(DidLevelModel::getDid).collect(Collectors.toList());

                    // 获取直推用户昨天充值成功的总金额
                    DidRechargeModel didRechargeByDidQuery = TaskMethod.assembleDidRechargeByDidQuery(didList);
                    String directSumMoney = ComponentUtil.didRechargeService.directSumMoney(didRechargeByDidQuery);

                    if (!StringUtils.isBlank(directSumMoney) && !directSumMoney.equals("0.00")){
                        // 计算团队充值奖励
                        String teamProfit = TaskMethod.getTeamProfit(teamRewardList, directSumMoney);
                        if (!StringUtils.isBlank(teamProfit)){
                            // 添加团队奖励收益
                            DidRewardModel didRewardModel = TaskMethod.assembleDidDirectProfit(5, data, teamProfit, directSumMoney);
                            ComponentUtil.didRewardService.add(didRewardModel);
                        }
                    }
                }
                log.info("----------------------------------TaskDidRecharge.didRechargeByTeam()----end");
            }catch (Exception e){
                log.error(String.format("this TaskDidRecharge.didRechargeByTeam() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }







}
