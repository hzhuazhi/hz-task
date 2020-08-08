package com.hz.task.master.core.runner.task;

import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.constant.CacheKey;
import com.hz.task.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.common.utils.constant.TkCacheKey;
import com.hz.task.master.core.model.did.DidCollectionAccountModel;
import com.hz.task.master.core.model.operate.OperateModel;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel;
import com.hz.task.master.core.model.wx.WxOrderModel;
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
 * @Description task:小微给出订单记录
 * @Author yoko
 * @Date 2020/8/8 16:29
 * @Version 1.0
 */
@Component
@EnableScheduling
public class TaskWxOrder {

    private final static Logger log = LoggerFactory.getLogger(TaskWxOrder.class);

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
     * @Description: 查询所有给出码最近的小微给出订单记录数据,判断小微数据监控是否异常
     * <p>
     *     每1分钟运行一次
     *     1.查询今日所有小微集合
     *     2.for循环查询每个小微给出订单记录。
     *     3.根据策略部署数据，进行次数失败次数比较，如果派单连续失败次数已经超过部署的策略的失败次数，则添加运营数据
     * </p>
     * @author yoko
     * @date 2019/12/6 20:25
     */
//    @Scheduled(cron = "5 * * * * ?")
//    @Scheduled(fixedDelay = 1000) // 每1分钟执行
    @Scheduled(fixedDelay = 60000) // 每1分钟执行
    public void countFailNum() throws Exception{
//        log.info("----------------------------------TaskWxOrder.countFailNum()----start");
        int curday = DateUtil.getDayNumber(new Date());
        // 查询策略里面的二维码连续给出失败次数
        StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.WX_NUM.getStgType());
        StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
        int failNum = strategyModel.getStgNumValue();
        // 根据条件获取派发订单里面去重复的小微ID集合
        WxOrderModel wxOrderBiWxIdQuery = TaskMethod.assembleTaskWxOrderByFialNumQuery(0, curday, 0);
        List<Long> synchroList = ComponentUtil.taskWxOrderService.getWxIdByOrderList(wxOrderBiWxIdQuery);
        for (Long data : synchroList){
            try{
                // 锁住这个数据流水
                String lockKey = CachedKeyUtils.getCacheKeyTask(TkCacheKey.LOCK_WX_ID, data);
                boolean flagLock = ComponentUtil.redisIdService.lock(lockKey);
                if (flagLock){
                    // 查询此小微最近给出订单记录
                    WxOrderModel wxOrderInfoQuery = TaskMethod.assembleTaskWxOrderByFialNumQuery(data, curday, failNum);
                    List<WxOrderModel> dataList = ComponentUtil.taskWxOrderService.getWxOrderList(wxOrderInfoQuery);
                    int checkNum = 0;// 小微是否有数据回执到服务端：0没有数据回执，1有回执
                    if (dataList != null && dataList.size() > 0){
                        if (dataList.size() == failNum){
                            for (WxOrderModel dataModel : dataList){
                                if (dataModel.getDataType() == 2){
                                    // 表示在规定范围内数据中有小微的回执消息
                                    checkNum = 1;
                                    break;
                                }
                            }
                        }else{
                            checkNum = 1;
                        }
                    }

                    if (checkNum == 0){
                        // 表示在规定范围内数据中没有小微的回执消息
                        String strKeyCache_wx_id_data = CachedKeyUtils.getCacheKey(TkCacheKey.WX_ID_DATA, data);
                        String strCache_wx_id_data = (String) ComponentUtil.redisService.get(strKeyCache_wx_id_data);
                        if (StringUtils.isBlank(strCache_wx_id_data)){
                            // 添加数据到运营表中
                            OperateModel operateModel = new OperateModel();
                            String remark = "我方小微ID：" + data + "，连续给出派单数量：" + failNum + "，没有任何回执消息" ;
                            operateModel = TaskMethod.assembleOperateData(0, null, null, 0, null, 2,
                                    "说明我方小微可能出现异常封号，获取不到数据", remark , 2, data,null);
                            ComponentUtil.operateService.add(operateModel);
                            // redis存储30分钟， 相同的小微30分钟存储一次
                            ComponentUtil.redisService.set(strKeyCache_wx_id_data, String.valueOf(data), THIRTY_MIN, TimeUnit.MINUTES);
                        }
                    }


                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey);
                }

//                log.info("----------------------------------TaskWxOrder.countFailNum()----end");
            }catch (Exception e){
                log.error(String.format("this TaskWxOrder.countFailNum() is error , the dataId=%s !", data));
                e.printStackTrace();
            }
        }
    }

}
