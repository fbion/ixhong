package com.ixhong.dao.impl;

import com.ixhong.dao.AccountFlowDao;
import com.ixhong.domain.pojo.AccountFlowDO;
import com.ixhong.domain.query.AccountFlowQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.List;

/**
 * Created by chenguang on 2015/6/3 0003.
 */
public class AccountFlowDaoImpl extends BaseDao implements AccountFlowDao {

    @Override
    public QueryResult<AccountFlowDO> query(AccountFlowQuery query) {
        QueryResult<AccountFlowDO> qr = new QueryResult<AccountFlowDO>();
        qr.setQuery(query);
        int amount = this.countAll(query);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }
        List<AccountFlowDO> adminAccountFlows = this.sqlSession.selectList("AccountFlowMapper.query", query);
        qr.setResultList(adminAccountFlows);
        return qr;
    }

    @Override
    public void insert(AccountFlowDO accountFlowDO) {
        this.sqlSession.insert("AccountFlowMapper.insert", accountFlowDO);
    }

    public int countAll(AccountFlowQuery query) {
        return this.sqlSession.selectOne("AccountFlowMapper.countAll", query);
    }
}
