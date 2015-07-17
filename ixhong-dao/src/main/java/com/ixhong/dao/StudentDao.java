package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentDO;

import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2015/4/22.
 */
public interface StudentDao {

    public void insert(StudentDO teacher);

    public StudentDO validate(String phone, String password);

    public StudentDO getByTelephone(String telephone);

    public StudentDO getByCardId(String cardId);

    public StudentDO getById(int id);

    public void updatePassword(int id, String password);

    public void updatePasswordByPhone(String phone, String password);

    public void updateStatus(Integer id, Integer status);

    public void updateScore(Integer id, Integer score);

    public void updateLoginTime(Integer id);

    public void updateContactInformation(Map contactMap);

    public void updateCertified(Integer id, int certified);

    public List<StudentDO> getByStudentIds(List<Integer> studentIds);


}
