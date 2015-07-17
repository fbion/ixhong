package com.ixhong.support.web.service.job;

import com.mongodb.client.MongoCollection;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.*;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 *  检测标的是否到期而未投满，则置为流标
 * Created by duanxiangchao on 2015/5/30.
 */
public class BiddingInvalidJob{

    private Logger logger = Logger.getLogger(BiddingOverdueJob.class);

    @Autowired
    private BiddingManager biddingManager;

    @Autowired
    private BiddingItemManager biddingItemManager;

    @Autowired
    private StudentManager studentManager;

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private MongoDBClient mongoDBClient;

    @Autowired
    private TransactionTemplate transactionTemplate;


    /**
     * 标的到期没有投满而流标   任务调度
     */
    public void execute() {
        try{
            List<BiddingDO> biddingList = biddingManager.getInvalidBidding();
            for(BiddingDO bidding: biddingList){
                try {
                    balanceOperation(bidding);
                }catch (Exception e){
                    throw new RuntimeException("BiddingInvalidJob error");
                }
            }
        }catch (Exception e){
            logger.error("BiddingInvalidJob error," + e.getMessage());
        }


    }


    private void balanceOperation(final BiddingDO bidding) {
        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus status) {
                //投资人退款
                int biddingId = bidding.getId();
                List<BiddingItemDO> biddingItems = biddingItemManager.getByBiddingId(biddingId);
                for(BiddingItemDO item: biddingItems){
                    int lenderId = item.getLenderId();
                    Double amount = item.getPrincipal();
                    LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(lenderId);
                    //返还冻结金额
                    Double frozenBefore = lenderAccount.getBalanceFrozen();
                    Double frozenAfter = CalculatorUtils.format(frozenBefore - amount);
                    Double balanceBefore = lenderAccount.getBalance();
                    Double balanceAfter = CalculatorUtils.format(balanceBefore  + amount);
                    lenderAccount.setBalanceFrozen(frozenAfter);
                    lenderAccount.setBalance(balanceAfter);
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
                    accountFlow.setDescription("流标，冻结金额退还￥" + amount + "元");
                    lenderAccountFlowManager.insert(accountFlow);
                    try {
                        //投资人流水写入mongodb
                        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
                        Document document = new Document();
                        document.append("operation", OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
                        document.append("lenderId",lenderId);
                        document.append("lenderName",lender.getName());
                        document.append("phone", lender.getPhone());
                        document.append("amount",amount);
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
                //更改投资记录的状态为流标
                biddingItemManager.updateStatusByBiddingId(biddingId, BiddingItemEnum.BID_FAIL.code);
                //更改学生状态为可申请贷款
                studentManager.updateStatus(bidding.getStudentId(), StudentStatusEnum.INIT.code);

                //更改标的状态为流标
                bidding.setStatus(BiddingStatusEnum.FAILURE.code);
                bidding.setEffective(0);
                biddingManager.update(bidding);


                return true;
            }
        });

    }

}
