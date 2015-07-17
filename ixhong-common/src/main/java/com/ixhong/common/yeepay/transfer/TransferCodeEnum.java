package com.ixhong.common.yeepay.transfer;

/**
 * Created by shenhongxi on 6/3/15.
 */
public enum TransferCodeEnum {

    RECEIVED("0025", "已接收"),

    REMITTED("0026", "已汇款"),

    REFUND("0027", "已退款"),

    REJECTED("0028", "已拒绝"),

    CHECKING("0029", "待复核"),

    UNKNOWN("0030", "未知");

    public String code;

    public String meaning;

    public String getCode() {
        return code;

    }

    public String getMeaning() {
        return meaning;
    }

    TransferCodeEnum(String code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }
    public static TransferCodeEnum value(String code){
        for(TransferCodeEnum e:values()){
            if(e.code==code){
                return e;
            }
        }
        return null;
    }
}
