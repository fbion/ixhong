package com.ixhong.manager.impl;

import com.ixhong.dao.AccountDao;
import com.ixhong.domain.pojo.AccountDO;
import com.ixhong.manager.AccountManager;

/**
 * Created by shenhongxi on 15/6/10.
 */
public class AccountManagerImpl implements AccountManager {

    private AccountDao accountDao;
    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public AccountDO get() {
        return accountDao.get();
    }

    @Override
    public void update(AccountDO account) {
        this.accountDao.update(account);
    }
}
