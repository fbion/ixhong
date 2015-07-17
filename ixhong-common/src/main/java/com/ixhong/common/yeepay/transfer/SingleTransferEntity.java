package com.ixhong.common.yeepay.transfer;

import com.ixhong.common.yeepay.PropertiesContainer;

import java.io.Serializable;

/**
 * Created by shenhongxi on 15/6/3.
 * 单笔打款（委托结算）
 */
public class SingleTransferEntity implements Serializable {

    private String cmd = TransferConstants.CMD_TRANSFER_SINGLE; //请求命令

    private String version = "1.1"; //接口版本，固定值

    private String groupId = PropertiesContainer.TRANSFER_MERCHANT_ID; //总公司商户编号

    private String merchantId = PropertiesContainer.TRANSFER_MERCHANT_ID; //实际发起交易的商户编号

    private String batchNo; //打款批次号

    private String bankName; //收款银行名称

    private String orderId; //订单号，最长50位

    //工行、农行、建行、深发、交通银行外，其他银行支行必填
    private String branchBankName; //支行名称

    private String amount; //打款金额，单位元，保留两位小数

    private String accountName; //收款账户名称

    private String accountNumber; //收款账户号

    private String accountType = "pu"; //对公

    private String branchBankProvince; //支行所在省

    private String branchBankCity; //支行所在市

    private String feeType = FeeTypeEnum.TARGET.code;

    private Integer urgency = 1; //是否加急

    private String hmac; //签名信息

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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBranchBankName() {
        return branchBankName;
    }

    public void setBranchBankName(String branchBankName) {
        this.branchBankName = branchBankName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBranchBankProvince() {
        return branchBankProvince;
    }

    public void setBranchBankProvince(String branchBankProvince) {
        this.branchBankProvince = branchBankProvince;
    }

    public String getBranchBankCity() {
        return branchBankCity;
    }

    public void setBranchBankCity(String branchBankCity) {
        this.branchBankCity = branchBankCity;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public Integer getUrgency() {
        return urgency;
    }

    public void setUrgency(Integer urgency) {
        this.urgency = urgency;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;

    }

}
