package com.ixhong.dao.impl;

import com.ixhong.dao.AccountDao;
import com.ixhong.domain.pojo.AccountDO;

/**
 * Created by shenhongxi on 15/6/10.
 */
public class AccountDaoImpl extends BaseDao implements AccountDao {

    @Override
    public AccountDO get() {
        return this.sqlSession.selectOne("AccountMapper.get");
    }

    @Override
    public void update(AccountDO account) {
        int record = this.sqlSession.update("AccountMapper.update",account);
        if(record == 0) {
            throw new RuntimeException("version is modified by other!");
        }
    }

}
