package com.hz.task.master.core.common.utils.constant;

/**
 * @author df
 * @Description:异常状态码
 * @create 2018-07-27 11:13
 **/
public class ErrorCode {

    /**
     * 常量异常
     */
    public final class ERROR_CONSTANT {
        /**
         * 没有被捕捉到的异常
         * 默认系统异常状态码=255
         */
        public static final String DEFAULT_EXCEPTION_ERROR_CODE = "255";

        /**
         * 没有被捕捉到的异常
         * 默认系统异常错误信息=SYS_ERROR
         */
        public static final String DEFAULT_EXCEPTION_ERROR_MESSAGE = "ERROR";

        /**
         * 被捕捉到的异常，并且捕捉的异常错误码为空，则默认异常状态码
         * 默认捕捉的异常状态码=256
         */
        public static final String DEFAULT_SERVICE_EXCEPTION_ERROR_CODE = "256";

        /**
         * 被捕捉到的异常，但是错误信息为空，则默认异常信息提醒
         * 默认捕捉的异常信息提醒=错误
         */
        public static final String DEFAULT_SERVICE_EXCEPTION_ERROR_MESSAGE = "错误";

    }


    /**
     * 异常-枚举类
     */
    public enum ENUM_ERROR {








        /***********************************************
         * S打头表示策略数据的错误
         **********************************************/
        S00001("S00001", "策略:检查手机短信类型,数据为空!", "策略:检查手机短信类型,数据为空!"),





        ;

        /**
         * 错误码
         */
        private String eCode;
        /**
         * 给客户端看的错误信息
         */
        private String eDesc;
        /**
         * 插入数据库的错误信息
         */
        private String dbDesc;




        private ENUM_ERROR(String eCode, String eDesc,String dbDesc) {
            this.eCode = eCode;
            this.eDesc = eDesc;
            this.dbDesc  = dbDesc;
        }

        public String geteCode() {
            return eCode;
        }

        public void seteCode(String eCode) {
            this.eCode = eCode;
        }

        public String geteDesc() {
            return eDesc;
        }

        public void seteDesc(String eDesc) {
            this.eDesc = eDesc;
        }

        public String getDbDesc() {
            return dbDesc;
        }

        public void setDbDesc(String dbDesc) {
            this.dbDesc = dbDesc;
        }
    }
}
