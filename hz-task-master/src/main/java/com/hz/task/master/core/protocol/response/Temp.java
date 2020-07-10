package com.hz.task.master.core.protocol.response;

import com.hz.task.master.core.common.utils.StringUtil;

/**
 * @Description TODO
 * @Author yoko
 * @Date 2020/5/9 19:36
 * @Version 1.0
 */
public class Temp {

    public static void main(String [] args){
        String money = "1000.04";
        String data = "100000.00";

        double result = Double.parseDouble(money)/ Double.parseDouble(data);
        System.out.println("result:" + result);
        String sb1 = StringUtil.getBigDecimalDivide(money, data);
        System.out.println("sb1:" + sb1);
    }
}
