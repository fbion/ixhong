package com.ixhong.lender.web.service.impl;

import com.mongodb.client.MongoCollection;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.IdCardUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.lender.web.service.BiddingService;
import com.ixhong.manager.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

/**
 * Created by jenny on 5/14/15.
 */
@Service
public class BiddingServiceImpl implements BiddingService {
    private Logger logger = Logger.getLogger(BiddingService.class);

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    public MongoDBClient mongoDBClient;

    @Autowired
    private StudentUsageManager studentUsageManager;

    @Autowired
    private StudentManager studentManager;


    @Autowired
    private StudentEducationManager studentEducationManager;

    @Autowired
    private StudentBasicManager studentBasicManager;

    @Autowired
    private YibaoCityManager yibaoCityManager;

    @Autowired
    private BiddingManager biddingManager;

    @Autowired
    private BiddingItemManager biddingItemManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ArticleManager articleManager;

    @Override
    public Result list() {
        Result result = new Result();
        try {
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(BiddingStatusEnum.ONGOING.code);
            statusList.add(BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
            statusList.add(BiddingStatusEnum.STAGING.code);

            List<ArticleDO> articles = articleManager.query(null, ArticleTypeEnum.NOTICE.code);
            result.addModel("article", articles.get(0));
            Collection<BiddingDO> biddingList = this.biddingManager.list(statusList);
            if (biddingList.size() > 0) {
                buildBiddingList(result, biddingList);
                result.setSuccess(true);
                return result;
            }
            result.setResultCode(ResultCode.PARAMETER_ERROR);
            result.setMessage("没找到对应的记录");
        } catch (Exception e) {
            logger.error("bidding list error:", e);
            result.setMessage("网络繁忙，请稍后再试");
        }
        return result;
    }

    private void buildBiddingList(Result result, Collection<BiddingDO> biddingList) {

        Map<String, Integer> basicMap = new HashMap<String, Integer>();
        Map<String, Integer> ageMap = new HashMap<String, Integer>();
        Map<String, String> titleMap = new HashMap<String, String>();
        Map<String, String> salaryMap = new HashMap<String, String>();

        List<Integer> studentIds = new ArrayList<Integer>();

        for (BiddingDO bidding : biddingList) {

            bidding.setStudentName(StringEscapeUtils.escapeHtml(bidding.getStudentName().substring(0, 1)) + "同学");
            String courseName = bidding.getCourseName();
            if (courseName.length() > 11) {
                courseName = courseName.substring(0,11);
                bidding.setCourseName(StringEscapeUtils.escapeHtml(courseName)+"...");
            }
            studentIds.add(bidding.getStudentId());
        }

        List<StudentBasicDO> studentBasics = studentBasicManager.getByStudentIds(studentIds);
        List<StudentUsageDO> studentUsages = studentUsageManager.getByStudentIds(studentIds);
        List<StudentDO> students = studentManager.getByStudentIds(studentIds);


        for (StudentUsageDO studentUsage : studentUsages) {
            String studentId = Integer.valueOf(studentUsage.getStudentId()).toString();
            String title = studentUsage.getExpectedTitle();
            if(title.length()>12){
                title = title.substring(0,12)+"...";
            }
            titleMap.put(studentId, title);
            salaryMap.put(studentId, studentUsage.getExpectedSalary());
        }

        for (StudentBasicDO studentBasic : studentBasics) {
            basicMap.put(studentBasic.getStudentId() + "", studentBasic.getGender());
        }

        for (StudentDO student : students) {
            Short year = IdCardUtils.getYearByIdCard(student.getCardId());
            Calendar calendar = Calendar.getInstance();
            Integer currentYear = calendar.get(Calendar.YEAR);
            ageMap.put(student.getId() + "", currentYear - year);
        }
        result.addModel("basicMap", basicMap);
        result.addModel("ageMap", ageMap);
        result.addModel("salaryMap", salaryMap);
        result.addModel("titleMap", titleMap);

        result.addModel("biddingList", biddingList);


    }

    @Override
    public Result query(BiddingQuery query) {
        Result result = new Result();
        try{
            QueryResult<BiddingDO> qr = this.biddingManager.query(query);
            Collection<BiddingDO> list = qr.getResultList();
            if(list!= null && list.size()>0){
                buildBiddingList(result,list);
            }
            result.addModel("query", query);
            result.addModel("queryResult", qr);
            result.addModel("biddingList", list);
            if(list.size()>0){
                result.addModel("listFlag",1);
            } else {
                result.addModel("listFlag",0);
            }

            result.setSuccess(true);
        } catch (Exception e){
            result.setSuccess(false);
            logger.error("Bidding query,error,",e);
            result.setMessage("网络繁忙，请稍后再试");
        }

        return result;
    }

    @Override
    public Result detail(Integer biddingId) {
        Result result = new Result();
        try{
            UserDO user = LoginContextHolder.getLoginUser();
            if(user != null){
                result.addModel("user", user);
                LenderAccountDO account = this.lenderAccountManager.getByLenderId(user.getId());
                if (account != null) {
                    result.addModel("balance", account.getBalance());
                }
            }

            BiddingDO bidding = this.biddingManager.getById(biddingId);
            if(bidding != null){
                result.addModel("bidding",bidding);
                Double required = bidding.getRequired();
                Double obtained = bidding.getObtained();
                if (obtained > required) {
                    obtained = required;
                }
                Double rate = bidding.getRate();
                int monthLimit = bidding.getMonthLimit();
                int gracePeriod = bidding.getGracePeriod();
                int months = monthLimit - gracePeriod;
                result.addModel("months", months);
                result.addModel("beginMonth", gracePeriod + 1);
                result.addModel("gracePeriodInterest", CalculatorUtils.gracePeriodInterest(required, rate));
                result.addModel("monthPay", CalculatorUtils.monthPay(months, required, rate));
                result.addModel("process", bidding.getProcess());
                result.addModel("notObtained", CalculatorUtils.format(required - obtained));

                int studentId = bidding.getStudentId();
                StudentDO student = this.studentManager.getById(studentId);

                StudentBasicDO studentBasic = this.studentBasicManager.getByStudentId(studentId);
                if (studentBasic != null) {
                    JSONObject address = JSONObject.fromObject(studentBasic.getAddress());
                    for (YibaoCityDO yibaoCity : this.yibaoCityManager.getByPid(address.getString("province"))) {
                        if (yibaoCity.getCode().equals(address.getString("city"))) {
                            result.addModel("city", yibaoCity.getName());
                            break;
                        }
                    }
                    result.addModel("gender" ,studentBasic.getGender());
                }

                if (student != null) {
                    result.addModel("age", IdCardUtils.getAgeByIdCard(student.getCardId()));
                }
                StudentEducationDO education = this.studentEducationManager.getByStudentId(studentId);
                if (education != null) {
                    result.addModel("degree", DegreeEnum.value(education.getDegree()).getMeaning());
                    result.addModel("graduateStatus", GraduateStatusEnum.value(education.getStatus()).getMeaning());
                }

                StudentUsageDO studentUsage = this.studentUsageManager.getByStudentId(studentId);
                if (studentUsage != null) {
                    result.addModel("usage", studentUsage);
                }

                List<BiddingItemDO> items = this.biddingItemManager.getByBiddingId(biddingId);
                result.addModel("items", items);
                double investSum = 0;
                for (BiddingItemDO item : items) {
                    investSum += item.getPrincipal();
                    String lenderName = this.lenderManager.getById(item.getLenderId()).getName();
                    String visible = lenderName.substring(0, lenderName.length() / 2);
                    String _lenderName = StringUtils.rightPad(visible, lenderName.length(), "*");;
                    if (user != null && item.getLenderId() == user.getId()) {
                        _lenderName = lenderName;
                    }
                    item.setLenderName(_lenderName);
                }
                result.addModel("investSum", CalculatorUtils.format(investSum));
                result.addModel("itemCount", items.size());

                result.addModel("billStages", buildStages(bidding));

                if (bidding.getStatus() == BiddingStatusEnum.ONGOING.code) {
                    Date invalidDate = bidding.getInvalidDate();
                    if (invalidDate.getTime() > System.currentTimeMillis()) {
                        result.addModel("deadTime", invalidDate);
                    }
                }
            }

            result.addModel("loanProtocalFile", Constants.LOAN_PROTOCOL_FILE);
            result.setSuccess(true);
        }catch (Exception e){
            logger.error("bidding detail error:", e);
            result.setMessage("Sorry, system busy, try it later!");
        }

        return result;
    }

    @Override
    public Result bidding(final Double money, final Integer biddingId, String dealPassword) {
        Result result = new Result();
        try{

            BiddingItemDO biddingItem = biddingItemManager.validateLenderBidding(biddingId, LoginContextHolder.getLoginUser().getId());
            if(biddingItem != null){
                result.setMessage("您已投过此标，请选择其他标的");
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setSuccess(false);
                return result;
            }

            final BiddingDO bidding = biddingManager.getById(biddingId);
            if(bidding.getStatus() != BiddingStatusEnum.ONGOING.code) {
                result.setMessage("已满标，无法继续投标");
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setSuccess(false);
                return result;
            }

            final Double obtained = bidding.getObtained();
            if(money > CalculatorUtils.format(bidding.getRequired() - obtained)){
                result.setMessage("投资金额超过可投金额");
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setSuccess(false);
                return result;
            }

            final Integer lenderId = LoginContextHolder.getLoginUser().getId();
            final LenderAccountDO account = lenderAccountManager.validateDealPassword(lenderId, dealPassword);
            if(account == null){
                result.setMessage("交易密码错误");
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setSuccess(false);
                return result;
            }

            if(money > account.getBalance()){
                result.setMessage("您的余额不足");
                result.setResultCode(ResultCode.PARAMETER_ERROR);
                result.setSuccess(false);
                return result;
            }

            transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {
                    //修改标的已获金额或标的状态
                    Double currentObtain = CalculatorUtils.format(obtained + money);
                    bidding.setObtained(currentObtain);
                    BiddingItemDO biddingItem = new BiddingItemDO();
                    if (currentObtain.compareTo(bidding.getRequired()) == 0) {
                        bidding.setStatus(BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
                        double interest = CalculatorUtils.format(bidding.getInterest() - biddingItemManager.getSumInterestByBiddingId(biddingId));
                        bidding.setProcess(100D);
                        biddingItem.setInterest(interest);
                        bidding.setRepayBeginDate(new Date());
                    } else {
                        double interest = CalculatorUtils.format(bidding.getInterest() * money / bidding.getRequired());
                        biddingItem.setInterest(interest);
                        bidding.setProcess(CalculatorUtils.format(currentObtain / bidding.getRequired() * 100));
                    }
                    biddingManager.update(bidding);


                    //冻结投资人金额
                    Double balanceBefore = account.getBalance();
                    Double balanceAfter = CalculatorUtils.format(balanceBefore - money);
                    Double frozenBefore = account.getBalanceFrozen();
                    Double frozenAfter = CalculatorUtils.format(frozenBefore + money);
                    account.setBalance(balanceAfter);
                    account.setBalanceFrozen(frozenAfter);
                    lenderAccountManager.updateBalance(account);

                    //添加投资人投资记录
                    LenderDO lender = lenderManager.getById(lenderId);
                    String lenderName = lender.getName();


                    biddingItem.setBiddingId(biddingId);
                    biddingItem.setLenderId(lenderId);
                    biddingItem.setLenderName(lenderName);
                    biddingItem.setPrincipal(money);
                    biddingItem.setCourseName(bidding.getCourseName());
                    biddingItem.setStudentId(bidding.getStudentId());
                    biddingItem.setStudentName(bidding.getStudentName());
                    biddingItem.setRate(bidding.getRate());
                    biddingItem.setTotalStage(bidding.getMonthLimit());
                    biddingItem.setStatus(BiddingItemEnum.BIDDING.code);
                    biddingItemManager.insert(biddingItem);

                    //生成投资人流水
                    LenderAccountFlowDO accountFlow = new LenderAccountFlowDO();
                    accountFlow.setLenderId(lenderId);
                    accountFlow.setLenderName(lenderName);
                    accountFlow.setLenderRealName(lender.getRealName());
                    accountFlow.setPhone(lender.getPhone());
                    accountFlow.setAmount(money);
                    accountFlow.setBalanceBefore(balanceBefore);
                    accountFlow.setBalanceAfter(balanceAfter);
                    accountFlow.setFrozenBefore(frozenBefore);
                    accountFlow.setFrozenAfter(frozenAfter);
                    accountFlow.setCourseName(bidding.getCourseName());
                    accountFlow.setStudentName(bidding.getStudentName());
                    accountFlow.setCurrentStage(1);
                    accountFlow.setTotalStage(bidding.getMonthLimit());
                    accountFlow.setRate(bidding.getRate());
                    accountFlow.setPrincipal(money);
                    accountFlow.setType(LenderFlowTypeEnum.FREEZE.code);
                    accountFlow.setBiddingId(biddingId);
                    accountFlow.setStatus(FlowStatusEnum.SUCCESS.code);
                    accountFlow.setDescription("投资"+"【"+bidding.getStudentName()+"_"+bidding.getCourseName()+"】,冻结资金"+money+"元");
                    lenderAccountFlowManager.insert(accountFlow);

                    try {
                        //将管理员发标审核的审核日志写入mongodb
                        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                        Document document = new Document();
                        document.append("operation", OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
                        document.append("lenderId", lenderId);
                        document.append("lenderName", lender.getName());
                        document.append("lenderRealName", lender.getRealName());
                        document.append("phone", lender.getPhone());
                        document.append("amount", money);
                        document.append("balanceBefore", balanceBefore);
                        document.append("balanceAfter", balanceAfter);
                        document.append("frozenBefore", frozenBefore);
                        document.append("frozenAfter", frozenAfter);
                        document.append("courseName", bidding.getCourseName());
                        document.append("studentName", bidding.getStudentName());
                        document.append("currentStage", 1);
                        document.append("totalStage", bidding.getMonthLimit());
                        document.append("rate", bidding.getRate());
                        document.append("type", LenderFlowTypeEnum.FREEZE.code);
                        document.append("biddingId", biddingId);
                        document.append("status",FlowStatusEnum.SUCCESS.code);
                        document.append("description", "投资"+"【"+bidding.getStudentName()+"_"+bidding.getCourseName()+"】,冻结资金"+money+"元");
                        document.append("created", System.currentTimeMillis());
                        dbCollection.insertOne(document);
                    } catch (Exception e) {
                        logger.error("write to mongodb error", e);
                    }
                    return true;
                }
            });

        }catch(Exception e){
            result.setSuccess(false);
            logger.error("Bidding error,",e);
            result.setMessage("网络异常，请稍后再试");
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * 生成还款账单（不包括管理费）
     * @param bidding
     * @return
     */
    private List<StudentBillStageDO> buildStages(BiddingDO bidding) {
        List<StudentBillStageDO> billStages = new ArrayList<StudentBillStageDO>();
        //仅还利息
        double required = bidding.getRequired();
        double rate = bidding.getRate();
        int monthLimit = bidding.getMonthLimit();
        int gracePeriod = bidding.getGracePeriod();
        Double gracePeriodInterest = CalculatorUtils.gracePeriodInterest(required, rate);
        for(int i = 1; i <= gracePeriod; i++){
            StudentBillStageDO billStage = new StudentBillStageDO();
            billStage.setStage(i);
            billStage.setPrincipal(0d);
            billStage.setInterest(gracePeriodInterest);
            billStage.setDeadline(DateUtils.addMonths(new Date(), i));
            billStages.add(billStage);
        }

        //等额本息
        int stage = gracePeriod;
        List<Map.Entry<Double,Double>> repays = CalculatorUtils.calculate(monthLimit - gracePeriod, required, rate);
        for(Map.Entry<Double, Double> map: repays){
            stage = stage + 1;
            StudentBillStageDO billStage = new StudentBillStageDO();
            billStage.setStage(stage);
            billStage.setPrincipal(map.getKey());
            billStage.setInterest(map.getValue());
            billStage.setDeadline(DateUtils.addMonths(new Date(),stage));
            billStages.add(billStage);
        }
        return billStages;
    }


}
