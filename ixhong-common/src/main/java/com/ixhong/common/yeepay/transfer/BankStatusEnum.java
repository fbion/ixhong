package com.ixhong.common.yeepay.transfer;

/**
 * Created by shenhongxi on 6/3/15.
 */
public enum BankStatusEnum {

    SUCCESS("S", "已成功"),

    ONGOING("I", "银行处理中"),

    FAILURE("F", "出款失败"),

    WAIT("W", "未出款"),

    UNKNOWN("U", "未知");

    public String code;

    public String meaning;

    public String getCode() {
        return code;

    }

    public String getMeaning() {
        return meaning;
    }

    BankStatusEnum(String code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }
    public static BankStatusEnum value(String code){
        for(BankStatusEnum e:values()){
            if(e.code==code){
                return e;
            }
        }
        return null;
    }
}
