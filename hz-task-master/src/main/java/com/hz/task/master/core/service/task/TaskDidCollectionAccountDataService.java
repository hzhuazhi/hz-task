package com.hz.task.master.core.service.task;

import com.hz.task.master.core.common.service.BaseService;
import com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel;

import java.util.List;

/**
 * @Description task：检测用户收款账号给出码以及成功的信息的Service层
 * @Author yoko
 * @Date 2020/6/19 10:31
 * @Version 1.0
 */
public interface TaskDidCollectionAccountDataService<T> extends BaseService<T> {

    /**
     * @Description: 根据条件获取派发订单里面去重复的收款账号ID集合
     * <p>
     *     可以是派发订单成功状态的收款账号集合，也可以是所有只要派发的收款账号集合
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/19 10:51
     */
    public List<Long> getDidCollectionAccountList(TaskDidCollectionAccountDataModel model);

    /**
     * @Description: 根据收款账号ID查询派发订单中当日给出码的总次数
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/19 10:52
     */
    public int countLimitNum(TaskDidCollectionAccountDataModel model);

    /**
     * @Description: 根据收款账号ID查询派发订单当日给出码的总成功次数，以及总成功金额
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/19 10:54
     */
    public TaskDidCollectionAccountDataModel getSucLimitNumAndMoney(TaskDidCollectionAccountDataModel model);


    /**
     * @Description: 查询用户账号最近几条订单的成功状态集合
     * <p>
     *     根据策略限制：如果连续3条订单都是失败的，则需要把这个用户收款账号审核状态修改成初始化状态（具体几条由策略部署）
     *     查询条件：一定是订单状态大于等于2；也就是超时跟成功订单，初始化订单无需查询
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/19 10:58
     */
    public List<TaskDidCollectionAccountDataModel> getOrderStatusByDidCollectionAccount(TaskDidCollectionAccountDataModel model);
}
