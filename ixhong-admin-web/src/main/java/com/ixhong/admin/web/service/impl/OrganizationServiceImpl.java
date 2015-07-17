package com.ixhong.admin.web.service.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ixhong.admin.web.service.OrganizationService;
import com.ixhong.common.mail.EmailSenderClient;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.yeepay.transfer.SingleTransferEntity;
import com.ixhong.common.yeepay.transfer.TransferQueryEntity;
import com.ixhong.common.yeepay.transfer.TransferUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.OperationsLogQuery;
import com.ixhong.domain.query.OrganizationQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by shenhongxi on 15/4/13.
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

    private Logger logger = Logger.getLogger(OrganizationService.class);

    private Logger flowLogger = Logger.getLogger("flow-data-logger");

    private Logger withdrawLogger = Logger.getLogger("withdraw-data-logger");

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    @Autowired
    private OrganizationManager organizationManager;

    @Autowired
    private OrganizationMemberManager organizationMemberManager;

    @Autowired
    private OrganizationRegistrationManager organizationRegistrationManager;

    @Autowired
    private OrganizationMiscManager organizationMiscManager;

    @Autowired
    private OrganizationAccountManager organizationAccountManager;

    @Autowired
    private YibaoCityManager yibaoCityManager;

    @Autowired
    private MongoDBClient mongoDBClient;

    @Autowired
    private EmailSenderClient emailSenderClient;

    @Autowired
    private OrganizationWithdrawManager organizationWithdrawManager;
    
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OrganizationAccountFlowManager organizationAccountFlowManager;

    @Override
    public Result list(OrganizationQuery query) {
        Result result = new Result();
        try {
            QueryResult<OrganizationDO> qr = this.organizationManager.query(query);
            Collection<OrganizationDO> organizations = qr.getResultList();
            for (OrganizationDO organization : organizations) {
                if (StringUtils.isNotBlank(organization.getBailPercentage())) {
                    organization.setBailPercentage(parseBailPercentage(organization.getBailPercentage()));
                }
            }
            result.addModel("query", query);
            result.addModel("queryResult", qr);
            result.addModel("organizations", organizations);
        } catch (Exception e) {
            logger.error("organization query error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result audit(OrganizationDO organization, OrganizationAccountDO account, String remoteIp) {
        Result result = new Result();
        try {
            OrganizationDO _organization = organizationManager.getById(organization.getId());
            if(_organization == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("该机构不存在");
                return result;
            }
            Integer _status = _organization.getStatus();
            if (_status == OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code ||
                    _status == OrganizationStatusEnum.MATERIAL_AUDITED_FAILED.code) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("该机已审核过，请勿重复审核");
                return result;
            }
            Integer status = organization.getStatus();
            if (status == OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code) {
                if (this.organizationManager.getByCode(organization.getCode()) != null) {
                    result.setResultCode(ResultCode.PARAMETER_ERROR);
                    result.setMessage("所填学校编号已存在，请重新输入");
                    return result;
                }
            }
            this.organizationManager.update(organization);
            if (status == OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code) {
                this.organizationAccountManager.update(account);
            }

            _organization.setCode(organization.getCode());
            _organization.setStatus(status);
            this.sendAuditEmail(_organization);

            try {
                //将操作日志写入mongodb
                MongoCollection<Document> dbCollection = this.mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                Document document = new Document();
                document.append("operation", OperationsLogTypeEnum.AUDIT_ORGANIZATION.code);
                document.append("name", _organization.getName()); //操作对象
                document.append("beforeStatus", _status);
                document.append("afterStatus", status);
                UserDO user = LoginContextHolder.getLoginUser();
                document.append("operator", user.getName());
                document.append("description", organization.getAuditNote());
                document.append("created", System.currentTimeMillis());
                document.append("remoteIp", remoteIp);
                dbCollection.insertOne(document);
            } catch (Exception e) {
                logger.error("write to mongodb error",e);
            }

            result.setResultCode(ResultCode.SUCCESS);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("audit organization error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result update(OrganizationDO _organization, OrganizationAccountDO _account, String remoteIp) {
        Result result = new Result();
        try {
            Integer organizationId = _organization.getId();
            OrganizationDO organization = organizationManager.getById(organizationId);
            if(organization == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("该机构不存在");
                return result;
            }

            organization.setCheckType(_organization.getCheckType());
            organization.setLevel(_organization.getLevel());
            this.organizationManager.update(organization);

            OrganizationAccountDO account = organizationAccountManager.getByOrganizationId(organizationId);
            String bailPercentage = account.getBailPercentage();
            account.setBailPercentage(_account.getBailPercentage());
            account.setLendingQuotas(_account.getLendingQuotas());
            this.organizationAccountManager.update(account);

            try {
                //将操作日志写入mongodb
                MongoCollection<Document> dbCollection = this.mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                Document document = new Document();
                document.append("operation", OperationsLogTypeEnum.UPDATE_BAIL_PERCENT.code);
                document.append("name", organization.getName()); //操作对象
                if (StringUtils.isNotBlank(bailPercentage)) {
                    document.append("beforeStatus", parseBailPercentage(bailPercentage));
                } else {
                    document.append("beforeStatus", "");
                }
                document.append("afterStatus", parseBailPercentage(account.getBailPercentage()));
                UserDO user = LoginContextHolder.getLoginUser();
                document.append("operator", user.getName());
                document.append("created", System.currentTimeMillis());
                document.append("remoteIp", remoteIp);
                dbCollection.insertOne(document);
            }catch (Exception e) {
                logger.error("write to mongodb error", e);
            }

            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("update bailPercent error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result detail(Integer organizationId) {
        Result result = new Result();
        try {
            OrganizationDO organization = this.organizationManager.getById(organizationId);
            if(organization == null) {
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("该机构不存在");
                return result;
            }
            OrganizationRegistrationDO registration = this.organizationRegistrationManager.getByOrganizationId(organizationId);
            OrganizationAccountDO account = this.organizationAccountManager.getByOrganizationId(organizationId);
            if (StringUtils.isNotBlank(account.getBailPercentage())) {
                account.setBailPercentage(parseBailPercentage(account.getBailPercentage()));
            }
            result.addModel("organization", organization);

            result.addModel("member", this.organizationMemberManager.getByOrganizationId(organizationId));
            result.addModel("registration", registration);
            result.addModel("misc", this.organizationMiscManager.getByOrganizationId(organizationId));
            result.addModel("account", account);

            String registrationAddress = registration.getAddress();
            JSONObject registrationAddressJSON = JSONObject.fromObject(registrationAddress);
            String registrationProvinceCode = registrationAddressJSON.get("province").toString();
            String registrationCityCode = registrationAddressJSON.get("city").toString();
            result.addModel("registrationProvince", this.getCityName("0", registrationProvinceCode));
            result.addModel("registrationCity", this.getCityName(registrationProvinceCode, registrationCityCode));
            result.addModel("specificAddress", registrationAddressJSON.get("specificAddress"));

            String bankAddress = account.getAddress();
            JSONObject bankAddressJSON = JSONObject.fromObject(bankAddress);
            String bankProvinceCode = bankAddressJSON.get("province").toString();
            String bankCityCode = bankAddressJSON.get("city").toString();
            result.addModel("bankProvince", this.getCityName("0", bankProvinceCode));
            result.addModel("bankCity", this.getCityName(bankProvinceCode, bankCityCode));

            String chain = registration.getChain();
            JSONObject chainJSON = JSONObject.fromObject(chain);
            Integer isChain = Integer.parseInt(chainJSON.get("isChain").toString());
            result.addModel("isChain", isChain);
            if (isChain == 1) {
                Integer branchAmount = Integer.parseInt(chainJSON.get("branchAmount").toString());
                Integer _type = Integer.parseInt(chainJSON.get("chainType").toString());
                String chainType = ChainTypeEnum.value(_type).meaning;
                result.addModel("branchAmount", branchAmount);
                result.addModel("chainType", chainType);
            }

            result.setResultCode(ResultCode.SUCCESS);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("query organization detail error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    private String getCityName(String parentId, String childId) {
        List<YibaoCityDO> childCities = this.yibaoCityManager.getByPid(parentId);
        for (YibaoCityDO childCity : childCities) {
            if (childId.equals(childCity.getCode())) {
                return childCity.getName();
            }
        }
        return "";
    }


    public Result auditOperationsLog(OperationsLogQuery query) {
        Result result = new Result();
        try{
            MongoCollection<Document> dbCollection = this.mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
            QueryResult<Document> qr = new QueryResult<Document>();
            qr.setQuery(query);

            Bson parentFilter = new BasicDBObject();
            List<Integer> operations = query.getOperations();
            if(operations != null) {
                parentFilter = Filters.in("operation", operations);
            }
            Date beginDate = query.getBeginDate();

            if(beginDate != null) {
                parentFilter = Filters.and(parentFilter,Filters.gte("created", beginDate.getTime()));
            }
            Date endDate = query.getEndDate();
            if(endDate != null) {
                parentFilter = Filters.and(parentFilter,Filters.lt("created", endDate.getTime()));
            }
            String name = query.getName();
            if(name != null) {
                parentFilter = Filters.and(parentFilter, Filters.eq("name", name));
            }
            String operator = query.getOperator();
            if(operator != null) {
                parentFilter = Filters.and(parentFilter, Filters.eq("operator", operator));
            }
            long amount = dbCollection.count(parentFilter);
            qr.setAmount((int)amount);
            if(amount == 0) {
                qr.setResultList(Collections.EMPTY_LIST);
                result.setSuccess(true);
                result.addModel("query", query);
                result.addModel("queryResult", qr);
                result.addModel("operations", qr.getResultList());
                return result;
            }

            int skip = query.getStartRow();
            List<Document> documents = new ArrayList<Document>();
            Iterator<Document> it = dbCollection.find(parentFilter).sort(new BasicDBObject("created", -1)).skip(skip).limit(12).iterator();
            while (it.hasNext()) {
                Document _document = it.next();
                String description = _document.getString("description");
                if(description != null) {
                    description = description.replaceAll("\n", " ");
                    _document.put("description", description);
                }
                documents.add(_document);
            }
            qr.setResultList(documents);
            result.addModel("query", query);
            result.addModel("queryResult", qr);
            result.addModel("operations", documents);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("audit operations query error,",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    private void sendAuditEmail(OrganizationDO organization) throws Exception {
        Integer status = organization.getStatus();

        StringBuilder sb = new StringBuilder(128);

        //sb.append("<div style='background:#fff;text-align:center;'>");
        sb.append("<div ><img src='" + Constants.EMAIL_LOGO + "'></div>");
        //sb.append("<div class='middle' style='background:url(http://stat.xuehaodai.com/ds/x8/5waaxqk5y8xvidllejvc9nyt0lojavys.png) no-repeat;width:600px;height:650px;margin:0 auto;position:relative;font-size:14px;'><div class='middle_center' style='position:absolute;left:0px;top:240px;'>" );
        //first
        sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:562px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>亲爱的<span class='font_color' style='color:#62b9df;'>"+organization.getName()+"</span>，您好！</p></div>");
        if (status == OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code) {
            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>恭喜您成功入驻学好贷，成为我们强有力的合作伙伴。</P>");
            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>您的机构代码是<span class='font_color' style='color:red;'>" + organization.getCode() + "</span>，该代码将成为借款人填写借款资料时的依据。</P>");
            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>现在，您可以<a href='http://jg.xuehaodai.com'>立即登录</a>去：</P>");

            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>1. 添加课程（包括名称、时长、价格）以供借款人选择；</P>");
            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>2. 添加教师账号（教师账号仅具备审批借款人的权限）；</P><br>");
        } else {
            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>感谢您注册学好贷。很遗憾，您提交的资料未能通过审核，学好贷工作人员将</P>");
            sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>会联系您进一步沟通。<br></P>");
        }

        sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:520px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>期待与您顺利合作，共同发展。</P>");
        //second
        sb.append("<div class='middle_center_w' style='margin-bottom:15px;width:562px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'>学好贷用户中心 敬启</p></div>");
        //third
        sb.append("<div class='middle_center_w' style='margin-bottom:35px;width:562px;'><p style='color:#605f5d;text-align:left;padding:0 0 0 40px;'><span style='color:#ADAAAA;'>此为自动发送邮件，请勿直接回复！如有任何疑问;请联系：</span><span style='color:#f87727;'>bd@xuehaodai.com</span></p></div>");
        sb.append("</div></div>");
        //sb.append("</div>");

        String content = sb.toString();
        //send email
        String title = "机构资料已通过审核";
        if (status != OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code) {
            title = "机构资料未通过审核";
        }
        emailSenderClient.sendHtmlEmail(organization.getEmail(), title, content);
    }

    @Override
    public Result auditWithdraw(final Integer id, final Integer status) {
        final Result result = new Result();
        try {
            OrganizationWithdrawDO organizationWithdraw = organizationWithdrawManager.getById(id);

            if(organizationWithdraw.getStatus() != OrganizationWithdrawStatusEnum.AUDITING.code) {
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("已审核，请勿重复审核");
                return result;
            }

            final Integer organizationId = organizationWithdraw.getOrganizationId();
            final OrganizationAccountDO account = organizationAccountManager.getByOrganizationId(organizationId);
            final Double withdraw = organizationWithdraw.getAmount();
            final Double fee = organizationWithdraw.getFee();
            final Double amount = CalculatorUtils.format(withdraw + fee);
            final Double balance = account.getBalance();

            if (status == OrganizationWithdrawStatusEnum.AUDITING_SUCCESS.code) {
                if (balance <= 0 || account.getBalanceFrozen() < 0 || amount.compareTo(balance) > 0) {
                    result.setResultCode(ResultCode.PARAMETER_ERROR);
                    result.setMessage("机构账户余额不足，无法提现（提现金额+手续费>余额）");
                    return result;
                }
            }


            final SingleTransferEntity entity = new SingleTransferEntity();
            final Long time = System.currentTimeMillis();
            final String _orderId = "JW" + organizationId + "-" + time;
            final String batchNo = DateFormatUtils.format(new Date(), DATE_FORMAT) + "0";
            entity.setBatchNo(batchNo);
            entity.setBankName(account.getBankName());
            entity.setOrderId(_orderId);
            entity.setBranchBankName(account.getBankBranch());
            entity.setAmount(new DecimalFormat("#.00").format(amount));
            entity.setAccountName(account.getName());
            entity.setAccountNumber(account.getAccountNumber());
            JSONObject address = JSONObject.fromObject(account.getAddress());
            entity.setBranchBankProvince(address.getString("province"));
            entity.setBranchBankCity(address.getString("city"));

            boolean isSuccess = transactionTemplate.execute(new TransactionCallback<Boolean>() {

                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {
                    try {
                        if (status == OrganizationWithdrawStatusEnum.AUDITING_FAILED.code){
                            organizationWithdrawManager.updateAuditStatus(id, status);
                            return true;
                        }

                        JSONObject responseJson = null;
                        try {
                            responseJson = TransferUtils.singleTransfer(entity);
                            withdrawLogger.error(responseJson.toString());
                        } catch (Exception e) {
                            logger.error("organization withdraw error, organizationId:" + organizationId, e);
                            result.setMessage("第三方支付系统繁忙，请稍候再试！");
                            throw new RuntimeException("organization withdraw error, organizationId:" + organizationId);
                        }

                        if (responseJson.containsKey("error")) {
                            result.setMessage(responseJson.getString("error"));
                            throw new RuntimeException(responseJson.getString("error"));
                        }

                        String returnCode = responseJson.getString("returnCode");
                        String transferCode = responseJson.getString("transferCode");
                        String bankStatus = responseJson.getString("bankStatus");
                        String errorMessage = responseJson.getString("errorMsg");

                        if (!returnCode.equals("1")) {
                            if (returnCode.equals("0034")) {
                                errorMessage = "打款批次号重复，请重新操作"; //并发操作时给的合理提示
                            }
                            result.setMessage(errorMessage);
                            throw new RuntimeException(errorMessage);
                        }

                        if (transferCode.equals("0025") || bankStatus.equals("I")) { //银行处理中

                            Double balanceAfter = CalculatorUtils.format(balance - amount);
                            Double frozenBefore = account.getBalanceFrozen();
                            Double frozenAfter = CalculatorUtils.format(frozenBefore + amount);
                            account.setBalance(balanceAfter);
                            account.setBalanceFrozen(frozenAfter);
                            organizationAccountManager.updateBalance(account);

                            //机构账户操作
                            OrganizationAccountFlowDO flow = new OrganizationAccountFlowDO();
                            flow.setWithdrawId(id);
                            flow.setOrderId(_orderId);
                            flow.setBatchNo(batchNo);
                            flow.setOrganizationId(organizationId);
                            flow.setAmount(amount);
                            flow.setWithdraw(withdraw);
                            flow.setFee(fee);
                            Double bail = account.getBail();
                            flow.setBailAfter(bail);
                            flow.setBailBefore(bail);
                            flow.setType(OrganizationFlowTypeEnum.WITHDRAW.code);
                            flow.setDescription("机构提现");
                            flow.setBalanceBefore(balance);
                            flow.setBalanceAfter(balanceAfter);
                            flow.setFrozenBefore(frozenBefore);
                            flow.setFrozenAfter(frozenAfter);
                            flow.setStatus(FlowStatusEnum.ONGOING.code);
                            organizationAccountFlowManager.insert(flow);

                            organizationWithdrawManager.updateAuditStatus(id, status);

                            buildFlowLog(flow, time);
                        }

                        //打款状态和银行处理状态
                        result.setMessage("操作成功，打款状态：" + TransferUtils.transferCodes.get(transferCode));
                        return true;
                    }catch (Exception e){
                        transactionStatus.setRollbackOnly();
                    }
                    return false;
                }
            });

            result.addModel("auditingSuccess", OrganizationWithdrawStatusEnum.AUDITING_SUCCESS.getCode());
            result.addModel("auditingFailed", OrganizationWithdrawStatusEnum.AUDITING_FAILED.getCode());
            result.setSuccess(isSuccess);
        } catch (Exception e) {
            logger.error("audit withdraw error",e);
            result.setMessage("系统繁忙，请稍后再试");
        }
        return result;
    }

    private void buildFlowLog(OrganizationAccountFlowDO flow,Long time){
        try {
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ORGANIZATION_ACCOUNT_FLOW);
            Document document = new Document();
            document.append("operation", OperationsLogTypeEnum.ORGANIZATION_ACCOUNT_FLOW.code);
            document.append("orderId", flow.getOrderId());
            document.append("organizationId", flow.getOrganizationId());
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
        json.put("organizationId", flow.getOrganizationId());
        json.put("orderId", flow.getOrderId());
        json.put("type", flow.getType());
        json.put("withdraw", flow.getWithdraw());
        json.put("fee", flow.getFee());
        json.put("amount", flow.getAmount());
        json.put("status", FlowStatusEnum.ONGOING.code);
        json.put("created", time);

        flowLogger.error(json.toString());
    }

    public static void main(String args[]){
        File file = new File("ixhong-admin-web/src/main/resources/10040003895.pfx");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getPath());
        InputStream is = null;
        try {

            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(is);
    }

    @Override
    public Result withdrawQuery(String orderId, String batchNo) {
        Result result = new Result();

        TransferQueryEntity entity = new TransferQueryEntity();
        entity.setOrderId(orderId);
        entity.setBatchNo(batchNo);
        entity.setPageNo(1);
        JSONObject response = null;
        try {
            response = TransferUtils.transferQuery(entity);
            result.addModel("transferCode", response.getString("transferCode"));
            result.addModel("bankStatus", response.getString("bankStatus"));
            result.setSuccess(true);
        } catch (Exception e) {
            result.setMessage("第三方支付系统繁忙，请重新操作");
            return result;
        }

        return result;
    }

    @Override
    public Result getAuditInfo(Integer organizationId) {
        Result result = new Result();
        try {
            OrganizationDO organization = this.organizationManager.getById(organizationId);
            result.addModel("organization", organization);
            OrganizationAccountDO account = this.organizationAccountManager.getByOrganizationId(organizationId);
            result.addModel("account", account);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.info("get audit info error");
            return result;
        }
        return result;
    }

    @Override
    public Result updateAccount(OrganizationAccountDO account) {
        Result result = new Result();
        try {
            organizationAccountManager.update(account);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.info("update account error");
            return result;
        }
        return result;
    }

    private String parseBailPercentage(String bailPercentage) {
        JSONObject json = JSONObject.fromObject(bailPercentage);
        return json.getDouble("undergraduate") + "," + json.getDouble("junior") + "," + json.getDouble("juniorLower");
    }
}
