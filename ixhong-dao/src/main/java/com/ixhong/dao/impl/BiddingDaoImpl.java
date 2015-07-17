package com.ixhong.dao.impl;

import com.ixhong.dao.BiddingDao;
import com.ixhong.domain.pojo.BiddingDO;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuichengrui on 2015/4/24.
 */
public class BiddingDaoImpl extends BaseDao implements BiddingDao{
    private Logger logger = Logger.getLogger(BiddingDao.class);


    @Override
    public void insert(BiddingDO bidding) {
        this.sqlSession.insert("BiddingMapper.insert",bidding);
    }

    @Override
    public BiddingDO getById(Integer id) {
        return this.sqlSession.selectOne("BiddingMapper.getById", id);
    }

    @Override
    public QueryResult<BiddingDO> query( BiddingQuery query) {
        QueryResult<BiddingDO> qr = new QueryResult<BiddingDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<BiddingDO> list =  this.sqlSession.selectList("BiddingMapper.query", params);
        qr.setResultList(list);
        return qr;
    }

    @Override
    public List<BiddingDO> list(List<Integer> statusList){
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("status",statusList);
        return this.sqlSession.selectList("BiddingMapper.list",statusList);
    }

    @Override
    public void update(BiddingDO bidding) {
        int record = this.sqlSession.update("BiddingMapper.update",bidding);
        if(record == 0){
            throw new RuntimeException("version is modified by other!");
        }
    }

    @Override
    public Integer count(Integer organizationId, List<Integer> status) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("organizationId", organizationId);
        params.put("status", status);
        return this.sqlSession.selectOne("BiddingMapper.countByStatus",params);
    }

    @Override
    public BiddingDO getCurrentByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("BiddingMapper.getCurrentByStudentId", studentId);
    }

    @Override
    public void updateForRepayment(BiddingDO bidding) {
        int record = this.sqlSession.update("BiddingMapper.updateForRepayment",bidding);
        if(record == 0) {
            throw new RuntimeException("version is modified by other!");
        }
    }

    @Override
    public List<BiddingDO> getInvalidBidding() {
        return this.sqlSession.selectList("BiddingMapper.getInvalidBidding");
    }

    /**
     * 总条数
     * @param params
     * @return
     */
    private int countAll(Map<String, Object> params) {
        return this.sqlSession.selectOne("BiddingMapper.countAll", params);
    }


}
