package com.ixhong.dao;

import com.ixhong.domain.pojo.BiddingDO;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by cuichengrui on 2015/4/24.
 */
public interface BiddingDao {

    public void insert(BiddingDO bidding);

    public BiddingDO getById(Integer id);

    public QueryResult<BiddingDO> query(BiddingQuery query);

    public Integer count(Integer organizationId, List<Integer> status);

    public BiddingDO getCurrentByStudentId(Integer studentId);

    public List<BiddingDO> list(List<Integer> statusList);

    public void update(BiddingDO bidding);

    public void updateForRepayment(BiddingDO bidding);

    public List<BiddingDO> getInvalidBidding();
}
