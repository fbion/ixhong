package com.ixhong.manager;

import com.ixhong.domain.pojo.TeacherDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.TeacherQuery;

/**
 * Created by liuguanqing on 15/4/7.
 */
public interface TeacherManager {

    public void insert(TeacherDO teacher);

    public TeacherDO validate(String name, String password);

    public void delete(Integer id);

    public TeacherDO getById(int id);

    public void updatePassword(int id, String password, int status);

    public TeacherDO getByTelephone(String phone);

    public QueryResult<TeacherDO> query(Integer organizationId, TeacherQuery query);

}
