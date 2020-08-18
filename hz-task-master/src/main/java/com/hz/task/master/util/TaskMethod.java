package com.hz.task.master.util;

import com.alibaba.fastjson.JSON;
import com.hz.task.master.core.common.exception.ServiceException;
import com.hz.task.master.core.common.utils.DateUtil;
import com.hz.task.master.core.common.utils.StringUtil;
import com.hz.task.master.core.common.utils.constant.ErrorCode;
import com.hz.task.master.core.common.utils.constant.ServerConstant;
import com.hz.task.master.core.model.bank.BankCollectionDataModel;
import com.hz.task.master.core.model.bank.BankCollectionModel;
import com.hz.task.master.core.model.bank.BankModel;
import com.hz.task.master.core.model.bank.BankTransferModel;
import com.hz.task.master.core.model.cat.CatDataAnalysisModel;
import com.hz.task.master.core.model.cat.CatDataBindingModel;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.client.ClientCollectionDataModel;
import com.hz.task.master.core.model.client.ClientDataModel;
import com.hz.task.master.core.model.did.*;
import com.hz.task.master.core.model.mobilecard.MobileCardDataModel;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import com.hz.task.master.core.model.operate.OperateModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.order.OrderStepModel;
import com.hz.task.master.core.model.pool.PoolOpenModel;
import com.hz.task.master.core.model.pool.PoolOriginModel;
import com.hz.task.master.core.model.pool.PoolWaitModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.strategy.StrategyZfbMoneyRule;
import com.hz.task.master.core.model.strategy.StrategyZfbRule;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.cat.CatGuest;
import com.hz.task.master.core.model.task.cat.CatMember;
import com.hz.task.master.core.model.task.cat.CatMsg;
import com.hz.task.master.core.model.task.cat.FromCatModel;
import com.hz.task.master.core.model.task.client.ClientModel;
import com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel;
import com.hz.task.master.core.model.task.wx.WxClient;
import com.hz.task.master.core.model.wx.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @Description 定时任务的公共类
 * @Author yoko
 * @Date 2020/1/11 16:20
 * @Version 1.0
 */
public class TaskMethod {
    private static Logger log = LoggerFactory.getLogger(TaskMethod.class);


    /**
     * @Description: 组装查询定时任务的查询条件
     * @param limitNum - 多少条数据
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskStatusQuery(int limitNum){
        StatusModel resBean = new StatusModel();
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setLimitNum(limitNum);
        return resBean;
    }


    /**
     * @Description: 组装更改运行状态的数据
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleTaskUpdateStatusModel(long id, int runStatus){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        resBean.setRunStatus(runStatus);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        return resBean;
    }



    /**
     * @Description: 组装更改运行状态的数据
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param info - 纪录失败的原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByInfo(long id, int runStatus, String info){
        StatusModel resBean = new StatusModel();
        resBean.setRunStatus(runStatus);
        resBean.setId(id);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        return resBean;
    }


    /**
     * @Description: 组装查询定时-填充的查询条件
     * @param limitNum - 多少条数据
     * @param workType - 补充数据的类型：1初始化，2补充失败，3补充数据成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskByWorkTypeQuery(int limitNum, int workType){
        StatusModel resBean = new StatusModel();
        resBean.setWorkType(workType);
        resBean.setLimitNum(limitNum);
        return resBean;
    }

    /**
     * @Description: 组装查询定时任务数据-填充的-未runStatus过的查询条件
     * @param limitNum - 多少条数据
     * @param workType - 补充数据的类型：1初始化，2补充数据失败（未匹配到银行卡的数据），3补充数据成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskByWorkTypeAndRunStatusQuery(int limitNum, int workType){
        StatusModel resBean = new StatusModel();
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setWorkType(workType);
        resBean.setLimitNum(limitNum);
        return resBean;
    }




    /**
     * @Description: 组装查询定时任务手机短信的查询条件
     * @param limitNum - 多少条数据
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
    */
    public static StatusModel assembleTaskByMobileCardDataQuery(int limitNum){
        StatusModel resBean = new StatusModel();
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setLimitNum(limitNum);
        return resBean;
    }


