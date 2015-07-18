package com.ixhong.lender.web.service.impl;

import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.enums.FlowStatusEnum;
import com.ixhong.domain.enums.LenderFlowTypeEnum;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.BiddingItemDO;
import com.ixhong.domain.pojo.LenderAccountDO;
import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.domain.query.BiddingItemQuery;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.lender.web.service.LenderAccountService;
import com.ixhong.manager.BiddingItemManager;
import com.ixhong.manager.LenderAccountFlowManager;
import com.ixhong.manager.LenderAccountManager;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by jenny on 5/15/15.
 */
@Service
public class LenderAccountServiceImpl implements LenderAccountService {

    private Logger logger = Logger.getLogger(LenderAccountService.class);

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private BiddingItemManager biddingItemManager;


    @Override
    public Result listFlow(LenderAccountFlowQuery query) {
            Result result = new Result();
            try {
                QueryResult<LenderAccountFlowDO> qr = this.lenderAccountFlowManager.query(query);
                result.addModel("query",query);
                result.addModel("queryResult", qr);
                result.addModel("flowTypeEnum", LenderFlowTypeEnum.values());
                result.addModel("flowMap", LenderFlowTypeEnum.getMap());
                Collection<LenderAccountFlowDO> flows =  qr.getResultList();
                for(LenderAccountFlowDO lenderAF : flows){
                    lenderAF.setAmount(CalculatorUtils.format(lenderAF.getBalanceAfter() - lenderAF.getBalanceBefore()));
                    lenderAF.setFrozen(CalculatorUtils.format(lenderAF.getFrozenAfter() - lenderAF.getFrozenBefore()));
                }
                result.addModel("flows", flows);
                result.addModel("flowSuccess", FlowStatusEnum.SUCCESS.code);
                result.addModel("flowFailure", FlowStatusEnum.FAILURE.code);
                result.setSuccess(true);

            } catch (Exception e) {
                logger.error("query student account flow error", e);
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                result.setSuccess(false);
                result.setMessage("系统繁忙，请稍后再试");
                return result;
            }
            return result;
    }

    @Override
    public Result listItem(BiddingItemQuery query) {
        Result result = new Result();
        //用户银行卡号信息
        try{
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(query.getLenderId());
            BigDecimal bigDecimal = new BigDecimal(Double.toString(lenderAccount.getTotalInterest()-lenderAccount.getCurrentInterest()));
            lenderAccount.setPendingEarn(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            result.addModel("account", lenderAccount);

            QueryResult<BiddingItemDO> qr = this.biddingItemManager.query(query);
            result.addModel("query",query);
            result.addModel("queryResult", qr);
            result.addModel("flows", qr.getResultList());
            result.setSuccess(true);

        }catch (Exception e){
            logger.error("list item error",e);
            result.setSuccess(false);
            result.setMessage("系统繁忙，请稍后再试");
        }
        return result;
    }

    @Override
    public Result addDealPassword(String password) {
        Result result = new Result();
        try{
            Integer lenderId = LoginContextHolder.getLoginUser().getId();

            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lenderId);
            if(lenderAccount == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("该账号不存在");
                return result;
            }

            lenderAccountManager.updateDealPassword(password, lenderId);
            result.setSuccess(true);
        }catch (Exception e){
            logger.error("add deal password error", e);
        }
        return result;
    }

    @Override
    public Result updateDealPassword(String oldPassword, String password) {
        Result result = new Result();
        try{
            Integer lenderId = LoginContextHolder.getLoginUser().getId();
            LenderAccountDO lenderAccount = lenderAccountManager.validateDealPassword(lenderId, oldPassword);
            if(lenderAccount == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("原密码不正确");
                return result;
            }

            lenderAccountManager.updateDealPassword(password, lenderId);
            result.setSuccess(true);
        }catch (Exception e){
            logger.error("update deal password error",e);
            result.setSuccess(false);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result getByLenderId(Integer id) {
        Result result = new Result();
        try{
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(id);
            result.addModel("lenderAccount", lenderAccount);
        }catch (Exception e){
            logger.error("get by lender id error",e);
            result.setSuccess(false);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result getFlowStatus(String orderId) {
        Result result = new Result();
        try {
            LenderAccountFlowDO flow = this.lenderAccountFlowManager.getByOrderId(orderId);
            result.addModel("status", flow.getStatus());
            String errorMessage = flow.getErrorMessage();
            if (StringUtils.isNotBlank(errorMessage)) {
                JSONObject error = JSONObject.fromObject(errorMessage);
                result.addModel("message", error.getString("message"));
            }
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("get flow status error", e);
        }
        return result;
    }

}
