package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.LenderService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.yeepay.tzt.RefundEntity;
import com.ixhong.common.yeepay.tzt.TZTUtils;
import com.ixhong.domain.enums.BiddingItemEnum;
import com.ixhong.domain.enums.FlowStatusEnum;
import com.ixhong.domain.enums.LenderFlowTypeEnum;
import com.ixhong.domain.pojo.BiddingItemDO;
import com.ixhong.domain.pojo.LenderAccountDO;
import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.domain.query.BiddingItemQuery;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.LenderQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.*;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chenguang on 2015/5/25 0025.
 */
@Service
public class LenderServiceImpl implements LenderService {

    private Logger logger = Logger.getLogger(LenderService.class);

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private BiddingItemManager biddingItemManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public Result list(LenderQuery query) {
        Result result = new Result();
        try {
            QueryResult<LenderDO> qr = lenderManager.query(query);
            result.addModel("query", query);
            result.addModel("queryResult", qr);
            List<LenderDO> _lenders = (List<LenderDO>)qr.getResultList();
            List<LenderDO> lenders = new ArrayList<LenderDO>();
            for(LenderDO lender : _lenders) {
                Integer biddingCount = biddingItemManager.biddingCount(lender.getId());
                lender.setBiddingCount(biddingCount);
                lenders.add(lender);
            }
            result.addModel("lenders", lenders);
        }catch (Exception e) {
            logger.error("lender list error", e);
            result.setMessage("系统错误");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result biddingItemList(String phone, String realName, int page) {
        Result result = new Result();
        try {
            if(StringUtils.isBlank(phone)) {
                phone = null;
            }
            if(StringUtils.isBlank(realName)) {
                realName = null;
            }
            List<Integer> lenderIds = lenderManager.getLenderIds(phone, realName);
            BiddingItemQuery query = new BiddingItemQuery();
            query.setPageSize(6);
            query.setCurrentPage(page);
            if(CollectionUtils.isNotEmpty(lenderIds)) {
                query.setLenderIds(lenderIds);
            }
            QueryResult<BiddingItemDO> qr = biddingItemManager.query(query);
            query.setLenderPhone(phone);
            query.setLenderRealName(realName);
            List<BiddingItemDO> _biddingItems = (List < BiddingItemDO >)qr.getResultList();
            List<BiddingItemDO> biddingItems = new ArrayList<BiddingItemDO>();
            for (BiddingItemDO biddingItem : _biddingItems) {
                LenderDO lender = lenderManager.getById(biddingItem.getLenderId());
                biddingItem.setLenderPhone(lender.getPhone());
                biddingItem.setLenderRealName(lender.getRealName());
                biddingItems.add(biddingItem);
            }
            result.addModel("query", query);
            result.addModel("queryResult", qr);
            result.addModel("biddingItems", biddingItems);
            result.addModel("biddingItemEnums", BiddingItemEnum.values());
        }catch (Exception e) {
            logger.error("investment list error", e);
            result.setMessage("系统错误");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result lenderAccountFlow(LenderAccountFlowQuery lenderAccountFlowQuery) {
        Result result = new Result();
        try{
            QueryResult<LenderAccountFlowDO> queryResult =  lenderAccountFlowManager.query(lenderAccountFlowQuery);
            result.addModel("flowStatus", LenderFlowTypeEnum.values());
            result.addModel("query",lenderAccountFlowQuery);
            result.addModel("result", queryResult);
            result.addModel("flows", queryResult.getResultList());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error(" organization query lender account flow error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result unbindBankCard(String cardId, String cardTop, String cardLast) {
        Result result = new Result();
        JSONObject responseJson = null;
        try {
            responseJson = TZTUtils.unbindBankCard(cardTop, cardLast, cardId);
        } catch (Exception e) {
            result.setMessage("支付系统繁忙或超时，请稍后再试");
            return result;
        }

        if (responseJson.containsKey("error_code")) {
            result.setMessage(responseJson.getString("error_msg") + "(" + responseJson.getString("error_code") + ")");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            return result;
        }
        if (responseJson.containsKey("clientSignError")) {
            result.setMessage(responseJson.getString("clientSignError"));
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            return result;
        }

        try {
            LenderDO lender = lenderManager.getByCardId(cardId);
            LenderAccountDO account = lenderAccountManager.getByLenderId(lender.getId());
            account.setBankCardId("");
            account.setBankName("");
            lenderAccountManager.update(account);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("unbind success, but set local cardId to null failed");
            result.setMessage("unbind success, but set local cardId to null failed");
        }

        return result;
    }

    @Override
    public Result refund(String yeepayOrderId, Double amount, String cause) {
        Result result = new Result();
        JSONObject responseJson = null;
        try {
            RefundEntity entity = new RefundEntity();
            entity.setOrderId(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
            entity.setYeepayOrderId(yeepayOrderId);
            entity.setAmount((int)(amount * 100));
            entity.setCause(cause);
            responseJson = TZTUtils.directRefund(entity);
        } catch (Exception e) {
            result.setMessage("支付系统繁忙或超时，请稍后再试");
            return result;
        }

        if (responseJson.containsKey("error_code")) {
            result.setMessage(responseJson.getString("error") + "(" + responseJson.getString("error_code") + ")");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            return result;
        }
        result.setMessage(responseJson.toString());
        result.setSuccess(true);

        return result;
    }

    @Override
    public Result queryRefund(String yeepayOrderId) {
        Result result = new Result();
        JSONObject responseJson = null;
        try {
            responseJson = TZTUtils.queryRefund(yeepayOrderId);
        } catch (Exception e) {
            result.setMessage("支付系统繁忙或超时，请稍后再试");
            return result;
        }

        if (responseJson.containsKey("error_code")) {
            result.setMessage(responseJson.getString("error") + "(" + responseJson.getString("error_code") + ")");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            return result;
        }
        result.setMessage(responseJson.toString());
        result.setSuccess(true);

        return result;
    }

    @Override
    public Result queryWithdraw(String orderId) {
        Result result = new Result();
        JSONObject responseJson = null;
        try {
            responseJson = TZTUtils.queryWithdraw(orderId);
        } catch (Exception e) {
            result.setMessage("支付系统繁忙或超时，请稍后再试");
            return result;
        }

        if (responseJson.containsKey("error_code")) {
            result.setMessage(responseJson.getString("error") + "(" + responseJson.getString("error_code") + ")");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            return result;
        }
        result.setMessage(responseJson.toString());
        result.setSuccess(true);

        return result;
    }

    @Override
    public Result queryRecharge(String orderId) {
        Result result = new Result();
        try {
            final LenderAccountFlowDO flow = lenderAccountFlowManager.getByOrderId(orderId);
            if (flow == null) {
                result.setMessage("该订单不存在");
                return result;
            }
            if (flow.getStatus() == FlowStatusEnum.SUCCESS.code || flow.getStatus() == FlowStatusEnum.FAILURE.code) {
                result.setMessage("该订单已被处理");
                return result;
            }

            JSONObject responseJson = null;
            try {
                responseJson = TZTUtils.queryOrder(orderId);
            } catch (Exception e) {
                result.setMessage("支付系统繁忙或超时，请稍后再试");
                return result;
            }

            if (responseJson.containsKey("error_code")) {
                result.setMessage(responseJson.getString("error_msg") + "(" + responseJson.getString("error_code") + ")");
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                return result;
            }

            if (responseJson.containsKey("clientSignError")) {
                result.setMessage(responseJson.getString("clientSignError"));
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                return result;
            }

            final int status = responseJson.getInt("status");

            final LenderAccountDO account = lenderAccountManager.getByLenderId(flow.getLenderId());
            transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {
                    if (status == 1) {
                        double before = account.getBalance();
                        Double current = CalculatorUtils.format(before + flow.getRecharge());
                        account.setBalance(current);
                        lenderAccountManager.updateBalance(account);

                        flow.setBalanceAfter(CalculatorUtils.format(flow.getBalanceBefore() + flow.getRecharge()));
                        flow.setStatus(FlowStatusEnum.SUCCESS.code);
                    } else if (status == 2 || status == 3) {
                        flow.setStatus(FlowStatusEnum.FAILURE.code);
                    }
                    lenderAccountFlowManager.update(flow);
                    return true;
                }
            });

            result.setSuccess(true);
        } catch (Exception e) {
            logger.info("recharge query and deal error");
            return result;
        }

        return result;
    }
}
