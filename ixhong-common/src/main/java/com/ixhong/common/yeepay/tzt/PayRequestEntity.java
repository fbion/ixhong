package com.ixhong.common.yeepay.tzt;

import com.ixhong.common.yeepay.PropertiesContainer;

import java.io.Serializable;

/**
 * Created by shenhongxi on 15/5/28.
 * 支付请求包装类
 */
public class PayRequestEntity implements Serializable {

    private String merchantId = PropertiesContainer.TZT_MERCHANT_ID; //商户编号

    private String orderId; //商户生成的订单号，最长50位

    private int transactionTime; //交易时间，时间戳, [精确到秒]

    private int amount; //交易金额，以分为单位的整形

    private String productName; //商品名称，最长50位

    private int identityType = 2; //用户标识类型，为2时指 userId

    private String identityId; //userId, 最长50位  <为方便查询，用身份证号码>

    private String cardTop; //卡号前6位

    private String cardLast; //卡号后4位

    private String callbackUrl; //回调通知商户地址

    private String clientIp; //用户ip

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(int transactionTime) {
        this.transactionTime = transactionTime;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getCardTop() {
        return cardTop;
    }

    public void setCardTop(String cardTop) {
        this.cardTop = cardTop;
    }

    public String getCardLast() {
        return cardLast;
    }

    public void setCardLast(String cardLast) {
        this.cardLast = cardLast;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
