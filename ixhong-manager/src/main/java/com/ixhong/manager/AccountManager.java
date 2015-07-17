package com.ixhong.manager;

import com.ixhong.domain.pojo.AccountDO;

/**
 * Created by chenguang on 2015/6/10 0010.
 */
public interface AccountManager {

    public AccountDO get();

    public void update(AccountDO account);
}