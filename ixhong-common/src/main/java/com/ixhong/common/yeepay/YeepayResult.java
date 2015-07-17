package com.ixhong.common.yeepay;

/**
 * Created by cuichengrui on 2015/5/11.
 */
public class YeepayResult {
    //退款
    private String r0Cmd; 			// 请求命令
    private String r1Code; 		// 请求结果
    private String r2TrxId; 		// 交易流水号
    private String r3Amt; 			// 交易金额
    private String r4Cur; 			// 交易币种
    private String hmac; 			// 签名校验

    //充值
    private String r5Pid;	 		//商品名称
    private String r6Order;		//商户订单号
    private String r8MP;			//商户扩展信息
    private String rbPayStatus;	//支付状态
    private String rcRefundCount;	//已退款次数
    private String rdRefundAmt;	//已退款金额

    public String getR0Cmd() {
        return r0Cmd;
    }

    public void setR0Cmd(String r0Cmd) {
        this.r0Cmd = r0Cmd;
    }

    public String getR1Code() {
        return r1Code;
    }

    public void setR1Code(String r1Code) {
        this.r1Code = r1Code;
    }

    public String getR2TrxId() {
        return r2TrxId;
    }

    public void setR2TrxId(String r2TrxId) {
        this.r2TrxId = r2TrxId;
    }

    public String getR3Amt() {
        return r3Amt;
    }

    public void setR3Amt(String r3Amt) {
        this.r3Amt = r3Amt;
    }

    public String getR4Cur() {
        return r4Cur;
    }

    public void setR4Cur(String r4Cur) {
        this.r4Cur = r4Cur;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getR5Pid() {
        return r5Pid;
    }

    public void setR5Pid(String r5Pid) {
        this.r5Pid = r5Pid;
    }

    public String getR6Order() {
        return r6Order;
    }

    public void setR6Order(String r6Order) {
        this.r6Order = r6Order;
    }

    public String getR8MP() {
        return r8MP;
    }

    public void setR8MP(String r8MP) {
        this.r8MP = r8MP;
    }

    public String getRbPayStatus() {
        return rbPayStatus;
    }

    public void setRbPayStatus(String rbPayStatus) {
        this.rbPayStatus = rbPayStatus;
    }

    public String getRcRefundCount() {
        return rcRefundCount;
    }

    public void setRcRefundCount(String rcRefundCount) {
        this.rcRefundCount = rcRefundCount;
    }

    public String getRdRefundAmt() {
        return rdRefundAmt;
    }

    public void setRdRefundAmt(String rdRefundAmt) {
        this.rdRefundAmt = rdRefundAmt;
    }
}
