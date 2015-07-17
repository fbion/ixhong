package com.ixhong.manager;

import com.ixhong.domain.pojo.AdminDO;

/**
 * Created by shenhongxi on 15/4/14.
 */
public interface AdminManager {

    public AdminDO validate(String name, String password);

}
