package com.hz.task.master.core.runner;

import com.hz.task.master.core.common.redis.RedisIdService;
import com.hz.task.master.core.common.redis.RedisService;
import com.hz.task.master.core.common.utils.constant.LoadConstant;
import com.hz.task.master.core.service.*;
import com.hz.task.master.core.service.task.*;
import com.hz.task.master.util.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Order(0)
public class AutowireRunner implements ApplicationRunner {
    private final static Logger log = LoggerFactory.getLogger(AutowireRunner.class);

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    Thread runThread = null;

    @Autowired
    private RedisIdService redisIdService;
    @Autowired
    private RedisService redisService;

    @Resource
    private LoadConstant loadConstant;

    @Autowired
    private QuestionMService questionMService;

    @Autowired
    private QuestionDService questionDService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskHodgepodgeService taskHodgepodgeService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private DidService didService;

    @Autowired
    private DidLevelService didLevelService;

    @Autowired
    private DidCollectionAccountService didCollectionAccountService;

    @Autowired
    private MobileCardService mobileCardService;

    @Autowired
    private MobileCardDataService mobileCardDataService;

    @Autowired
    private BankService bankService;

    @Autowired
    private BankTransferService bankTransferService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private DidRechargeService didRechargeService;

    @Autowired
    private BankCollectionService bankCollectionService;

    @Autowired
    private BankCollectionDataService bankCollectionDataService;

    @Autowired
    private DidRewardService didRewardService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WxService wxService;

    @Autowired
    private WxClerkService wxClerkService;

    @Autowired
    private WxClerkDataService wxClerkDataService;

    @Autowired
    private CatAllDataService catAllDataService;

    @Autowired
    private CatDataService catDataService;

    @Autowired
    private CatDataOfflineService catDataOfflineService;

    @Autowired
    private CatDataBindingService catDataBindingService;

    @Autowired
    private DidBalanceDeductService didBalanceDeductService;

    @Autowired
    private ClientDataService clientDataService;

    @Autowired
    private ClientAllDataService clientAllDataService;

    @Autowired
    private ClientCollectionDataService clientCollectionDataService;

    @Autowired
    private DidTeamGradeService didTeamGradeService;

    @Autowired
    private CatDataAnalysisService catDataAnalysisService;

    @Autowired
    private OperateService operateService;

    @Autowired
    private CatDataAnalysisUnusualService catDataAnalysisUnusualService;

    @Autowired
    private OrderStepService orderStepService;

    @Autowired
    private WxFriendService wxFriendService;

    @Autowired
    private WxOrderService wxOrderService;

    @Autowired
    private WxAllDataService wxAllDataService;

    @Autowired
    private PoolOpenService poolOpenService;

    @Autowired
    private PoolWaitService poolWaitService;

    @Autowired
    private PoolOriginService poolOriginService;

    @Autowired
    private DidWxMonitorService didWxMonitorService;

    @Autowired
    private DidWxSortService didWxSortService;








    @Autowired
    private TaskMobileCardService taskMobileCardService;

    @Autowired
    private TaskBankCollectionService taskBankCollectionService;

    @Autowired
    private TaskDidRechargeService taskDidRechargeService;

    @Autowired
    private TaskDidRewardService taskDidRewardService;

    @Autowired
    private TaskCatAllDataService taskCatAllDataService;

    @Autowired
    private TaskCatDataService taskCatDataService;

    @Autowired
    private TaskOrderService taskOrderService;

    @Autowired
    private TaskCatDataOfflineService taskCatDataOfflineService;

    @Autowired
    private TaskBankLimitService taskBankLimitService;

    @Autowired
    private TaskCatDataBindingService taskCatDataBindingService;

    @Autowired
    private DidCollectionAccountQrCodeService didCollectionAccountQrCodeService;

    @Autowired
    private TaskDidCollectionAccountDataService taskDidCollectionAccountDataService;

    @Autowired
    private TaskDidBalanceDeductService taskDidBalanceDeductService;

    @Autowired
    private TaskClientAllDataService taskClientAllDataService;

    @Autowired
    private TaskClientDataService taskClientDataService;

    @Autowired
    private TaskCatDataAnalysisService taskCatDataAnalysisService;

    @Autowired
    private TaskWxService taskWxService;

    @Autowired
    private TaskDidService taskDidService;

    @Autowired
    private TaskWxOrderService taskWxOrderService;

    @Autowired
    private TaskWxAllDataService taskWxAllDataService;

    @Autowired
    private TaskPoolOpenService taskPoolOpenService;

