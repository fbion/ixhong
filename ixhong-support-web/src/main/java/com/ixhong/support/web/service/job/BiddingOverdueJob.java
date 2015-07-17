package com.ixhong.support.web.service.job;

import com.mongodb.client.MongoCollection;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

/**
 * Description 标的预期处理
 * Created by duanxiangchao on 2015/6/3.
 */
public class BiddingOverdueJob {

    private Logger logger = Logger.getLogger(BiddingOverdueJob.class);

    @Autowired
    private MongoDBClient mongoDBClient;

    @Autowired
    private BiddingManager biddingManager;

    @Autowired
    private BiddingItemManager biddingItemManager;

    @Autowired
    private StudentBillStageManager studentBillStageManager;

    @Autowired
    private StudentManager studentManager;

    @Autowired
    private StudentAccountManager studentAccountManager;

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private LenderBillStageManager lenderBillStageManager;

    @Autowired
    private OrganizationAccountManager organizationAccountManager;

    @Autowired
    private OrganizationAccountFlowManager organizationAccountFlowManager;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private AccountFlowManager accountFlowManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final String PATTERN = "yyyy-MM-dd";

    /**
     * 标的逾期   任务调度
     */
    public void execute(){
        try {
            forNoPaid();
            forPending();
            overdue(BillStageStatusEnum.NO_PAID.code);
            overdue(BillStageStatusEnum.PENDING.code);
        }catch (Exception e){
            logger.error("BiddingOverdueJob error," + e.getMessage());
        }

    }


    /**
     * 将“NO_PAID”状态的bidding，如果逾期则调整is_overdue = 1
     */
    private void forNoPaid() {
        Date date = new Date();
        String quartzDate = DateFormatUtils.format(DateUtils.addDays(date, -1), PATTERN);//设置为昨天

        List<StudentBillStageDO> billStages = studentBillStageManager.getOverdueBillStage(BillStageStatusEnum.NO_PAID.code, null, 0,DateFormatUtils.format(date,PATTERN));
        for(StudentBillStageDO billStage : billStages) {
            billStage.setIsOverdue(1);
            billStage.setQuartzDate(quartzDate);
            try {
                this.studentBillStageManager.update(billStage);
            } catch (Exception e) {
                logger.error("BiddingOverdueJob,error:" + e);
            }
        }

    }

    /**
     * 将“PENDING”状态的bidding，如果逾期则调整is_overdue = 1
     */
    private void forPending() {
        Date date = new Date();
        String quartzDate = DateFormatUtils.format(DateUtils.addDays(date, -1), PATTERN);//设置为昨天

        List<StudentBillStageDO> billStages = studentBillStageManager.getOverdueBillStage(BillStageStatusEnum.PENDING.code, null, 0,DateFormatUtils.format(date,PATTERN));
        for(StudentBillStageDO billStage : billStages) {
            billStage.setIsOverdue(1);
            billStage.setQuartzDate(quartzDate);
            try {
                this.studentBillStageManager.update(billStage);
            } catch (Exception e) {
                logger.error("BiddingOverdueJob,error:" + e);
            }
        }
    }

    /**
     * 对逾期的bidding，计算罚金
     */
    private void overdue(int status) {
        Date date = new Date();
        String quartzDate = DateFormatUtils.format(date, PATTERN);//

        while (true) {
            List<StudentBillStageDO> billStages = studentBillStageManager.getOverdueBillStage(status, quartzDate, 1,quartzDate);

            if (CollectionUtils.isEmpty(billStages)) {
                break;
            }

            for (StudentBillStageDO billStage : billStages) {
                double amount = billStage.getAmount();
                billStage.setQuartzDate(quartzDate);
                //是否第一次逾期
                if(billStage.getOverdueDays() > 0) {
                    billStage.setOverdueDays(billStage.getOverdueDays() + 1);
                    billStage.setOverdueFee(CalculatorUtils.format(billStage.getOverdueFee() + amount * 0.5 / 100));
                    studentBillStageManager.update(billStage);
                }else {
                    //对于第一次逾期，计算罚金和垫付
                    billStage.setOverdueDays(1);
                    billStage.setOverdueFee(CalculatorUtils.format( amount * 0.5 / 100 ));
                    firstTimeOverdue(billStage);
                }
            }
        }
    }

