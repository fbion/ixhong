package com.ixhong.dao;

import com.ixhong.domain.pojo.AdminDO;

/**
 * Created by shenhongxi on 15/4/14.
 */
public interface AdminDao {

    public AdminDO validate(String name, String password);

}
