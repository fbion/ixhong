package com.ixhong.manager.impl;

import com.ixhong.dao.StudentBillStageDao;
import com.ixhong.domain.pojo.StudentBillStageDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentBillStageQuery;
import com.ixhong.manager.StudentBillStageManager;

import java.util.Date;
import java.util.List;

/**
 * Created by duanxiangchao on 2015/5/13.
 */
public class StudentBillStageManagerImpl implements StudentBillStageManager{

    private StudentBillStageDao studentBillStageDao;

    public StudentBillStageDao getStudentBillStageDao() {
        return studentBillStageDao;
    }

    public void setStudentBillStageDao(StudentBillStageDao studentBillStageDao) {
        this.studentBillStageDao = studentBillStageDao;
    }

    @Override
    public int insertBatch(List<StudentBillStageDO> billStageList) {
        return this.studentBillStageDao.insertBatch(billStageList);
    }

    @Override
    public QueryResult<StudentBillStageDO> query(StudentBillStageQuery query) {
        return this.studentBillStageDao.query(query);
    }

    @Override
    public StudentBillStageDO getById(Integer id, Integer studentId) {
        return this.studentBillStageDao.getById(id, studentId);
    }

    @Override
    public void update(StudentBillStageDO bill) {
        this.studentBillStageDao.update(bill);
    }

    @Override
    public void updateStatus(Integer biddingId, Integer stage, Integer status) {
        this.studentBillStageDao.updateStatus(biddingId, stage, status);
    }

    public List<StudentBillStageDO> getByStatus(int status) {
        return this.studentBillStageDao.getByStatus(status);
    }

    @Override
    public List<StudentBillStageDO> getByBiddingId(Integer biddingId) {
        return this.studentBillStageDao.getByBiddingId(biddingId);
    }

    @Override
    public List<StudentBillStageDO> getOverdueBillStage(int status,String  quartDate,Integer overdue,String date) {
        return this.studentBillStageDao.getOverdueBillStage(status,quartDate,overdue,date);
    }

    @Override
    public List<StudentBillStageDO> getForSMS(Date deadline,int isOverdue,int startRow,int pageSize) {
        return this.studentBillStageDao.getForSMS(deadline,isOverdue,startRow,pageSize);
    }
}
