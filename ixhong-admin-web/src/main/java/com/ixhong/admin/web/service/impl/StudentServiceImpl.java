package com.ixhong.admin.web.service.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ixhong.admin.web.service.StudentService;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.CertifyUtils;
import com.ixhong.common.utils.HttpUtils;
import com.ixhong.common.utils.IdCardUtils;
import com.ixhong.common.utils.MoneyFormatUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.misc.SystemConfig;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentBillStageQuery;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.*;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 * Created by cuichengrui on 2015/4/22.
 */
@Service
public class StudentServiceImpl implements StudentService {

    private Logger logger = Logger.getLogger(StudentService.class);

    @Autowired
    public BiddingManager biddingManager;

    @Autowired
    public TeacherManager teacherManager;

    @Autowired
    public StudentManager studentManager;

    @Autowired
    public StudentBasicManager studentBasicManager;

    @Autowired
    public StudentEducationManager studentEducationManager;

    @Autowired
    public StudentJobManager studentJobManager;

    @Autowired
    public StudentRelativesManager studentRelativesManager;

    @Autowired
    public StudentUsageManager studentUsageManager;

    @Autowired
    public CourseManager courseManager;

    @Autowired
    public MongoDBClient mongoDBClient;

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private InterestRateManager interestRateManager;

    @Autowired
    private YibaoCityManager yibaoCityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OrganizationAccountManager organizationAccountManager;

    @Autowired
    private OrganizationAccountFlowManager organizationAccountFlowManager;

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private LenderBillStageManager lenderBillStageManager;

    @Autowired
    private StudentBillStageManager studentBillStageManager;

    @Autowired
    private BiddingItemManager biddingItemManager;

