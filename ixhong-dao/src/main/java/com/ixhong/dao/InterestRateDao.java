package com.ixhong.dao;

import com.ixhong.domain.pojo.InterestRateDO;

import java.util.List;

/**
 * Created by chenguang on 15/4/22.
 */
public interface InterestRateDao {

    public List<InterestRateDO> list();

    public void insert(InterestRateDO interestRate);

    public InterestRateDO getById(Integer id);

    public void update(Integer id);

    public InterestRateDO getByMonthLimit(Integer monthLimit);

}
