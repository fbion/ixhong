package com.ixhong.manager.impl;

import com.ixhong.dao.LenderBillStageDao;
import com.ixhong.domain.pojo.LenderBillStageDO;
import com.ixhong.domain.query.LenderBillStageQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.LenderBillStageManager;

import java.util.List;

/**
 * Created by jenny on 6/2/15.
 */
public class LenderBillStageManagerImpl implements LenderBillStageManager {
    private LenderBillStageDao lenderBillStageDao;

    public void setLenderBillStageDao(LenderBillStageDao lenderBillStageDao){
        this.lenderBillStageDao = lenderBillStageDao;
    }


    @Override
    public QueryResult<LenderBillStageDO> query(LenderBillStageQuery query) {
        return this.lenderBillStageDao.query(query);
    }

    @Override
    public int insertBatch(List<LenderBillStageDO> billStageList) {
        return this.lenderBillStageDao.insertBatch(billStageList);
    }

    @Override
    public List<LenderBillStageDO> get(Integer biddingId, Integer stage) {
        return this.lenderBillStageDao.get(biddingId,stage);
    }

    @Override
    public LenderBillStageDO get(Integer biddingId, Integer lenderId, Integer stage) {
        return this.lenderBillStageDao.get(biddingId, lenderId, stage);
    }

    @Override
    public void update(LenderBillStageDO lenderBill) {
        this.lenderBillStageDao.update(lenderBill);
    }
}
