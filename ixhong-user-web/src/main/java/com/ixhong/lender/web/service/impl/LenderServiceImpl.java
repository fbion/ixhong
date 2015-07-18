package com.ixhong.lender.web.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.CertifyUtils;
import com.ixhong.common.utils.HttpUtils;
import com.ixhong.common.yeepay.tzt.BindBankCardRequestEntity;
import com.ixhong.common.yeepay.tzt.PayRequestEntity;
import com.ixhong.common.yeepay.tzt.TZTUtils;
import com.ixhong.common.yeepay.tzt.WithdrawRequestEntity;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.misc.SystemConfig;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.LenderBillStageQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.lender.web.service.LenderService;
import com.ixhong.manager.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jenny on 5/11/15.
 */
@Service
public class LenderServiceImpl implements LenderService {


    private Logger logger = Logger.getLogger(LenderService.class);

    private Logger flowLogger = Logger.getLogger("flow-data-logger");

    private Logger rechargeLogger = Logger.getLogger("recharge-data-logger");

    private Logger withdrawLogger = Logger.getLogger("withdraw-data-logger");

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private MongoDBClient mongoDBClient;

    @Autowired
    private YibaoCityManager yibaoCityManager;

    @Autowired
    private BankManager bankManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private LenderBillStageManager lenderBillStageManager;

    @Autowired
    private AccountFlowManager accountFlowManager;

    @Autowired
    private AccountManager accountManager;

    private String registerPhoneCode = "lenderRegisterPhoneCode";

    private String withdrawPhoneCode = "lenderWithdrawPhoneCode";

    @Override
    public Result register(LenderDO lender, String code) {
        Result result = new Result();
        //用户名
        LenderDO _lender = this.lenderManager.getByName(lender.getName());
        if (_lender != null) {
            result.setMessage("用户名已存在！");
            result.setResultCode(ResultCode.PARAMETER_ERROR);
            return result;
        }
        // 手机号码
        _lender = this.lenderManager.getByTelephone(lender.getPhone());
        if (_lender != null) {
            result.setResultCode(ResultCode.PARAMETER_ERROR);
            result.setMessage("手机号码已被注册");
            return result;
        }
        // 验证码
        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
        Bson parentFilter = Filters.and(Filters.eq("phone", lender.getPhone()), Filters.eq("type", registerPhoneCode));

        Document document = dbCollection.find(parentFilter).limit(1).first();
        result.setResultCode(ResultCode.VALIDATE_FAILURE);
        if (document == null) {
            result.setSuccess(false);
            result.setMessage("验证码非法");
            return result;
        }
        Long created = document.getLong("created");
        if (created + 20 * 60000L <= System.currentTimeMillis()) {
            result.setSuccess(false);
            result.setMessage("验证码已经过期");
            return result;
        }
        if (!code.equals(document.get("checkCode"))) {
            result.setSuccess(false);
            result.setMessage("验证码错误");
            return result;
        }
        this.lenderManager.insert(lender);
        this.lenderAccountManager.insert(lender.getId());
        result.setResultCode(ResultCode.SUCCESS);
        result.setSuccess(true);
        result.setMessage("注册成功");
        return result;
    }

