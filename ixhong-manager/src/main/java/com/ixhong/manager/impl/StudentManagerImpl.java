package com.ixhong.manager.impl;

import com.ixhong.dao.StudentDao;
import com.ixhong.domain.pojo.StudentDO;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.StudentManager;

import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2015/4/22.
 */
public class StudentManagerImpl implements StudentManager{

    private StudentDao studentDao;

    public void setStudentDao(StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    @Override
    public void insert(StudentDO student) {
         this.studentDao.insert(student);
    }

    @Override
    public StudentDO validate(String name, String password) {
        return this.studentDao.validate(name,password);
    }

    @Override
    public StudentDO getById(int id) {
        return this.studentDao.getById(id);
    }

    @Override
    public void updatePassword(int id, String password) {
         this.studentDao.updatePassword(id,password);
    }

    @Override
    public void updatePasswordByPhone(String phone, String password) {
         this.studentDao.updatePasswordByPhone(phone,password);
    }

    @Override
    public StudentDO getByTelephone(String phone) {
        return this.studentDao.getByTelephone(phone);
    }

    @Override
    public StudentDO getByCardId(String cardId) {
        return this.studentDao.getByCardId(cardId);
    }

    @Override
    public QueryResult<StudentDO> query(Integer organizationId, BiddingQuery query) {
        return null;
    }

    @Override
    public void updateStatus(Integer studentId, Integer status) {
        this.studentDao.updateStatus(studentId, status);
    }

    @Override
    public void updateLoginTime(Integer id) {
        this.studentDao.updateLoginTime(id);
    }

    @Override
    public void updateContactInformation(Map contactMap) {
        this.studentDao.updateContactInformation(contactMap);
    }

    @Override
    public void updateCertified(Integer id, int certified) {
        this.studentDao.updateCertified(id, certified);
    }

    @Override
    public List<StudentDO> getByStudentIds(List<Integer> studentIds) {
        return this.studentDao.getByStudentIds(studentIds);
    }

    @Override
    public void updateScore(Integer studentId, Integer score) {
        this.studentDao.updateScore(studentId,score);
    }
}
