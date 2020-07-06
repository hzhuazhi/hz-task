package com.hz.task.master.core.model.strategy;

/**
 * @Description 策略：检测定位客户端监听的数据是否属于支付宝转账规则配置的实体属性Bean
 * @Author yoko
 * @Date 2020/7/6 19:57
 * @Version 1.0
 */
public class StrategyZfbRule {

    /**
     * 需要匹配的关键字
     */
    private String key;

    /**
     * 需要匹配多少个关键字
     */
    private Integer keyNum;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getKeyNum() {
        return keyNum;
    }

    public void setKeyNum(Integer keyNum) {
        this.keyNum = keyNum;
    }
}
