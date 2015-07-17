package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentBillStageDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentBillStageQuery;

import java.util.Date;
import java.util.List;

/**
 * Created by duanxiangchao on 2015/5/13.
 */
public interface StudentBillStageDao {

    public int insertBatch(List<StudentBillStageDO> billStageList);

    public QueryResult<StudentBillStageDO> query(StudentBillStageQuery query);

    public StudentBillStageDO getById(Integer id, Integer studentId);

    public void update(StudentBillStageDO bill);

    public void updateStatus(Integer biddingId, Integer stage, Integer status);

    public List<StudentBillStageDO> getByStatus(int status);

    public List<StudentBillStageDO> getByBiddingId(Integer biddingId);

    public List<StudentBillStageDO> getOverdueBillStage(int status, String quartDate, Integer overdue, String date);

    public List<StudentBillStageDO> getForSMS(Date deadline, int isOverdue, int startRow, int pageSize);
}
