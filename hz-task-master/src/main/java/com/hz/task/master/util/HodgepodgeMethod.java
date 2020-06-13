package com.hz.task.master.util;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.utils.BeanUtils;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.model.question.QuestionMModel;
import com.hz.task.master.core.model.region.RegionModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.protocol.response.question.QuestionM;
import com.hz.task.master.core.protocol.response.question.ResponseQuestion;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @Description 公共方法类
 * @Author yoko
 * @Date 2020/1/7 20:25
 * @Version 1.0
 */
public class HodgepodgeMethod {
    private static Logger log = LoggerFactory.getLogger(HodgepodgeMethod.class);

    /**
     * @Description: 组装查询地域的查询条件
     * @param ip
     * @return RegionModel
     * @author yoko
     * @date 2019/12/18 18:41
     */
    public static RegionModel assembleRegionModel(String ip){
        RegionModel resBean = new RegionModel();
        resBean.setIp(ip);
        return resBean;
    }


    /**
     * @Description: 组装查询策略数据条件的方法
     * @return com.pf.play.rule.core.model.strategy.StrategyModel
     * @author yoko
     * @date 2020/5/19 17:12
     */
    public static StrategyModel assembleStrategyQuery(int stgType){
        StrategyModel resBean = new StrategyModel();
        resBean.setStgType(stgType);
        return resBean;
    }


    /**
     * @Description: 根据token获取缓存中用户ID的值
     * @param token - 登录token
     * @return Long
     * @author yoko
     * @date 2019/11/21 18:01
     */
    public static long getDidByToken(String token){
        Long did = 0L;
        if (!StringUtils.isBlank(token)){
            //        String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.TOKEN_INFO, token);
            String strCache = (String) ComponentUtil.redisService.get(token);
            if (!StringUtils.isBlank(strCache)) {
                // 登录存储在缓存中的用户id
                did = Long.parseLong(strCache);
            }
        }
        return did;
    }

    /**
     * @Description: 百问百答类别集合的数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param questionMList - 百问百答类别集合
     * @param rowCount - 总行数
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleQuestionMResult(long stime, String sign, List<QuestionMModel> questionMList, Integer rowCount){
        ResponseQuestion dataModel = new ResponseQuestion();
        if (questionMList != null && questionMList.size() > ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO){
            List<QuestionM> dataList = BeanUtils.copyList(questionMList, QuestionM.class);
            dataModel.qMList = dataList;
        }
        if (rowCount != null){
            dataModel.rowCount = rowCount;
        }
        dataModel.setStime(stime);
        dataModel.setSign(sign);
        return JSON.toJSONString(dataModel);
    }



    public static void main(String [] args){

        List<StrategyData> list = new ArrayList<>();
        StrategyData bean1 = new StrategyData();
        bean1.setId(1L);
        bean1.setStgKey(1L);
        bean1.setStgValue("1000.00");
        bean1.setStgValueOne("0.01");

        StrategyData bean2 = new StrategyData();
        bean2.setId(2L);
        bean2.setStgKey(2L);
        bean2.setStgValue("2000.00");
        bean2.setStgValueOne("0.02");

        StrategyData bean3 = new StrategyData();
        bean3.setId(3L);
        bean3.setStgKey(3L);
        bean3.setStgValue("3000.00");
        bean3.setStgValueOne("0.03");

        StrategyData bean4 = new StrategyData();
        bean4.setId(4L);
        bean4.setStgKey(4L);
        bean4.setStgValue("4000.00");
        bean4.setStgValueOne("0.04");

        StrategyData bean5 = new StrategyData();
        bean5.setId(5L);
        bean5.setStgKey(5L);
        bean5.setStgValue("5000.00");
        bean5.setStgValueOne("0.05");

        list.add(bean1);
        list.add(bean2);
        list.add(bean3);
        list.add(bean4);
        list.add(bean5);
        String str = JSON.toJSONString(list);
        System.out.println(str);
    }


    

}
