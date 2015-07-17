package com.ixhong.dao;

import com.ixhong.domain.pojo.LenderBillStageDO;
import com.ixhong.domain.query.LenderBillStageQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by jenny on 6/2/15.
 */
public interface LenderBillStageDao {

    /**
     * 分页条件查询
     * @param query
     * @return
     */
    public QueryResult<LenderBillStageDO> query(LenderBillStageQuery query);

    public int insertBatch(List<LenderBillStageDO> billStageList);

    public void update(LenderBillStageDO lenderBill);

    public List<LenderBillStageDO> get(Integer biddingId, Integer stage);

    public LenderBillStageDO get(Integer biddingId, Integer lenderId, Integer stage);

}
