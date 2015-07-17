package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.InterestRateService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.InterestRateDO;
import com.ixhong.domain.pojo.UserDO;
import com.ixhong.manager.InterestRateManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenguang on 2015/4/22 0022.
 */
@Service
public class InterestRateServiceImpl implements InterestRateService {

    private Logger logger = Logger.getLogger(InterestRateService.class);

    @Autowired
    private InterestRateManager interestRateManager;

    @Override
    public Result list() {
        Result result = new Result();
        try {
            UserDO user = LoginContextHolder.getLoginUser();
            List<InterestRateDO> interestRates = interestRateManager.list();
            result.addModel("interestRates", interestRates);
            result.addModel("operator", user.getName());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("interest rate list error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result add(Integer monthLimit, Double rate, String description) {
        Result result = new Result();
        try {
            InterestRateDO interestRate = interestRateManager.getByMonthLimit(monthLimit);
            if(interestRate != null) {
                result.setResultCode(ResultCode.FILE_EXISTS_ERROR);
                result.setMessage("该借款期限已经存在，请先将原借款期限置为无效");
                return result;
            }else {
                interestRate = new InterestRateDO();
            }
            interestRate.setMonthLimit(monthLimit);
            interestRate.setRate(rate);
            interestRate.setDescription(description);
            interestRateManager.insert(interestRate);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("interest rate add error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result disable(Integer id) {
        Result result = new Result();
        try {
            interestRateManager.update(id);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("interest rate disable error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }
}