    @Override
    public Result updatePassword(String phone, String oldPassword, String password) {
        Result result = new Result();
        try {
            LenderDO lender = lenderManager.validate(phone, oldPassword);
            if (lender == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("原密码不正确");
                return result;
            }
            lenderManager.updatePasswordByPhone(phone, password);
            result.setMessage("密码修改成功");
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("update password error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result isPhoneExisted(String phone) {
        Result result = new Result();
        LenderDO lender = this.lenderManager.getByTelephone(phone);
        if (lender != null) {
            result.setResultCode(ResultCode.SUCCESS);
            result.setMessage("手机号码已被注册");
            return result;
        }
        result.setResultCode(ResultCode.PARAMETER_ERROR);
        result.setMessage("手机号码未被注册");
        return result;
    }

    @Override
    public Result resetPassword(String phone, String password) {
        Result result = new Result();
        try {
            LenderDO lender = lenderManager.getByTelephone(phone);
            if (lender == null) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("该手机号尚未注册");
                return result;
            }
            lenderManager.updatePasswordByPhone(phone, password);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("reset password error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }


    @Override
    public Result login(String phone, String password) {
        Result result = new Result();
        try {
            LenderDO lender = this.lenderManager.validate(phone, password);
            this.lenderManager.updateLoginTime(lender.getId());
            if (lender != null) {
                result.setSuccess(true);
                result.setMessage("登录成功");
                result.addModel("lender", lender);
                return result;
            }
            result.setResultCode(ResultCode.PARAMETER_ERROR);
            result.setMessage("用户名或密码错误");
            return result;
        } catch (Exception e) {
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统繁忙请稍后再试");
        }
        result.setResultCode(ResultCode.PARAMETER_ERROR);
        result.setMessage("用户名或密码错误");
        return result;
    }

    public Result checkPhoneCode(String phone, String code, String type) {
        Result result = new Result();
        try {
            MongoCollection dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
            Bson parentFilter = Filters.and(Filters.eq("phone", phone), Filters.eq("type", type));
            Document document = (Document) dbCollection.find(parentFilter).projection(new Document("_id", 0)).limit(1).first();

            if (document == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("验证码不正确");
                return result;
            }

            if (!code.equals(document.get("code"))) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("验证码不正确");
                return result;
            }

            if (System.currentTimeMillis() - document.getLong("created") >= 20 * 60 * 1000) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("验证码已过期");
                return result;
            }

            result.setSuccess(true);
            result.setMessage("验证码正确");
        } catch (Exception e) {
            logger.error("check phone code error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result sendPhoneCode(String phone, String card_id, String type) {
        Result result = new Result();
        try {
            if (type.equals("lenderFindPasswordPhoneCode") || type.equals("lenderFindDealPasswordPhoneCode")) {
                LenderDO lender = lenderManager.getByTelephone(phone);
                if (lender == null) {
                    result.setResultCode(ResultCode.PARAMETER_ERROR);
                    result.setMessage("该手机号尚未注册");
                    return result;
                }

                if (card_id != null) {
                    LenderDO _lender = lenderManager.getById(LoginContextHolder.getLoginUser().getId());
                    if (!_lender.getCardId().equals(card_id)) {
                        result.setResultCode(ResultCode.PARAMETER_ERROR);
                        result.setMessage("请输入实名认证的身份证号");
                        return result;
                    }
                }
            }

            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
            Bson parentFilter = Filters.and(Filters.eq("phone", phone), Filters.eq("type", type));
            Document document = dbCollection.find(parentFilter).projection(new Document("_id", 0)).limit(1).first();
            String code = RandomStringUtils.random(6, false, true);
            String content = "";
            if ("lenderAddBankCardPhoneCode".equals(type)) {
                content = "您正在用手机添加银行卡，验证码为：" + code + "，20分钟内有效。【学好贷】";
            } else if ("lenderFindPasswordPhoneCode".equals(type)) {
                content = "您正在用手机找回密码，验证码为：" + code + "，20分钟内有效。【学好贷】";
            } else if ("lenderFindDealPasswordPhoneCode".equals(type)) {
                content = "您正在用手机找回交易密码，验证码为：" + code + "，20分钟内有效。【学好贷】";
            }
            //如果是第一次发送
            if (document == null) {
                boolean isSuccess = smsSend(phone, content);
                if (isSuccess) {
                    document = new Document();
                    document.append("phone", phone);
                    document.append("code", code);
                    document.append("type", type);
                    document.append("times", 1);
                    document.append("created", System.currentTimeMillis());
                    dbCollection.insertOne(document);
                }
                result.setSuccess(isSuccess);
                result.setResultCode(ResultCode.SUCCESS);
                result.setMessage("短信发送成功");
            }

            //再次发送，如果验证码超过24小时，则过期
            if (System.currentTimeMillis() - document.getLong("created") > 24 * 60 * 60 * 1000) {
                boolean isSuccess = smsSend(phone, content);
                if (isSuccess) {
                    document.clear();
                    document.append("code", code);
                    document.append("times", 1);
                    document.append("created", System.currentTimeMillis());
                    dbCollection.updateOne(parentFilter, new Document("$set", document));
                }
                result.setSuccess(isSuccess);
                result.setResultCode(ResultCode.EXPIRED.SUCCESS);
                result.setMessage("短信发送成功");
            }

            if (System.currentTimeMillis() - document.getLong("created") <= 60 * 1000) {
                result.setResultCode(ResultCode.SMS_SENDING_INTERVAL_LIMIT);
                result.setMessage("短信发送时间间隔过短");
                return result;
            }

            Integer times = document.getInteger("times");
            if (times >= 3) {
                result.setResultCode(ResultCode.SMS_TIMES_LIMIT);
                result.setMessage("短信发送超过最大次数");
                return result;
            }

            boolean isSuccess = smsSend(phone, content);
            if (isSuccess) {
                document.clear();
                document.append("code", code);
                document.append("created", System.currentTimeMillis());
                document.append("times", times + 1);
                dbCollection.updateOne(parentFilter, new Document("$set", document));
            }
            result.setSuccess(true);
            result.setMessage("短信发送成功");
            return result;
        } catch (Exception e) {
            logger.error("send phone code error!", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("手机验证码发送失败");
        }
        return result;
    }

    @Override
    public Result isCardIdExisted(String cardId) {
        return null;
    }

    @Override
    public Result isNameExisted(String name) {
        Result result = new Result();
        LenderDO lender = this.lenderManager.getByName(name);
        if (lender != null) {
            result.setResultCode(ResultCode.SUCCESS);
            result.setMessage("用户名已注册");
            return result;
        }
        result.setResultCode(ResultCode.PARAMETER_ERROR);
        result.setMessage("用户名未注册");

        return result;
    }

    @Override
    public Result getById(Integer id) {
        Result result = new Result();
        try {
            LenderDO lender = this.lenderManager.getById(id);
            if (lender != null) {
                result.addModel("lender", lender);
                result.setSuccess(true);
                result.setMessage("affected one record");
                return result;
            }

        } catch (Exception e) {
            result.setMessage("系统错误");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        result.setMessage("affected 0 record");
        result.setResultCode(ResultCode.PARAMETER_ERROR);
        return result;
    }

    @Override
    public Result getCities(String pid) {
        Result result = new Result();
        try {
            List<YibaoCityDO> cities = yibaoCityManager.getByPid(pid);
            result.addModel("cities", cities);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("get cities error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("城市列表获取失败");
        }
        return result;
    }

    @Override
    public Result lenderAccountDetail() {
        Result result = new Result();
        try {
            List<YibaoCityDO> provinces = yibaoCityManager.getByPid("0");
            List<BankDO> banks = bankManager.getAll();
            LenderDO lender = lenderManager.getById(LoginContextHolder.getLoginUser().getId());
            String realName = "*" + lender.getRealName().substring(1);
            result.addModel("provinces", provinces);
            result.addModel("banks", banks);
            result.addModel("realName", realName);
            result.addModel("lender", lender);

            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(LoginContextHolder.getLoginUser().getId());
            if (lenderAccount != null) {
                result.addModel("lenderAccount", lenderAccount);
            }

            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("get cities and banks error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result addBankCard(LenderAccountDO lenderAccount, String phoneCode, String requestId) {
        Result result = new Result();
        try {
            Integer lenderId = LoginContextHolder.getLoginUser().getId();
            LenderAccountDO _lenderAccount = this.lenderAccountManager.getByLenderId(lenderId);
            if (StringUtils.isNotBlank(_lenderAccount.getBankCardId())) {
                result.setMessage("您已绑定了银行卡，不可再次绑定");
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                return result;
            }

            JSONObject responseJson = TZTUtils.bindBankCard(requestId, phoneCode);
            if (responseJson.containsKey("error_code")) {
                result.setMessage(responseJson.getString("error_msg") + "(" + responseJson.getString("error_code") + ")");
            } else if (responseJson.containsKey("clientSignError")) {
                result.setMessage(responseJson.getString("clientSignError"));
            } else {
                String bankCardId = lenderAccount.getBankCardId();
                //比较卡号前6位、后4位
                if (bankCardId.substring(0, 6).equals(responseJson.getString("card_top"))
                        && bankCardId.substring(bankCardId.length() - 4, bankCardId.length()).equals(responseJson.getString("card_last"))) {
                    lenderAccountManager.update(lenderAccount);
                    LenderDO lender = lenderManager.getById(lenderId);
                    result.addModel("realName", lender.getRealName());
                    result.setSuccess(true);
                } else {
                    result.setMessage("添加银行卡失败，请重新操作");
                }
            }
        } catch (Exception e) {
            logger.error("add bank card error", e);
            result.setMessage("网络繁忙，请稍后再试");
        }
        return result;
    }


    @Override
    public Result registerSendPhoneCode(String phone) {
        Result result = new Result();
        try {
            LenderDO _lender = this.lenderManager.getByTelephone(phone);
            if (_lender != null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("手机号已被注册");
                return result;
            }

            String code = RandomStringUtils.random(6, false, true);
            String content = "您正在注册学好贷，您的手机验证码为：" + code + "，20分钟内有效。【学好贷】";
            result = sendPhoneCheckCode(phone, code, content, registerPhoneCode, 3);
        } catch (Exception e) {
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("网络繁忙，请稍后再试");
            return result;
        }
        return result;
    }

    @Override
    public Result withdrawSendPhoneCode() {
        Result result = new Result();
        try {
            Integer lenderId = LoginContextHolder.getLoginUser().getId();
            LenderDO lender = this.lenderManager.getById(lenderId);
            String phone = lender.getPhone();
            if (StringUtils.isBlank(phone)) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("您的注册手机号不存在");
                return result;
            }

            String code = RandomStringUtils.random(6, false, true);
            String content = "您正在使用学好贷申请提现至银行卡，您的手机验证码为：" + code + "，20分钟内有效。【学好贷】";
            result = sendPhoneCheckCode(phone, code, content, withdrawPhoneCode, 20);
        } catch (Exception e) {
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("网络繁忙，请稍后再试");
            return result;
        }
        return result;
    }

    /**
     * 发送手机验证码common
     * @param phone 手机号
     * @param checkCode 验证码
     * @param content 发送内容
     * @param type 发送手机号类型，用于业务区别(例如注册或提现)
     * @param limit 每天发送限制次数
     * @return
     */
    private Result sendPhoneCheckCode(String phone,String checkCode,String content,String type,Integer limit){
        Result result = new Result();
        try {
            //到mongo检测，发的次数和过期
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
            Bson parentFilter = Filters.and(Filters.eq("phone", phone), Filters.eq("type", type));

            Document document = dbCollection.find(parentFilter).projection(new Document("_id",0)).limit(1).first();
            if(document == null) {
                //如果是第一次发送
                boolean isSuccess = smsSend(phone, content);
                if(isSuccess) {
                    document = new Document();
                    document.append("phone", phone);
                    document.append("checkCode", checkCode);
                    document.append("type", type);
                    document.append("created", System.currentTimeMillis());
                    document.append("times", 1);
                    dbCollection.insertOne(document);
                }
                result.setResultCode(ResultCode.SUCCESS);
                result.setMessage("短信发送成功");
                result.setSuccess(isSuccess);
                return result;
            }

            //如果已经发送过
            long timestamp = document.getLong("created");//毫秒数
            //如果为24小时外发送--已过期
            if(timestamp + 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
                //重新发送验证码
                boolean isSuccess = smsSend(phone, content);
                if(isSuccess) {
                    document.clear();
                    document.append("checkCode", checkCode);
                    document.append("created", System.currentTimeMillis());
                    document.append("times", 1);
                    dbCollection.updateOne(parentFilter, new Document("$set", document));
                }
                result.setResultCode(ResultCode.SUCCESS);
                result.setMessage("短信发送成功");
                result.setSuccess(isSuccess);
                return result;
            }

            //如果mongo中已经存在此记录
            int times = document.getInteger("times");
            if(times >= limit) {
                result.setResultCode(ResultCode.SMS_TIMES_LIMIT);
                result.setMessage("短信发送条数超过" + limit + "条限制，请您24小时后再试");
                result.setSuccess(false);
                return result;
            }
            //如果没超过1分钟,重新发送验证码报时间间隔太短
            if(timestamp + 60000L >= System.currentTimeMillis()) {
                result.setResultCode(ResultCode.SMS_SENDING_INTERVAL_LIMIT);
                result.setMessage("短信发送时间间隔过短，请您稍后再试");
                result.setSuccess(false);
                return result;
            }

            boolean isSuccess= smsSend(phone, content);
            if(isSuccess) {
                document.clear();
                document.append("times", times + 1);
                document.append("checkCode", checkCode);
                document.append("created", System.currentTimeMillis());
                dbCollection.updateOne(parentFilter, new Document("$set", document));
            }
            result.setResultCode(ResultCode.SUCCESS);
            result.setMessage("短信发送成功");
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("系统繁忙，请稍后再试");
            return result;
        }
        return result;
    }

    public boolean smsSend(String phone, String content) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sn", Constants.SMS_SEND_SN);
        params.put("pwd", Constants.SMS_SEND_PASSWORD);
        params.put("mobile", phone);
        params.put("content", content);
        params.put("msgfmt", "");
        params.put("ext","");
        params.put("stime","");
        params.put("rrid","");
        String response = HttpUtils.httpGet(Constants.SMS_SEND_URL, params);
        return response.startsWith("-") ? false : true;
    }


    @Override
    public Result getDetailById(Integer lengerId) {
        Result result = new Result();
        try {
            //用户银行卡号信息
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lengerId);
            Double pendingEarn = CalculatorUtils.format(lenderAccount.getTotalInterest() - lenderAccount.getCurrentInterest());
            lenderAccount.setPendingEarn(pendingEarn);
            result.addModel("account", lenderAccount);
            //账单明细---最近五笔交易
            LenderAccountFlowQuery query = new LenderAccountFlowQuery();
            query.setPageSize(5);
            query.setCurrentPage(1);
            query.setLenderId(LoginContextHolder.getLoginUser().getId());
            QueryResult<LenderAccountFlowDO> lenderAccountFlow = this.lenderAccountFlowManager.query(query);
            Collection<LenderAccountFlowDO> list = lenderAccountFlow.getResultList();
            for (LenderAccountFlowDO lenderAF : list) {
                lenderAF.setAmount(CalculatorUtils.format(lenderAF.getBalanceAfter() - lenderAF.getBalanceBefore()));
                lenderAF.setFrozen(CalculatorUtils.format(lenderAF.getFrozenAfter() - lenderAF.getFrozenBefore()));
            }
            result.addModel("flows", list);

            //待收款明细
            LenderBillStageQuery lenderBillStageQuery = new LenderBillStageQuery();
            lenderBillStageQuery.setLenderId(LoginContextHolder.getLoginUser().getId());
            lenderBillStageQuery.setCurrentPage(1);
            lenderBillStageQuery.setPageSize(2);

            lenderBillStageQuery.setStatus(BillStageStatusEnum.PAID.code);
            QueryResult<LenderBillStageDO> items = this.lenderBillStageManager.query(lenderBillStageQuery);
            result.addModel("billStages", items.getResultList());
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("系统繁忙，请稍后再试");
            return result;
        }
    }

    @Override
    public Result certify(String name, String cardId) {
        Result result = new Result();
        try {
            //一个身份证号只能对应一个账号
            LenderDO _lender = lenderManager.getByCardId(cardId);
            if(_lender != null) {
                result.setMessage("该身份证号已认证不能重复认证");
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                return result;
            }

            String source = CertifyUtils.certify(name, cardId);
            if (source.equals(ResultCode.SYSTEM_ERROR.code)) {
                result.setMessage("风控系统访问异常，请稍后再试");
                return result;
            }
            String code = StringUtils.split(source, ",")[0];
            if (code.equals("00") || code.equals("10")) {
                LenderDO lender = new LenderDO();
                lender.setId(LoginContextHolder.getLoginUser().getId());
                lender.setCardId(cardId);
                lender.setRealName(name);
                lender.setCertified(1);
                lenderManager.updateCertified(lender);
                result.setSuccess(true);
                return result;
            }
            result.setResultCode(ResultCode.VALIDATE_FAILURE);
            result.setMessage("实名认证失败");
        } catch (Exception e) {
            logger.error("identity error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result bankCardList() {
        Result result = new Result();
        try {
            Integer lenderId = LoginContextHolder.getLoginUser().getId();
            LenderDO lender = lenderManager.getById(lenderId);
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lenderId);
            result.addModel("lenderAccount", lenderAccount);
            result.addModel("lender", lender);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("bank card list error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result isCertify() {
        Result result = new Result();
        try {
            //如果实名认证未通过，跳转到实名认证页面
            LenderDO _lender = lenderManager.getById(LoginContextHolder.getLoginUser().getId());
            if (_lender.getCertified() == 0) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("请先进行实名认证");
                result.setResponseUrl("/lender/certify.jhtml");
                return result;
            }

            //没有设置交易密码，跳转到设置交易密码页面d
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(LoginContextHolder.getLoginUser().getId());
            if (lenderAccount == null || StringUtils.isBlank(lenderAccount.getDealPassword())) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("请先设置交易密码");
                result.setResponseUrl("/lender/add_deal_password.jhtml");
                return result;
            }

            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("is certify error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result bindBankCard(String bankCardId, String phone, String clientIp) {
        Result result = new Result();
        try {
            Integer lenderId = LoginContextHolder.getLoginUser().getId();
            LenderAccountDO lenderAccount = this.lenderAccountManager.getByLenderId(lenderId);
            if (StringUtils.isNotBlank(lenderAccount.getBankCardId())) {
                result.setMessage("您已绑定了银行卡，不可再次绑定");
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                return result;
            }

            LenderDO lender = this.lenderManager.getById(lenderId);
            BindBankCardRequestEntity requestEntity = new BindBankCardRequestEntity();
            requestEntity.setRequestId("xuehaodai_" + lenderId + "_" + System.currentTimeMillis());
            requestEntity.setIdentityId(lender.getCardId());
            requestEntity.setCardId(lender.getCardId());
            requestEntity.setRealName(lender.getRealName());
            requestEntity.setBankCardId(bankCardId);
            requestEntity.setPhone(phone);
            requestEntity.setClientIp(clientIp);

            JSONObject responseJson = TZTUtils.bindBankCardRequest(requestEntity);
            if (responseJson.containsKey("error_code")) {
                result.setMessage(responseJson.getString("error_msg") + "(" + responseJson.getString("error_code") + ")");
            } else if (responseJson.containsKey("clientSignError")) {
                result.setMessage(responseJson.getString("clientSignError"));
            } else {
                result.addModel("requestId", responseJson.getString("requestid"));
                result.setSuccess(true);
            }
        } catch (Exception e) {
            logger.error("bind bank card request error", e);
            result.setMessage("系统繁忙或超时，请重新操作");
        }
        return result;
    }

    @Override
    public Result recharge(double recharge, String clientIp, String dealPassword) {
        Result result = new Result();
        Integer lenderId = LoginContextHolder.getLoginUser().getId();

        LenderAccountDO account = this.lenderAccountManager.validateDealPassword(lenderId, dealPassword);
        if (account == null) {
            result.setMessage("交易密码不正确");
            return result;
        }
        if (StringUtils.isBlank(account.getBankCardId()) || StringUtils.isBlank(account.getDealPassword())) {
            result.setMessage("请先绑定交易银行卡");
            return result;
        }

        double fee = 0;//充值手续费，投资人不收，单位元
        double amount = recharge + fee;//手续费 + 充值额，单位元

        LenderDO lender = this.lenderManager.getById(lenderId);
        String bankCardId = account.getBankCardId();

        String cardTop = bankCardId.substring(0, 6);
        String cardLast = bankCardId.substring(bankCardId.length() - 4, bankCardId.length());
        Long time = System.currentTimeMillis();

        PayRequestEntity requestEntity = new PayRequestEntity();
        String orderId = "LR" + lenderId + "-" + time;
        requestEntity.setOrderId(orderId);
        requestEntity.setTransactionTime(((Long) (time / 1000)).intValue());
        String protocol = "https://";
        requestEntity.setAmount((int) (recharge * 100));
        if (systemConfig.getEnvRecharge() != null) {
            requestEntity.setAmount((int) (0.01 * 100));
            protocol = "http://";
        }
        requestEntity.setProductName("理财人充值");
        requestEntity.setIdentityId(lender.getCardId());
        requestEntity.setCardTop(cardTop);
        requestEntity.setCardLast(cardLast);
        requestEntity.setCallbackUrl(protocol + systemConfig.getHostName() + "/lender/recharge_callback.jhtml");
        requestEntity.setClientIp(clientIp);

        JSONObject responseJson = null;
        try {
            responseJson = TZTUtils.directBindPay(requestEntity);
            rechargeLogger.error(responseJson.toString());
        } catch (Exception e) {
            logger.error("recharge request error, lenderId:" + lenderId, e);
            result.setMessage("第三方支付系统繁忙，请稍后再试");
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
        //正确流程
        //result.addModel("orderId", responseJson.getString("orderid"));
        //result.addModel("yeepayOrderId", responseJson.getString("yborderid"));
        //result.addModel("amount", Double.parseDouble(responseJson.getString("amount")) / 100);
        result.addModel("recharge", Double.parseDouble(responseJson.getString("amount")) / 100);
        result.addModel("orderId", responseJson.getString("orderid"));

        LenderAccountFlowDO flow = new LenderAccountFlowDO();
        flow.setLenderId(lenderId);
        flow.setLenderName(lender.getName());
        flow.setLenderRealName(lender.getRealName());
        flow.setPhone(lender.getPhone());
        flow.setType(LenderFlowTypeEnum.RECHARGE.code);
        flow.setOrderId(orderId);
        flow.setAmount(amount);
        flow.setRecharge(recharge);
        flow.setFee(fee);
        flow.setBalanceBefore(account.getBalance());
        flow.setBalanceAfter(account.getBalance());
        flow.setFrozen(0D);
        flow.setFrozenBefore(account.getBalanceFrozen());
        flow.setFrozenAfter(account.getBalanceFrozen());
        flow.setDescription("快捷充值");
        flow.setStatus(FlowStatusEnum.ONGOING.code);
        this.lenderAccountFlowManager.insert(flow);

        buildRechargeFlowLog(flow, System.currentTimeMillis());

        result.setSuccess(true);

        return result;
    }

    @Override
    public Result withdraw(final Double withdraw, String clientIp, String dealPassword, String phoneCode) {
        final Result result = new Result();
        Integer lenderId = LoginContextHolder.getLoginUser().getId();
        try {
            final LenderAccountDO account = this.lenderAccountManager.validateDealPassword(lenderId, dealPassword);
            if (account == null) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("交易密码不正确");
                return result;
            }

            if (StringUtils.isBlank(account.getBankName()) || StringUtils.isBlank(account.getBankCardId())){
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("您还未绑定银行卡");
                return result;
            }

            if(account.getBalance() <= 0 || account.getBalanceFrozen() < 0) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("账户余额不足，无法提现");
                return result;
            }

            //校验手机校验码是否正确
            final LenderDO lender = lenderManager.getById(lenderId);
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
            Bson parentFilter = Filters.and(Filters.eq("phone", lender.getPhone()), Filters.eq("type", withdrawPhoneCode));
            Document document = dbCollection.find(parentFilter).limit(1).first();

            result.setResultCode(ResultCode.VALIDATE_FAILURE);
            result.addModel("flag", "code_error");
            if (document == null) {
                result.setMessage("验证码非法");
                return result;
            }
            Long created = document.getLong("created");
            if (created + 20 * 60000L <= System.currentTimeMillis()) {
                result.setMessage("验证码已经过期");
                return result;
            }
            if (!phoneCode.equals(document.get("checkCode"))) {
                result.setMessage("验证码错误");
                return result;
            }

            // 提现金额+手续费=交易总额
            final Double amount = CalculatorUtils.format(withdraw + 2);
            if (amount.compareTo(account.getBalance()) > 0){
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("账户余额不足");
                return result;
            }

            final long time = System.currentTimeMillis();
            //计算出商家订单号
            final String orderId = "LW" + lenderId + "-" + time;

            /**
             * 发送提现请求
             */
            String bankCardId = account.getBankCardId();
            String top = bankCardId.substring(0, 6);
            String last = bankCardId.substring(bankCardId.length() - 4, bankCardId.length());
            final WithdrawRequestEntity requestEntity = new WithdrawRequestEntity();
            requestEntity.setRequestId(orderId);
            requestEntity.setIdentityId(lender.getCardId());
            requestEntity.setCardTop(top);
            requestEntity.setCardLast(last);
            requestEntity.setAmount(((Double) (withdraw * 100)).intValue());
            requestEntity.setClientIp(clientIp);

            //事务操作
            boolean isSuccess = transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {
                    try {
                        //修改投资人账户
                        Double balanceBefore = account.getBalance();
                        Double balanceAfter = CalculatorUtils.format(balanceBefore - amount);
                        Double frozenBefore = account.getBalanceFrozen();
                        Double frozenAfter = CalculatorUtils.format(frozenBefore + amount);
                        account.setBalance(balanceAfter);
                        account.setBalanceFrozen(frozenAfter);
                        lenderAccountManager.updateBalance(account);

                        //新建一条投资人流水
                        final LenderAccountFlowDO flow = new LenderAccountFlowDO();
                        flow.setLenderId(lender.getId());
                        flow.setLenderName(lender.getName());
                        flow.setLenderRealName(lender.getRealName());
                        flow.setPhone(lender.getPhone());
                        flow.setType(LenderFlowTypeEnum.WITHDRAW.code);
                        flow.setOrderId(orderId);
                        flow.setAmount(amount);
                        flow.setWithdraw(withdraw);
                        flow.setFee(2.0D);
                        flow.setDescription("提现金额:" + withdraw + "元；手续费2元。");
                        flow.setBalanceBefore(balanceBefore);
                        flow.setBalanceAfter(balanceAfter);
                        flow.setFrozenBefore(frozenBefore);
                        flow.setFrozenAfter(frozenAfter);
                        //将流水状态设为提现处理中
                        flow.setStatus(FlowStatusEnum.ONGOING.code);
                        lenderAccountFlowManager.insert(flow);

                        JSONObject responseJson = null;
                        try {
                            responseJson = TZTUtils.withdraw(requestEntity);
                            withdrawLogger.error(responseJson.toString());
                        } catch (Exception e) {
                            logger.error("lender withdraw request error, lenderId:" + lender.getId(), e);
                            result.setMessage("第三方支付系统繁忙，请稍后再试");
                            throw new RuntimeException(e);
                        }

                        if (responseJson.containsKey("error_code")) {
                            result.setResultCode(ResultCode.VALIDATE_FAILURE);
                            result.setMessage("{" + responseJson.getString("error_code") + ":" + responseJson.getString("error_msg") + "}");
                            throw new RuntimeException("{" + responseJson.getString("error_code") + ":" + responseJson.getString("error_msg") + "}");
                        }

                        if (responseJson.containsKey("clientSignError")) {
                            result.setResultCode(ResultCode.VALIDATE_FAILURE);
                            result.setMessage(responseJson.getString("clientSignError"));
                            throw new RuntimeException(responseJson.getString("clientSignError"));
                        }

                        //提现请求状态：FAILURE：请求失败 SUCCESS：请求成功 UNKNOW:未知
                        String withdrawStatus = responseJson.getString("status");
                        if (withdrawStatus.equals("FAILURE")) {
                            result.setResultCode(ResultCode.VALIDATE_FAILURE);
                            result.setMessage("提现请求失败，请稍后重试!");
                            throw new RuntimeException("提现请求失败，状态：" + withdrawStatus);
                        }

                        //易宝返回的商家订单号
                        String requestId = responseJson.getString("requestid");
                        if (!orderId.equals(requestId)) {
                            result.setResultCode(ResultCode.VALIDATE_FAILURE);
                            result.setMessage("订单号不一致，订单号：" + orderId);
                            throw new RuntimeException("订单号不一致，订单号：" + orderId);
                        }

                        //提现流水日志
                        buildFlowLog(flow, time);
                        return true;
                    } catch (Exception e) {
                        transactionStatus.setRollbackOnly();
                    }
                    return false;
                }
            });

            result.setSuccess(isSuccess);
        } catch (Exception e) {
            logger.error("withdraw error, lenderId:" + lenderId, e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误，请稍后再试!");
        }
        return result;
    }

    /**
     * 提现后投资人账户流水日志处理
     */
    private void buildFlowLog(LenderAccountFlowDO flow,Long time){
        try {
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
            Document document = new Document();
            document.append("operation", OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
            document.append("flowId", flow.getId());
            document.append("lenderId", flow.getLenderId());
            document.append("orderId", flow.getOrderId());
            document.append("type", flow.getType());
            document.append("withdraw", flow.getWithdraw());
            document.append("fee", flow.getFee());
            document.append("amount", flow.getAmount());
            document.append("status", FlowStatusEnum.ONGOING.code);
            document.append("description", flow.getDescription());
            document.append("created", time);
            dbCollection.insertOne(document);
        } catch (Exception e) {
            logger.error("write to mongodb error", e);
        }
        //log4j自定义日志文件里面写入一条记录
        JSONObject json = new JSONObject();
        json.put("flowId", flow.getId());
        json.put("lenderId", flow.getLenderId());
        json.put("orderId", flow.getOrderId());
        json.put("withdraw", flow.getWithdraw());
        json.put("fee", flow.getFee());
        json.put("amount", flow.getAmount());
        json.put("status", FlowStatusEnum.ONGOING.code);
        json.put("created", time);

        flowLogger.error(json.toString());
    }

    @Override
    public Result rechargeCallback(String data, String encryptKey) {
        logger.info("----------------yeepay callback start--------------");
        Result result = new Result();
        try {
            JSONObject responseJson = TZTUtils.decryptCallbackData(data, encryptKey);
            rechargeLogger.error(responseJson.toString());

            if (responseJson.containsKey("clientSignError")) {
                result.setMessage(responseJson.getString("clientSignError"));
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                return result;
            }

            String orderId = responseJson.getString("orderid");
            final LenderAccountFlowDO flow = this.lenderAccountFlowManager.getByOrderId(orderId);
            if (flow == null) {
                result.setMessage("充值订单不存在，订单号：" + orderId);
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                return result;
            }
            int status = flow.getStatus();
            if (status == FlowStatusEnum.SUCCESS.code || status == FlowStatusEnum.FAILURE.code) {
                result.setMessage("该订单已被处理");
                return result;
            }

            if (responseJson.containsKey("errorcode") || responseJson.getInt("status") == 0) {
                String code = responseJson.getString("errorcode");
                String message = responseJson.getString("errormsg");
                JSONObject error = new JSONObject();
                error.put("code", code);
                error.put("message", message);
                String errorMessage = error.toString();
                this.lenderAccountFlowManager.update(flow.getId(), errorMessage);

                buildRechargeFlowLog(flow, System.currentTimeMillis());
                result.setMessage(message + "(" + code + ")");
                return result;
            }

            //需要比较返回的金额与商家数据库中订单的金额是否相等，只有相等的情况下才认为是交易成功.
            if (systemConfig.getEnvRecharge() == null && flow.getRecharge().compareTo(NumberUtils.toDouble(responseJson.getString("amount")) / 100) != 0) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("支付金额与商家数据库中订单的金额不相等");
                return result;
            }

            //事务操作
            transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {

                    Integer lenderId = flow.getLenderId();
                    LenderAccountDO account = lenderAccountManager.getByLenderId(lenderId);
                    double before = account.getBalance();
                    Double recharge = flow.getRecharge();
                    Double current = CalculatorUtils.format(before + recharge);
                    account.setBalance(current);
                    lenderAccountManager.updateBalance(account);

                    flow.setBalanceAfter(current);
                    flow.setBalanceBefore(before);
                    flow.setStatus(FlowStatusEnum.SUCCESS.code);
                    lenderAccountFlowManager.update(flow);

                    Double fee = CalculatorUtils.format(recharge * 0.004);
                    if (fee > 0) {
                        AccountFlowDO accountFlow = new AccountFlowDO();
                        accountFlow.setType(AdminFlowTypeEnum.RECHARGE_FEE.code);
                        accountFlow.setAmount(fee);
                        AccountDO ixhongAccount = accountManager.get();
                        accountFlow.setBalanceAfter(CalculatorUtils.format(ixhongAccount.getBalance() - fee));
                        accountFlow.setLenderId(lenderId);
                        accountFlowManager.insert(accountFlow);

                        ixhongAccount.setBalance(accountFlow.getBalanceAfter());
                        accountManager.update(ixhongAccount);
                    }

                    buildRechargeFlowLog(flow, System.currentTimeMillis());

                    return true;
                }
            });

            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("recharge error", e);
            result.setMessage("充值失败，请稍后再试");
        }
        return result;
    }

    private void buildRechargeFlowLog(LenderAccountFlowDO flow, long time) {
        try {
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
            Document document = new Document();
            document.append("operation", OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
            document.append("lenderId", flow.getLenderId());
            document.append("orderId", flow.getOrderId());
            document.append("type", flow.getType());
            document.append("recharge", flow.getRecharge());
            document.append("fee", flow.getFee());
            document.append("amount", flow.getAmount());
            document.append("flowId", flow.getId());
            document.append("status", flow.getStatus());
            document.append("description", flow.getDescription());
            document.append("created", time);
            dbCollection.insertOne(document);
        } catch (Exception e) {
            logger.error("write to mongodb error", e);
        }
        //log4j自定义日志文件里面写入一条记录
        JSONObject json = new JSONObject();
        json.put("lenderId", flow.getLenderId());
        json.put("orderId", flow.getOrderId());
        json.put("recharge", flow.getRecharge());
        json.put("fee", flow.getFee());
        json.put("amount", flow.getAmount());
        json.put("flowId", flow.getId());
        json.put("status", flow.getStatus());
        json.put("created", time);

        flowLogger.error(json.toString());
    }

    @Override
    public Result resetDealPassword(String phone, String password) {
        Result result = new Result();
        try {
            LenderDO lender = lenderManager.getByTelephone(phone);
            if (lender == null) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("该手机号尚未注册");
                return result;
            }
            lenderAccountManager.updateDealPassword(password, lender.getId());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("reset password error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result accountDetail() {
        Result result = new Result();
        try {
            Integer lenderId = LoginContextHolder.getLoginUser().getId();

            //校验是否进行实名认证
            LenderDO lender = lenderManager.getById(lenderId);
            if (lender == null || lender.getCertified() == 0) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("请先进行实名认证");
                result.addModel("step", "certify");
                return result;
            }

            //校验是否设置交易密码
            LenderAccountDO account = lenderAccountManager.getByLenderId(lenderId);
            if (account == null || StringUtils.isBlank(account.getDealPassword())) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("请先设置交易密码");
                result.addModel("step", "password");
                return result;
            }

            //校验是否绑定银行卡
            if (StringUtils.isBlank(account.getBankCardId())) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("请先绑定银行卡");
                result.addModel("step", "bank");
                return result;
            }
            result.addModel("lender", lender);
            result.addModel("account", account);
            String bankCardId = account.getBankCardId();
            int length = bankCardId.length();
            String cardTop = bankCardId.substring(0, 6);
            String cardLast = bankCardId.substring(length - 4, length);
            result.addModel("bankCardId", StringUtils.rightPad(cardTop, length - 4, "*") + cardLast);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("account detail error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }


    @Override
    public Result billStageList(LenderBillStageQuery query){
        Result result = new Result();
        try{
            QueryResult<LenderBillStageDO> qr = this.lenderBillStageManager.query(query);
            result.addModel("query",query);
            result.addModel("queryResult", qr);
            result.addModel("flowTypeEnum", LenderFlowTypeEnum.values());
            result.addModel("flowMap", LenderFlowTypeEnum.getMap());
            Collection<LenderBillStageDO> flows =  qr.getResultList();
            result.addModel("flows", flows);
            result.setSuccess(true);
            return result;
        }catch (Exception e){
            logger.error("query student account flow error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setSuccess(false);
            result.setMessage("系统繁忙，请稍后再试");
            return result;
        }
    }


}