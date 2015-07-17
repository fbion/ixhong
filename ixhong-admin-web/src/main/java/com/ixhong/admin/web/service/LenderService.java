package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.LenderQuery;

/**
 * Created by chenguang on 2015/5/25 0025.
 */
public interface LenderService {

    /**
     * 投资人用户列表
     * @param query
     * @return
     */
    public Result list(LenderQuery query);

    /**
     * 投资记录列表
     * @param phone
     * @param realName
     * @param page
     * @return
     */
    public Result biddingItemList(String phone, String realName, int page);


    /**
     * 管理员根据条件查询投资人流水
     * @param lenderAccountFlowQuery
     * @return
     */
    public Result lenderAccountFlow(LenderAccountFlowQuery lenderAccountFlowQuery);

    /**
     * 银行卡解绑
     * @param cardId
     * @param cardTop
     * @param cardLast
     * @return
     */
    public Result unbindBankCard(String cardId, String cardTop, String cardLast);

    /**
     * 退款
     * @param yeepayOrderId
     * @param amount
     * @param cause
     * @return
     */
    public Result refund(String yeepayOrderId, Double amount, String cause);

    /**
     * 退款查询
     * @param yeepayOrderId
     * @return
     */
    public Result queryRefund(String yeepayOrderId);

    /**
     * 提现查询
     * @param orderId
     * @return
     */
    public Result queryWithdraw(String orderId);

    /**
     * 充值查询
     * @param orderId
     * @return
     */
    public Result queryRecharge(String orderId);
}
