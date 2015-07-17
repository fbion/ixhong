package com.ixhong.manager;

import com.ixhong.domain.pojo.InterestRateDO;

import java.util.List;

/**
 * Created by Chengrui on 2015/4/22.
 */
public interface InterestRateManager {
    public List<InterestRateDO> list();

    public void insert(InterestRateDO interestRate);

    public InterestRateDO getById(Integer id);

    public void update(Integer id);

    public InterestRateDO getByMonthLimit(Integer monthLimit);

}