    //今天就要逾期的   处理方法
    private void firstTimeOverdue(final StudentBillStageDO billStage) {

        try {
            transactionTemplate.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {

                    //查询学生账户余额是否能自动还款
                    Integer studentId = billStage.getStudentId();
                    double amount = billStage.getAmount();
                    double fee = billStage.getFee();
                    double totalAmount = CalculatorUtils.format(amount + fee);
                    StudentAccountDO studentAccount = studentAccountManager.getByStudentId(studentId);
                    double studentBalance = studentAccount.getBalance();
                    if(studentBalance >= totalAmount){
                        //学生余额大于当期改换
                    }else{

                    }

                    Integer organizationId = billStage.getOrganizationId();

                    OrganizationAccountDO organizationAccount = organizationAccountManager.getByOrganizationId(organizationId);
                    double bail = organizationAccount.getBail();//保证金



                    //逾期需要垫付
                    Integer biddingId = billStage.getBiddingId();
                    BiddingDO bidding = biddingManager.getById(biddingId);
                    String studentName = billStage.getStudentName();
                    int stage = billStage.getStage();



                    if (bail >= amount) {
                        //机构垫付
                        //机构扣除保证金
                        billStage.setAdvanceType(AdvanceTypeEnum.ORGANIZATION.code);
                        double bailBefore = bail;
                        double bailAfter = CalculatorUtils.format(bail - amount);
                        organizationAccount.setBail(bailAfter);
                        organizationAccountManager.updateBalance(organizationAccount);

                        //生成机构账户流水
                        OrganizationAccountFlowDO orgAccountFlow = new OrganizationAccountFlowDO();
                        orgAccountFlow.setOrganizationId(organizationId);
                        orgAccountFlow.setBiddingId(biddingId);
                        orgAccountFlow.setStudentId(studentId);
                        orgAccountFlow.setStudentName(studentName);
                        orgAccountFlow.setStudentPhone(bidding.getStudentPhone());
                        orgAccountFlow.setType(OrganizationFlowTypeEnum.ORGANIZATION_ADVANCE_AMOUNT.code);
                        orgAccountFlow.setAmount(amount);
                        double balance = organizationAccount.getBalance();

                        orgAccountFlow.setBalanceBefore(balance);
                        orgAccountFlow.setBalanceAfter(balance);
                        orgAccountFlow.setBailBefore(bailBefore);
                        orgAccountFlow.setBailAfter(bailAfter);
                        orgAccountFlow.setDescription("标的逾期，机构保证金垫付￥" + amount);
                        organizationAccountFlowManager.insert(orgAccountFlow);

                        try {
                            //将机构流水写入mongodb
                            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ORGANIZATION_ACCOUNT_FLOW);
                            Document document = new Document();
                            document.append("operation", OperationsLogTypeEnum.ORGANIZATION_ACCOUNT_FLOW.code);
                            document.append("organizationId", organizationId);
                            document.append("biddingId", biddingId);
                            document.append("studentId", bidding.getStudentId());
                            document.append("type", OrganizationFlowTypeEnum.ORGANIZATION_ADVANCE_AMOUNT.code);
                            document.append("amount", amount);
                            document.append("balanceBefore", balance);
                            document.append("balanceAfter", balance);
                            document.append("bailBefore", bailBefore);
                            document.append("bailAfter", bailBefore);
                            document.append("description", "标的逾期，机构保证金垫付￥" + amount);
                            document.append("created", System.currentTimeMillis());
                            dbCollection.insertOne(document);
                        } catch (Exception e) {
                            logger.error("write to mongodb error", e);
                        }
                    } else {

                        //学好贷垫付
                        billStage.setAdvanceType(AdvanceTypeEnum.PLATFORM.code);
                        AccountDO account = accountManager.get();
                        double balance = account.getBalance();
                        double balanceAfter = CalculatorUtils.format(balance - amount);
                        account.setBalance(balanceAfter);
                        accountManager.update(account);

                        AccountFlowDO accountFlow = new AccountFlowDO();
                        accountFlow.setBalanceAfter(balanceAfter);
                        accountFlow.setStudentId(studentId);
                        accountFlow.setStudentName(studentName);
                        accountFlow.setBiddingId(biddingId);
                        accountFlow.setStage(stage);
                        accountFlow.setAmount(amount);
                        accountFlow.setStatus(0);
                        accountFlow.setType(AdminFlowTypeEnum.ADMIN_ADVANCE_AMOUNT.code);
                        accountFlow.setStatus(FlowStatusEnum.SUCCESS.code);
                        accountFlow.setDescription("标的逾期，学好贷平台垫付￥" + amount);
                        accountFlowManager.insert(accountFlow);
                        //暂时不用写mongodb，@liuguanqing
                    }
                    //还款给投资人
                    repayToLender(billStage);

                    bidding.setRepaid(CalculatorUtils.format(bidding.getRepaid() + amount));
                    if(stage == billStage.getTotalStage()){
                        bidding.setStatus(BiddingStatusEnum.PAID_OFF.code);
                    }
                    biddingManager.updateForRepayment(bidding);
                    studentBillStageManager.update(billStage);
                    return true;
                }
            });
        } catch (Exception e) {
            logger.error("BiddingOverdueJob,error" + e);
        }

    }

    private void studentRepay(StudentBillStageDO billStage){

    }

    private void repayToLender(StudentBillStageDO billStage){
        int biddingId = billStage.getBiddingId();
        int stage = billStage.getStage();
        String studentName = billStage.getStudentName();
        String courseName = billStage.getCourseName();
        int totalStage = billStage.getTotalStage();
        //获取还款单对应的收款计划
        List<LenderBillStageDO> lenderBillList = lenderBillStageManager.get(biddingId, stage);
        //根据收款计划，还款
        for(LenderBillStageDO lenderBillStage:lenderBillList){
            Integer lenderId = lenderBillStage.getLenderId();
            LenderDO lender = lenderManager.getById(lenderId);
            double principal = lenderBillStage.getPrincipal();
            double currentInterest = lenderBillStage.getInterest();
            double amount = lenderBillStage.getAmount();
            LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lenderId);
            //账户余额
            double balanceBefore = lenderAccount.getBalance();
            double balanceAfter = CalculatorUtils.format(lenderBillStage.getAmount() + balanceBefore);

            double frozen = lenderAccount.getBalanceFrozen();
            //设置当前收益   加  本期利息
            lenderAccount.setCurrentInterest(lenderAccount.getCurrentInterest() + currentInterest);
            //设置投资中本金  减  本期收回的本金
            lenderAccount.setPrincipal(CalculatorUtils.format(lenderAccount.getPrincipal() - principal));
            lenderAccount.setBalance(balanceAfter);
            lenderAccountManager.updateBalance(lenderAccount);

            //更改收款计划的状态
            lenderBillStage.setStatus(BillStageStatusEnum.PAID.code);
            lenderBillStage.setRepaymentDate(new Date());
            lenderBillStageManager.update(lenderBillStage);

            //更改投资记录的状态
            BiddingItemDO biddingItem = biddingItemManager.getById(lenderBillStage.getBiddingItemId());
            biddingItem.setCurrentInterest(CalculatorUtils.format(biddingItem.getCurrentInterest() + currentInterest));
            biddingItem.setCurrentStage(stage);
            biddingItem.setRepaid(CalculatorUtils.format(biddingItem.getRepaid() + principal));
            biddingItem.setNextRepayDate(DateUtils.addMonths(lenderBillStage.getDeadline(), 1));
            if(stage == totalStage){
                //如果是最后一期，更改投资记录状态为还清
                biddingItem.setStatus(BiddingItemEnum.PAID_OFF.code);
                biddingItem.setNextRepayDate(null);
            }
            biddingItemManager.updateInTransaction(biddingItem);

            //生成投资人收款流水
            LenderAccountFlowDO accountFlow = new LenderAccountFlowDO();
            accountFlow.setLenderId(lenderId);
            accountFlow.setLenderName(lender.getName());
            accountFlow.setLenderRealName(lender.getRealName());
            accountFlow.setPhone(lender.getPhone());
            accountFlow.setAmount(amount);
            accountFlow.setBalanceBefore(balanceBefore);
            accountFlow.setBalanceAfter(balanceAfter);
            accountFlow.setFrozenBefore(frozen);
            accountFlow.setFrozenAfter(frozen);
            accountFlow.setStudentName(studentName);
            accountFlow.setCourseName(courseName);
            accountFlow.setCurrentStage(stage);
            accountFlow.setTotalStage(totalStage);
            accountFlow.setPrincipal(principal);
            accountFlow.setType(LenderFlowTypeEnum.RECEIVE.code);
            accountFlow.setStatus(FlowStatusEnum.SUCCESS.code);
            accountFlow.setBiddingId(biddingId);
            accountFlow.setDescription("标的收到还款￥" + amount + "元");
            lenderAccountFlowManager.insert(accountFlow);

            try {
                //投资人流水写入mongodb
                MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
                Document document = new Document();
                document.append("operation",OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
                document.append("lenderId",lenderId);
                document.append("lenderName",lender.getName());
                document.append("phone", lender.getPhone());
                document.append("amount",amount);
                document.append("balanceBefore",balanceBefore);
                document.append("balanceAfter",balanceAfter);
                document.append("frozenBefore",frozen);
                document.append("frozenAfter",frozen);
                document.append("studentName",studentName);
                document.append("courseName",courseName);
                document.append("currentStage",stage);
                document.append("totalStage",totalStage);
                document.append("principal",principal);
                document.append("type", LenderFlowTypeEnum.RECEIVE.code);
                document.append("biddingId",biddingId);
                document.append("description", "标的收到还款￥" + amount + "元");
                document.append("created", System.currentTimeMillis());
                dbCollection.insertOne(document);
            } catch (Exception e) {
                logger.error("write to mongodb error", e);
            }
        }
    }
}
