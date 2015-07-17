package com.ixhong.manager.impl;

import com.ixhong.dao.LenderAccountDao;
import com.ixhong.domain.pojo.LenderAccountDO;
import com.ixhong.manager.LenderAccountManager;

/**
 * Created by chenguang on 2015/5/14 0014.
 */
public class LenderAccountManagerImpl implements LenderAccountManager {

    private LenderAccountDao lenderAccountDao;

    public void setLenderAccountDao(LenderAccountDao lenderAccountDao) {
        this.lenderAccountDao = lenderAccountDao;
    }

    @Override
    public LenderAccountDO getByLenderId(Integer lenderId) {
        return lenderAccountDao.getByLenderId(lenderId);
    }

    @Override
    public void insert(Integer lenderId) {
        lenderAccountDao.insert(lenderId);
    }

    @Override
    public void update(LenderAccountDO lenderAccount) {
        lenderAccountDao.update(lenderAccount);
    }

    @Override
    public void updateBalance(LenderAccountDO lenderAccount) {
        lenderAccountDao.updateBalance(lenderAccount);
    }

    @Override
    public void updateDealPassword(String password, Integer lenderId) {
        lenderAccountDao.updateDealPassword(password, lenderId);
    }

    @Override
    public LenderAccountDO validateDealPassword(Integer lenderId, String dealPassword) {
        return lenderAccountDao.validateDealPassword(lenderId, dealPassword);
    }

}