    @Override
    public Result list(BiddingQuery query) {
        Result result = new Result();
        try {
            UserDO user = LoginContextHolder.getLoginUser();
            Integer organizationId = user.getOrganizationId();
            query.setOrganizationId(organizationId);
            QueryResult<BiddingDO> queryResult = this.biddingManager.query( query);

            result.addModel("query",query);
            result.addModel("queryResult",queryResult);
            result.addModel("statusEnums", BiddingStatusEnum.values());
            result.addModel("biddings", queryResult.getResultList());

            result.setResultCode(ResultCode.SUCCESS);
            result.setSuccess(true);
        } catch(Exception e) {
            logger.error("admin bidding audit list error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public Result auditBidding(Integer id, final Integer status, String auditNote) {
        Result result = new Result();
        try {
            final BiddingDO bidding = biddingManager.getById(id);
            UserDO user = LoginContextHolder.getLoginUser();
            //验证标的状态
            if (bidding.getStatus() != BiddingStatusEnum.ADMIN_AUDITING.code){
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("该标已审核过，请勿重新审核");
                return result;
            }

            //事务操作
            transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {

                    //若管理员发标审核操作为拒绝通过，则同时置标的为无效状态并且学生状态为资料审核不通过
                    if (status == BiddingStatusEnum.ADMIN_AUDITED_FAILURE.code) {
                        bidding.setEffective(0);
                        studentManager.updateStatus(bidding.getStudentId(), StudentStatusEnum.FAILURE.code);
                    }
                    //修改标的状态
                    bidding.setStatus(status);
                    bidding.setInvalidDate(DateUtils.addDays(new Date(), 5));
                    biddingManager.update(bidding);
                    if (status == BiddingStatusEnum.ADMIN_AUDITED_FAILURE.code) {
                        //发送短信告知学生借款失败
                        String content = "很遗憾，您的借款申请没有通过审核。【阳光E学】";
                        String phone = bidding.getStudentPhone();
                        try {
                            boolean isSuccess = smsSend(phone, content);
                            if (isSuccess) {
                                MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
                                Document document = new Document();
                                document.append("phone", phone);
                                document.append("type", "audit");
                                document.append("content", content);
                                document.append("created", System.currentTimeMillis());
                                dbCollection.insertOne(document);
                            }
                        } catch (Exception e) {
                            logger.error("auditing error:" + e);
                        }
                    }
                    return true;
                }
            });

            try {
                //将管理员发标审核的审核日志写入mongodb
                MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                Document document = new Document();
                document.append("operation",OperationsLogTypeEnum.BIDDING_ADMIN_AUDIT.code);
                document.append("biddingId",id);
                document.append("beforeStatus",BiddingStatusEnum.ADMIN_AUDITING.code);
                document.append("afterStatus",status);
                document.append("adminId",user.getId());
                document.append("adminName",user.getName());
                document.append("description",auditNote);
                document.append("created", System.currentTimeMillis());
                dbCollection.insertOne(document);
            } catch (Exception e) {
                logger.error("write to mongodb error", e);
            }
            result.setResultCode(ResultCode.SUCCESS);
            result.setSuccess(true);
        } catch(Exception e) {
            logger.error("admin bidding audit student error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public Result auditFullBidding(Integer id, final Integer status, String auditNote) {
        Result result = new Result();
        try {
            final BiddingDO bidding = biddingManager.getById(id);
            UserDO user = LoginContextHolder.getLoginUser();
            //验证标的状态
            if (bidding.getStatus() != BiddingStatusEnum.FULL_ADMIN_AUDITING.code){
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("该标已审核过，请勿重新审核");
                return result;
            }

            if(status == BiddingStatusEnum.FULL_ADMIN_AUDITED_FAILURE.code){
                auditFailure(bidding);
            }else{
                auditSuccess(bidding);
            }

            try {
                //将管理员满标审核的审核日志写入mongodb
                MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                Document document = new Document();
                document.append("operation",OperationsLogTypeEnum.BIDDING_FULL_ADMIN_AUDIT.code);
                document.append("biddingId",id);
                document.append("beforeStatus",BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
                document.append("afterStatus",status);
                document.append("adminId",user.getId());
                document.append("adminName",user.getName());
                document.append("description",auditNote);
                document.append("created", System.currentTimeMillis());
                dbCollection.insertOne(document);
            } catch (Exception e) {
                logger.error("write to mongodb error", e);
            }
            result.setResultCode(ResultCode.SUCCESS);
            result.setSuccess(true);
        } catch(Exception e) {
            logger.error("admin bidding full audit student error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    private void auditSuccess(final BiddingDO bidding) throws Exception {
        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {
                //根据biddingId取得投资列表
                int biddingId = bidding.getId();
                Double required = bidding.getRequired();
                List<BiddingItemDO> itemList = biddingItemManager.getByBiddingId(biddingId);
                Double totalInvestment = 0D;
                for(BiddingItemDO item: itemList) {
                    totalInvestment += item.getPrincipal();
                }
                //TODO 检查当前标的总投资的钱数与标的借款金额是否一致
                if(totalInvestment.compareTo(bidding.getRequired()) != 0) {
                    throw new RuntimeException("投资金额不符合，需要借款额:" + bidding.getRequired() + ",收到总投资额:" + totalInvestment);
                }

                //生成学生还款记录
                List<StudentBillStageDO> billStageList = buildAndSaveStudentStageList(bidding);

                //生成投资人lenderFlow记录
                buildAndSaveLenderFlow(itemList, billStageList, bidding);

                //满标审核通过，机构得到标款
                int organizationId = bidding.getOrganizationId();
                OrganizationAccountDO organizationAccount = organizationAccountManager.getByOrganizationId(organizationId);

                Double bailBefore = organizationAccount.getBail();
                Double balanceBefore = organizationAccount.getBalance();
                Double balanceAfter = CalculatorUtils.format(balanceBefore + required);

                //生成机构账户流水
                OrganizationAccountFlowDO orgAccountFlow = new OrganizationAccountFlowDO();
                orgAccountFlow.setOrganizationId(organizationId);
                orgAccountFlow.setBiddingId(biddingId);
                orgAccountFlow.setStudentId(bidding.getStudentId());
                orgAccountFlow.setStudentName(bidding.getStudentName());
                orgAccountFlow.setStudentPhone(bidding.getStudentPhone());
                orgAccountFlow.setType(OrganizationFlowTypeEnum.FULL_BIDDING_TRANSFER.code);
                orgAccountFlow.setAmount(totalInvestment);
                orgAccountFlow.setBalanceBefore(balanceBefore);
                orgAccountFlow.setBalanceAfter(balanceAfter);
                orgAccountFlow.setBailBefore(bailBefore);
                orgAccountFlow.setBailAfter(bailBefore);
                orgAccountFlow.setDescription("借款满标，收到标款￥" + totalInvestment);
                organizationAccountFlowManager.insert(orgAccountFlow);

                try {
                    //将机构流水写入mongodb
                    MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ORGANIZATION_ACCOUNT_FLOW);
                    Document document = new Document();
                    document.append("operation",OperationsLogTypeEnum.ORGANIZATION_ACCOUNT_FLOW.code);
                    document.append("organizationId",organizationId);
                    document.append("biddingId",biddingId);
                    document.append("studentId",bidding.getStudentId());
                    document.append("type", OrganizationFlowTypeEnum.FULL_BIDDING_TRANSFER.code);
                    document.append("amount",totalInvestment);
                    document.append("balanceBefore",balanceBefore);
                    document.append("balanceAfter", balanceAfter);
                    document.append("bailBefore",bailBefore);
                    document.append("bailAfter",bailBefore);
                    document.append("description", "借款满标，收到标款￥" + totalInvestment);
                    document.append("created", System.currentTimeMillis());
                    dbCollection.insertOne(document);
                } catch (Exception e) {
                    logger.error("write to mongodb error", e);
                }

                //扣除保证金
                Double bail = required * bidding.getBailPercentage() / 100.00d;
                balanceBefore = balanceAfter;
                balanceAfter = CalculatorUtils.format(balanceAfter - bail);
                Double bailAfter = CalculatorUtils.format(bailBefore + bail);

                //更改机构的账户余额和保证金余额
                organizationAccount.setBalance(balanceAfter);
                organizationAccount.setBail(bailAfter);
                organizationAccountManager.updateBalance(organizationAccount);

                //生成扣除保证金流水
                orgAccountFlow.setType(OrganizationFlowTypeEnum.BAIL.code);
                orgAccountFlow.setAmount(bail);
                orgAccountFlow.setBalanceBefore(balanceBefore);
                orgAccountFlow.setBalanceAfter(balanceAfter);
                orgAccountFlow.setBailBefore(bailBefore);
                orgAccountFlow.setBailAfter(bailAfter);
                orgAccountFlow.setDescription("扣除标的保证金￥" + bail);
                organizationAccountManager.update(organizationAccount);
                organizationAccountFlowManager.insert(orgAccountFlow);
                try {
                    //将机构流水写入mongodb
                    MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ORGANIZATION_ACCOUNT_FLOW);
                    Document document = new Document();
                    document.append("organizationId",organizationId);
                    document.append("biddingId",biddingId);
                    document.append("operation",OperationsLogTypeEnum.ORGANIZATION_ACCOUNT_FLOW.code);
                    document.append("type", OrganizationFlowTypeEnum.BAIL.code);
                    document.append("studentId",bidding.getStudentId());
                    document.append("amount",bail);
                    document.append("balanceBefore",balanceBefore);
                    document.append("balanceAfter", balanceAfter);
                    document.append("bail",bail);
                    document.append("bailBefore",bailBefore);
                    document.append("bailAfter",bailAfter);
                    document.append("description", "扣除标的保证金￥" + bail);
                    document.append("created", System.currentTimeMillis());
                    dbCollection.insertOne(document);
                } catch (Exception e) {
                    logger.error("write to mongodb error", e);
                }

                //更改学生状态为审核通过
                studentManager.updateStatus(bidding.getStudentId(), StudentStatusEnum.SUCCESS.code);
                //更改标的状态位还款中
                bidding.setStatus(BiddingStatusEnum.STAGING.code);
                biddingManager.update(bidding);
                //发送短信通知学生借款成功
                StudentBillStageDO _studentBill = billStageList.get(0);
                Date date = new Date();
                Date deadline = DateUtils.setHours(_studentBill.getDeadline(), 14);
                String content = "您成功借款"+required+"元并于"+DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss")+"向"+bidding.getOrganizationName()+"支付学费，您需要在"+
                        DateFormatUtils.format(deadline, "yyyy-MM-dd")+"前充值还款"+CalculatorUtils.format(_studentBill.getAmount()+_studentBill.getFee())+"元，请您按时还款【阳光E学】";
                String phone = bidding.getStudentPhone();
                try {
                    boolean isSuccess = smsSend(phone, content);
                    if(isSuccess){
                        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
                        Document document = new Document();
                        document.append("phone", phone);
                        document.append("type","audit");
                        document.append("content",content);
                        document.append("created", System.currentTimeMillis());
                        dbCollection.insertOne(document);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    //生成学生还款单
    private List<StudentBillStageDO> buildAndSaveStudentStageList(BiddingDO bidding) {
        List<StudentBillStageDO> billStageList = new ArrayList<StudentBillStageDO>();
        //仅还利息
        double required = bidding.getRequired();
        double rate = bidding.getRate();
        int monthLimit = bidding.getMonthLimit();
        int gracePeriod = bidding.getGracePeriod();
        double graceRate = bidding.getGraceRate();
        int studentId = bidding.getStudentId();
        int biddingId = bidding.getId();
        int organizationId = bidding.getOrganizationId();
        String courseName = bidding.getCourseName();
        String studentName = bidding.getStudentName();
        String phone = studentManager.getById(studentId).getPhone();
        if(gracePeriod > 0){
            //宽恕期内月服务费
            Double gracePeriodFee = CalculatorUtils.calculateGraceFee(required, graceRate);
            //宽恕期利息
            Double gracePeriodInterest = CalculatorUtils.gracePeriodInterest(required, rate);
            for(int i = 1; i <= gracePeriod; i++){
                StudentBillStageDO billStage = new StudentBillStageDO();
                billStage.setStudentId(studentId);
                billStage.setBiddingId(biddingId);
                billStage.setCourseName(courseName);
                billStage.setStudentName(studentName);
                billStage.setPhone(phone);
                billStage.setOrganizationId(organizationId);
                billStage.setOrganizationName(bidding.getOrganizationName());
                billStage.setFee(gracePeriodFee);
                if(i == 1){
                    billStage.setStatus(BillStageStatusEnum.PENDING.code);
                } else {
                    billStage.setStatus(BillStageStatusEnum.NO_PAID.code);
                }
                billStage.setStage(i);
                billStage.setTotalStage(monthLimit);
                billStage.setAmount(gracePeriodInterest);
                billStage.setPrincipal(0d);
                billStage.setInterest(gracePeriodInterest);
                billStage.setDeadline(DateUtils.addMonths(bidding.getRepayBeginDate(), i));
                billStageList.add(billStage);
            }
        }
        //等额本息
        int stage = gracePeriod;
        //todo
        List<Map.Entry<Double,Double>> repayList = CalculatorUtils.calculate(monthLimit - gracePeriod, required, rate);
        Double monthPay = CalculatorUtils.monthPay(monthLimit - gracePeriod, required, rate);
        //宽恕期外月服务费
        double feeRate = bidding.getFeeRate();
        Double fee = CalculatorUtils.calculateMonthFee(required,gracePeriod,monthLimit,graceRate,feeRate);

        for(Map.Entry<Double, Double> map: repayList){
            stage = stage + 1;
            StudentBillStageDO billStage = new StudentBillStageDO();
            billStage.setStudentId(studentId);
            billStage.setBiddingId(biddingId);
            billStage.setCourseName(courseName);
            billStage.setStudentName(studentName);
            billStage.setPhone(phone);
            billStage.setOrganizationId(organizationId);
            billStage.setOrganizationName(bidding.getOrganizationName());
            if(stage == monthLimit){
                //计算宽恕期外最后一月服务费
                fee = CalculatorUtils.format(CalculatorUtils.calculateLastMonthFee(required,feeRate,monthLimit,gracePeriod,graceRate));
            }
            billStage.setFee(fee);
            if(stage == 1){
                billStage.setStatus(BillStageStatusEnum.PENDING.code);
            }else{
                billStage.setStatus(BillStageStatusEnum.NO_PAID.code);
            }

            billStage.setStage(stage);
            billStage.setTotalStage(monthLimit);//TODO
            billStage.setAmount(monthPay);
            billStage.setPrincipal(map.getKey());
            billStage.setInterest(map.getValue());
            billStage.setDeadline(DateUtils.addMonths(bidding.getRepayBeginDate(), stage));
            billStageList.add(billStage);
        }
        //插入学生还款单
        studentBillStageManager.insertBatch(billStageList);
        return billStageList;
    }

    //生成投资人收款计划,投资人扣除冻结金额，生成投资流水
    private void buildAndSaveLenderFlow(List<BiddingItemDO> itemList,List<StudentBillStageDO> billStageList,BiddingDO bidding) {

        Integer biddingId = bidding.getId();
        double required = bidding.getRequired();
        String phone = bidding.getStudentPhone();
        String courseName = bidding.getCourseName();
        String studentName = bidding.getStudentName();
        int monthLimit = bidding.getMonthLimit();
        int studentId = bidding.getStudentId();

        Map<Integer,List<LenderBillStageDO>> lenderBillStages = new HashMap<Integer, List<LenderBillStageDO>>();
        for(StudentBillStageDO stage : billStageList) {
        /*
         * 遍历学生还款单
         * 根据学生的还款单，生成每个投资人的收款单
         */
            for(BiddingItemDO item : itemList) {//遍历投资人
                LenderBillStageDO billStage = new LenderBillStageDO();
                Integer lenderId = item.getLenderId();
                double percentage = item.getPrincipal() / required;
                billStage.setStudentId(studentId);
                billStage.setBiddingId(biddingId);
                billStage.setBiddingItemId(item.getId());
                billStage.setLenderId(lenderId);
                billStage.setPhone(phone);
                billStage.setStudentName(studentName);
                billStage.setCourseName(courseName);
                billStage.setStatus(stage.getStatus());
                billStage.setStage(stage.getStage());
                billStage.setTotalStage(monthLimit);
                double currentPrincipal = CalculatorUtils.format(stage.getPrincipal() * percentage);
                billStage.setPrincipal(currentPrincipal);
                double currentInterest = CalculatorUtils.format(stage.getInterest() * percentage);
                billStage.setInterest(currentInterest);
                billStage.setAmount(CalculatorUtils.format(currentInterest + currentPrincipal));
                billStage.setDeadline(stage.getDeadline());
                List<LenderBillStageDO> _list = lenderBillStages.get(lenderId);
                if(_list == null){
                    _list = new ArrayList<LenderBillStageDO>();
                    lenderBillStages.put(lenderId,_list);
                }
                _list.add(billStage);
            }
        }

        double interest = bidding.getInterest();
        //重置最后一次还款，保证每个人的总收益等于预计总收益
        for(BiddingItemDO item : itemList) {
            List<LenderBillStageDO> _stages = lenderBillStages.get(item.getLenderId());
            double totalPrinciple = 0;
            double totalInterest = 0;
            double totalAmount = 0;
            for(LenderBillStageDO _stage : _stages) {
                totalPrinciple += _stage.getPrincipal();
                totalInterest += _stage.getInterest();
                totalAmount += _stage.getAmount();
            }
            LenderBillStageDO last = _stages.get(_stages.size() - 1);
            last.setPrincipal(CalculatorUtils.format(last.getPrincipal() + item.getPrincipal() - totalPrinciple));
            last.setInterest(CalculatorUtils.format(last.getInterest() + item.getInterest() - totalInterest));
            last.setAmount(CalculatorUtils.format(last.getAmount() + item.getPrincipal() + item.getInterest() - totalAmount));
        }

        //插入收款计划
        Set<Integer> stageSet = lenderBillStages.keySet();
        for(Integer key: stageSet){
           List<LenderBillStageDO> _list = lenderBillStages.get(key);
           lenderBillStageManager.insertBatch(_list);
        }

        //投资人投资成功，扣除冻结金额，生成流水
        for(int i= 0; i<itemList.size(); i++){
            //遍历投资列表
            BiddingItemDO item = itemList.get(i);
            int lenderId = item.getLenderId();
            Double amount = item.getPrincipal();
            LenderDO lender = lenderManager.getById(lenderId);
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lenderId);
            Double frozenBefore = lenderAccount.getBalanceFrozen();
            Double frozenAfter = CalculatorUtils.format(frozenBefore - amount);
            Double totalInterest = lenderAccount.getTotalInterest();
            lenderAccount.setTotalInterest(CalculatorUtils.format(totalInterest + item.getInterest()));

            //扣除冻结金额
            lenderAccount.setBalanceFrozen(frozenAfter);
            lenderAccount.setPrincipal(CalculatorUtils.format(lenderAccount.getPrincipal() + amount));
            lenderAccountManager.updateBalance(lenderAccount);
            item.setStatus(BiddingItemEnum.REPAYING.code);
            item.setNextRepayDate(DateUtils.addMonths(bidding.getRepayBeginDate(), 1));
            biddingItemManager.updateInTransaction(item);

            //生成投资人流水
            LenderAccountFlowDO accountFlow = new LenderAccountFlowDO();
            accountFlow.setLenderId(lenderId);
            accountFlow.setLenderName(lender.getName());
            accountFlow.setLenderRealName(lender.getRealName());
            accountFlow.setPhone(lender.getPhone());
            accountFlow.setAmount(amount);
            accountFlow.setBalanceBefore(lenderAccount.getBalance());
            accountFlow.setBalanceAfter(lenderAccount.getBalance());
            accountFlow.setFrozenBefore(frozenBefore);
            accountFlow.setFrozenAfter(frozenAfter);
            accountFlow.setStudentName(item.getStudentName());
            accountFlow.setCourseName(item.getCourseName());
            accountFlow.setCurrentStage(1);
            accountFlow.setTotalStage(item.getTotalStage());
            accountFlow.setRate(item.getRate());
            accountFlow.setStatus(FlowStatusEnum.SUCCESS.code);
            accountFlow.setPrincipal(item.getPrincipal());
            accountFlow.setType(LenderFlowTypeEnum.BID_SUCCESS.code);
            accountFlow.setBiddingId(biddingId);
            accountFlow.setDescription("标的投资成功，扣除冻结金额￥" + amount + "元");
            lenderAccountFlowManager.insert(accountFlow);

            try {
                //投资人流水写入mongodb
                MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
                Document document = new Document();
                document.append("operation",OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
                document.append("lenderId",lenderId);
                document.append("lenderName",lender.getName());
                document.append("lenderRealname",lender.getRealName());
                document.append("phone", lender.getPhone());
                document.append("amonut",amount);
                document.append("balanceBefore",lenderAccount.getBalance());
                document.append("balanceAfter",lenderAccount.getBalance());
                document.append("frozenBefore",frozenBefore);
                document.append("frozenAfter",frozenAfter);
                document.append("studentName",item.getStudentName());
                document.append("courseName",item.getCourseName());
                document.append("currentStage",1);
                document.append("totalStage",item.getTotalStage());
                document.append("rate",item.getRate());
                document.append("principal",item.getPrincipal());
                document.append("type", LenderFlowTypeEnum.BID_SUCCESS.code);
                document.append("biddingId",biddingId);
                document.append("description", "标的投资成功，扣除冻结金额￥" + amount + "元");
                document.append("created", System.currentTimeMillis());
                dbCollection.insertOne(document);
            } catch (Exception e) {
                logger.error("write to mongodb error", e);
            }

        }
    }

    private void auditFailure(final BiddingDO bidding) throws Exception {
        //满标审核不通过
        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {
                Integer biddingId = bidding.getId();
                //投资人退款
                //根据biddingId取得投资列表
                List<BiddingItemDO> itemList = biddingItemManager.getByBiddingId(biddingId);

                for(BiddingItemDO item: itemList){
                    int lenderId = item.getLenderId();
                    Double amount = item.getPrincipal();
                    LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lenderId);
                    Double frozenBefore = lenderAccount.getBalanceFrozen();
                    Double frozenAfter = CalculatorUtils.format(lenderAccount.getBalanceFrozen() - amount);
                    Double balanceBefore = lenderAccount.getBalance();
                    Double balanceAfter = CalculatorUtils.format(balanceBefore  + amount);
                    lenderAccount.setBalanceFrozen(frozenAfter);
                    lenderAccount.setBalance(balanceAfter);
                    //扣除冻结金额
                    lenderAccountManager.updateBalance(lenderAccount);
                    //生成投资人流水
                    LenderDO lender = lenderManager.getById(lenderId);
                    LenderAccountFlowDO accountFlow = new LenderAccountFlowDO();
                    accountFlow.setLenderId(lenderId);
                    accountFlow.setLenderName(lender.getName());
                    accountFlow.setLenderRealName(lender.getRealName());
                    accountFlow.setPhone(lender.getPhone());
                    accountFlow.setAmount(amount);
                    accountFlow.setBalanceBefore(balanceBefore);
                    accountFlow.setBalanceAfter(balanceAfter);
                    accountFlow.setFrozenBefore(frozenBefore);
                    accountFlow.setFrozenAfter(frozenAfter);
                    accountFlow.setStudentName(item.getStudentName());
                    accountFlow.setCourseName(item.getCourseName());
                    accountFlow.setCurrentStage(1);
                    accountFlow.setTotalStage(item.getTotalStage());
                    accountFlow.setRate(item.getRate());
                    accountFlow.setPrincipal(item.getPrincipal());
                    accountFlow.setType(LenderFlowTypeEnum.REFUND.code);
                    accountFlow.setBiddingId(biddingId);
                    accountFlow.setStatus(FlowStatusEnum.SUCCESS.code);
                    accountFlow.setDescription("标的流标，冻结金额退还￥" + amount + "元");

                    lenderAccountFlowManager.insert(accountFlow);
                    try {
                        //投资人流水写入mongodb
                        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
                        Document document = new Document();

                        document.append("operation",OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
                        document.append("lenderId",lenderId);
                        document.append("lenderName",lender.getName());
                        document.append("lenderRealname",lender.getRealName());
                        document.append("phone", lender.getPhone());
                        document.append("amonut",amount);
                        document.append("balanceBefore",balanceBefore);
                        document.append("balanceAfter",balanceAfter);
                        document.append("frozenBefore",frozenBefore);
                        document.append("frozenAfter",frozenAfter);
                        document.append("studentName",item.getStudentName());
                        document.append("courseName",item.getCourseName());
                        document.append("currentStage",1);
                        document.append("totalStage",item.getTotalStage());
                        document.append("rate",item.getRate());
                        document.append("principal",item.getPrincipal());
                        document.append("type", LenderFlowTypeEnum.BID_SUCCESS.code);
                        document.append("biddingId",biddingId);
                        document.append("description", "标的流标，冻结金额退回￥" + amount + "元");
                        document.append("created", System.currentTimeMillis());
                        dbCollection.insertOne(document);
                    } catch (Exception e) {
                        logger.error("write to mongodb error", e);
                    }
                }
                //更改投资记录的状态为
                biddingItemManager.updateStatusByBiddingId(biddingId, BiddingItemEnum.BID_FAIL.code);
                //更改学生状态为审核不通过
                studentManager.updateStatus(bidding.getStudentId(), StudentStatusEnum.FAILURE.code);
                //更改标的状态
                bidding.setStatus(BiddingStatusEnum.FULL_ADMIN_AUDITED_FAILURE.code);
                bidding.setEffective(0);
                biddingManager.update(bidding);
                //发送短信告知学生借款失败
                String content = "很遗憾，您的借款申请没有通过审核。【阳光E学】";
                String phone = bidding.getStudentPhone();
                try {
                    boolean isSuccess = smsSend(phone, content);
                    if(isSuccess){
                        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_SMS_SEND_OPERATION_COLLECTION);
                        Document document = new Document();
                        document.append("phone", phone);
                        document.append("type","audit");
                        document.append("content",content);
                        document.append("created", System.currentTimeMillis());
                        dbCollection.insertOne(document);
                    }
                } catch (Exception e) {
                    logger.error("auditFailure,error:" + e);
                }
                return true;
            }
        });
    }

    @Override
    public Result detail(Integer biddingId, Integer studentId) {
        Result result = new Result();
        try {
            BiddingDO bidding = this.biddingManager.getById(biddingId);
            if (bidding == null){
                result.setResultCode(ResultCode.VALIDATE_FAILURE);
                result.setMessage("this bidding is not existed!");
                return result;
            }
            //页面所需的业务数据
            StudentDO student = this.studentManager.getById(studentId);
            StudentBasicDO studentBasic = this.studentBasicManager.getByStudentId(studentId);
            StudentEducationDO studentEducation = this.studentEducationManager.getByStudentId(studentId);
            StudentJobDO studentJob = this.studentJobManager.getByStudentId(studentId);
            StudentRelativesDO studentRelatives = this.studentRelativesManager.getByStudentId(studentId);
            StudentUsageDO studentUsage = this.studentUsageManager.getByStudentId(studentId);
            if (studentUsage != null){
                CourseDO course = this.courseManager.getById(studentUsage.getCourseId());
                result.addModel("course",course);
                //获取月供
                InterestRateDO interestRate = this.interestRateManager.getByMonthLimit(studentUsage.getMonthLimit());
                if(interestRate == null) {
                    result.setResultCode(ResultCode.PARAMETER_ERROR);
                    result.setMessage("参数错误");
                    return result;
                }
                double rate = interestRate.getRate();
                int monthLimit = studentUsage.getMonthLimit();
                int gracePeriod = studentUsage.getGracePeriod();
                int months = monthLimit - gracePeriod;
                Double required = studentUsage.getRequired();
                result.addModel("rate", rate);
                double graceRate = bidding.getGraceRate();
                double monthRate = bidding.getMonthRate();
                double gracePay = CalculatorUtils.gracePeriod(required, rate, graceRate);
                double monthFee = CalculatorUtils.calculateMonthFee(required, gracePeriod, monthLimit, graceRate, bidding.getFeeRate());
                double monthPay = CalculatorUtils.format(CalculatorUtils.monthPay(months, required, rate) + monthFee);
                //兼容老数据算法
                if (monthRate < 0) {
                    double oldFeeRate = (required <= 20000 ? FeeEnum.BELOW_2W.code : FeeEnum.ABOVE_2W.code) / 100;
                    double gracePeriodFee = CalculatorUtils.format(required * oldFeeRate * monthLimit / gracePeriod);
                    double interest = CalculatorUtils.gracePeriodInterest(required, rate);
                    gracePay = CalculatorUtils.format(gracePeriodFee + interest);
                    monthPay = CalculatorUtils.monthPay(months, required, rate);
                }

                result.addModel("gracePay", gracePay); //包括服务费
                result.addModel("monthPay", monthPay); //包括服务费
            }
            if (studentRelatives != null){
                JSON primaryRelatives = JSONObject.fromObject(studentRelatives.getPrimary());
                JSON minorRelatives = JSONObject.fromObject(studentRelatives.getMinor());
                JSON otherRelatives = JSONObject.fromObject(studentRelatives.getOther());
                result.addModel("primaryRelatives",primaryRelatives);
                result.addModel("minorRelatives",minorRelatives);
                result.addModel("otherRelatives",otherRelatives);
            }
            if (studentBasic != null){
                JSON residence = JSONObject.fromObject(studentBasic.getResidence());
                result.addModel("residence",residence);
                JSONObject basicAddress = JSONObject.fromObject(studentBasic.getAddress());
                result.addModel("basicAddress",basicAddress);
                //获取学生所在地址的省市
                String basicAddressProvinceCode = basicAddress.getString("province");
                String basicAddressCityCode = basicAddress.getString("city");
                result.addModel("studentAddressProvince", this.getCityName("0", basicAddressProvinceCode));
                result.addModel("studentAddressCity", this.getCityName(basicAddressProvinceCode, basicAddressCityCode));
            }

            result.addModel("student",student);
            result.addModel("basic",studentBasic);
            result.addModel("education",studentEducation);
            result.addModel("job",studentJob);
            result.addModel("usage",studentUsage);
            result.addModel("birthDate",IdCardUtils.getBirthByIdCard(student.getCardId()));

            //页面所需的图片路径
            result.addModel("imgHost", systemConfig.getImageHostName());
            //页面所需的枚举数据
            //basic
            result.addModel("genderEnum", GenderEnum.values());
            result.addModel("censusTypeEnum", CensusTypeEnum.values());
            result.addModel("childrenEnum", ChildNumberEnum.values());
            result.addModel("marryStatusEnum", MarryStatusEnum.values());
            result.addModel("houseTypeEnum", HouseTypeEnum.values());
            result.addModel("censusAddressEnum", CensusAddressEnum.values());
            //education
            result.addModel("degreeEnum", DegreeEnum.values());
            result.addModel("graduateStatusEnum", GraduateStatusEnum.values());
            //job
            result.addModel("workStatusEnum", WorkStatusEnum.values());
            //usage
            result.addModel("monthLimitEnum", MonthLimitEnum.values());
            result.addModel("gracePeriodEnum", GracePeriodEnum.values());
            //relatives
            result.addModel("relativesEnum", RelativesEnum.values());
            result.addModel("bidding", bidding);
            //获取标的审核日志
            try {
                if (biddingId != null) {
                    Bson parentFilter = Filters.and(Filters.eq("operation", OperationsLogTypeEnum.STUDENT_ORGANIZATION_AUDIT.code), Filters.eq("biddingId", biddingId));
                    MongoCollection<Document> collection = mongoDBClient.getCollection(Constants.MONGO_ORGANIZATION_OPERATION_COLLECTION);
                    Document document = collection.find(parentFilter).sort(new Document("created",-1)).limit(1).first();
                    result.addModel("organizationAuditNote", document.get("description") != null ? document.get("description") : "");

                    Bson adminFilter = Filters.and(Filters.eq("operation", OperationsLogTypeEnum.BIDDING_ADMIN_AUDIT.code), Filters.eq("biddingId", biddingId), Filters.eq("beforeStatus", BiddingStatusEnum.ADMIN_AUDITING.code));
                    collection = mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                    document = collection.find(adminFilter).sort(new Document("created", -1)).limit(1).first();
                    result.addModel("biddingAdminAuditNote", document.get("description") != null ? document.get("description") : "");
                } else {
                    result.addModel("organizationAuditNote", "");
                    result.addModel("biddingAdminAuditNote", "");
                }
            } catch (Exception e) {
                logger.error("read from mongodb error!");
            }

            result.setSuccess(true);
        } catch(Exception e) {
            logger.error("student audit detail error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    /**
     * 得到对应地址的省市名称
     * @param parentId
     * @param childId
     * @return
     */
    private String getCityName(String parentId, String childId) {
        List<YibaoCityDO> childCities = this.yibaoCityManager.getByPid(parentId);
        for (YibaoCityDO childCity : childCities) {
            if (childId.equals(childCity.getCode())) {
                return childCity.getName().trim();
            }
        }
        return "";
    }

    @Override
    public Result getById(Integer studentId) {
        Result result = new Result();
        try {
            result.addModel("student", this.studentManager.getById(studentId));
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("get student error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Override
    public Result certify(Integer id, String name, String cardId, String photoUrl) {
        Result result = new Result();
        try {
            String verifyResult = CertifyUtils.certify(name, cardId, photoUrl);
            if (verifyResult.equals(ResultCode.SYSTEM_ERROR.code)) {
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                result.setMessage("风控系统访问异常，请稍后再试");
                return result;
            } else {
                String[] results = verifyResult.split(",");
                result.addModel("_code", results[0]);
                if (results.length == 3) {
                    result.addModel("_score", results[2]);
                }
                if (results[0].equals("00") || results[0].equals("10")) {
                    this.studentManager.updateCertified(id, 1);
                }
                result.setSuccess(true);
            }
        } catch (Exception e) {
            logger.error("certify realName error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result billStageList(StudentBillStageQuery query) {
        Result result = new Result();
        try{
            QueryResult<StudentBillStageDO> qr = this.studentBillStageManager.query(query);
            result.addModel("query",query);
            result.addModel("queryResult",qr);
            result.addModel("billStages", qr.getResultList());
            result.addModel("statusEnums", BillStageStatusEnum.values());
            result.setSuccess(true);
        }catch(Exception e){
            logger.error("organization get student billStage error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result contractDetail(Integer biddingId) {
        Result result = new Result();
        try {
            BiddingDO bidding = biddingManager.getById(biddingId);
            if (bidding == null || (bidding.getStatus() != BiddingStatusEnum.FULL_ADMIN_AUDITING.code && bidding.getStatus() != BiddingStatusEnum.STAGING.code)){
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setMessage("标的不存在或状态不合法");
                return result;
            }
            StudentDO student = studentManager.getById(bidding.getStudentId());
            List<BiddingItemDO> biddingItems = biddingItemManager.getByBiddingId(biddingId);
            //借款合同所需数据
            result.addModel("bidding", bidding);
            result.addModel("student", student);
            //还款起止时间---合同上的借款开始时间暂定为满标时的时间
            Date begin = bidding.getRepayBeginDate();
            //协议编号
            result.addModel("beginDate", DateFormatUtils.format(begin, "yyyyMMdd"));
            //借款金额大写
            result.addModel("required", MoneyFormatUtils.getInstance().format(bidding.getRequired()));
            //出借人列表
            for(BiddingItemDO biddingItem : biddingItems){
                LenderDO lender = lenderManager.getById(biddingItem.getLenderId());
                biddingItem.setLenderRealName(lender.getRealName());
                String cardId = lender.getCardId();
                biddingItem.setLenderCardId(cardId == null ? "" : cardId.substring(0,6)+"********"+cardId.substring(14));
            }
            result.addModel("biddingItems", biddingItems);
            //还款账单列表
            List<StudentBillStageDO> bills = buildBill(bidding);
            result.addModel("bills", bills);
            result.addModel("repayBeginDate", DateFormatUtils.format(begin, Constants.DATE_PATTERN_CHINESE));
            result.addModel("repayEndDate", DateFormatUtils.format(bills.get(bills.size() - 1).getDeadline(), Constants.DATE_PATTERN_CHINESE));
            //获取协议生效日
            result.addModel("actionDay", DateUtils.getFragmentInDays(begin, Calendar.MONTH));

            //宽恕期的管理费//TODO 服务协议管理费表格处调整
            double feeRate = bidding.getFeeRate();
            if (bidding.getMonthRate() < 0){
                feeRate = (bidding.getRequired() <= 20000 ? FeeEnum.BELOW_2W.code : FeeEnum.ABOVE_2W.code);
            }
            double totalFee = CalculatorUtils.format(CalculatorUtils.calculateTotalFee(bidding.getRequired(), feeRate, bidding.getMonthLimit()));
            result.addModel("fee", totalFee);
            result.addModel("feeCapital", MoneyFormatUtils.getInstance().format(totalFee));

            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("get contract detail error,",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    /**
     * 得到学生还款账单
     * @param bidding
     */
    private List<StudentBillStageDO> buildBill(BiddingDO bidding) {

        List<StudentBillStageDO> billStageList = new ArrayList<StudentBillStageDO>();

        //借款总金额
        double required = bidding.getRequired();
        //利率
        double rate = bidding.getRate();
        //还款期限
        int monthLimit = bidding.getMonthLimit();
        //宽恕期
        int gracePeriod = bidding.getGracePeriod();
        double graceRate = bidding.getGraceRate();
        double monthRate = bidding.getMonthRate();
        double feeRate = bidding.getFeeRate();
        //计算出宽恕期内的月服务费
        Double gracePeriodFee = CalculatorUtils.format(CalculatorUtils.calculateGraceFee(required, graceRate));
        //计算出宽恕期外的月服务费
        Double monthPeriodFee = CalculatorUtils.format(CalculatorUtils.calculateMonthFee(required, gracePeriod, monthLimit, graceRate, feeRate));
        //计算出宽恕期外最后一期的服务费
        Double lastMonthFee = CalculatorUtils.format(CalculatorUtils.calculateLastMonthFee(required, feeRate, monthLimit, gracePeriod, graceRate));
        if (monthRate < 0) { //兼容老数据算法
            double oldFeeRate = (required <= 20000 ? FeeEnum.BELOW_2W.code : FeeEnum.ABOVE_2W.code) / 100;
            gracePeriodFee = CalculatorUtils.format(required * oldFeeRate * monthLimit / gracePeriod);
        }
        //计算出宽恕期的利息
        Double gracePeriodInterest = CalculatorUtils.gracePeriodInterest(required, rate);
        //满标时的时间暂作为合同里面的借款开始时间
        Date begin = bidding.getRepayBeginDate();

        //宽恕期还款账单
        StudentBillStageDO billStage = null;
        for(int i = 1; i <= gracePeriod; i++){
            billStage = new StudentBillStageDO();
            billStage.setStage(i);
            billStage.setDeadline(DateUtils.addMonths(begin, i));
            billStage.setRepayDeadline(DateFormatUtils.format(DateUtils.addMonths(begin, i), Constants.DATE_PATTERN[0]));
            billStage.setInterest(gracePeriodInterest);
            billStage.setPrincipal(0.00d);
            billStage.setFee(gracePeriodFee);
            billStage.setTotalAmount(CalculatorUtils.format(gracePeriodInterest+gracePeriodFee));
            billStageList.add(billStage);
        }

        int stage = gracePeriod;
        //计算出等额本息期需还本金(key)和利息(value)
        List<Map.Entry<Double, Double>> repayList = CalculatorUtils.calculate((monthLimit - gracePeriod), required, rate);

        //等额本息还款期账单
        for (Map.Entry<Double, Double> map: repayList) {
            stage = stage + 1;
            billStage = new StudentBillStageDO();
            billStage.setStage(stage);
            billStage.setDeadline(DateUtils.addMonths(begin, stage));
            billStage.setRepayDeadline(DateFormatUtils.format(DateUtils.addMonths(begin, stage), Constants.DATE_PATTERN[0]));
            billStage.setInterest(map.getValue());
            billStage.setPrincipal(map.getKey());
            double monthFee = graceRate < 0 ? 0.00d : ((stage == monthLimit) ? lastMonthFee : monthPeriodFee);
            billStage.setFee(monthFee);
            billStage.setTotalAmount(CalculatorUtils.format(map.getKey() + map.getValue() + monthFee));
            billStageList.add(billStage);
        }
        return billStageList;
    }

    @Override
    public Result biddingInvalidDelay(Integer id) {
        Result result = new Result();
        try{
            BiddingDO bidding = biddingManager.getById(id);
            if(bidding == null){
                result.setMessage("标的不存在，请属性页面！");
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                return result;
            }
            bidding.setInvalidDate(DateUtils.addDays(bidding.getInvalidDate(),3));
            biddingManager.update(bidding);
            result.setSuccess(true);
            result.setMessage("标的延期成功");
        }catch (Exception e){
            logger.error("bidding invalid delay error,",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    //发送短信
    private boolean smsSend(String phone,String content) throws Exception{
        Map<String, String> params = new HashMap<String, String>();
        params.put("sn", Constants.SMS_SEND_SN);
        params.put("pwd", Constants.SMS_SEND_PASSWORD);
        params.put("mobile", phone);
        params.put("content", content);
        params.put("msgfmt", "");
        String response = HttpUtils.httpGet(Constants.SMS_SEND_URL, params);
        return response.startsWith("-") ? false : true;
    }

}