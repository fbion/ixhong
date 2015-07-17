package com.ixhong.manager.impl;

import com.ixhong.dao.AccountFlowDao;
import com.ixhong.domain.pojo.AccountFlowDO;
import com.ixhong.domain.query.AccountFlowQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.AccountFlowManager;

/**
 * Created by chenguang on 2015/6/3 0003.
 */
public class AccountFlowManagerImpl implements AccountFlowManager {

    private AccountFlowDao accountFlowDao;

    public void setAccountFlowDao(AccountFlowDao accountFlowDao) {
        this.accountFlowDao = accountFlowDao;
    }

    @Override
    public QueryResult<AccountFlowDO> query(AccountFlowQuery query) {
        return accountFlowDao.query(query);
    }

    @Override
    public void insert(AccountFlowDO adminAccountFlow) {
        this.accountFlowDao.insert(adminAccountFlow);
    }
}
