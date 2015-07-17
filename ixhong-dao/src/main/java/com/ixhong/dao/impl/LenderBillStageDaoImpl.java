package com.ixhong.dao.impl;

import com.ixhong.dao.LenderBillStageDao;
import com.ixhong.domain.pojo.LenderBillStageDO;
import com.ixhong.domain.query.LenderBillStageQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jenny on 6/2/15.
 */
public class LenderBillStageDaoImpl extends BaseDao implements LenderBillStageDao {


    @Override
    public QueryResult<LenderBillStageDO> query(LenderBillStageQuery query) {
        QueryResult<LenderBillStageDO> qr = new QueryResult<LenderBillStageDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<LenderBillStageDO> flows =  this.sqlSession.selectList("LenderBillStageMapper.query", params);
        qr.setResultList(flows);
        return qr;
    }

    @Override
    public int insertBatch(List<LenderBillStageDO> billStageList) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("billStageList", billStageList);
        return this.sqlSession.insert("LenderBillStageMapper.insertBatch",map);
    }

    @Override
    public void update(LenderBillStageDO lenderBill) {
        int record = this.sqlSession.update("LenderBillStageMapper.update", lenderBill);
        if(record == 0) {
            throw  new RuntimeException("version is modified by other!");
        }
    }

    @Override
    public List<LenderBillStageDO> get(Integer biddingId, Integer stage) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("biddingId", biddingId);
        params.put("stage", stage);
        return this.sqlSession.selectList("LenderBillStageMapper.getByBiddingIdAndStage", params);
    }

    @Override
    public LenderBillStageDO get(Integer biddingId, Integer lenderId, Integer stage) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("biddingId", biddingId);
        params.put("lenderId", lenderId);
        params.put("stage", stage);
        return this.sqlSession.selectOne("LenderBillStageMapper.getByStage", params);
    }

    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("LenderBillStageMapper.countAll",params);
    }
}
