package com.ixhong.common.yeepay.tzt;

import com.ixhong.common.yeepay.PropertiesContainer;

import java.io.Serializable;

/**
 * Created by shenhongxi on 15/5/28.
 * 提现请求包装类
 */
public class WithdrawRequestEntity implements Serializable {

    private String merchantId = PropertiesContainer.TZT_MERCHANT_ID; //商户编号

    private String requestId; //商户生成的唯一提现订单号

    private int identityType = 2; //用户标识类型，为2时指 userId

    private String identityId; //userId, 最长50位  <为方便查询，用身份证号码>

    private String cardTop; //卡号前6位

    private String cardLast; //卡号后4位

    private int amount; //交易金额，以分为单位的整形

    private String drawType = DrawTypeEnum.URGENT.code; //提现类型

    private String clientIp; //用户ip

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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDrawType() {
        return drawType;
    }

    public void setDrawType(String drawType) {
        this.drawType = drawType;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
