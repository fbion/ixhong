package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentAccountDO;

/**
 * Created by Chengrui on 2015/5/13.
 */
public interface StudentAccountManager {

    public void insert(StudentAccountDO studentAccount);

    public void update(StudentAccountDO studentAccount);

    public StudentAccountDO getById(Integer id);

    public StudentAccountDO getByStudentId(Integer studentId);

    public void updateBalanceFrozen(Integer studentId, String balanceFrozen);

    public StudentAccountDO getByStudentIdForUpdate(Integer studentId);

    /**
     * 更新学生账户余额
     * @return
     */
    public void updateBalance(StudentAccountDO studentAccount);
}
