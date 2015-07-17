package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.query.AccountFlowQuery;
import com.ixhong.domain.query.OrganizationWithdrawQuery;

/**
 * Created by chenguang on 2015/6/4 0004.
 */
public interface AccountService {

    /**
     * 机构提现
     * @param query
     * @return
     */
    public Result withdrawList(OrganizationWithdrawQuery query);

    /**
     * 学好贷账户流水
     * @param query
     * @return
     */
    public Result flowList(AccountFlowQuery query);
}
