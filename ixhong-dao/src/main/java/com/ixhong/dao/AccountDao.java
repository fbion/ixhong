package com.ixhong.dao;

import com.ixhong.domain.pojo.AccountDO;

/**
 * Created by shenhongxi on 15/6/10.
 */
public interface AccountDao {

    public AccountDO get();

    public void update(AccountDO account);
}
