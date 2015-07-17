package com.ixhong.common.yeepay.tzt;

import com.ixhong.common.yeepay.PropertiesContainer;

import java.io.Serializable;

/**
 * Created by shenhongxi on 15/7/7.
 */
public class RefundEntity implements Serializable {

    private String merchantId = PropertiesContainer.TZT_MERCHANT_ID; //商户编号

    private String orderId; //商户生成的订单号，最长50位

    private String yeepayOrderId; //易宝订单号

    private int amount; //交易金额，以分为单位的整形

    private int currency = 156; //交易币种，人名币

    private String cause; //退款说明

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

    public String getYeepayOrderId() {
        return yeepayOrderId;
    }

    public void setYeepayOrderId(String yeepayOrderId) {
        this.yeepayOrderId = yeepayOrderId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}
