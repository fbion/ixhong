package com.ixhong.dao;

import com.ixhong.domain.pojo.AccountFlowDO;
import com.ixhong.domain.query.AccountFlowQuery;
import com.ixhong.domain.query.QueryResult;

/**
 * Created by chenguang on 2015/6/3 0003.
 */
public interface AccountFlowDao {

    public QueryResult<AccountFlowDO> query(AccountFlowQuery query);

    public void insert(AccountFlowDO accountFlowDO);
}
