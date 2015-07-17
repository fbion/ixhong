package com.ixhong.common.yeepay.tzt;

/**
 * Created by liuguanqing on 15/5/28.
 */
public class TZTConstants {

    //绑卡请求地址
    public static final String BIND_BANK_CARD_URL = "https://ok.yeepay.com/payapi/api/tzt/invokebindbankcard";
    //绑卡确认请求地址
    public static final String CONFIRM_BIND_BANK_CARD_URL = "https://ok.yeepay.com/payapi/api/tzt/confirmbindbankcard";
    //支付请求地址
    public static final String DIRECT_BIND_PAY_URL = "https://ok.yeepay.com/payapi/api/tzt/directbindpay";
    //订单查询请求地址
    public static final String PAYMENT_QUERY_URL = "https://ok.yeepay.com/merchant/query_server/pay_single";
    //取现接口请求地址
    public static final String WITHDRAW_URL = "https://ok.yeepay.com/payapi/api/tzt/withdraw";
    //取现查询接口请求地址
    public static final String QUERY_WITHDRAW_URL = "https://ok.yeepay.com/payapi/api/tzt/drawrecord";
    //绑卡查询接口请求地址
    public static final String QUERY_AUTH_BIND_LIST_URL = "https://ok.yeepay.com/payapi/api/bankcard/authbind/list";
    //银行卡信息查询接口请求地址
    public static final String BANK_CARD_CHECK_URL = "https://ok.yeepay.com/payapi/api/bankcard/check";
    //清算数据下载请求地址
    public static final String PAY_CLEAR_DATA_URL = "https://ok.yeepay.com/merchant/query_server/pay_clear_data";
    //解绑银行卡
    public static final String UNBIND_BANK_CARD_URL = "https://ok.yeepay.com/payapi/api/tzt/unbind";
    //退款
    public static final String DIRECT_REFUND_URL = "https://ok.yeepay.com/merchant/query_server/direct_refund";
    //退款查询
    public static final String QUERY_REFUND_URL = "https://ok.yeepay.com/merchant/query_server/refund_single";
}
