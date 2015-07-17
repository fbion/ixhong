package com.ixhong.common.yeepay;

import java.io.Serializable;

/**
 * Created by Chengrui on 2015/5/21.
 */
public class YeepayRequestEntity implements Serializable {


    //业务类型
    private String p0Cmd;
    //商户编号
    private String p1MerId = PropertiesContainer.B2C_MERCHANT_ID;
    //商户订单号
    private String p2Order;
    //支付金额
    private String p3Amt;
    //交易币种
    private String p4Cur = "CNY";
    //商品名称
    private String p5Pid = "";
    //商品种类
    private String p6Pcat = "";
    //商品描述
    private String p7Pdesc = "";
    //支付成功后，商户接受的回调地址
    private String p8Url;
    //送货地址
    private String p9SAF = "0";
    //商户扩展信息
    private String paMP = "";
    //支付通道编码，说明：默认为“”，则到易宝支付网关.若不需显示易宝支付的页面，直接跳转到各银行，则根据支付通道编码列表设置此参数值
    private String pdFrpId;
    //应当机制，说明：固定值为1，需要应答
    private String prNeedResponse;
    //商户密钥
    private String keyValue = PropertiesContainer.B2C_MERCHANT_PRIVATE_KEY;

    public void setPdFrpId(String pdFrpId) {
        this.pdFrpId = pdFrpId;
    }

    public void setPrNeedResponse(String prNeedResponse) {
        this.prNeedResponse = prNeedResponse;
    }

    public void setP0Cmd(String p0Cmd) {
        this.p0Cmd = p0Cmd;
    }

    public void setP8Url(String p8Url) {
        this.p8Url = p8Url;
    }

    public void setP2Order(String p2Order) {
        this.p2Order = p2Order;
    }

    public void setP7Pdesc(String p7Pdesc) {
        this.p7Pdesc = p7Pdesc;
    }

    public void setP3Amt(String p3Amt) {
        this.p3Amt = p3Amt;
    }

    public String getP0Cmd() {
        return p0Cmd;
    }

    public String getP1MerId() {
        return p1MerId;
    }

    public String getP2Order() {
        return p2Order;
    }

    public String getP3Amt() {
        return p3Amt;
    }

    public String getP4Cur() {
        return p4Cur;
    }

    public String getP5Pid() {
        return p5Pid;
    }

    public String getP6Pcat() {
        return p6Pcat;
    }

    public String getP7Pdesc() {
        return p7Pdesc;
    }

    public String getP8Url() {
        return p8Url;
    }

    public String getP9SAF() {
        return p9SAF;
    }

    public String getPaMP() {
        return paMP;
    }

    public String getPdFrpId() {
        return pdFrpId;
    }

    public String getPrNeedResponse() {
        return prNeedResponse;
    }

    public String getKeyValue() {
        return keyValue;
    }
}
