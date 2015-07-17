package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.AdminService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.pojo.AdminDO;
import com.ixhong.domain.pojo.UserDO;
import com.ixhong.manager.AdminManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shenhongxi on 15/4/13.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private Logger logger = Logger.getLogger(AdminService.class);

    @Autowired
    private AdminManager adminManager;

    @Override
    public Result login(String name, String password) {
        Result result = new Result();
        try {
            AdminDO admin = this.adminManager.validate(name, password);
            if (admin == null) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("用户名或密码错误");
                return result;
            }
            UserDO user = new UserDO();
            user.setId(admin.getId());
            user.setAdminId(admin.getId());
            user.setName(admin.getName());
            user.setType(UserTypeEnum.ADMIN.code);
            result.addModel("user", user);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("login error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

}
