package com.ixhong.manager.impl;

import com.ixhong.dao.BiddingDao;
import com.ixhong.domain.pojo.BiddingDO;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.BiddingManager;

import java.util.Collection;
import java.util.List;

/**
 * Created by cuichengrui on 2015/4/24.
 */
public class BiddingManagerImpl implements BiddingManager {

    private BiddingDao biddingDao;

    public void setBiddingDao(BiddingDao biddingDao) {
        this.biddingDao = biddingDao;
    }

    @Override
    public void insert(BiddingDO bidding) {
        this.biddingDao.insert(bidding);
    }

    @Override
    public BiddingDO getById(Integer id) {
        return this.biddingDao.getById(id);
    }

    @Override
    public QueryResult<BiddingDO> query( BiddingQuery query) {
        return biddingDao.query(query);
    }


    @Override
    public Integer count(Integer organizationId, List<Integer> status) {
        return this.biddingDao.count(organizationId, status);
    }

    @Override
    public BiddingDO getCurrentByStudentId(Integer studentId) {
        return this.biddingDao.getCurrentByStudentId(studentId);
    }

    @Override
    public Collection<BiddingDO> list(List<Integer> statusList) {
        Collection<BiddingDO> biddingList = biddingDao.list(statusList);
        return biddingList;
    }

    @Override
    public void update(BiddingDO bidding) {
        this.biddingDao.update(bidding);
    }

    @Override
    public void updateForRepayment(BiddingDO bidding) {
        this.biddingDao.updateForRepayment(bidding);
    }

    @Override
    public List<BiddingDO> getInvalidBidding() {
        return this.biddingDao.getInvalidBidding();
    }

}
