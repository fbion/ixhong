package com.ixhong.dao.impl;

import com.ixhong.dao.StudentBillStageDao;
import com.ixhong.domain.pojo.StudentBillStageDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentBillStageQuery;

import java.util.*;

/**
 * Created by duanxiangchao on 2015/5/13.
 */
public class StudentBillStageDaoImpl extends BaseDao implements StudentBillStageDao {
    @Override
    public int insertBatch(List<StudentBillStageDO> billStageList) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("billStageList", billStageList);
        return this.sqlSession.insert("StudentBillStageMapper.insertBatch",map);
    }

    @Override
    public QueryResult<StudentBillStageDO> query(StudentBillStageQuery query) {
        QueryResult<StudentBillStageDO> qr = new QueryResult<StudentBillStageDO>();
        qr.setQuery(query);
        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }
        List<StudentBillStageDO> billStageList =  this.sqlSession.selectList("StudentBillStageMapper.query", params);
        qr.setResultList(billStageList);
        return qr;
    }

    /**
     * 总条数
     * @param params
     * @return
     */
    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("StudentBillStageMapper.countAll", params);
    }

    @Override
    public StudentBillStageDO getById(Integer id, Integer studentId) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("id",id);
        params.put("studentId",studentId);
        return this.sqlSession.selectOne("StudentBillStageMapper.getById", params);
    }

    @Override
    public void update(StudentBillStageDO bill) {
        int record = this.sqlSession.update("StudentBillStageMapper.update", bill);
        if(record == 0) {
            throw new RuntimeException("version is modified by other!");
        }
    }

    @Override
    public void updateStatus(Integer biddingId, Integer stage, Integer status) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("biddingId",biddingId);
        params.put("stage",stage);
        params.put("status",status);
        this.sqlSession.update("StudentBillStageMapper.updateStatus", params);
    }

    @Override
    public List<StudentBillStageDO> getByStatus(int status) {
        return this.sqlSession.selectList("StudentBillStageMapper.getByStatus",status);
    }

    @Override
    public List<StudentBillStageDO> getByBiddingId(Integer biddingId) {
        return this.sqlSession.selectList("StudentBillStageMapper.getByBiddingId", biddingId);
    }

    @Override
    public List<StudentBillStageDO> getOverdueBillStage(int status,String quartzDate,Integer overdue,String date) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("status",status);
        params.put("quartzDate",quartzDate);
        params.put("overdue",overdue);
        params.put("date",date);
        return this.sqlSession.selectList("StudentBillStageMapper.getOverdueBillStage",params);
    }

    @Override
    public List<StudentBillStageDO> getForSMS(Date deadline,int isOverdue,int startRow,int pageSize) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("deadline",deadline);
        params.put("startRow",startRow);
        params.put("pageSize",pageSize);
        params.put("isOverdue",isOverdue);
        return this.sqlSession.selectList("StudentBillStageMapper.getForSMS",params);
    }
}
