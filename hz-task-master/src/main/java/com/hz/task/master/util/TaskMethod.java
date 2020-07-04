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
import com.hz.task.master.core.model.cat.CatDataBindingModel;
import com.hz.task.master.core.model.cat.CatDataModel;
import com.hz.task.master.core.model.cat.CatDataOfflineModel;
import com.hz.task.master.core.model.did.*;
import com.hz.task.master.core.model.mobilecard.MobileCardDataModel;
import com.hz.task.master.core.model.mobilecard.MobileCardModel;
import com.hz.task.master.core.model.order.OrderModel;
import com.hz.task.master.core.model.strategy.StrategyData;
import com.hz.task.master.core.model.strategy.StrategyModel;
import com.hz.task.master.core.model.task.base.StatusModel;
import com.hz.task.master.core.model.task.cat.CatMsg;
import com.hz.task.master.core.model.task.did.TaskDidCollectionAccountDataModel;
import com.hz.task.master.core.model.wx.WxClerkDataModel;
import com.hz.task.master.core.model.wx.WxClerkModel;
import com.hz.task.master.core.model.wx.WxModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
     * @param rewardType - 奖励类型：1充值奖励，2充值总金额档次奖励，3直推奖励，4裂变奖励，5团队奖励
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
        //段峰
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


    }





}
