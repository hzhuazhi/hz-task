package com.hz.task.master.core.model.strategy;

/**
 * @Description 策略：检测截取客户端监听的数据中支付宝转账的具体金额规则配置的实体属性Bean
 * @Author yoko
 * @Date 2020/7/6 19:58
 * @Version 1.0
 */
public class StrategyZfbMoneyRule {

    /**
     * 截取关键字开始
     */
    private String startKey;

    /**
     * 截取关键字结束
     */
    private String endKey;


    public String getStartKey() {
        return startKey;
    }

    public void setStartKey(String startKey) {
        this.startKey = startKey;
    }

    public String getEndKey() {
        return endKey;
    }

    public void setEndKey(String endKey) {
        this.endKey = endKey;
    }
}
