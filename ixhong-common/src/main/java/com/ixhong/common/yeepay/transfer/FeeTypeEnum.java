package com.ixhong.common.yeepay.transfer;

/**
 * Created by shenhongxi on 6/3/15.
 */
public enum FeeTypeEnum {

    SOURCE("SOURCE", "商户承担"),

    TARGET("TARGET", "用户承担");

    public String code;

    public String meaning;

    public String getCode() {
        return code;

    }

    public String getMeaning() {
        return meaning;
    }

    FeeTypeEnum(String code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }
    public static FeeTypeEnum value(String code){
        for(FeeTypeEnum e:values()){
            if(e.code==code){
                return e;
            }
        }
        return null;
    }
}