    /**
     * @Description: 组装查询定时任务银行卡数据填充的查询条件
     * @param limitNum - 多少条数据
     * @param workType - 补充数据的类型：1初始化，2补充数据失败（未匹配到银行卡的数据），3补充数据成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskBankCollectionDataByWorkTypeQuery(int limitNum, int workType){
        StatusModel resBean = new StatusModel();
//        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
//        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setWorkType(workType);
        resBean.setLimitNum(limitNum);
        return resBean;
    }



    /**
     * @Description: 组装更改运行状态的数据-手机卡短信类容
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByMobileCardDataModel(long id, int runStatus, int dataType){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        resBean.setRunStatus(runStatus);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        resBean.setDataType(dataType);
        return resBean;
    }


    /**
     * @Description: 组装更改运行状态的数据-数据填充
     * @param id - 主键ID
     * @param workType - 补充数据的类型：1初始化，2补充数据失败（），3补充数据成功
     * @param info - 纪录失败的原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByWorkType(long id, int workType, String info){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        resBean.setWorkType(workType);
        return resBean;
    }


    /**
     * @Description: 组装查询定时任务根据订单状态-失效订单、成功订单等。
     * @param limitNum - 多少条数据
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3有质疑，4成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskByOrderStatusQuery(int limitNum, int orderStatus){
        StatusModel resBean = new StatusModel();
        resBean.setOrderStatus(orderStatus);
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setLimitNum(limitNum);
        return resBean;
    }


    /**
     * @Description: 组装查询定时任务根据订单状态-失效订单、成功订单等。
     * @param limitNum - 多少条数据
     * @param didStatus - 订单状态_用户操作的状态：1初始化，2失败，3超时后默认成功，4用户点击成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskByOrderDidStatusQuery(int limitNum, int didStatus){
        StatusModel resBean = new StatusModel();
        resBean.setDidStatus(didStatus);
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setLimitNum(limitNum);
        return resBean;
    }


    /**
     * @Description: 组装查询定时任务根据订单状态-失效订单、成功订单等。
     * @param limitNum - 多少条数据
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskByOrderNotifyQuery(int limitNum){
        StatusModel resBean = new StatusModel();
        resBean.setSendNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setSendStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setLimitNum(limitNum);
        return resBean;
    }


    /**
     * @Description: 组装更改发送状态的数据
     * @param id - 主键ID
     * @param sendStatus - 发送状态：0初始化，1锁定，2计算失败，3计算成功
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateSendStatus(long id, int sendStatus){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        resBean.setSendStatus(sendStatus);
        if (sendStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setSendNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        return resBean;
    }




    /**
     * @Description: 组装更改运行状态的数据-银行卡数据填充
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param workType - 补充数据的类型：1初始化，2补充数据失败（未匹配到银行卡的数据等），3补充数据成功
     * @param info - 纪录失败的原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByBankCollectionDataWorkType(long id, int runStatus, int workType, String info){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
//        resBean.setRunStatus(runStatus);
//        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
//            // 表示失败：失败则需要运行次数加一
//            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
//        }
        resBean.setWorkType(workType);
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        return resBean;
    }


    /**
     * @Description: 校验策略类型数据
     * @return void
     * @author yoko
     * @date 2019/12/2 14:35
     */
    public static void checkStrategyByMobileCardTypeList(StrategyModel strategyModel) throws Exception{
        if (strategyModel == null){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00001.geteCode(), ErrorCode.ENUM_ERROR.S00001.geteDesc());
        }
    }


    /**
     * @Description: 归类短信归属类型
     * @param mobileCardDataModel - 短信信息
     * @param strategyDataList - 策略：检查手机短信类型的规则
     * @return
     * @author yoko
     * @date 2020/6/3 16:23
    */
    public static int screenMobileCardDataType(MobileCardDataModel mobileCardDataModel, List<StrategyData> strategyDataList){
        int type = 0;// 筛选之后的最终类型
        while (true){
            for (StrategyData strategyData : strategyDataList){
                int keyType = strategyData.getStgValueTwo();// 策略：短信定义的类型
                String[] keyArray = strategyData.getStgValue().split(",");// 策略：短信分类的关键字
                int keyNum = 0;// 短信分类需要满足几个关键字的符合
                int countKeyNum = 0;// 计算已经满足了几个关键字的符合
                // 循环关键字匹配
                if (keyType == 1){
                    keyNum = 2;
                    // 只需要匹配一个关键字：广告类型
                    countKeyNum  = countAccordWithKey(mobileCardDataModel.getSmsContent(), keyArray);// 具体筛选
                    if (countKeyNum >= keyNum){
                        type = 1;// 属于广告短信
                        break;
                    }
                }else if(keyType == 2){
                    // 银行卡挂失短息
                    keyNum = 2;
                    // 需要匹配二个关键字：银行卡挂失
                    countKeyNum  = countAccordWithKey(mobileCardDataModel.getSmsContent(), keyArray);// 具体筛选
                    if (countKeyNum >= keyNum){
                        type = 2;// 属于银行卡挂失
                        break;
                    }
                }else if(keyType == 3){
                    // 手机欠费短息
                    keyNum = 1;
                    // 需要匹配一个关键字：手机欠费
                    countKeyNum  = countAccordWithKey(mobileCardDataModel.getSmsContent(), keyArray);// 具体筛选
                    if (countKeyNum >= keyNum){
                        type = 3;// 属于手机欠费
                        break;
                    }
                }else if(keyType == 4){
                    // 普通短息：银行收款信息
                    keyNum = 4;
                    // 需要匹配四个关键字：银行收款信息
                    countKeyNum  = countAccordWithKey(mobileCardDataModel.getSmsContent(), keyArray);// 具体筛选
                    if (countKeyNum >= keyNum){
                        type = 4;// 属于银行收款信息
                        break;
                    }
                }else if(keyType == 5){
                    // 手机号变更
                    keyNum = 3;
                    // 需要匹配三个关键字：手机号变更
                    countKeyNum  = countAccordWithKey(mobileCardDataModel.getSmsContent(), keyArray);// 具体筛选
                    if (countKeyNum >= keyNum){
                        type = 5;// 属于手机号变更
                        break;
                    }
                }

            }
            if (type == 0){
                type = 6;// 类型6表示其它类型的
            }
            break;

        }
        return type;
    }

    /**
     * @Description: 计算满足了几个关键字
     * @param content - 短信类容
     * @param keyArray - 关键字
     * @return
     * @author yoko
     * @date 2020/6/3 15:38
    */
    public static int countAccordWithKey(String content, String[] keyArray){
        int count = 0;// 计算已经满足了几个关键字的符合
        for (String str : keyArray){
            if (content.indexOf(str) > -1){
                count ++;
            }
        }
        return count;
    }

    /**
     * @Description: 组装根据端口号查询银行卡的查询条件
     * @param smsNum - 短信端口号
     * @return
     * @author yoko
     * @date 2020/6/3 17:09
    */
    public static BankModel assembleBankQuery(String smsNum){
        BankModel resBean = new BankModel();
        resBean.setSmsNum(smsNum);
        return resBean;
    }


    /**
     * @Description: 根据银行卡尾号去匹配短信类容里面的尾号来筛选出归属的银行卡
     * @param bankList -  银行卡集合信息
     * @param smsContent - 短信类容
     * @return
     * @author yoko
     * @date 2020/6/3 17:07
    */
    public static BankModel screenBankReportTheLossOf(List<BankModel> bankList, String smsContent){
        for (BankModel dataModel : bankList){
            if (smsContent.indexOf(dataModel.getLastNum()) > -1){
                return dataModel;
            }
        }
        return null;
    }



    /**
     * @Description: 组装更新手机卡的使用状态的数据
     * @param phoneNum - 手机号
     * @param info - 停用原因
     * @return
     * @author yoko
     * @date 2020/6/3 17:29
    */
    public static MobileCardModel assembleUpMobileCardUseStatusData(String phoneNum, String info){
        MobileCardModel resBean = new MobileCardModel();
        resBean.setPhoneNum(phoneNum);
        resBean.setRemark(info);// 停用原因
        resBean.setUseStatus(2);
        return resBean;
    }


    /**
     * @Description: 组装银行卡的回传数据
     * @param mobileCardDataModel - 手机短信
     * @return
     * @author yoko
     * @date 2020/6/3 17:47
    */
    public static BankCollectionDataModel assembleBankCollectionDataModel(MobileCardDataModel mobileCardDataModel){
        BankCollectionDataModel resBean = new BankCollectionDataModel();
        if (!StringUtils.isBlank(mobileCardDataModel.getPhoneNum())){
            resBean.setPhoneNum(mobileCardDataModel.getPhoneNum());
        }

        if (!StringUtils.isBlank(mobileCardDataModel.getSmsNum())){
            resBean.setSmsNum(mobileCardDataModel.getSmsNum());
        }else {
            // 短信端口号都为空，则肯定返回空出去：必填项
            return null;
        }

        if (!StringUtils.isBlank(mobileCardDataModel.getSmsContent())){
            resBean.setSmsContent(mobileCardDataModel.getSmsContent());
        }else {
            // 短信类容：必填项
            return null;
        }

        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));

        return resBean;
    }


    /**
     * @Description: 组装查询手机号信息的方法
     * @param phoneNum - 手机号
     * @return
     * @author yoko
     * @date 2020/6/3 20:16
    */
    public static MobileCardModel assembleMobileCardQueryByPhoneNum(String phoneNum){
        MobileCardModel resBean = new MobileCardModel();
        resBean.setPhoneNum(phoneNum);
        return resBean;
    }


    /**
     * @Description: 组装查询银行卡信息的查询条件
     * @param mobileCardId - 手机号的主键ID
     * @param smsNum - 短信端口号
     * @return
     * @author yoko
     * @date 2020/6/3 20:26
    */
    public static BankModel assembleBankByMobileCardAndSmsNumQuery(long mobileCardId, String smsNum){
        BankModel resBean = new BankModel();
        resBean.setMobileCardId(mobileCardId);
        resBean.setSmsNum(smsNum);
        return resBean;
    }


    /**
     * @Description: 根据短信内容中包含的银行卡尾号与银行卡的尾号进行匹配-筛选出符合的银行卡
     * @param bankList - 银行卡集合
     * @param smsContent - 银行短信类容
     * @return
     * @author yoko
     * @date 2020/6/4 11:03
    */
    public static BankModel bankMatchingByLastNum(List<BankModel> bankList, String smsContent){
        for (BankModel bankModel : bankList){
            if (smsContent.indexOf(bankModel.getLastNum()) > -1){
                return bankModel;
            }
        }
        return null;
    }


    /**
     * @Description: 组装填充银行卡回调的信息
     * @param bankModel - 银行卡信息
     * @param bankCollectionDataModel - 银行回调的短信信息
     * @param mobileCardId - 手机号的主键ID
     * @return
     * @author yoko
     * @date 2020/6/4 11:33
    */
    public static BankCollectionDataModel assembleBankCollectionData(BankModel bankModel, BankCollectionDataModel bankCollectionDataModel, long mobileCardId) throws Exception{
        BankCollectionDataModel resBean = new BankCollectionDataModel();
        resBean.setId(bankCollectionDataModel.getId());
        resBean.setMobileCardId(mobileCardId);
        resBean.setBankId(bankModel.getId());

        //截取短信类容获取短信类容里面的金额
        String [] startKeyArr = bankModel.getStartKey().split("#");
        String [] endKeyArr = bankModel.getEndKey().split("#");

        int startIndex = 0;
        int endIndex = 0;
        for (String str : startKeyArr){
            startIndex = getIndexOfByStr(bankCollectionDataModel.getSmsContent(), str);
            if (startIndex > 0){
                startIndex = startIndex + str.length();
                break;
            }
        }

        for (String str : endKeyArr){
            endIndex = getIndexOfByStr(bankCollectionDataModel.getSmsContent(), str);
            if (endIndex > 0){
                break;
            }
        }
        String money = bankCollectionDataModel.getSmsContent().substring(startIndex, endIndex).replaceAll(",","");
        if (StringUtils.isBlank(money)){
            return null;
        }else {
            resBean.setMoney(money);
        }
        return resBean;
    }

    /**
     * @Description: 获取key在内容中的下标位
     * @param content - 类容
     * @param key - 匹配的关键字
     * @return
     * @author yoko
     * @date 2020/6/4 11:18
    */
    public static int getIndexOfByStr(String content, String key){
        if (content.indexOf(key) > -1){
            return content.indexOf(key);
        }else {
            return 0;
        }
    }








    /**
     * @Description: 组装查询定时任务已经补充完毕数据的银行回调数据
     * @param limitNum - 多少条数据
     * @param workType - 补充数据的类型：1初始化，2补充数据失败（未匹配到银行卡的数据），3补充数据成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskBankCollectionDataByOrderQuery(int limitNum, int workType){
        StatusModel resBean = new StatusModel();
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setWorkType(workType);
        resBean.setLimitNum(limitNum);
        return resBean;
    }

    /**
     * @Description: 组装查询用户充值订单的查询条件
     * @param bankId - 银行卡主键ID
     * @return
     * @author yoko
     * @date 2020/6/4 15:03
    */
    public static DidRechargeModel assembleDidRechargeQuery(long bankId, int workType){
        DidRechargeModel resBean = new DidRechargeModel();
        resBean.setBankId(bankId);
        resBean.setOrderStatus(1);
        resBean.setInvalidTime("1");
        resBean.setWorkType(workType);
        return resBean;
    }


    /**
     * @Description: 组装更改运行状态的数据-银行卡数据已填充的匹配
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param info - 纪录失败的原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByBankCollectionDataByOrder(long id, int runStatus, String info){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        resBean.setRunStatus(runStatus);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        return resBean;
    }


    /**
     * @Description: 组装更新用户充值订单的订单状态
     * @param id - 用户充值订单的主键ID
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3成功
     * @return
     * @author yoko
     * @date 2020/6/4 15:58
    */
    public static DidRechargeModel assembleDidRechargeUpdateStatus(long id, int orderStatus){
        DidRechargeModel resBean = new DidRechargeModel();
        resBean.setId(id);
        resBean.setUpOrderStatus(orderStatus);
        return resBean;
    }


    /**
     * @Description: 组装添加银行收款数据的方法
     * @param bankId - 银行卡主键ID
     * @param orderNo - 用户的充值订单号
     * @param money - 收款金额
     * @return com.hz.task.master.core.model.bank.BankCollectionModel
     * @author yoko
     * @date 2020/6/4 16:04
     */
    public static BankCollectionModel assembleBankCollectionAdd(long bankId, String orderNo, String money){
        BankCollectionModel resBean = new BankCollectionModel();
        resBean.setBankId(bankId);
        resBean.setOrderNo(orderNo);
        resBean.setMoney(money);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 填充银行卡数据中的订单号数据
     * @param id - 银行卡数据的主键ID
     * @param orderNo - 用户充值订单号
     * @return com.hz.task.master.core.model.bank.BankCollectionDataModel
     * @author yoko
     * @date 2020/6/4 16:11
     */
    public static BankCollectionDataModel assembleBankCollectionDataUpdate(long id, String orderNo){
        BankCollectionDataModel resBean = new BankCollectionDataModel();
        resBean.setId(id);
        resBean.setOrderNo(orderNo);
        return resBean;
    }


    /**
     * @Description: 组装查询定时任务用户充值订单为初始化的数据
     * @param limitNum - 多少条数据
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3成功
     * @param invalidTime - 失效时间
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskDidRechargeByInvalidTimeQuery(int limitNum, int orderStatus, String invalidTime){
        StatusModel resBean = new StatusModel();
//        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
//        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setOrderStatus(orderStatus);
        resBean.setLimitNum(limitNum);
        if (!StringUtils.isBlank(invalidTime)){
            resBean.setInvalidTime(invalidTime);
        }
        return resBean;
    }



    /**
     * @Description: 组装更改运行状态的数据-订单状态
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3成功
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByDidRechargeByOrderStatus(long id, int runStatus, Integer orderStatus){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        resBean.setRunStatus(runStatus);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        if (orderStatus != null && orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        return resBean;
    }


    /**
     * @Description: 组装查询定时任务用户充值订单为成功的数据的查询条件
     * @param limitNum - 多少条数据
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3成功
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static StatusModel assembleTaskDidRechargeBySuccessOrderQuery(int limitNum, int orderStatus){
        StatusModel resBean = new StatusModel();
        resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.RUN_NUM_FIVE);
        resBean.setRunStatus(ServerConstant.PUBLIC_CONSTANT.RUN_STATUS_THREE);
        resBean.setOrderStatus(orderStatus);
        resBean.setLimitNum(limitNum);
        return resBean;
    }

    /**
     * @Description: 计算用户自己的充值收益
     * @param moneyList - 策略：充值金额列表以及对应的金额的奖励
     * @param moneyId - 对应充值成功订单的金额的主键ID
     * @return java.lang.String
     * @author yoko
     * @date 2020/6/4 19:54
     */
    public static String getRechargeProfit(List<StrategyData> moneyList, long moneyId){
        String str = "";
        for (StrategyData strategyData : moneyList){
            if (strategyData.getId() == moneyId){
                str = StringUtil.getMultiply(strategyData.getStgValue(), strategyData.getStgValueOne());
                double res = Double.parseDouble(str);
                if (res > 0){
                    break;
                }else{
                    return null;
                }
            }
        }
        return str;
    }



    /**
     * @Description: 组装更改运行状态的数据-订单状态
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param info - 错误原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByDidRechargeBySuccessOrderStatus(long id, int runStatus, String info){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        resBean.setRunStatus(runStatus);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }

        return resBean;
    }

    /**
     * @Description: 组装查询用户信息的查询条件
     * @param did - 用户ID
     * @return
     * @author yoko
     * @date 2020/6/5 9:49
    */
    public static DidModel assembleDidQueryByDid(long did){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        return resBean;
    }

    /**
     * @Description: 计算用户档次奖励的金额
     * @param moneyGradeList - 档次奖励规则
     * @param totalRechargeMoney - 总充值金额
     * @param orderMoney - 本次充值金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/6/5 11:08
     */
    public static Map<String, String> getGradeProfit(List<StrategyData> moneyGradeList, String totalRechargeMoney, String orderMoney){
        List<StrategyData> dataList = new ArrayList<>();
        for (StrategyData moneyGrade : moneyGradeList){
            // 筛选符合档次的金额
            boolean flag = StringUtil.getBigDecimalSubtract(totalRechargeMoney, moneyGrade.getStgValue());
            if (flag){
                dataList.add(moneyGrade);
            }
        }
        if (dataList == null || dataList.size() <= 0){
            return null;
        }

        // 从符合档次的金额中，找出最大符合的奖励
        Optional<StrategyData> strategyDataMax = dataList.stream().max(Comparator.comparing(StrategyData :: getStgValue));
        StrategyData dataModel = strategyDataMax.get();

        // 计算档次奖励的具体金额; 档次奖励 = 充值金额 * 档次奖励的规则比例
        String gradeProfit = StringUtil.getMultiply(orderMoney, dataModel.getStgValueOne());
        Map<String, String> map = new HashMap<>();
        map.put("gradeProfit", gradeProfit);
        map.put("stgGradeProfit", dataModel.getStgValue());
        return map;
    }


    /**
     * @Description: 组装充多少送多少的数据
     * @param didRechargeModel - 充值成功订单
     * @param rewardType - 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励
     * @param rechargeProfit - 充多少送多少的收益值
     * @return com.hz.task.master.core.model.did.DidRewardModel
     * @author yoko
     * @date 2020/6/5 11:27
     */
    public static DidRewardModel assembleDidRechargeProfit(DidRechargeModel didRechargeModel, int rewardType, String rechargeProfit){
        DidRewardModel resBean = new DidRewardModel();
        resBean.setDid(didRechargeModel.getDid());
        resBean.setOrderNo(didRechargeModel.getOrderNo());
        resBean.setMoney(rechargeProfit);
        resBean.setRewardType(rewardType);
        resBean.setProof(didRechargeModel.getOrderMoney());
        resBean.setOrigin(didRechargeModel.getOrderMoney());
        resBean.setOriginIid(didRechargeModel.getDid());
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;

    }


    /**
     * @Description: 组装充值档次奖励的数据
     * @param didRechargeModel - 充值成功订单
     * @param rewardType - 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励
     * @param didGradeProfit - 档次奖励的收益值
     * @param stgGradeProfit - 策略：具体的档次金额
     * @return com.hz.task.master.core.model.did.DidRewardModel
     * @author yoko
     * @date 2020/6/5 11:27
     */
    public static DidRewardModel assembleDidGradeProfit(DidRechargeModel didRechargeModel, int rewardType, String didGradeProfit, String stgGradeProfit){
        DidRewardModel resBean = new DidRewardModel();
        resBean.setDid(didRechargeModel.getDid());
        resBean.setOrderNo(didRechargeModel.getOrderNo());
        resBean.setMoney(didGradeProfit);
        resBean.setRewardType(rewardType);
        resBean.setProof(stgGradeProfit);
        resBean.setOrigin(didRechargeModel.getOrderMoney());
        resBean.setOriginIid(didRechargeModel.getDid());
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: 组装用户充值成功后更新用户的信息
     * @param did - 用户ID
     * @param money - 充值的金额
     * @return
     * @author yoko
     * @date 2020/6/5 11:57
    */
    public static DidModel assembleUpdateDidMoneyByRecharge(long did, String money){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        resBean.setRecharge(money);
        resBean.setVipType(2);
        return resBean;
    }


    /**
     * @Description: 组装查询昨天充值成功的用户
     * @return
     * @author yoko
     * @date 2020/6/5 18:16
    */
    public static DidRechargeModel assembleTaskDidRechargeByDirectQuery(){
        DidRechargeModel resBean = new DidRechargeModel();
        resBean.setOrderStatus(3);
        resBean.setCurday(DateUtil.getIntYesterday());
        return resBean;
    }


    /**
     * @Description: 根据用户ID查询这个用户旗下的直推用户或者裂变用户的查询条件
     * @param levelDid - 层级关系的用户ID
     * @param levelType - 层级关系类型：1直推关系，2裂变关系
     * @return com.hz.task.master.core.model.did.DidLevelModel
     * @author yoko
     * @date 2020/6/5 19:14
     */
    public static DidLevelModel assembleDidLevelQuery(long levelDid, int levelType){
        DidLevelModel resBean = new DidLevelModel();
        resBean.setLevelDid(levelDid);
        resBean.setLevelType(levelType);
        return resBean;
    }

    /**
     * @Description: 组装查询昨天用户充值成功的查询条件
     * @param did - 用户ID
     * @return
     * @author yoko
     * @date 2020/6/5 19:37
    */
    public static DidRechargeModel assembleDidRechargeByDidQuery(long did){
        DidRechargeModel resBean = new DidRechargeModel();
        resBean.setDid(did);
        resBean.setOrderStatus(3);
        resBean.setCurday(DateUtil.getIntYesterday());
        return resBean;
    }



    /**
     * @Description: 组装直推用户的奖励数据
     * @param didRechargeModel - 充值成功订单
     * @param rewardType - 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励
     * @param did - 获得奖励的用户ID
     * @param acNum - 触发奖励机制的用户账号
     * @param directProfit - 直推奖励的金额
     * @return com.hz.task.master.core.model.did.DidRewardModel
     * @author yoko
     * @date 2020/6/5 11:27
     */
    public static DidRewardModel assembleDidDirectProfit(DidRechargeModel didRechargeModel, int rewardType, long did, String acNum, String directProfit){
        DidRewardModel resBean = new DidRewardModel();
        resBean.setDid(did);
        resBean.setOrderNo(didRechargeModel.getOrderNo());
        resBean.setMoney(directProfit);
        resBean.setRewardType(rewardType);
        resBean.setProof(acNum);
        resBean.setOrigin(didRechargeModel.getOrderMoney());
        resBean.setOriginIid(didRechargeModel.getDid());
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 组装10团队长直推的用户消耗成功奖励数据
     * @param rewardType - 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励，5团队奖励，6订单成功消耗奖励，7团队日派单消耗成功，8触发额度奖励，9团队总额等级奖励，10团队长直推的用户消耗成功奖励
     * @param moneyReward - 奖励金额
     * @param did - 获得奖励的用户ID
     * @param orderModel - 订单号
     * @return com.hz.task.master.core.model.did.DidRewardModel
     * @author yoko
     * @date 2020/6/5 11:27
     */
    public static DidRewardModel assembleTeamDirectConsumeProfit(int rewardType, long did, String moneyReward, OrderModel orderModel){
        DidRewardModel resBean = new DidRewardModel();
        resBean.setDid(did);
        resBean.setOrderNo(orderModel.getOrderNo());
        resBean.setMoney(moneyReward);
        resBean.setRewardType(rewardType);
        resBean.setProof(orderModel.getOrderMoney());
        resBean.setOrigin(orderModel.getOrderMoney());
        resBean.setOriginIid(orderModel.getDid());
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 组装查询直推用户昨天充值成功的总金额的查询条件
     * @param didList - 用户ID集合：直推用户集合
     * @return
     * @author yoko
     * @date 2020/6/6 11:40
    */
    public static DidRechargeModel assembleDidRechargeByDidQuery(List<Long> didList){
        DidRechargeModel resBean = new DidRechargeModel();
        resBean.setDidList(didList);
        resBean.setOrderStatus(3);
        resBean.setCurday(DateUtil.getIntYesterday());
        return resBean;
    }
    
    
    /**
     * @Description: 组装查询可爱猫的查询条件
     * @param toWxid - 可爱猫的to_wxid
     * @return 
     * @author yoko
     * @date 2020/6/6 19:31
    */
    public static WxModel assembleWxModel(String toWxid){
        WxModel resBean = new WxModel();
        resBean.setToWxid(toWxid);
        return resBean;
    }


    /**
     * @Description: 计算团队日充值奖励金额
     * @param teamRewardList - 团队日充值累计总额奖励规则列表
     * @param directSumMoney - 团队日充值成功总金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/6/6 11:48
     */
    public static String getTeamProfit(List<StrategyData> teamRewardList, String directSumMoney){
        String teamProfit = "";
        for (StrategyData dataModel : teamRewardList){
            String [] rule = dataModel.getStgValue().split("-");
            String ratio = dataModel.getStgValueOne();
            if(rule[0].equals(rule[1])){
                double dbl = Double.parseDouble(rule[0]);
                if (Double.parseDouble(directSumMoney) >= dbl){
                    teamProfit = StringUtil.getMultiply(directSumMoney, ratio);
                    break;
                }
            }else{
                double start = Double.parseDouble(rule[0]);
                double end = Double.parseDouble(rule[1]);
                if (Double.parseDouble(directSumMoney) >= start && Double.parseDouble(directSumMoney) <= end){
                    teamProfit = StringUtil.getMultiply(directSumMoney, ratio);
                    break;
                }
            }
        }
        return teamProfit;
    }



    /**
     * @Description: 组装团队奖励用户的奖励数据
     * @param rewardType - 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励，5团队奖励，6订单成功消耗奖励，7团队日派单消耗成功
     * @param did - 获得奖励的用户ID
     * @param teamProfit - 团队奖励的金额
     * @param directSumMoney - 昨日团队总充值成功金额
     * @return com.hz.task.master.core.model.did.DidRewardModel
     * @author yoko
     * @date 2020/6/5 11:27
     */
    public static DidRewardModel assembleDidDirectProfit(int rewardType, long did, String teamProfit, String directSumMoney){
        DidRewardModel resBean = new DidRewardModel();
        resBean.setDid(did);
        resBean.setOrderNo(String.valueOf(DateUtil.getIntYesterday()));// 写明是昨天的
        resBean.setMoney(teamProfit);
        resBean.setRewardType(rewardType);
        resBean.setProof(directSumMoney);
        resBean.setOrigin(directSumMoney);
//        resBean.setOriginIid(did);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 组装用户的奖励数据
     * @param didRewardModel - 用户奖励数据
     * @return
     * @author yoko
     * @date 2020/6/6 14:45
    */
    public static DidModel assembleDidMoneyByReward(DidRewardModel didRewardModel){
        DidModel resBean = new DidModel();
        resBean.setId(didRewardModel.getDid());
        resBean.setBalance(didRewardModel.getMoney());
        resBean.setTotalProfit(didRewardModel.getMoney());
        // 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励，5团队奖励
        if (didRewardModel.getRewardType() == 1){
            resBean.setTotalRechargeProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 2){
            resBean.setTotalGradeProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 3){
            resBean.setTotalDirectProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 4){
            resBean.setTotalIndirectProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 5){
            resBean.setTotalTeamProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 6){
            resBean.setTotalConsumeProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 7){
            resBean.setTotalTeamConsumeProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 8){
            resBean.setTotalTriggerQuotaProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 9){
            resBean.setTotalTeamConsumeCumulativeProfit(didRewardModel.getMoney());
        }else if(didRewardModel.getRewardType() == 10){
            resBean.setTotalTeamDirectConsumeProfit(didRewardModel.getMoney());
        }

        return resBean;

    }


    /**
     * @Description: 组装可爱猫回调订单数据的方法
     * @param msg - 可爱猫回调的具体数据
     * @param catAllDataId - 可爱猫原始数据的主键ID
     * @return
     * @author yoko
     * @date 2020/6/6 16:56
    */
    public static CatDataModel assembleCatDataAddData(CatMsg msg, long catAllDataId){
        CatDataModel resBean = new CatDataModel();
        resBean.setAllId(catAllDataId);
        if (!StringUtils.isBlank(msg.getTo_wxid())){
            resBean.setToWxid(msg.getTo_wxid());
        }else {
            return null;
        }

        if (!StringUtils.isBlank(msg.getMoney())){
            resBean.setOrderMoney(msg.getMoney());
        }else {
            return null;
        }
        if (!StringUtils.isBlank(msg.getShopowner())){
            if (msg.getShopowner().indexOf("(") > -1){
                int index = msg.getShopowner().lastIndexOf("(");
                resBean.setWxName(msg.getShopowner().substring(0, index));
            }else {
                return null;
            }
        }else {
            return null;
        }

        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;

    }


    /**
     * @Description: 组装更新可爱猫回调订单的数据
     * @param id - 可爱猫回调订单的主键ID
     * @param wxId - 归属小微管理的主键ID：对应表tb_fn_wx的主键ID
     * @return
     *
     * @author yoko
     * @date 2020/6/6 19:59
    */
    public static CatDataModel assembleCatDataUpdate(long id, long wxId){
        CatDataModel resBean = new CatDataModel();
        resBean.setId(id);
        resBean.setWxId(wxId);
        return resBean;
    }



    /**
     * @Description: 组装查询初始化的订单信息（未超过有效期的）
     * @param wxId - 小微管理员主键ID
     * @param wxNickname - 收款账号的微信昵称
     * @param collectionType - 收款账号类型：1微信，2支付宝，3银行卡
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/6/6 21:37
     */
    public static OrderModel assembleOrderQuery(long wxId, String wxNickname, int collectionType){
        OrderModel resBean = new OrderModel();
        resBean.setWxId(wxId);
        if (!StringUtils.isBlank(wxNickname)){
            resBean.setWxNickname(wxNickname);
        }
        resBean.setCollectionType(collectionType);
        resBean.setOrderStatus(1);
        resBean.setInvalidTime("1");
        return resBean;
    }


    /**
     * @Description: 组装更新可爱猫回调订单数据
     * @param id - 主键ID
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3有质疑，4成功
     * @param orderNo - 派单的那个订单号
     * @return com.hz.task.master.core.model.cat.CatDataModel
     * @author yoko
     * @date 2020/6/7 9:24
     */
    public static CatDataModel assembleCatDataUpdate(long id, int orderStatus, String orderNo){
        CatDataModel resBean = new CatDataModel();
        resBean.setId(id);
        resBean.setOrderStatus(orderStatus);
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        return resBean;
    }



    /**
     * @Description: 组装更新订单状态的数据
     * @param id - 主键ID
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3有质疑，4成功
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/6/7 10:09
     */
    public static OrderModel assembleUpdateOrderStatus(long id, int orderStatus){
        OrderModel resBean = new OrderModel();
        resBean.setId(id);
        resBean.setOrderStatus(orderStatus);
        return resBean;
    }


    /**
     * @Description: 组装更新小微管理的使用状态的方法
     * @param id - 小微管理的主键ID
     * @param useStatus - 使用状态:1初始化有效正常使用，2无效暂停使用
     * @param remark - 备注
     * @return com.hz.task.master.core.model.wx.WxModel
     * @author yoko
     * @date 2020/6/7 10:54
     */
    public static WxModel assembleWxUpdateUseStatus(long id, int useStatus, String remark){
        WxModel resBean = new WxModel();
        resBean.setId(id);
        resBean.setUseStatus(useStatus);
        resBean.setRemark(remark);
        return resBean;
    }
    
    /**
     * @Description: 组装更新用户超时订单时的金额变化的条件
     * @param did - 用户ID
     * @param orderMoney - 订单金额
     * @return
     * @author yoko
     * @date 2020/6/8 13:56
    */
    public static DidModel assembleUpdateDidMoneyByInvalid(long did, String orderMoney){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        resBean.setOrderMoney(orderMoney);
        return resBean;
    }


    /**
     * @Description: 组装更新用户成功订单时的金额变化的条件
     * @param did - 用户ID
     * @param orderMoney - 订单金额
     * @return
     * @author yoko
     * @date 2020/6/8 13:56
     */
    public static DidModel assembleUpdateDidMoneyBySuccess(long did, String orderMoney){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        resBean.setLockMoney(orderMoney);
        return resBean;
    }


    /**
     * @Description: 组装更新用户收款账号的方法
     * @param id - 主键ID
     * @param remark - 备注
     * @return
     * @author yoko
     * @date 2020/6/10 18:34
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateSwitch(long id, String remark){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(id);
        resBean.setRemark(remark);
        return resBean;
    }


    /**
     * @Description: 根据可爱猫解析数据是否是小微旗下下线以及绑定小微的数据
     * @param msg
     * @return
     * @author yoko
     * @date 2020/6/11 15:21
    */
    public static Map<String, String> getWxNameByCatData(String msg){
        Map<String,String> map = new HashMap<>();
        if (msg.indexOf("<first_data><![CDATA[") > -1 && msg.indexOf("已取消") > -1){
            String wxName = msg.substring(msg.indexOf("<first_data><![CDATA[") + "<first_data><![CDATA[".length(), msg.indexOf("已取消"));
            map.put("wxName", wxName);
            map.put("dataType", "1");// 小微下线
        }else if(msg.indexOf("<first_data><![CDATA[你已接受") > -1 && msg.indexOf("的邀请") > -1){
            String wxName = msg.substring(msg.indexOf("<first_data><![CDATA[你已接受") + "<first_data><![CDATA[你已接受".length(), msg.indexOf("的邀请"));
            map.put("wxName", wxName);
            map.put("dataType", "2");// 绑定小微：可以理解为小微与店下成员绑定关系
        }else {
            return null;
        }
        return map;
    }


    /**
     * @Description: 组装查询小微旗下账号的查询天骄
     * @param wxId - 小微管理的主键ID
     * @return
     * @author yoko
     * @date 2020/6/11 16:40
    */
    public static WxClerkModel assembleWxClerkQuery(long wxId){
        WxClerkModel resBean = new WxClerkModel();
        resBean.setWxId(wxId);
        return resBean;
    }

    /**
     * @Description: 组装查询收款账号的查询条件
     * @param id - 主键ID
     * @param wxName - 微信名称
     * @return
     * @author yoko
     * @date 2020/6/11 16:53
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountQuery(long id, String wxName){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(id);
        resBean.setPayee(wxName);
        return resBean;
    }

    /**
     * @Description: 组装添加收款账号上下线的数据
     * @param wxId - 小微管理的主键ID
     * @param did - 用户ID
     * @param collectionAccountId - 用户收款账号
     * @param dataType - 数据类型;1激活上线，2下线，3通过解绑小微进行下线，4通过修改微信名称之后在解绑小微下线
     * @return com.hz.task.master.core.model.wx.WxClerkDataModel
     * @author yoko
     * @date 2020/6/11 17:54
     */
    public static WxClerkDataModel assembleWxClerkData(long wxId, long did, long collectionAccountId, int dataType){
        WxClerkDataModel resBean = new WxClerkDataModel();
        resBean.setWxId(wxId);
        resBean.setDid(did);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setDataType(dataType);
        return resBean;
    }


    public static CatDataOfflineModel assembleCatDataOffline(long allId, long wxId, String toWxid, String wxName, long collectionAccountId, int matchingType){
        CatDataOfflineModel resBean = new CatDataOfflineModel();
        resBean.setAllId(allId);
        resBean.setWxId(wxId);
        resBean.setToWxid(toWxid);
        resBean.setWxName(wxName);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setMatchingType(matchingType);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: 组装更改运行状态的数据
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param dataType -  数据类型：1初始化，2没有找到对应名称的收款账号，3找到对应的账号并且有派单的，4找到对应收款账号并且没有派单的
     * @param info - 执行原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleTaskUpdateStatusByCatDataOffline(long id, int runStatus, int dataType, String info){
        StatusModel resBean = new StatusModel();
        resBean.setId(id);
        resBean.setRunStatus(runStatus);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        if (dataType != 0){
            resBean.setDataType(dataType);
        }
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        return resBean;
    }


    /**
     * @Description: 根据收款账号以及创建时间组装查询订单的查询条件
     * @param collectionAccountId - 用户收款账号
     * @param offlineTime - 小微下线时间
     * @return
     * @author yoko
     * @date 2020/6/12 21:58
    */
    public static OrderModel assembleOrderByCreateTimeQuery(long collectionAccountId, String offlineTime) throws Exception{
        OrderModel resBean = new OrderModel();
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setOrderStatus(2);
        resBean.setStartTime(DateUtil.addDateByTime(offlineTime, -10));
        resBean.setEndTime(offlineTime);
        return resBean;
    }


    /**
     * @Description: 修改用户收款账号的审核信息
     * @param collectionAccountId - 用户收款账号的ID
     * @return
     * @author yoko
     * @date 2020/6/12 23:08
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateCheckData(long collectionAccountId){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(collectionAccountId);
        resBean.setCheckStatus(1);
        resBean.setCheckInfo("检测：小微下线");
        return resBean;
    }

    /**
     * @Description: 修改用户收款账号的审核信息
     * @param collectionAccountId - 用户收款账号的ID
     * @return
     * @author yoko
     * @date 2020/6/12 23:08
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateCheckDataInfo(long collectionAccountId, String checkInfo){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(collectionAccountId);
        resBean.setCheckStatus(1);
        resBean.setCheckInfo(checkInfo);
        return resBean;
    }

    /**
     * @Description: 组装要修改订单状态的数据
     * @param orderList
     * @return
     * @author yoko
     * @date 2020/6/13 11:51
    */
    public static OrderModel assembleUpdateOrderStatus(List<OrderModel> orderList){
        OrderModel resBean = new OrderModel();

        List<Long> idList = orderList.stream().map(OrderModel::getId).collect(Collectors.toList());
        resBean.setIdList(idList);
        resBean.setOrderStatus(4);
        resBean.setRemark("小微下线，强制修改成功");
        return resBean;
    }


    /**
     * @Description: 组装更新可爱猫回调店员下线的订单信息
     * @param id - 主键ID
     * @param orderList - 订单集合
     * @return
     * @author yoko
     * @date 2020/6/13 12:30
    */
    public static CatDataOfflineModel assembleCatDataOffline(long id, List<OrderModel> orderList){
        CatDataOfflineModel resBean = new CatDataOfflineModel();
        resBean.setId(id);
        String orderNo = "";
        String orderMoney = "";
        double money = 0;
        for (OrderModel orderModel : orderList){
            orderNo = orderNo + orderModel.getId() + ",";
            money = money + Double.parseDouble(orderModel.getOrderMoney());
        }
        orderMoney = String.valueOf(money);
        resBean.setOrderNo(orderNo);
        resBean.setOrderMoney(orderMoney);
        return resBean;
    }

    /**
     * @Description: 组装查询银行卡信息的查询条件
     * @return
     * @author yoko
     * @date 2020/6/13 19:25
    */
    public static BankModel assembleBankQuery(){
        BankModel resBean = new BankModel();
        resBean.setUseStatus(1);
        return resBean;
    }


    /**
     * @Description: 组装查询银行卡收款金额的查询条件
     * @param bankId - 银行卡主键ID
     * @param curday - 日期
     * @param curdayStart - 开始日期
     * @param curdayEnd - 结束日期
     * @return com.hz.task.master.core.model.did.DidRechargeModel
     * @author yoko
     * @date 2020/6/13 20:55
     */
    public static DidRechargeModel assembleDidRechargeByBankIdQuery(long bankId, int curday, int curdayStart, int curdayEnd){
        DidRechargeModel resBean = new DidRechargeModel();
        resBean.setBankId(bankId);
        if (curday > 0){
            resBean.setCurday(curday);
        }
        if (curdayStart > 0){
            resBean.setCurdayStart(curdayStart);
        }
        if (curdayEnd > 0){
            resBean.setCurdayEnd(curdayEnd);
        }
        return resBean;
    }

    /**
     * @Description: 组装查询银行卡转账金额的查询条件
     * @param bankId - 银行卡主键ID
     * @param curday - 日期
     * @param curdayStart - 开始日期
     * @param curdayEnd - 结束日期
     * @return com.hz.task.master.core.model.bank.BankTransferModel
     * @author yoko
     * @date 2020/6/13 20:55
     */
    public static BankTransferModel assembleBankTransferByBankIdQuery(long bankId, int curday, int curdayStart, int curdayEnd){
        BankTransferModel resBean = new BankTransferModel();
        resBean.setBankId(bankId);
        if (curday > 0){
            resBean.setCurday(curday);
        }
        if (curdayStart > 0){
            resBean.setCurdayStart(curdayStart);
        }
        if (curdayEnd > 0){
            resBean.setCurdayEnd(curdayEnd);
        }
        return resBean;
    }


    /**
     * @Description: 组装更新银行卡开关的方法
     * @param bankId - 银行卡主键ID
     * @param daySwitch - 日开关是否启用（等于1正常使用，其它是暂停）:：1正常使用，2暂停使用
     * @param monthSwitch - 月开关是否启用：1正常使用，2暂停使用
     * @param totalSwitch - 总开关是否启用：1正常使用，2暂停使用
     * @param limitInfo - 限制缘由
     * @return com.hz.task.master.core.model.bank.BankModel
     * @author yoko
     * @date 2020/6/13 21:15
     */
    public static BankModel assembleBankSwitchUpdate(long bankId, int daySwitch, int monthSwitch, int totalSwitch, String limitInfo){
        BankModel resBean = new BankModel();
        resBean.setId(bankId);
        if (daySwitch > 0){
            resBean.setDaySwitch(daySwitch);
        }
        if (monthSwitch > 0){
            resBean.setMonthSwitch(monthSwitch);
        }
        if (totalSwitch > 0){
            resBean.setTotalSwitch(totalSwitch);
        }
        resBean.setLimitInfo(limitInfo);
        return resBean;
    }

    /**
     * @Description: 组装修改小微旗下店员关联关系的方法
     * @param wxId - 小微主键ID
     * @param collectionAccountId - 收款账号主键ID
     * @return com.hz.task.master.core.model.wx.WxClerkModel
     * @author yoko
     * @date 2020/6/15 17:59
     */
    public static WxClerkModel assembleWxClerkUpdate(long wxId, long collectionAccountId){
        WxClerkModel resBean = new WxClerkModel();
        resBean.setWxId(wxId);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setYn(1);
        return resBean;
    }


    /**
     * @Description: 组装可爱猫回调店员绑定小微的方法
     * @param allId - 原始数据的ID：对应表tb_fn_cat_all_data的主键ID
     * @param toWxid - 可爱猫的to_wxid
     * @param wxName - 微信名称
     * @return com.hz.task.master.core.model.cat.CatDataBindingModel
     * @author yoko
     * @date 2020/6/16 22:17
     */
    public static CatDataBindingModel assembleCatDataBinding(long allId, String toWxid, String wxName){
        CatDataBindingModel resBean = new CatDataBindingModel();
        resBean.setAllId(allId);
        resBean.setToWxid(toWxid);
        resBean.setWxName(wxName);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: 组装根据微信昵称查询收款账号的方法
     * @param wxName - 微信昵称
     * @return
     * @author yoko
     * @date 2020/6/17 10:38
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountQueryByPayee(String wxName){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setPayee(wxName);
        return resBean;
    }

    /**
     * @Description: 组装更新可爱猫回调店员绑定小微的信息
     * @param id - 主键ID
     * @param matchingType - 数据匹配的类型：1根据小微ID跟微信昵称不能匹配到收款账号，2根据小微ID跟微信昵称能匹配到收款账号
     * @param wxId - 小微的主键ID
     * @param collectionAccountId - 收款账号的主键ID
     * @return
     * @author yoko
     * @date 2020/6/17 10:45
    */
    public static CatDataBindingModel assembleCatDataBindingUpdate(long id, int matchingType, long wxId, long collectionAccountId){
        CatDataBindingModel resBean = new CatDataBindingModel();
        resBean.setId(id);
        resBean.setMatchingType(matchingType);
        if (wxId != 0){
            resBean.setWxId(wxId);
        }
        if (collectionAccountId != 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        return resBean;
    }


    /**
     * @Description: 组装修改二维码成功的次数修改数据
     * @param id - 二维码主键ID
     * @param isLimitNum - 已限制的次数
     * @return
     * @author yoko
     * @date 2020/6/19 9:59
    */
    public static DidCollectionAccountQrCodeModel assembleDidCollectionAccountQrCode(long id, int isLimitNum){
        DidCollectionAccountQrCodeModel resBean = new DidCollectionAccountQrCodeModel();
        resBean.setId(id);
        resBean.setIsLimitNum(1);
        return resBean;
    }

    /**
     * @Description: 组装查询收款账号上限数据的查询条件
     * @param orderStatus - 订单状态
     * @param collectionAccountId - 用户账号ID
     * @param curday - 日期
     * @param dataLimitNum - 需要查询多少条数据
     * @param whereOrderStatus - 查询订单状态大于几的订单状态
     * @return com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel
     * @author yoko
     * @date 2020/6/19 11:28
     */
    public static TaskDidCollectionAccountDataModel assembleTaskDidCollectionAccountData(int orderStatus, long collectionAccountId, int curday, int dataLimitNum, int whereOrderStatus){
        TaskDidCollectionAccountDataModel resBean = new TaskDidCollectionAccountDataModel();
        if (orderStatus != 0){
            resBean.setOrderStatus(orderStatus);
        }
        if (collectionAccountId != 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        if (curday != 0){
            resBean.setCurday(curday);
        }
        if (dataLimitNum != 0){
            resBean.setDataLimitNum(dataLimitNum);
        }
        if (whereOrderStatus != 0){
            resBean.setWhereOrderStatus(whereOrderStatus);
        }
        return resBean;
    }


    /**
     * @Description: 组装查询二维码的查询条件
     * @param collectionAccountId - 收款账号的主键ID
     * @return
     * @author yoko
     * @date 2020/6/18 19:34
     */
    public static DidCollectionAccountQrCodeModel assembleDidCollectionAccountQrCode(long collectionAccountId){
        DidCollectionAccountQrCodeModel resBean = new DidCollectionAccountQrCodeModel();
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setIsLimitNum(1);
        resBean.setUseStatus(1);
        return resBean;
    }

    /**
     * @Description: 组装更新收款账号被暂停的原因的方法
     * @param id - 收款账号的主键ID
     * @param checkInfo - 被暂停的原因
     * @return
     * @author yoko
     * @date 2020/6/19 14:10
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdate(long id, String checkInfo){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(id);
        resBean.setUseStatus(2);
        resBean.setCheckInfo(checkInfo);
        return resBean;
    }

    /**
     * @Description: 组装修改用户余额流水的订单状态
     * @param orderNo - 订单号
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3有质疑，4成功
     * @return com.hz.task.master.core.model.did.DidBalanceDeductModel
     * @author yoko
     * @date 2020/7/2 19:23
     */
    public static DidBalanceDeductModel assembleDidBalanceDeductUpdate(String orderNo, int orderStatus){
        DidBalanceDeductModel resBean = new DidBalanceDeductModel();
        resBean.setOrderNo(orderNo);
        resBean.setOrderStatus(orderStatus);
        return resBean;
    }

    /**
     * @Description: 更新用户的余额的组装方法
     * @param did - 用户的主键ID
     * @param money - 金额
     * @return com.hz.task.master.core.model.did.DidModel
     * @author yoko
     * @date 2020/7/3 13:48
     */
    public static DidModel assembleDidUpdateBalance(long did, String money){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        resBean.setOrderMoney(money);
        return resBean;
    }


    /**
     * @Description: check校验客户端监听的原始数据必填字段值
     * @param clientModel
     * @return
     * @author yoko
     * @date 2020/7/6 20:10
    */
    public static boolean checkClientModel(ClientModel clientModel){
        if (StringUtils.isBlank(clientModel.getContent())){
            return false;
        }
        if (StringUtils.isBlank(clientModel.getToken())){
            return false;
        }
        return true;
    }

    /**
     * @Description: check校验数据是否属于支付宝收款信息
     * @param content - 客户端监听到的支付宝数据
     * @param strategyZfbRule - 检测定位客户端监听的数据是否属于支付宝转账规则配置
     * @return boolean
     * @author yoko
     * @date 2020/7/6 20:17
     */
    public static boolean checkZfbData(String content, StrategyZfbRule strategyZfbRule){
        String [] fg_str = strategyZfbRule.getKey().split("#");
        int checkNum = strategyZfbRule.getKeyNum();
        int num = 0;

        for (String str : fg_str){
            if (content.indexOf(str) > -1){
                num ++;
            }
        }
        if (num >= checkNum){
            return true;
        }else {
            return false;
        }
    }

    /**
     * @Description: 截取数据里面的收款金额
     * @param content - 客户端监听到的支付宝数据
     * @param strategyZfbMoneyRule - 检测截取客户端监听的数据中支付宝转账的具体金额规则配置
     * @return java.lang.String
     * @author yoko
     * @date 2020/7/6 20:22
     */
    public static String getZfbMoney(String content, StrategyZfbMoneyRule strategyZfbMoneyRule){
        //截取短信类容获取短信类容里面的金额
        String [] startKeyArr = strategyZfbMoneyRule.getStartKey().split("#");
        String [] endKeyArr = strategyZfbMoneyRule.getEndKey().split("#");

        int startIndex = 0;
        int endIndex = 0;
        for (String str : startKeyArr){
            startIndex = getIndexOfByStr(content, str);
            if (startIndex > 0){
                startIndex = startIndex + str.length();
                break;
            }
        }

        for (String str : endKeyArr){
            endIndex = getIndexOfByStr(content, str);
            if (endIndex > 0){
                break;
            }
        }
        String money = content.substring(startIndex, endIndex).replaceAll(",","");
        if (StringUtils.isBlank(money)){
            return null;
        }else {
            return money;
        }
    }


    /**
     * @Description: 组装客户端监听数据回调订单的数据
     * @param clientModel - 客户端监听数据回调原始数据
     * @param orderMoney - 收款金额
     * @param allId - 客户端监听数据回调原始数据的主键ID
     * @return com.hz.task.master.core.model.client.ClientDataModel
     * @author yoko
     * @date 2020/7/6 20:38
     */
    public static ClientDataModel assembleClientDataModel(ClientModel clientModel, String orderMoney, long allId){
        ClientDataModel resBean = new ClientDataModel();
        resBean.setAllId(allId);
        resBean.setUserId(clientModel.getToken());
        resBean.setOrderMoney(orderMoney);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 根据支付宝账号ID查询收款账号信息
     * @param userId - 支付宝账号ID
     * @return
     * @author yoko
     * @date 2020/7/6 21:08
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountByUserIdQuery(String userId){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setUserId(userId);
        return resBean;
    }


    /**
     * @Description: 组装更新可爱猫回调订单的数据
     * @param id - 可爱猫回调订单的主键ID
     * @param did - 归属小微管理的主键ID：对应表tb_fn_wx的主键ID
     * @return
     *
     * @author yoko
     * @date 2020/6/6 19:59
     */
    public static ClientDataModel assembleClientDataUpdate(long id, long did){
        ClientDataModel resBean = new ClientDataModel();
        resBean.setId(id);
        resBean.setDid(did);
        return resBean;
    }

    /**
     * @Description: 组装查询初始化的订单信息（未超过有效期的）-支付宝
     * @param did - 用户ID
     * @param userId - 支付宝账号ID
     * @param collectionType - 收款账号类型：1微信，2支付宝，3银行卡
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/6/6 21:37
     */
    public static OrderModel assembleOrderByZfbQuery(long did, String userId, int collectionType){
        OrderModel resBean = new OrderModel();
        resBean.setDid(did);
        resBean.setUserId(userId);
        resBean.setCollectionType(collectionType);
        resBean.setOrderStatus(1);
        resBean.setInvalidTime("1");
        return resBean;
    }


    /**
     * @Description: 组装更新客户端监听数据回调订单数据
     * @param id - 主键ID
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3有质疑，4成功
     * @param orderNo - 派单的那个订单号
     * @return com.hz.task.master.core.model.cat.CatDataModel
     * @author yoko
     * @date 2020/6/7 9:24
     */
    public static ClientDataModel assembleClientDataUpdate(long id, int orderStatus, String orderNo){
        ClientDataModel resBean = new ClientDataModel();
        resBean.setId(id);
        resBean.setOrderStatus(orderStatus);
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        return resBean;
    }

    /**
     * @Description: 组装添加客户端监听的收款信息：存储所有收款信息的方法
     * @param allId - 客户端监听数据回调原始数据的主键ID
     * @param clientModel - 解析json的数据
     * @param did - 用户ID
     * @param jsonData - json数据
     * @return com.hz.task.master.core.model.client.ClientCollectionDataModel
     * @author yoko
     * @date 2020/7/7 16:28
     */
    public static ClientCollectionDataModel assembleClientCollectionDataAdd(long allId, ClientModel clientModel, long did, String jsonData){
        ClientCollectionDataModel resBean = new ClientCollectionDataModel();
        resBean.setAllId(allId);
        resBean.setDid(did);
        resBean.setUserId(clientModel.getToken());
        resBean.setJsonData(jsonData);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: 组装查询属于团队长的用户ID集合
     * @param isTeam
     * @return
     * @author yoko
     * @date 2020/7/7 22:11
    */
    public static DidModel assembleDidByIsTeamQuery(int isTeam){
        DidModel resBean = new DidModel();
        resBean.setIsTeam(isTeam);
        return resBean;
    }


    /**
     * @Description: 组装查询直推用户昨天派单消耗成功的总金额的查询条件
     * @param didList - 用户ID集合：直推用户集合
     * @return
     * @author yoko
     * @date 2020/6/6 11:40
     */
    public static OrderModel assembleOrderQuery(List<Long> didList){
        OrderModel resBean = new OrderModel();
        resBean.setDidList(didList);
        resBean.setOrderStatus(4);
        resBean.setCurday(DateUtil.getIntYesterday());
        return resBean;
    }


    /**
     * @Description: 组装根据allId查询客户端监听的收款信息的方法
     * @param allId
     * @return
     * @author yoko
     * @date 2020/7/8 19:32
    */
    public static ClientCollectionDataModel assembleClientCollectionDataByAllId(long allId){
        ClientCollectionDataModel resBean = new ClientCollectionDataModel();
        resBean.setAllId(allId);
        return resBean;
    }

    /**
     * @Description: 计算可以奖励几次-触发奖励
     * @param divideStr - 总成功金额除以触发奖励的金额的结果
     * @param triggerQuotaGrade - 触发奖励的等级：团队消耗总和除以10万得到的整数就是等级
     * @return int
     * @author yoko
     * @date 2020/7/10 17:28
     */
    public static int getDivideResult(String divideStr, int triggerQuotaGrade){
        int num = 0;
        if (!StringUtils.isBlank(divideStr)){
            if (divideStr.equals("0")){
                return num;
            }else {
                boolean flag = isInteger(divideStr);
                if (flag){
                    // 整数
                    int resultNum = Integer.parseInt(divideStr);
                    if (resultNum > 0){
                        if (resultNum > triggerQuotaGrade){
                            num = resultNum - triggerQuotaGrade;
                        }else{
                            return num;
                        }
                    }else {
                        return num;
                    }
                }else{
                    if (divideStr.indexOf("\\.")> -1){
                        String [] str = divideStr.split("\\.");
                        if (str != null && str.length == 2){
                            int resultNum = Integer.parseInt(str[0]);
                            if (resultNum > 0){
                                if (resultNum > triggerQuotaGrade){
                                    num = resultNum - triggerQuotaGrade;
                                }else{
                                    return num;
                                }
                            }else {
                                num = 0;
                                return num;
                            }
                        }else {
                            return num;
                        }
                    }else {
                        return num;
                    }
                }

            }
        }
        return num;
    }

    /**
     * @Description: 判断是否是整数
     * @param str
     * @return
     * @author yoko
     * @date 2020/7/10 17:17
    */
    public static boolean isInteger(String str){
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }


    /**
     * @Description: 用户奖励的具体等级更新的数据组装
     * @param did - 用户ID
     * @param triggerQuotaGrade - 触发奖励的等级：团队消耗总和除以10万得到的整数就是等级
     * @param teamConsumeCumulativeGrade - 团队总额等级：总和到达多少级
     * @return com.hz.task.master.core.model.did.DidModel
     * @author yoko
     * @date 2020/7/10 17:46
     */
    public static DidModel assembleUpdateDidData(long did, int triggerQuotaGrade, int teamConsumeCumulativeGrade){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        if (triggerQuotaGrade > 0){
            resBean.setTriggerQuotaGrade(triggerQuotaGrade);
        }
        if (teamConsumeCumulativeGrade > 0){
            resBean.setTeamConsumeCumulativeGrade(teamConsumeCumulativeGrade);
        }
        return resBean;
    }

    /**
     * @Description: 组装已经到达的等级-团队总额等级奖励
     * @param teamConsumeCumulativeRewardList - 策略：团队总额等级奖励规则列表
     * @param directSumMoney - 直推消耗的总成功金额
     * @param teamConsumeCumulativeGrade - 团队总额等级：总和到达多少级
     * @return java.util.List<com.hz.task.master.core.model.strategy.StrategyData>
     * @author yoko
     * @date 2020/7/10 18:47
     */
    public static List<StrategyData> getTeamConsumeCumulativeRewardList(List<StrategyData> teamConsumeCumulativeRewardList, String directSumMoney, int teamConsumeCumulativeGrade){
        List<StrategyData> resList = new ArrayList<>();
        if (teamConsumeCumulativeRewardList != null && teamConsumeCumulativeRewardList.size() > 0){
            for (StrategyData data : teamConsumeCumulativeRewardList){
                if (data.getStgValueTwo()> teamConsumeCumulativeGrade){
                    boolean flag = StringUtil.getBigDecimalSubtract(directSumMoney, data.getStgValue());
                    if (flag){
                        resList.add(data);
                    }
                }
            }
        }else {
            return null;
        }
        return resList;
    }


    /**
     * @Description: 组装查询团队长奖励金额等级纪录数据的查询条件
     * @param did - 用户ID
     * @return
     * @author yoko
     * @date 2020/7/11 21:51
    */
    public static DidTeamGradeModel assembleDidTeamGradeQuery(long did){
        DidTeamGradeModel resBean = new DidTeamGradeModel();
        resBean.setDid(did);
        return resBean;
    }


    /**
     * @Description: 根据用户ID查询这个用户的上级ID查询条件
     * @param did - 用户ID
     * @param levelType - 层级关系类型：1直推关系，2裂变关系
     * @return com.hz.task.master.core.model.did.DidLevelModel
     * @author yoko
     * @date 2020/6/5 19:14
     */
    public static DidLevelModel assembleDidSuperiorQuery(long did, int levelType){
        DidLevelModel resBean = new DidLevelModel();
        resBean.setDid(did);
        resBean.setLevelType(levelType);
        return resBean;
    }


    /**
     * @Description: 组装更改运行状态的数据
     * @param id - 主键ID
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @param orderStatus - 订单状态
     * @param info - 纪录失败的原因
     * @return StatusModel
     * @author yoko
     * @date 2019/12/10 10:42
     */
    public static StatusModel assembleUpdateStatusByOrderStatusAndInfo(long id, int runStatus, int orderStatus, String info){
        StatusModel resBean = new StatusModel();
        resBean.setRunStatus(runStatus);
        if (orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        resBean.setId(id);
        if (runStatus == ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO){
            // 表示失败：失败则需要运行次数加一
            resBean.setRunNum(ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ONE);
        }
        if (!StringUtils.isBlank(info)){
            resBean.setInfo(info);
        }
        return resBean;
    }


//    /**
//     * @Description: 解析可爱猫数据，根据msg数据来确定此数据的类型
//     * @param msg
//     * @return
//     * @author yoko
//     * @date 2020/7/21 21:16
//    */
//    public static Map<String, Object> getCatDataType(String msg){
//        Map<String, Object> map = new HashMap<>();
//        int dataType = 0;
//        String dataValue = "";
//        // 首先解析数据是否是json数据
//        boolean flag = false;
//        CatMsg catMsg = new CatMsg();
//        try {
//            catMsg = JSON.parseObject(msg, CatMsg.class);
//            flag = true;
//        }catch (Exception e){
//            flag = false;
//        }
//
//        if (flag){
//            // 属于json数据
//            if (catMsg.get)
//        }else {
//            // 不属于json数据：普通数据
//            if (msg.equals("4")){
//                // 表示群审核完毕更新微信参数
//                dataType = 3;
//            }
//
//        }
//
//        return null;
//    }



    /**
     * @Description: 解析可爱猫数据type =200的数据，根据msg数据来确定此数据的类型
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/21 21:16
     */
    public static int getCatDataTypeByTwoHundred(String msg){
        int num = 0;
        if (msg.equals("3")){
            num = 3;
        }else if (msg.equals("2")){
            num = 8;
        }else if (msg.equals("收到红包，请在手机上查看")){
            num = 5;
        }
//        else if(msg.substring(0,2).equals("你被") && msg.substring(msg.length() - 4, msg.length()).equals("移出群聊")){
//            num = 6;
//        }
        else if(msg.indexOf("1#") > -1){
            String [] fg_msg = msg.split("#");
            if (fg_msg.length == 2){
                String money = fg_msg[1];
                // 金额是否有效
                if (money.indexOf(".") > -1){
                    String[] fg_money = money.split("\\.");
                    boolean flag1 = StringUtil.isNumer(fg_money[0]);
                    boolean flag2 = StringUtil.isNumer(fg_money[1]);
//                    boolean flag = StringUtil.isNumberByMoney(money);
                    if (flag1 && flag2){
                        num = 7;
                    }else {
                        num = 2;
                    }
                }else {
                    boolean flag = StringUtil.isNumer(money);
                    if (flag){
                        num = 7;
                    }else {
                        num = 2;
                    }
                }
            }else {
                num = 2;
            }
        }else{
            num = 2;
        }
        return num;
    }

    /**
     * @Description: 组装查询收款账号的查询条件
     * @param robot_wxid - 小微的toWxid
     * @param from_name - 微信群名称
     * @return
     * @author yoko
     * @date 2020/7/22 14:51
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountByToWxidAndPayeeQuery(String robot_wxid, String from_name){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setToWxid(robot_wxid);
        resBean.setPayee(from_name);
        return resBean;
    }

    /**
     * @Description: 组装根据微信群名称查询收款账号
     * @param from_name - 微信群名称
     * @return
     * @author yoko
     * @date 2020/7/22 16:03
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountByPayee(String from_name, int acType){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setPayee(from_name);
        resBean.setAcType(acType);
        return resBean;
    }

    /**
     * @Description: 组装添加可爱猫解析数据
     * @param fromCatModel - 可爱猫数据
     * @param dataType - 数据类型：1初始化，2其它，3发送固定指令3表示审核使用，4加群信息，5发红包，6剔除成员，7成功收款，8收款失败
     * @param allId - 可爱猫原始数据
     * @param wxId - 小微的主键ID
     * @param did - 用户ID
     * @param collectionAccountId - 收款账号ID
     * @param collectionAccountType - 收款账号状态：1初始化，2没找到对应的收款账号，3账号被删除，4账号名称被修改，5正常状态的账号
     * @param dataFrom - 数据来源：1可爱猫，2新版微信端
     * @return com.hz.task.master.core.model.cat.CatDataAnalysisModel
     * @author yoko
     * @date 2020/7/22 16:45
     */
    public static CatDataAnalysisModel assembleCatDataAnalysisData(FromCatModel fromCatModel, int dataType, long allId, long wxId,
                                                                   long did, long collectionAccountId, int collectionAccountType, int dataFrom){
        CatDataAnalysisModel resBean = new CatDataAnalysisModel();
        resBean.setAllId(allId);
        resBean.setWxId(wxId);
        if (did > 0){
            resBean.setDid(did);
        }
        if (collectionAccountId > 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        if (collectionAccountType > 0){
            resBean.setCollectionAccountType(collectionAccountType);
        }
        if (!StringUtils.isBlank(fromCatModel.getFinal_from_wxid())){
            resBean.setFinalFromWxid(fromCatModel.getFinal_from_wxid());
        }
        if (!StringUtils.isBlank(fromCatModel.getFrom_name())){
            resBean.setFromName(fromCatModel.getFrom_name());
        }
        if (!StringUtils.isBlank(fromCatModel.getFinal_from_name())){
            resBean.setFinalFromName(fromCatModel.getFinal_from_name());
        }
        if (!StringUtils.isBlank(fromCatModel.getFrom_wxid())){
            resBean.setFromWxid(fromCatModel.getFrom_wxid());
        }
        if (!StringUtils.isBlank(fromCatModel.getMsg())){
            resBean.setMsg(fromCatModel.getMsg());
        }
        if (!StringUtils.isBlank(fromCatModel.getMsg_type())){
            resBean.setMsgType(fromCatModel.getMsg_type());
        }
        if (!StringUtils.isBlank(fromCatModel.getRobot_wxid())){
            resBean.setRobotWxid(fromCatModel.getRobot_wxid());
        }
        if (!StringUtils.isBlank(fromCatModel.getType())){
            resBean.setType(fromCatModel.getType());
        }
        CatMsg catMsg = new CatMsg();
        try{
            catMsg = JSON.parseObject(fromCatModel.getMsg(), CatMsg.class);
        }catch (Exception e){
            catMsg = null;
        }
        if (catMsg != null){
            if (!StringUtils.isBlank(catMsg.getGroup_wxid())){
                resBean.setGroupWxid(catMsg.getGroup_wxid());
            }
            if (!StringUtils.isBlank(catMsg.getGroup_name())){
                resBean.setGroupName(catMsg.getGroup_name());
            }
            if (!StringUtils.isBlank(catMsg.getGuest())){
                resBean.setGuest(catMsg.getGuest());
            }
            if (!StringUtils.isBlank(catMsg.getMember_wxid())){
                resBean.setGuest(catMsg.getMember_wxid());
            }
            if (!StringUtils.isBlank(catMsg.getMember_nickname())){
                resBean.setMemberNickname(catMsg.getMember_nickname());
            }
        }
        resBean.setDataType(dataType);
        resBean.setDataFrom(dataFrom);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;

    }



    /**
     * @Description: 组装添加微信/可爱猫解析数据
     * @param wxClient - 微信回调原始数据
     * @param dataType - 数据类型：1初始化，2其它，3发送固定指令3表示审核使用，4加群信息，5发红包，6剔除成员，7成功收款，8收款失败，9发送固定指令4表示暂停使用微信群，10小微登入，11小微登出
     * @param allId - 可爱猫原始数据
     * @param wxId - 小微的主键ID
     * @param did - 用户ID
     * @param collectionAccountId - 收款账号ID
     * @param collectionAccountType - 收款账号状态：1初始化，2没找到对应的收款账号，3账号被删除，4账号名称被修改，5正常状态的账号
     * @param dataFrom - 数据来源：1可爱猫，2新版微信端
     * @return com.hz.task.master.core.model.cat.CatDataAnalysisModel
     * @author yoko
     * @date 2020/7/22 16:45
     */
    public static CatDataAnalysisModel assembleCatDataAnalysisByWxData(WxClient wxClient, int dataType, long allId, long wxId,
                                                                       long did, long collectionAccountId, int collectionAccountType, int dataFrom){
        CatDataAnalysisModel resBean = new CatDataAnalysisModel();
        resBean.setAllId(allId);
        resBean.setWxId(wxId);
        if (did > 0){
            resBean.setDid(did);
        }
        if (collectionAccountId > 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        if (collectionAccountType > 0){
            resBean.setCollectionAccountType(collectionAccountType);
        }
        String final_from_wxid = "";
        String final_from_name = "";
        if (dataType == 2 || dataType == 3 || dataType == 7 || dataType == 8 || dataType == 9){
            if (!StringUtils.isBlank(wxClient.wxid1)){
                // 发信息的微信ID
                final_from_wxid = wxClient.wxid1;
            }

            if (!StringUtils.isBlank(wxClient.wxid2)){
                // 发信息的微信昵称
                final_from_name = wxClient.wxid2;
            }


        }else{
            // 其它类型数据
            if (!StringUtils.isBlank(wxClient.chartid)){
                // 群ID
                final_from_wxid = wxClient.chartid;
            }

            if (!StringUtils.isBlank(wxClient.wxid3)){
                // 发信息的微信群名称
                final_from_name = wxClient.wxid3;
            }

        }
        if (!StringUtils.isBlank(final_from_wxid)){
            // 发信息的微信ID/群ID
            resBean.setFinalFromWxid(final_from_wxid);
        }
        if (!StringUtils.isBlank(wxClient.wxid3)){
            // 群名称
            resBean.setFromName(wxClient.wxid3);
        }
        if (!StringUtils.isBlank(final_from_name)){
            resBean.setFinalFromName(final_from_name);
        }
        if (!StringUtils.isBlank(wxClient.chartid)){
            // 微信群ID
            resBean.setFromWxid(wxClient.chartid);
        }
        if (!StringUtils.isBlank(wxClient.content)){
            resBean.setMsg(wxClient.content);
        }
        if (!StringUtils.isBlank(wxClient.type)){
            resBean.setMsgType(wxClient.type);
        }
        if (!StringUtils.isBlank(wxClient.log_wechatid)){
            resBean.setRobotWxid(wxClient.log_wechatid);
        }
        if (!StringUtils.isBlank(wxClient.type)){
            resBean.setType(wxClient.type);
        }

        resBean.setDataType(dataType);
        resBean.setDataFrom(dataFrom);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;

    }

    /**
     * @Description: 解析可爱猫数据获得详细数据-加群信息
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/25 18:03
    */
    public static CatMsg getCatMstData(String msg){
        CatMsg catMsg = new CatMsg();
        try{
            catMsg = JSON.parseObject(msg, CatMsg.class);
        }catch (Exception e){
            catMsg = null;
        }
        return catMsg;
    }

    /**
     * @Description: 解析可爱猫数据获得详细数据-移出群信息
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/25 18:03
     */
    public static CatMember getCatMemberData(String msg){
        CatMember catMember = new CatMember();
        try{
            catMember = JSON.parseObject(msg, CatMember.class);
        }catch (Exception e){
            catMember = null;
        }
        return catMember;
    }


    /**
     * @Description: 解析可爱猫数据type =400的数据，根据msg数据来确定此数据的类型
     * <p>
     *     加群信息
     * </p>
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/21 21:16
     */
    public static int getCatDataTypeByFourHundred(String msg){
        int num = 0;
        if (msg.indexOf("group_wxid") > -1){
            num = 4;
        }else{
            num = 2;
        }
        return num;
    }


    /**
     * @Description: 解析可爱猫数据type =410的数据，根据msg数据来确定此数据的类型
     * <p>
     *     移出群信息
     * </p>
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/21 21:16
     */
    public static int getCatDataTypeByFourHundredAndTen(String msg){
        int num = 0;
        if (msg.indexOf("member_wxid") > -1){
            num = 6;
        }else{
            num = 2;
        }
        return num;
    }

    /**
     * @Description: 组装要更新的微信群收款账号信息
     * @param catDataAnalysisModel - 可爱猫解析的数据
     * @param collectionAccountId - 用户收款账号ID
     * @return com.hz.task.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/7/23 10:15
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateByWxGroup(CatDataAnalysisModel catDataAnalysisModel, long collectionAccountId){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(collectionAccountId);
        resBean.setWxId(catDataAnalysisModel.getWxId());
        if (!StringUtils.isBlank(catDataAnalysisModel.getFromWxid())){
            resBean.setAcName(catDataAnalysisModel.getFromWxid());
        }
        if (!StringUtils.isBlank(catDataAnalysisModel.getFinalFromWxid())){
            resBean.setAcNum(catDataAnalysisModel.getFinalFromWxid());
            resBean.setUserId(catDataAnalysisModel.getFinalFromWxid());
        }
        return resBean;

    }

    /**
     * @Description: 校验数据加群数据的正确性
     * @param guest
     * @return
     * @author yoko
     * @date 2020/7/23 10:47
    */
    public static boolean checkCatGuest(String guest){
        boolean flag = false;
        if (!StringUtils.isBlank(guest)){
            try {
//                CatGuest catGuest = JSON.parseObject(guest, CatGuest.class);
                List<CatGuest> catGuest = JSON.parseArray(guest, CatGuest.class);
                flag = true;
                return flag;
            }catch (Exception e){
                return flag;
            }
        }
        return flag;

    }


    /**
     * @Description: 组装查询微信群收款账号的查询条件
     * @param acName - 微信群ID
     * @param payee - 微信群名称
     * @param acType - 账号类型：微信群
     * @return com.hz.task.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/7/23 11:10
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountQueryByAcNameAndPayee(String acName, String payee, int acType){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        if (!StringUtils.isBlank(acName)){
            resBean.setAcName(acName);
        }
        if (!StringUtils.isBlank(payee)){
            resBean.setPayee(payee);
        }
        resBean.setAcType(acType);
        return resBean;
    }

    /**
     * @Description: 根据用户ID，收款账号ID，支付类型查询用户最新的第一个订单
     * @param did - 用户ID
     * @param collectionAccountId - 收款账号ID
     * @param collectionType - 支付类型：3微信群支付
     * @param replenishType -  是否是补单：1初始化不是补单，2是补单
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/23 14:52
     */
    public static OrderModel assembleOrderByNewestQuery(long did, long collectionAccountId, int collectionType, int replenishType){
        OrderModel resBean = new OrderModel();
        resBean.setDid(did);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setCollectionType(collectionType);
        resBean.setReplenishType(replenishType);
        return resBean;
    }


    /**
     * @Description: 组装运营数据的方法
     * @param analysisId - 可爱猫数据解析表的主键ID
     * @param didCollectionAccountModel - 用户收款账号信息
     * @param orderModel - 订单信息
     * @param punishType - 处罚类型：1不处罚，2处罚
     * @param punishMoney - 处罚金额
     * @param dataType - 数据类型：1初始化，2其它，3加群
     * @param dataExplain - 数据说明
     * @param remark - 备注
     * @param endType - 是否需要操作完毕才能派单类型：1需要处理完毕，2不需要处理完毕；此数据需要处理成功，才能给此用户进行派单
     * @param wxId - 我放小微的主键ID
     * @param money - 用户上报的成功金额
     * @return com.hz.task.master.core.model.operate.OperateModel
     * @author yoko
     * @date 2020/7/23 15:07
     */
    public static OperateModel assembleOperateData(long analysisId, DidCollectionAccountModel didCollectionAccountModel, OrderModel orderModel,
                                                  int punishType, String punishMoney, int dataType, String dataExplain, String remark, int endType, long wxId, String money){
        OperateModel resBean = new OperateModel();
        if (analysisId > 0){
            resBean.setAnalysisId(analysisId);
        }
        if (didCollectionAccountModel != null && didCollectionAccountModel.getId() > 0){
            resBean.setDid(didCollectionAccountModel.getDid());
            if (didCollectionAccountModel.getWxId() > 0){
                resBean.setWxId(didCollectionAccountModel.getWxId());
            }
            resBean.setCollectionAccountId(didCollectionAccountModel.getId());
            if (!StringUtils.isBlank(didCollectionAccountModel.getAcName())){
                resBean.setGroupWxid(didCollectionAccountModel.getAcName());
            }
            if (!StringUtils.isBlank(didCollectionAccountModel.getPayee())){
                resBean.setGroupName(didCollectionAccountModel.getPayee());
            }
            if (!StringUtils.isBlank(didCollectionAccountModel.getUserId())){
                resBean.setUserId(didCollectionAccountModel.getUserId());
            }
        }
        if (wxId > 0){
            resBean.setWxId(wxId);
        }
        if (orderModel != null && orderModel.getId() > 0){
            resBean.setOrderNo(orderModel.getOrderNo());
            resBean.setOrderMoney(orderModel.getOrderMoney());
            resBean.setOrderStatus(orderModel.getOrderStatus());
        }
        if (punishType != 0){
            resBean.setPunishType(punishType);
        }
        if (!StringUtils.isBlank(punishMoney)){
            resBean.setPunishMoney(punishMoney);
        }
        if (dataType != 0){
            resBean.setDataType(dataType);
        }
        if (!StringUtils.isBlank(dataExplain)){
            resBean.setDataExplain(dataExplain);
        }
        if (!StringUtils.isBlank(remark)){
            resBean.setRemark(remark);
        }
        if (endType != 0){
            resBean.setEndType(endType);
        }
        if (!StringUtils.isBlank(money)){
            resBean.setMoney(money);
        }
        return resBean;
    }

    /**
     * @Description: 组装填充可爱猫解析的订单信息的方法
     * @param id - 主键ID
     * @param orderModel - 订单信息
     * @param money - 用户上报的成功金额
     * @return com.hz.task.master.core.model.cat.CatDataAnalysisModel
     * @author yoko
     * @date 2020/7/23 15:36
     */
    public static CatDataAnalysisModel assembleCatDataAnalysisUpdate(long id, OrderModel orderModel, String money){
        CatDataAnalysisModel resBean = new CatDataAnalysisModel();
        resBean.setId(id);
        resBean.setOrderNo(orderModel.getOrderNo());
        resBean.setOrderMoney(orderModel.getOrderMoney());
        resBean.setOrderStatus(orderModel.getOrderStatus());
        if (!StringUtils.isBlank(money)){
            resBean.setMoney(money);
        }
        return resBean;
    }

    /**
     * @Description: 组装修改订单号的操作状态
     * @param id - 订单号的主键ID
     * @param didStatus - 1初始化，2用户加群，3用户发红包，4剔除成员，5收款失败，6收款部分（跟订单金额不相同），7收款成功
     * @param eliminateType - 剔除成员类型：1初始化，2需要剔除成员，3已剔除支付用户成员
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/23 16:00
     */
    public static OrderModel assembleOrderUpdateDidStatus(long id, int didStatus, int eliminateType, String remark){
        OrderModel resBean = new OrderModel();
        resBean.setId(id);
        resBean.setDidStatus(didStatus);
        if (!StringUtils.isBlank(remark)){
            resBean.setRemark(remark);
        }
        return resBean;
    }

    /**
     * @Description: 组装查询订单数据的查询条件
     * @param did - 用户ID
     * @return
     * @author yoko
     * @date 2020/7/23 17:50
    */
    public static OrderModel assembleOrderByCreateTime(long did, long collectionAccountId){
        OrderModel resBean = new OrderModel();
        resBean.setDid(did);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setCreateTime(DateUtil.addDateMinute(-5));
        return resBean;
    }


    /**
     * @Description: 校验数据是否是剔除成员信息
     * @param msg - 剔除成员信息
     * @return
     * @author yoko
     * @date 2020/7/23 20:54
    */
    public static boolean checkCatMember(String msg){
        boolean flag = false;
        try {

            CatMember catMember = JSON.parseObject(msg, CatMember.class);
            flag = true;
            return flag;
        }catch (Exception e){
            return flag;
        }
    }


    /**
     * @Description: 组装查询运营数据的方法
     * @param did - 用户ID
     * @param wxId - 小微的主键ID
     * @param collectionAccountId - 收款账号ID
     * @param groupWxid - 微信群ID
     * @param groupName - 微信群名称
     * @param orderNo - 订单号
     * @param punishType - 处罚类型：1不处罚，2处罚
     * @return com.hz.task.master.core.model.operate.OperateModel
     * @author yoko
     * @date 2020/7/23 22:09
     */
    public static OperateModel assembleOperateQuery(long did, long wxId, long collectionAccountId, String groupWxid,
                                                    String groupName, String orderNo, int punishType){
        OperateModel resBean = new OperateModel();
        if (did > 0){
            resBean.setDid(did);
        }
        if (wxId > 0){
            resBean.setWxId(wxId);
        }
        if (collectionAccountId > 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        if (!StringUtils.isBlank(groupWxid)){
            resBean.setGroupWxid(groupWxid);
        }
        if (!StringUtils.isBlank(groupName)){
            resBean.setGroupName(groupName);
        }
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        if (punishType > 0){
            resBean.setPunishType(punishType);
        }
        return resBean;

    }

    /**
     * @Description: 组装根据收款账号ID查询的方法
     * @param id - 收款账号ID
     * @return
     * @author yoko
     * @date 2020/7/25 19:58
    */
    public static DidCollectionAccountModel assembleDidCollectionAccountByIdQuery(long id){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(id);
        return resBean;
    }


    /**
     * @Description: 组装订单步骤详情的数据
     * @param id - 主键ID
     * @param did - 用户ID
     * @param orderModel - 订单信息
     * @param collectionAccountId - 收款账号ID
     * @param isOkCollectionAccount - 收款账号是否正常：1正常，2异常（修改名称，被删除）
     * @param money - 用户成功收款上报的金额
     * @param redPackInvalidType - 支付用户发红包是否超时：1初始化，2已超时，3未超时
     * @param moneyFitType - 金额是否与上报金额一致：1初始化，2少了，3多了，4一致
     * @param replyInvalidType - 回复成功or失败是否超时：1初始化，2已超时，3未超时
     * @param eliminateType - 剔除成员类型：1初始化，2需要剔除成员，3已剔除支付用户成员
     * @param redPackTime - 发红包的时间
     * @param replyTime - 回复时间
     * @return com.hz.task.master.core.model.order.OrderStepModel
     * @author yoko
     * @date 2020/7/25 21:07
     */
    public static OrderStepModel assembleOrderStepData(long id, long did, OrderModel orderModel, long collectionAccountId, int isOkCollectionAccount, String money, int redPackInvalidType,
                                                          int moneyFitType, int replyInvalidType, int eliminateType, String redPackTime, String replyTime) throws Exception{
        OrderStepModel resBean = new OrderStepModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (did > 0){
            resBean.setDid(did);
        }
        if (orderModel != null && orderModel.getId() > 0){
            if (!StringUtils.isBlank(orderModel.getOrderNo())){
                resBean.setOrderNo(orderModel.getOrderNo());
            }
            if (!StringUtils.isBlank(orderModel.getOrderMoney())){
                resBean.setOrderMoney(orderModel.getOrderMoney());
            }
            if (orderModel.getOrderStatus() != null && orderModel.getOrderStatus() > 0){
                resBean.setOrderStatus(orderModel.getOrderStatus());
            }
            if (!StringUtils.isBlank(orderModel.getInvalidTime())){
                resBean.setInvalidTime(orderModel.getInvalidTime());
            }
        }
        if (collectionAccountId > 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        if (isOkCollectionAccount > 0){
            resBean.setIsOkCollectionAccount(2);
        }
        if (!StringUtils.isBlank(money)){
            resBean.setMoney(money);
        }
        if (redPackInvalidType > 0){
            resBean.setRedPackInvalidType(redPackInvalidType);
        }
        if (moneyFitType > 0){
            resBean.setMoneyFitType(moneyFitType);
        }
        if (replyInvalidType > 0){
            resBean.setReplyInvalidType(replyInvalidType);
        }
        if (eliminateType > 0){
            resBean.setEliminateType(eliminateType);
        }
        if (!StringUtils.isBlank(redPackTime)){
            resBean.setRedPackTime(redPackTime);
        }
        if (!StringUtils.isBlank(replyTime)){
            resBean.setReplyTime(replyTime);
        }
        return resBean;
    }


    /**
     * @Description: 组装根据主键ID或者订单号查询订单步骤的方法
     * @param id - 主键ID
     * @param orderNo - 订单号
     * @return com.hz.task.master.core.model.order.OrderStepModel
     * @author yoko
     * @date 2020/7/25 21:23
     */
    public static OrderStepModel assembleOrderStepByIdOrOrderNoQuery(long id, String orderNo){
        OrderStepModel resBean = new OrderStepModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        return resBean;
    }
    
    
    /**
     * @Description: 组装查询订单初始化，但是超过有效期的订单的查询条件
     * @param orderStatus - 订单状态
     * @return 
     * @author yoko
     * @date 2020/7/26 14:24 
    */
    public static StatusModel assembleStatusModelQueryByInvalidTime(int limitNum, int orderStatus){
        StatusModel resBean = new StatusModel();
        resBean.setOrderStatus(orderStatus);
//        resBean.setInvalidTime("1");
        resBean.setLimitNum(limitNum);
        return resBean;
    }

    /**
     * @Description: 组装修改订单的状态的方法
     * @param id - 订单主键ID
     * @param orderStatus - 订单状态
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/26 14:43
     */
    public static OrderModel assembleOrderUpdateStatus(long id, int orderStatus){
        OrderModel resBean = new OrderModel();
        resBean.setId(id);
        resBean.setOrderStatus(orderStatus);
        return resBean;
    }

    /**
     * @Description: 组装修改订单的状态、运行状态的方法
     * @param id - 订单主键ID
     * @param orderStatus - 订单状态
     * @param runStatus - 运行计算状态：：0初始化，1锁定，2计算失败，3计算成功
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/26 14:43
     */
    public static OrderModel assembleOrderUpdateOrderStatusAndRunStatus(long id, int orderStatus, int runStatus){
        OrderModel resBean = new OrderModel();
        resBean.setId(id);
        resBean.setOrderStatus(orderStatus);
        resBean.setRunStatus(runStatus);
        return resBean;
    }

    /**
     * @Description: 组装更新订单状态的条件
     * @param orderNo - 订单号
     * @param orderStatus - 订单状态
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/26 16:56
     */
    public static OrderModel assembleOrderUpdateStatusByOrderNo(String orderNo, int orderStatus){
        OrderModel resBean = new OrderModel();
        resBean.setOrderNo(orderNo);
        resBean.setOrderStatus(orderStatus);
        return resBean;
    }


    /**
     * @Description: 组装添加派单数据的方法-微信群-补充订单
     * @param did -  用户ID
     * @param orderNo - 订单号
     * @param orderMoney - 订单金额
     * @param collectionType - 支付类型
     * @param remark - 备注
     * @param replenishType - 是否是补单：1初始化不是补单，2是补单
     * @return com.hz.fine.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/6/2 14:53
     */
    public static OrderModel assembleOrderByReplenish(long did, String orderNo, String orderMoney,
                                                       long collectionAccountId, int collectionType, String remark, int replenishType) throws Exception{
        OrderModel resBean = new OrderModel();
        resBean.setDid(did);
        resBean.setOrderNo(orderNo);
        resBean.setOrderMoney(orderMoney);
        resBean.setOrderStatus(3);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setCollectionType(collectionType);
        // 订单失效时间
        resBean.setInvalidTime(DateUtil.getNowPlusTime());
        resBean.setRemark(remark);
        resBean.setReplenishType(replenishType);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 组装扣除用户余额流水的数据的方法
     * @param did - 用户ID
     * @param orderNo - 订单号
     * @param money - 订单金额
     * @param lockTime - 锁定时间
     * @return com.hz.fine.master.core.model.did.DidBalanceDeductModel
     * @author yoko
     * @date 2020/7/2 14:52
     */
    public static DidBalanceDeductModel assembleDidBalanceDeductAdd(long did, String orderNo, String money, int lockTime){
        DidBalanceDeductModel resBean = new DidBalanceDeductModel();
        resBean.setDid(did);
        resBean.setOrderNo(orderNo);
        resBean.setMoney(money);
        String delayTime = DateUtil.addDateMinute(30);
        resBean.setDelayTime(delayTime);
        resBean.setLockTime(DateUtil.addDateMinute(lockTime));
        resBean.setRemark("补充订单");
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 组装扣除用户余额流水的数据的方法-当订单上报金额多余订单金额的时候
     * @param did - 用户ID
     * @param orderNo - 订单号
     * @param money - 订单金额
     * @param lockTime - 锁定时间
     * @return com.hz.fine.master.core.model.did.DidBalanceDeductModel
     * @author yoko
     * @date 2020/7/2 14:52
     */
    public static DidBalanceDeductModel assembleDidBalanceDeductAddByManyMoney(long did, String orderNo, String money, int lockTime, String remark){
        DidBalanceDeductModel resBean = new DidBalanceDeductModel();
        resBean.setDid(did);
        resBean.setOrderNo(orderNo);
        resBean.setMoney(money);
        String delayTime = DateUtil.addDateMinute(30);
        resBean.setDelayTime(delayTime);
        resBean.setLockTime(DateUtil.addDateMinute(lockTime));
        resBean.setOrderStatus(3);
        resBean.setRemark(remark);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: 组装更新用户金额的方法
     * @param did - 用户ID
     * @param orderMoney - 派单的具体金额
     * @return com.hz.fine.master.core.model.did.DidModel
     * @author yoko
     * @date 2020/6/9 10:47
     */
    public static DidModel assembleUpdateDidBalance(long did, String orderMoney){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        resBean.setOrderMoney(orderMoney);
        return resBean;
    }


    /**
     * @Description: 组装修改订单号的操作状态的方法
     * @param id - 订单号的主键ID
     * @param isRedPack - 是否发了红包：1初始化未发红包，2发了红包
     * @param redPackTime - 发红包的时间
     * @param isReply - 是否回复：1初始化未回复，2系统默认回复，3已回复失败，4已回复成功
     * @param replyData - 回复类容
     * @param replyTime - 回复时间
     * @param actualMoney - 回复的实际金额
     * @param moneyFitType - 金额是否与上报金额一致：1初始化，2少了，3多了，4一致
     * @param remark - 备注
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/23 16:00
     */
    public static OrderModel assembleOrderUpdateRedPackData(long id, int isRedPack, String redPackTime, int isReply, String replyData,
                                                            String replyTime, String actualMoney, int moneyFitType, String remark, String profit){
        OrderModel resBean = new OrderModel();
        resBean.setId(id);
        if(isRedPack > 0){
            resBean.setIsRedPack(isRedPack);
        }
        if (!StringUtils.isBlank(redPackTime)){
            resBean.setRedPackTime(redPackTime);
        }
        if (isReply > 0){
            resBean.setIsReply(isReply);
        }
        if (!StringUtils.isBlank(replyData)){
            resBean.setReplyData(replyData);
        }
        if (!StringUtils.isBlank(replyTime)){
            resBean.setReplyTime(replyTime);
        }
        if (!StringUtils.isBlank(actualMoney)){
            resBean.setActualMoney(actualMoney);
        }
        if (moneyFitType > 0){
            resBean.setMoneyFitType(moneyFitType);
        }
        if (!StringUtils.isBlank(remark)){
            resBean.setRemark(remark);
        }
        if (!StringUtils.isBlank(profit)){
            resBean.setProfit(profit);
        }
        return resBean;
    }

    /**
     * @Description: 组装跟新用户的锁定金额
     * @param did - 用户ID
     * @param lockMoney - 锁定金额
     * @return
     * @author yoko
     * @date 2020/7/28 15:51
    */
    public static DidModel assembleDidDataByLockMoney(long did, String lockMoney){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        resBean.setLockMoney(lockMoney);
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
     * @Description: 计算派单的订单金额的奖励金额（收益）
     * @param consumeMoneyList - 消耗金额范围内的奖励规则列表
     * @param orderMoney - 订单金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/6/6 11:48
     */
    public static String getConsumeProfit(List<StrategyData> consumeMoneyList, String orderMoney){
        String profit = "";
        for (StrategyData dataModel : consumeMoneyList){
            String [] rule = dataModel.getStgValue().split("-");
            String ratio = dataModel.getStgValueOne();
            if(rule[0].equals(rule[1])){
                double dbl = Double.parseDouble(rule[0]);
                if (Double.parseDouble(orderMoney) >= dbl){
                    profit = StringUtil.getMultiply(orderMoney, ratio);
                    break;
                }
            }else{
                double start = Double.parseDouble(rule[0]);
                double end = Double.parseDouble(rule[1]);
                if (Double.parseDouble(orderMoney) >= start && Double.parseDouble(orderMoney) <= end){
                    profit = StringUtil.getMultiply(orderMoney, ratio);
                    break;
                }
            }
        }
        return profit;
    }

    /**
     * @Description: 组装修改小微旗下店员关联关系的方法
     * @param wxId - 小微主键ID
     * @param collectionAccountId - 收款账号主键ID
     * @return com.hz.task.master.core.model.wx.WxClerkModel
     * @author yoko
     * @date 2020/6/15 17:59
     */
    public static WxClerkModel assembleWxClerkAddOrQuery(long wxId, long collectionAccountId){
        WxClerkModel resBean = new WxClerkModel();
        resBean.setWxId(wxId);
        resBean.setCollectionAccountId(collectionAccountId);
        return resBean;
    }


    /**
     * @Description: 组装小微加好友记录的添加方法或者查询方法
     * @param wxId - 小微的主键ID
     * @param did - 用户ID
     * @param collectionAccountId - 收款账号ID
     * @param finalFromWxid - 微信的原始账号
     * @param curday - 日期
     * @param fromWxid - 微信群ID
     * @param dataType - 数据类型：0初始化，1加好友，2加群
     * @return com.hz.task.master.core.model.wx.WxFriendModel
     * @author yoko
     * @date 2020/8/1 18:07
     */
    public static WxFriendModel assembleWxFriendAddOrQuery(long wxId, long did, long collectionAccountId, String finalFromWxid, int curday, String fromWxid, int dataType){
        WxFriendModel resBean = new WxFriendModel();
        if (wxId > 0){
            resBean.setWxId(wxId);
        }
        if (did > 0){
            resBean.setDid(did);
        }
        if (collectionAccountId > 0){
            resBean.setCollectionAccountId(collectionAccountId);
        }
        if (!StringUtils.isBlank(finalFromWxid)){
            resBean.setFinalFromWxid(finalFromWxid);
        }
        if(curday > 0){
            resBean.setCurday(curday);
        }
        if (!StringUtils.isBlank(fromWxid)){
            resBean.setFromWxid(fromWxid);
        }
        if (dataType > 0){
            resBean.setDataType(dataType);
        }
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }
    
    /**
     * @Description: 组装查询小微的查询条件
     * @param isOk - 是否以及完成了限制目标：1未完成，2完成
     * @return 
     * @author yoko
     * @date 2020/8/2 9:43 
    */
    public static WxModel assembleWxByIsOk(int isOk){
        WxModel resBean = new WxModel();
        resBean.setIsOk(isOk);
        return resBean;
    }

    /**
     * @Description: 组装查询小微与好友纪录的查询条件
     * @param wxId - 小微的主键ID
     * @param curday - 日期
     * @param dataType - 数据类型：0初始化，1加好友，2加群
     * @return
     * @author yoko
     * @date 2020/8/2 9:55
    */
    public static WxFriendModel assembleWxFriendQuery(long wxId, int curday, int dataType){
        WxFriendModel resBean = new WxFriendModel();
        resBean.setWxId(wxId);
        if (curday > 0){
            resBean.setCurday(curday);
        }
        if (dataType > 0){
            resBean.setDataType(dataType);
        }
        return resBean;
    }


    /**
     * @Description: 组装更新小微加好友限制的组装方法
     * @param id - 小微主键ID
     * @param isOk - 是否以及完成了限制目标：1未完成，2完成
     * @param isDataNum - 已经加了多少用户
     * @param isOkGroup - 加群是否以及完成了限制目标：1未完成，2完成
     * @param isGroupNum - 已经加了多少个群
     * @return
     * @author yoko
     * @date 2020/8/2 10:21
    */
    public static WxModel assembleWxUpdate(long id, int isOk, int isDataNum, int isOkGroup, int isGroupNum){
        WxModel resBean = new WxModel();
        resBean.setId(id);
        if (isOk > 0){
            resBean.setIsOk(isOk);
        }
        if (isDataNum > 0){
            resBean.setIsDataNum(isDataNum);
        }
        if (isOkGroup > 0){
            resBean.setIsOkGroup(isOkGroup);
        }
        if (isGroupNum > 0){
            resBean.setIsGroupNum(isGroupNum);
        }
        return resBean;
    }


    /**
     * @Description: 组装更新收款账号发红包个数的方法
     * @param id - 收款账号主键ID
     * @param redPackNum - 发红包个数
     * @param isInvalid - 是否失效：1未失效，2已失效
     * @return com.hz.task.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/8/3 11:16
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateRedPackNumOrInvalid(long id, int redPackNum, int isInvalid){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(id);
        resBean.setRedPackNum(redPackNum);
        if (isInvalid > 0){
            resBean.setIsInvalid(isInvalid);
        }
        return resBean;
    }

    /**
     * @Description: 组装查询开启出码的用户账号
     * @param switchType - 个人出码开关：1打开状态，2暂停状态
     * @return
     * @author yoko
     * @date 2020/8/3 15:44
    */
    public static DidModel assembleDidBySwitch(int switchType){
        DidModel resBean = new DidModel();
        resBean.setSwitchType(switchType);
        return resBean;
    }


    /**
     * @Description: 组装查询有效的收款账号的查询条件
     * @param did - 用户ID
     * @param acType - 用户账号类型
     * @return com.hz.task.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/8/3 15:54
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountByEffective(long did, int acType){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setDid(did);
        resBean.setAcType(acType);
        return resBean;
    }

    /**
     * @Description: 组装更新用户的群序号更出码开关的方法
     * @param did - 用户ID
     * @param groupNum - 群序号
     * @param switchType - 个人出码开关：1打开状态，2暂停状态
     * @return com.hz.fine.master.core.model.did.DidModel
     * @author yoko
     * @date 2020/7/30 20:09
     */
    public static DidModel assembleUpdateGroupOrSwitchData(long did, int groupNum, int switchType){
        DidModel resBean = new DidModel();
        resBean.setId(did);
        if (groupNum > 0){
            resBean.setGroupNum(groupNum);
        }
        if (switchType > 0){
            resBean.setSwitchType(switchType);
        }
        return resBean;
    }

    /**
     * @Description: 组装更新收款账号的审核信息的方法
     * @param id - 主键ID
     * @param checkStatus - 审核状态
     * @param checkInfo - 审核说明
     * @return com.hz.task.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/8/7 20:45
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateCheckInfo(long id, int checkStatus, String checkInfo){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setId(id);
        resBean.setCheckInfo(checkInfo);
        return resBean;
    }


    /**
     * @Description: 组装查询根据条件获取派发订单里面去重复的小微ID集合
     * @param wxId - 小微主键ID
     * @param curday - 日期
     * @param limitNum - 查询的条数
     * @return
     * @author yoko
     * @date 2020/1/11 16:23
     */
    public static WxOrderModel assembleTaskWxOrderByFialNumQuery(long wxId, int curday, int limitNum){
        WxOrderModel resBean = new WxOrderModel();
        if (wxId > 0){
            resBean.setWxId(wxId);
        }
        if (curday > 0){
            resBean.setCurday(curday);
        }
        if (limitNum > 0){
            resBean.setLimitNum(limitNum);
        }
        return resBean;
    }


    /**
     * @Description: 组装查询小微订单纪录的查询条件
     * @param wxId - 小微主键ID
     * @param collectionAccountId - 收款账号ID
     * @return com.hz.task.master.core.model.wx.WxOrderModel
     * @author yoko
     * @date 2020/8/8 20:47
     */
    public static WxOrderModel assembleWxOrderQuery(long wxId, long collectionAccountId, int dataType){
        WxOrderModel resBean = new WxOrderModel();
        resBean.setWxId(wxId);
        resBean.setCollectionAccountId(collectionAccountId);
        resBean.setDataType(dataType);
        return resBean;
    }


    /**
     * @Description: 组装更新小微订单记录的回执数据
     * @param id - 主键ID
     * @param dataType - 数据类型;1初始化，2表示小微有数据回执
     * @return com.hz.task.master.core.model.wx.WxOrderModel
     * @author yoko
     * @date 2020/8/8 20:51
     */
    public static WxOrderModel assembleWxOrderUpdate(long id, int dataType){
        WxOrderModel resBean = new WxOrderModel();
        resBean.setId(id);
        resBean.setDataType(dataType);
        return resBean;
    }


    /**
     * @Description: 解析微信数据type =1的数据，根据msg数据来确定此数据的类型
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/21 21:16
     */
    public static int getWxDataTypeByOne(String msg){
        int num = 0;
        if (msg.equals("3")){
            num = 3;
        }else if (msg.equals("2")){
            num = 8;
        }else if(msg.equals("4")){
            num = 9;
        }else if(msg.indexOf("1#") > -1){
            String [] fg_msg = msg.split("#");
            if (fg_msg.length == 2){
                String money = fg_msg[1];
                // 金额是否有效
                if (money.indexOf(".") > -1){
                    String[] fg_money = money.split("\\.");
                    boolean flag1 = StringUtil.isNumer(fg_money[0]);
                    boolean flag2 = StringUtil.isNumer(fg_money[1]);
//                    boolean flag = StringUtil.isNumberByMoney(money);
                    if (flag1 && flag2){
                        num = 7;
                    }else {
                        num = 2;
                        log.info("");
                    }
                }else {
                    boolean flag = StringUtil.isNumer(money);
                    if (flag){
                        num = 7;
                    }else {
                        num = 2;
                    }
                }
            }else {
                num = 2;
            }
        }else{
            num = 2;
        }
        return num;
    }


    /**
     * @Description: 解析微信数据type =10000的数据，根据msg数据来确定此数据的类型
     * @param msg
     * @return
     * @author yoko
     * @date 2020/7/21 21:16
     */
    public static int getWxDataTypeByTenThousand(String msg){
        int num = 0;
        if (msg.indexOf("分享的二维码加入") > -1){
            // 加群:只计算不属于小微加群的信息
            // 其它成员加群数据案例：卢云"通过扫描"龙-接单"分享的二维码加入群聊
            // 小微加群数据案例：龙-接单"邀请你加入了群聊，群聊参与人还有：卢云
            num = 4;
        }else if (msg.indexOf("收到红包，请在手机上查看") > -1){
            num = 5;
        }else if(msg.indexOf("移出群聊") > -1){
            num = 6;
        }
        else{
            num = 2;
        }
        return num;
    }


    /**
     * @Description: 修改用户收款账号的审核信息
     * @param acNum - 用户的微信ID
     * @param acType - 收款账号类型
     * @param checkInfo - 被审核的原因
     * @return
     * @author yoko
     * @date 2020/6/12 23:08
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateCheckDataInfoByAcNum(String acNum, int acType, String checkInfo){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setAcNum(acNum);
        resBean.setAcType(acType);
        resBean.setCheckStatus(1);
        resBean.setCheckInfo(checkInfo);
        return resBean;
    }

    /**
     * @Description: 组装修改小微登入、登出状态的方法
     * @param id - 小微主键ID
     * @param loginType - 小微登录状态：1正常登录中，2登入
     * @return com.hz.task.master.core.model.wx.WxModel
     * @author yoko
     * @date 2020/8/12 20:03
     */
    public static WxModel assembleWxUpdateByLoginType(long id, int loginType){
        WxModel resBean = new WxModel();
        resBean.setId(id);
        resBean.setLoginType(loginType);
        return resBean;
    }


    /**
     * @Description: 修改用户收款账号的归属小微登录状态：登入、登出的方法
     * @param wxId - 小微主键ID
     * @param acType - 收款账号类型
     * @param loginType - 归属小微登录状态：1登出/未登录，2登入/已登录
     * @return
     * @author yoko
     * @date 2020/6/12 23:08
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountUpdateLoginType(long wxId, int acType, int loginType){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setWxId(wxId);
        resBean.setAcType(acType);
        resBean.setLoginType(loginType);
        return resBean;
    }


    /**
     * @Description: check校验用户余额是否低于池子中的用户最低保底金额
     * @param didModel - 用户信息
     * @param minMoney - 策略里面池子中的用户余额不得低于的保底金额
     * @return void
     * @author yoko
     * @date 2020/8/13 19:28
     */
    public static boolean checkDidMoney(DidModel didModel, String minMoney){
        boolean flag = false;
        if (didModel == null || didModel.getId() == null || didModel.getId() <= 0){
            return false;
        }

        if(StringUtils.isBlank(didModel.getBalance())){
            return false;
        }else{
            boolean flag_money = StringUtil.getBigDecimalSubtract(didModel.getBalance(), minMoney);
            if (!flag_money){
                return false;
            }else {
                flag = true;
            }
        }
        return flag;
    }


    /**
     * @Description: 组装添加移出池子的起因数据
     * @param did - 用户ID
     * @param dataType - 数据类型:1初始化，2其它，3余额不足，4有效群不足，5有订单未回复，6有违规操作
     * @param origin - 起因
     * @return com.hz.task.master.core.model.pool.PoolOriginModel
     * @author yoko
     * @date 2020/8/14 16:18
     */
    public static PoolOriginModel assemblePoolOriginAdd(long did, int dataType, String origin){
        PoolOriginModel resBean = new PoolOriginModel();
        resBean.setDid(did);
        resBean.setDataType(dataType);
        resBean.setOrigin(origin);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: 根据条件查询用户获取微信群收款账号信息-有效
     * @param did - 用户ID
     * @param acType - 收款账号类型
     * @param isInvalid - 是否失效：1未失效，2已失效
     * @param checkStatus - 收款账号审核：1初始化，2审核失败，3审核成功
     * @param useStatus - 使用状态:1初始化有效正常使用，2无效暂停使用
     * @param loginType - 归属小微登录状态：1登出/未登录，2登入/已登录
     * @param countGroupNum - 有效微信群个数
     * @return com.hz.fine.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/5/15 17:17
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountListEffective(long did, int acType, int isInvalid, int checkStatus,int useStatus, int loginType, int countGroupNum){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        resBean.setDid(did);
        if(acType > 0){
            resBean.setAcType(acType);
        }
        resBean.setIsInvalid(isInvalid);
        if (isInvalid == 1){
            // 未失效
            resBean.setInvalidTimeStart("1");
        }
        resBean.setCheckStatus(checkStatus);
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        if (loginType > 0){
            resBean.setLoginType(loginType);
        }
        if (countGroupNum > 0){
            resBean.setCountGroupNum(countGroupNum);
        }
        return resBean;
    }

    /**
     * @Description: 根据条件查询用户获取微信群收款账号信息-有效
     * @param didList - 用户ID集合
     * @param acType - 收款账号类型
     * @param isInvalid - 是否失效：1未失效，2已失效
     * @param checkStatus - 收款账号审核：1初始化，2审核失败，3审核成功
     * @param useStatus - 使用状态:1初始化有效正常使用，2无效暂停使用
     * @param loginType - 归属小微登录状态：1登出/未登录，2登入/已登录
     * @param countGroupNum - 有效微信群个数
     * @return com.hz.fine.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/5/15 17:17
     */
    public static DidCollectionAccountModel assembleDidCollectionAccountByDidListEffective(List<Long> didList, int acType, int isInvalid, int checkStatus,int useStatus, int loginType, int countGroupNum){
        DidCollectionAccountModel resBean = new DidCollectionAccountModel();
        if (didList != null && didList.size() > 0){
            resBean.setIdList(didList);
        }
        if(acType > 0){
            resBean.setAcType(acType);
        }
        resBean.setIsInvalid(isInvalid);
        if (isInvalid == 1){
            // 未失效
            resBean.setInvalidTimeStart("1");
        }
        resBean.setCheckStatus(checkStatus);
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        if (loginType > 0){
            resBean.setLoginType(loginType);
        }
        if (countGroupNum > 0){
            resBean.setCountGroupNum(countGroupNum);
        }
        return resBean;
    }

    /**
     * @Description: check用户有效群信息
     * @param didCollectionAccountList - 有效群集合
     * @return
     * @author yoko
     * @date 2020/8/13 20:02
     */
    public static boolean checkDidCollectionAccountListEffective(List<DidCollectionAccountModel> didCollectionAccountList) throws Exception{
        if (didCollectionAccountList == null || didCollectionAccountList.size() <= 0){
            return false;
        }else{
            return true;
        }
    }

    /**
     * @Description: 组装查询订单信息的查询条件
     * @param did - 用户ID
     * @param collectionType - 收款账号类型：1微信，2支付宝，3微信群
     * @param replenishType -  是否是补单：1初始化不是补单，2是补单
     * @param isRedPack - 是否发了红包：1初始化未发红包，2发了红包
     * @param isReply - 是否回复：1初始化未回复，2系统默认回复，3已回复失败，4已回复成功
     * @return com.hz.fine.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/7/20 20:48
     */
    public static OrderModel assembleOrderByIsReply(long did, int collectionType, int replenishType, int isRedPack, int isReply){
        OrderModel resBean = new OrderModel();
        resBean.setDid(did);
        if(collectionType > 0){
            resBean.setCollectionType(collectionType);
        }
        if(replenishType > 0){
            resBean.setReplenishType(replenishType);
        }
        if (isRedPack > 0){
            resBean.setIsRedPack(isRedPack);
        }
        if (isReply > 0){
            resBean.setIsReply(isReply);
        }
        return resBean;
    }


    /**
     * @Description: check用户是否有未回复的订单信息
     * @param orderModel - 订单信息
     * @return
     * @author yoko
     * @date 2020/8/13 20:02
     */
    public static boolean checkOrderByIsReply(OrderModel orderModel){
        if (orderModel != null && orderModel.getId() > 0){
            return false;
        }else {
            return true;
        }
    }

    /**
     * @Description: 组装更新抢单等待中的条件
     * @param id - 主键ID
     * @param did - 用户ID
     * @return com.hz.fine.master.core.model.pool.PoolWaitModel
     * @author yoko
     * @date 2020/8/13 17:36
     */
    public static PoolWaitModel assemblePoolWaitUpdate(long id, long did, int yn){
        PoolWaitModel resBean = new PoolWaitModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (did > 0){
            resBean.setDid(did);
        }
        if (yn > 0){
            resBean.setYn(yn);
        }
        return resBean;
    }

    /**
     * @Description: 组装更新抢单进行中的查询条件
     * @param id - 主键ID
     * @param did - 用户ID
     * @return com.hz.fine.master.core.model.pool.PoolWaitModel
     * @author yoko
     * @date 2020/8/13 17:36
     */
    public static PoolOpenModel assemblePoolOpenUpdate(long id, long did, int yn){
        PoolOpenModel resBean = new PoolOpenModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (did > 0){
            resBean.setDid(did);
        }
        if (yn > 0){
            resBean.setYn(yn);
        }
        return resBean;
    }


    /**
     * @Description: 组装添加抢单进行中的方法
     * @param did - 用户ID
     * @return com.hz.fine.master.core.model.pool.PoolWaitModel
     * @author yoko
     * @date 2020/8/13 17:36
     */
    public static PoolOpenModel assemblePoolOpenAdd(long did, int dataType){
        PoolOpenModel resBean = new PoolOpenModel();
        resBean.setDid(did);
        if (dataType > 0){
            resBean.setDataType(dataType);
        }
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: 根据订单ID修改订单状态以及备注
     * @param id - 主键ID
     * @param orderStatus - 订单状态
     * @param remark - 备注
     * @return com.hz.task.master.core.model.order.OrderModel
     * @author yoko
     * @date 2020/8/18 14:57
     */
    public static OrderModel assembleUpdateOrderById(long id, int orderStatus, String remark){
        OrderModel resBean = new OrderModel();
        resBean.setId(id);
        resBean.setOrderStatus(orderStatus);
        resBean.setRemark(remark);
        return resBean;
    }











    public static void main(String [] args) {
        String reg = "^.*\\d{4}.*$";
        String s = "您尾号8902的储蓄卡账户2020年4月24日14时39分永久挂失成功。详情请询95533。[建设银行]";
        if (s.matches(reg)) {
            // TODO
        }
        String content = "【南京银行】您尾号5298的账号与10月10日15时11份收到由无锡公司汇入的300.00元";
        String startKey = "汇入的#人民币";
        String endKey = "元#（付方";
        String[] startKeyArr = startKey.split("#");
        String[] endKeyArr = endKey.split("#");
        int startIndex = 0;
        int endIndex = 0;
        for (String str : startKeyArr) {
            startIndex = getIndexOfByStr(content, str);
            if (startIndex > 0) {
                startIndex = startIndex + str.length();
                break;
            }
        }

        for (String str : endKeyArr) {
            endIndex = getIndexOfByStr(content, str);
            if (endIndex > 0) {
                break;
            }
        }
        String money = content.substring(startIndex, endIndex);
        System.out.println("money:" + money);


        String json = "[{\"id\":1,\"stgKey\":1,\"stgValue\":\"1000.00\",\"stgValueOne\":\"0.01\",\"stgValueTwo\":1},{\"id\":2,\"stgKey\":2,\"stgValue\":\"2000.00\",\"stgValueOne\":\"0.02\",\"stgValueTwo\":2},{\"id\":3,\"stgKey\":3,\"stgValue\":\"3000.00\",\"stgValueOne\":\"0.03\",\"stgValueTwo\":3},{\"id\":8,\"stgKey\":8,\"stgValue\":\"8000.01\",\"stgValueOne\":\"0.03\",\"stgValueTwo\":3},{\"id\":4,\"stgKey\":4,\"stgValue\":\"4000.00\",\"stgValueOne\":\"0.04\",\"stgValueTwo\":4},{\"id\":5,\"stgKey\":5,\"stgValue\":\"5000.00\",\"stgValueOne\":\"0.05\",\"stgValueTwo\":5}]";
        List<StrategyData> dataList = JSON.parseArray(json, StrategyData.class);
//        Optional<StrategyData> userOp= dataList.stream().max(Comparator.comparingInt(StrategyData ::getStgValue));
        Optional<StrategyData> userOp= dataList.stream().max(Comparator.comparing(StrategyData :: getStgValue));
        StrategyData maxEmp = userOp.get();
        System.out.println("maxEmp:" + maxEmp.getStgValue());

        Optional<StrategyData> userOp1= dataList.stream().max(Comparator.comparing(StrategyData :: getStgValueTwo));
        StrategyData maxEmp1 = userOp1.get();
        System.out.println("maxEmp1:" + maxEmp1.getStgValueTwo());

        List<Long> idList = dataList.stream().map(StrategyData::getId).collect(Collectors.toList());
        for (Long id : idList){
            System.out.println("id:" + id);
        }
//        List<String> tableNames=list.stream().map(User::getMessage).collect(Collectors.toList());
//        Double sum = investorList.stream().mapToDouble(n -> CommonUtils.isNumeric(n.getInvestMoney()) ?
//        int ageSum = userList.stream().collect(Collectors.summingInt(User::getAge));

//        String sb1 = "1,0,0,4".replaceAll(",","");
        String sb1 = "1004".replaceAll(",","");
        System.out.println("sb1:" + sb1);

        String sb2 = "老只((3asd1(**增)";
        if (sb2.indexOf("(") > -1){
            int a = sb2.lastIndexOf("(");
//            String [] wxNameArr = sb2.split("\\(");
//            resBean.setWxName(wxNameArr[0]);
            String sb3 = sb2.substring(0, a);
            System.out.println("sb3:" + sb3);
        }
        StrategyZfbMoneyRule strategyZfbMoneyRule = new StrategyZfbMoneyRule();
        strategyZfbMoneyRule.setStartKey("付款#收款");
        strategyZfbMoneyRule.setEndKey("元");
        String sb4 = getZfbMoney("卢云通过扫码向你付款0.01元收款通知", strategyZfbMoneyRule);
        System.out.println("sb4:" + sb4);

        boolean flag = isInteger("0");
        System.out.println("flag:" + flag);
        String divideStr = "55.55";
        String [] str = divideStr.split("\\.");
        System.out.println("str:" + str.length);

        String sb5 = "你被\"卢云\"移出群聊";
        String sb5_start = sb5.substring(0,2);
        String sb5_end = sb5.substring(sb5.length()-4,sb5.length());
        System.out.println("sb5_start:"+ sb5_start);
        System.out.println("sb5_end:"+ sb5_end);

    }





}
