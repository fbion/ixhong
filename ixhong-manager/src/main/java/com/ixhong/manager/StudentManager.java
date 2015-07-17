package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentDO;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2015/4/22.
 */
public interface StudentManager {

    public void insert(StudentDO teacher);

    public StudentDO validate(String phone, String password);

    public StudentDO getById(int id);

    public void updatePassword(int id, String password);

    public void updatePasswordByPhone(String phone, String password);

    public StudentDO getByTelephone(String phone);

    public StudentDO getByCardId(String cardId);

    public void updateScore(Integer id, Integer score);

    public QueryResult<StudentDO> query(Integer organizationId, BiddingQuery query);

    public void updateStatus(Integer studentId, Integer status);

    public void updateLoginTime(Integer id);

    public void updateContactInformation(Map contactMap);

    public void updateCertified(Integer id, int certified);

    /**
     * 根据学生ID批量查询学生信息  默认最大10条
     * @param studentIds
     * @return
     */
    public List<StudentDO> getByStudentIds(List<Integer> studentIds);

}