    @Autowired
    private TaskPoolWaitService taskPoolWaitService;










    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("AutowireRunner ...");
        ComponentUtil.redisIdService = redisIdService;
        ComponentUtil.redisService = redisService;
        ComponentUtil.loadConstant = loadConstant;
        ComponentUtil.questionMService = questionMService;
        ComponentUtil.questionDService = questionDService;

        ComponentUtil.taskService = taskService;
        ComponentUtil.taskHodgepodgeService = taskHodgepodgeService;
        ComponentUtil.regionService = regionService;
        ComponentUtil.didService = didService;
        ComponentUtil.didLevelService = didLevelService;
        ComponentUtil.didCollectionAccountService = didCollectionAccountService;
        ComponentUtil.mobileCardService = mobileCardService;
        ComponentUtil.mobileCardDataService = mobileCardDataService;
        ComponentUtil.bankService = bankService;
        ComponentUtil.bankTransferService = bankTransferService;
        ComponentUtil.strategyService = strategyService;
        ComponentUtil.didRechargeService = didRechargeService;
        ComponentUtil.bankCollectionService = bankCollectionService;
        ComponentUtil.bankCollectionDataService = bankCollectionDataService;
        ComponentUtil.didRewardService = didRewardService;
        ComponentUtil.orderService = orderService;
        ComponentUtil.wxService = wxService;
        ComponentUtil.wxClerkService = wxClerkService;
        ComponentUtil.wxClerkDataService = wxClerkDataService;
        ComponentUtil.catAllDataService = catAllDataService;
        ComponentUtil.catDataService = catDataService;
        ComponentUtil.catDataOfflineService = catDataOfflineService;
        ComponentUtil.catDataBindingService = catDataBindingService;
        ComponentUtil.didCollectionAccountQrCodeService = didCollectionAccountQrCodeService;
        ComponentUtil.didBalanceDeductService = didBalanceDeductService;
        ComponentUtil.clientAllDataService = clientAllDataService;
        ComponentUtil.clientDataService = clientDataService;
        ComponentUtil.clientCollectionDataService = clientCollectionDataService;
        ComponentUtil.didTeamGradeService = didTeamGradeService;
        ComponentUtil.catDataAnalysisService = catDataAnalysisService;
        ComponentUtil.operateService = operateService;
        ComponentUtil.catDataAnalysisUnusualService = catDataAnalysisUnusualService;
        ComponentUtil.orderStepService = orderStepService;
        ComponentUtil.wxFriendService = wxFriendService;
        ComponentUtil.wxOrderService = wxOrderService;
        ComponentUtil.wxAllDataService = wxAllDataService;
        ComponentUtil.poolOpenService = poolOpenService;
        ComponentUtil.poolWaitService = poolWaitService;
        ComponentUtil.poolOriginService = poolOriginService;
        ComponentUtil.didWxMonitorService = didWxMonitorService;
        ComponentUtil.didWxSortService = didWxSortService;



        ComponentUtil.taskMobileCardService = taskMobileCardService;
        ComponentUtil.taskBankCollectionService = taskBankCollectionService;
        ComponentUtil.taskDidRechargeService = taskDidRechargeService;
        ComponentUtil.taskDidRewardService = taskDidRewardService;
        ComponentUtil.taskCatAllDataService = taskCatAllDataService;
        ComponentUtil.taskCatDataService = taskCatDataService;
        ComponentUtil.taskOrderService = taskOrderService;
        ComponentUtil.taskCatDataOfflineService = taskCatDataOfflineService;
        ComponentUtil.taskBankLimitService = taskBankLimitService;
        ComponentUtil.taskCatDataBindingService = taskCatDataBindingService;
        ComponentUtil.taskDidCollectionAccountDataService = taskDidCollectionAccountDataService;
        ComponentUtil.taskDidBalanceDeductService = taskDidBalanceDeductService;
        ComponentUtil.taskClientAllDataService = taskClientAllDataService;
        ComponentUtil.taskClientDataService = taskClientDataService;
        ComponentUtil.taskCatDataAnalysisService = taskCatDataAnalysisService;
        ComponentUtil.taskWxService = taskWxService;
        ComponentUtil.taskDidService = taskDidService;
        ComponentUtil.taskWxOrderService = taskWxOrderService;
        ComponentUtil.taskWxAllDataService = taskWxAllDataService;
        ComponentUtil.taskPoolOpenService = taskPoolOpenService;
        ComponentUtil.taskPoolWaitService = taskPoolWaitService;

        runThread = new RunThread();
        runThread.start();






    }

    /**
     * @author df
     * @Description: TODO(模拟请求)
     * <p>1.随机获取当日金额的任务</p>
     * <p>2.获取代码信息</p>
     * @create 20:21 2019/1/29
     **/
    class RunThread extends Thread{
        @Override
        public void run() {
            log.info("启动啦............");
        }
    }




}
