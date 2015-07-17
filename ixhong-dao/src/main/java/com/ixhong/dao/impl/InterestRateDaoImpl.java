package com.ixhong.dao.impl;

import com.ixhong.dao.InterestRateDao;
import com.ixhong.domain.pojo.InterestRateDO;

import java.util.List;

/**
 * Created by chenguang on 2015/4/22 0022.
 */
public class InterestRateDaoImpl extends BaseDao implements InterestRateDao {

    @Override
    public List<InterestRateDO> list() {
        return this.sqlSession.selectList("InterestRateMapper.list");
    }

    @Override
    public void insert(InterestRateDO interestRate) {
        this.sqlSession.insert("InterestRateMapper.insert", interestRate);
    }

    @Override
    public InterestRateDO getById(Integer id) {
        return this.sqlSession.selectOne("InterestRateMapper.getById", id);
    }

    @Override
    public InterestRateDO getByMonthLimit(Integer monthLimit) {
        return this.sqlSession.selectOne("InterestRateMapper.getByMonthLimit", monthLimit);
    }

    @Override
    public void update(Integer id) {
        this.sqlSession.update("InterestRateMapper.update", id);
    }
}
