package com.ixhong.manager.impl;

import com.ixhong.dao.AdminDao;
import com.ixhong.domain.pojo.AdminDO;
import com.ixhong.manager.AdminManager;

/**
 * Created by shenhongxi on 2015/4/14.
 */
public class AdminManagerImpl implements AdminManager {

    private AdminDao adminDao;

    @Override
    public AdminDO validate(String name, String password) {
        return this.adminDao.validate(name, password);
    }

    public void setAdminDao(AdminDao adminDao) {
        this.adminDao = adminDao;
    }
}
