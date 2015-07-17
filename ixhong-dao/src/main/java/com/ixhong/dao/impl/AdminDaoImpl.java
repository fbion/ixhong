package com.ixhong.dao.impl;

import com.ixhong.dao.AdminDao;
import com.ixhong.domain.pojo.AdminDO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shenhongxi on 2015/4/14.
 */
public class AdminDaoImpl extends BaseDao implements AdminDao {
    @Override
    public AdminDO validate(String name, String password) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("name",name);
        params.put("password", password);
        return this.sqlSession.selectOne("AdminMapper.validate",params);
    }
}
