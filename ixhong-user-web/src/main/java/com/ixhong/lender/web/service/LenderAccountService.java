package com.ixhong.lender.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.query.BiddingItemQuery;
import com.ixhong.domain.query.LenderAccountFlowQuery;

/**
 * Created by jenny on 5/15/15.
 */
public interface LenderAccountService {

    /**
     * 投资人账单明细查询
     * @param query
     * @return
     */
    public Result listFlow(LenderAccountFlowQuery query);

    /**
     * 投资记录查询列表
     * @param query
     * @return
     */
    public Result listItem(BiddingItemQuery query);

    /**
     * 添加交易密码
     * @param password
     * @return
     */
    public Result addDealPassword(String password);

    /**
     * 更新交易密码
     * @param oldPassword
     * @param password
     * @return
     */
    public Result updateDealPassword(String oldPassword, String password);

    public Result getByLenderId(Integer id);

    public Result getFlowStatus(String orderId);
}
