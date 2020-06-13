package com.hz.task.master.core.service.impl.task;

import com.hz.task.master.core.common.dao.BaseDao;
import com.hz.task.master.core.common.exception.ServiceException;
import com.hz.task.master.core.common.service.impl.BaseServiceImpl;
import com.hz.task.master.core.mapper.DidMapper;
import com.hz.task.master.core.mapper.DidRechargeMapper;
import com.hz.task.master.core.mapper.DidRewardMapper;
import com.hz.task.master.core.mapper.task.TaskDidRechargeMapper;
import com.hz.task.master.core.model.did.DidModel;
import com.hz.task.master.core.model.did.DidRechargeModel;
import com.hz.task.master.core.model.did.DidRewardModel;
import com.hz.task.master.core.service.task.TaskDidRechargeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description task:用户充值订单的Service层的实现层
 * @Author yoko
 * @Date 2020/6/4 17:27
 * @Version 1.0
 */
//@Transactional
@Service
public class TaskDidRechargeServiceImpl<T> extends BaseServiceImpl<T> implements TaskDidRechargeService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private TaskDidRechargeMapper taskDidRechargeMapper;

    @Autowired
    private DidRewardMapper didRewardMapper;// 用户奖励

    @Autowired
    private DidMapper didMapper;// 用户

    public BaseDao<T> getDao() {
        return taskDidRechargeMapper;
    }

    @Override
    public List<DidRechargeModel> getDidRechargeList(Object obj) {
        return taskDidRechargeMapper.getDidRechargeList(obj);
    }

    @Override
    public int updateDidRechargeStatus(Object obj) {
        return taskDidRechargeMapper.updateDidRechargeStatus(obj);
    }

    @Transactional(rollbackFor=Exception.class)
    @Override
    public boolean didRechargeSuccessOrder(DidRewardModel didRechargeProfit, DidRewardModel didGradeProfit, DidModel upDidMoney) throws Exception{
        int num1 = 0;
        int num2 = 0;
        int rechargeProfitNum = 0;
        int gradeProfitNum = 0;
        if (didRechargeProfit != null && !StringUtils.isBlank(didRechargeProfit.getMoney())){
            num1 = 1;
            rechargeProfitNum = didRewardMapper.add(didRechargeProfit);
        }

        if (didGradeProfit != null && !StringUtils.isBlank(didGradeProfit.getMoney())){
            num2 = 1;
            gradeProfitNum = didRewardMapper.add(didGradeProfit);
        }

//        int i = 1;
//        if (i == 1){
//            throw new ServiceException("didRechargeSuccessOrder", "故意出错!");
//
//        }

        int didNum = didMapper.updateDidMoneyByRecharge(upDidMoney);
        if (num1> 0 && num2 >0){
            if (rechargeProfitNum > 0 && gradeProfitNum > 0 && didNum > 0){
                return true;
            }else {
                    throw new ServiceException("didRechargeSuccessOrder", "三个执行更新SQL其中有一个或者多个响应行为0");
//                throw new RuntimeException();
            }
        }

        if (num1 >0 && num2 == 0){
            if (rechargeProfitNum > 0 && didNum > 0){
                return true;
            }else {
                throw new ServiceException("didRechargeSuccessOrder", "二个执行更新SQL其中有一个或者二个响应行为0");
            }
        }
        return false;
    }

    @Override
    public List<Long> getRechargeDidList(DidRechargeModel model) {
        return taskDidRechargeMapper.getRechargeDidList(model);
    }
}
