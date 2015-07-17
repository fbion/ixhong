package com.ixhong.common.yeepay.tzt;

/**
 * Created by shenhongxi on 5/28/15.
 */
public enum DrawTypeEnum {

    URGENT("NATRALDAY_URGENT", "自然日T+0"),

    NORMAL("NATRALDAY_NORMAL", "自然日T+1");

    public String code;

    public String meaning;

    public String getCode() {
        return code;

    }

    public String getMeaning() {
        return meaning;
    }

    DrawTypeEnum(String code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }
    public static DrawTypeEnum value(String code){
        for(DrawTypeEnum e:values()){
            if(e.code==code){
                return e;
            }
        }
        return null;
    }
}
