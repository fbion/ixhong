package com.ixhong.common.yeepay.transfer;

import com.ixhong.common.yeepay.PropertiesContainer;

/**
 * Created by shenhongxi on 15/6/4.
 */
public class TransferQueryEntity {

    private String cmd = TransferConstants.CMD_TRANSFER_QUERY;

    private String version = "1.0";

    private String groupId = PropertiesContainer.TRANSFER_MERCHANT_ID;

    private String merchantId = PropertiesContainer.TRANSFER_MERCHANT_ID;

    private String queryMode = "1"; //查询本公司自己发的交易甲方填写1即可

    private String orderId; //商户订单号

    private String batchNo; //打款批次号

    private Integer pageNo; //查询页码

    private String hmac;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getQueryMode() {
        return queryMode;
    }

    public void setQueryMode(String queryMode) {
        this.queryMode = queryMode;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
}
