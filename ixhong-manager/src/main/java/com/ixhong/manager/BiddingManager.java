package com.ixhong.manager;

import com.ixhong.domain.pojo.BiddingDO;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by duanxiangchao on 2015/4/22.
 */
public interface BiddingManager {

    public void insert(BiddingDO bidding);

    public BiddingDO getById(Integer id);

    public QueryResult<BiddingDO> query(BiddingQuery query);

    public Integer count(Integer organizationId, List<Integer> status);

    public BiddingDO getCurrentByStudentId(Integer studentId);

    public Collection<BiddingDO> list(List<Integer> statusList);

    public void update(BiddingDO bidding);

    public void updateForRepayment(BiddingDO bidding);

    public List<BiddingDO> getInvalidBidding();
}
