package com.ixhong.manager.impl;

import com.ixhong.dao.InterestRateDao;
import com.ixhong.domain.pojo.InterestRateDO;
import com.ixhong.manager.InterestRateManager;

import java.util.List;

/**
 * Created by chenguang on 2015/4/22 0022.
 */
public class InterestRateManagerImpl implements InterestRateManager {

    private InterestRateDao interestRateDao;

    public void setInterestRateDao(InterestRateDao interestRateDao) {
        this.interestRateDao = interestRateDao;
    }

    @Override
    public List<InterestRateDO> list() {
        return interestRateDao.list();
    }

    @Override
    public void insert(InterestRateDO interestRate) {
        interestRateDao.insert(interestRate);
    }

    @Override
    public InterestRateDO getById(Integer id) {
        return interestRateDao.getById(id);
    }

    @Override
    public void update(Integer id) {
        interestRateDao.update(id);
    }

    @Override
    public InterestRateDO getByMonthLimit(Integer monthLimit) {
        return interestRateDao.getByMonthLimit(monthLimit);
    }
}
