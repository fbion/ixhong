package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentAccountDO;

/**
 * Created by cuichengrui on 2015/5/13.
 */
public interface StudentAccountDao {

    public void insert(StudentAccountDO studentAccount);

    public void update(StudentAccountDO studentAccount);

    public StudentAccountDO getById(Integer id);

    public StudentAccountDO getByStudentId(Integer studentId);

    public StudentAccountDO getByStudentIdForUpdate(Integer studentId);

    public void updateBalance(StudentAccountDO studentAccount);

}
