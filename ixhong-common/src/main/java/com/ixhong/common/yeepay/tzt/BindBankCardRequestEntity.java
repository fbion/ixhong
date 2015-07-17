package com.ixhong.common.yeepay.tzt;

import com.ixhong.common.yeepay.PropertiesContainer;

import java.io.Serializable;

/**
 * Created by shenhongxi on 15/5/27.
 * 绑卡请求包装类
 */
public class BindBankCardRequestEntity implements Serializable {

    private String merchantId = PropertiesContainer.TZT_MERCHANT_ID; //商户编号

    private String requestId; //绑卡请求号，最长50位

    private int identityType = 2; //用户标识类型,为2时指userId，如 lenderId

    private String identityId; //userId，最长50位  <为方便查询，用身份证号码>

    private String idCardType = "01"; //身份证

    private String cardId; //身份证号

    private String realName; //持卡人姓名

    private String bankCardId; //银行卡号

    private String phone; //银行预留手机号

    private String clientIp; //用户支付时使用的网络中断IP

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getIdentityType() {
        return identityType;
    }

    public void setIdentityType(int identityType) {
        this.identityType = identityType;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getBankCardId() {
        return bankCardId;
    }

    public void setBankCardId(String bankCardId) {
        this.bankCardId = bankCardId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
