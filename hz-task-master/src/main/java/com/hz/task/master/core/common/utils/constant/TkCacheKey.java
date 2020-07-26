package com.hz.task.master.core.common.utils.constant;

/**
 * @Description task任务的redis的key
 * @Author yoko
 * @Date 2020/6/3 14:50
 * @Version 1.0
 */
public interface TkCacheKey {

    /**
     * 手机短信类容的task的redis
     */
    String LOCK_MOBILE_CARD_DATA = "-1";

    /**
     * 银行卡回调短信work数据填充的task的redis
     */
    String LOCK_BANK_COLLECTION_DATA_WORK_TYPE = "-2";

    /**
     * 手机号数据
     */
    String  MOBILE_CARD = "-3";

    /**
     * 银行卡回调短信work数据填充完毕的task的redis
     */
    String LOCK_BANK_COLLECTION_DATA_WORK_TYPE_IS_OK = "-4";


    /**
     * 用户充值订单加锁
     * 锁住
     */
    String LOCK_DID_RECHARGE = "-5";

    /**
     * 用户充值订单加锁-已失效的订单
     * 锁住
     */
    String LOCK_DID_RECHARGE_INVALID = "-6";

    /**
     * 锁：用户充值成功订单的流水
     * 锁住
     */
    String LOCK_DID_RECHARGE_SUCCESS_ORDER = "-7";

    /**
     * 锁：用户奖励数据的流水
     * 锁住
     */
    String LOCK_DID_REWARD = "-8";

    /**
     * 锁：可爱猫原始数据的流水
     * 锁住
     */
    String LOCK_CAT_ALL_DATA = "-9";


    /**
     * 锁：可爱猫回调订单-补充数据的流水
     * 锁住
     */
    String LOCK_CAT_DATA_WORK_TYPE = "-10";

    /**
     * 锁：可爱猫回调订单-正式匹配派单数据
     * 锁住
     */
    String LOCK_CAT_DATA = "-11";

    /**
     * 锁：可爱猫回调订单匹配派单数据的订单数据
     * 锁住
     */
    String LOCK_CAT_DATA_BY_ORDER = "-12";

    /**
     * 锁：失效订单的流水- 派单数据
     * 锁住
     */
    String LOCK_ORDER_INVALID = "-13";

    /**
     * 锁：成功订单的流水- 派单数据
     * 锁住
     */
    String LOCK_ORDER_SUCCESS = "-14";

    /**
     * 锁：成功订单的流水的数据同步给下游- 派单数据
     * 锁住
     */
    String LOCK_ORDER_NOTIFY = "-15";

    /**
     * 锁：可爱猫回调店员下线的数据的流水
     * 锁住
     */
    String LOCK_CAT_DATA_OFFLINE = "-16";

    /**
     * 锁：银行卡金额流水task的redis
     * 锁住
     */
    String LOCK_BANK = "-17";

    /**
     * 锁：可爱猫回调店员绑定小微的数据的流水
     * 锁住
     */
    String LOCK_CAT_DATA_BINDING = "-18";

    /**
     * 锁：检测用户收款账号给出码次数的数据的流水-这里锁用户账号ID
     * 锁住
     */
    String LOCK_DID_COLLECTION_ACCOUNT_LIMIT_NUM = "-19";

    /**
     * 锁：检测用户收款账号给出码订单成功次数，订单金额的数据的流水-这里锁用户账号ID
     * 锁住
     */
    String LOCK_DID_COLLECTION_ACCOUNT_LIMIT_SUC_NUM = "-20";

    /**
     * 锁：检测用户收款账号连续失败次数-这里锁用户账号ID
     * 锁住
     */
    String LOCK_DID_COLLECTION_ACCOUNT_FAIL_NUM = "-21";

    /**
     * 锁：用户扣减余额流水的数据的流水
     * 锁住
     */
    String LOCK_DID_BALANCE_DEDUCT = "-22";

    /**
     * 锁：客户端监听数据回调原始数据的的流水
     * 锁住
     */
    String LOCK_CLIENT_ALL_DATA = "-23";

    /**
     * 锁：客户端监听数据回调订单-补充数据的流水
     * 锁住
     */
    String LOCK_CLIENT_DATA_WORK_TYPE = "-24";

    /**
     * 锁：客户端监听数据回调订单-正式匹配派单数据
     * 锁住
     */
    String LOCK_CLIENT_DATA = "-25";

    /**
     * 锁：已超过失效时间，并且用户操作状态属于初始化状态的流水- 派单数据
     * 锁住
     */
    String LOCK_ORDER_INVALID_BY_DID_STATUS_INIT = "-26";

    /**
     * 锁：可爱猫数据解析-补充数据的流水
     * 锁住
     */
    String LOCK_CAT_DATA_ANALYSIS_WORK_TYPE = "-27";

    /**
     * 锁：失效订单的流水:正要执行- 派单数据
     * 锁住
     */
    String LOCK_ORDER_INVALID_START = "-28";

    /**
     * 锁：可爱猫数据解析-补充成功数据的流水
     * 锁住
     */
    String LOCK_CAT_DATA_ANALYSIS_WORK_TYPE_SUC = "-29";

}
