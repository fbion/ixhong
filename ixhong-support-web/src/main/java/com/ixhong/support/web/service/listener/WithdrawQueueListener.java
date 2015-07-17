package com.ixhong.support.web.service.listener;

import com.mongodb.client.MongoCollection;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.yeepay.transfer.BankStatusEnum;
import com.ixhong.common.yeepay.transfer.TransferCodeEnum;
import com.ixhong.common.yeepay.transfer.TransferQueryEntity;
import com.ixhong.common.yeepay.transfer.TransferUtils;
import com.ixhong.common.yeepay.tzt.TZTUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.FlowStatusEnum;
import com.ixhong.domain.enums.OperationsLogTypeEnum;
import com.ixhong.domain.enums.OrganizationWithdrawStatusEnum;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Iterator;

/**
 * Created by liuguanqing on 15/5/18.
 */
@Service
public class WithdrawQueueListener implements MessageListener {

    private Logger flowLogger = Logger.getLogger("flow-data-logger");

    private Logger lenderWithdrawLogger = Logger.getLogger("lender-withdraw-data-logger");

    private Logger organizationWithdrawLogger = Logger.getLogger("organization-withdraw-data-logger");

    private Logger logger = Logger.getLogger(WithdrawQueueListener.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private OrganizationAccountFlowManager organizationAccountFlowManager;

    @Autowired
    private OrganizationAccountManager organizationAccountManager;

    @Autowired
    private MongoDBClient mongoDBClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AccountFlowManager accountFlowManager;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private OrganizationWithdrawManager organizationWithdrawManager;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage)message;

        try {
            JSONObject json = JSONObject.fromObject(textMessage.getText());
            if(!json.has("flowId")) {
                return;
            }
            int flowId = json.getInt("flowId");
            String from = json.getString("from");
            if (json.has("lenderId")) {
                int lenderId = json.getInt("lenderId");
                this.lenderWithdraw(flowId, lenderId, from);
            } else if (json.has("organizationId")) {
                int organizationId = json.getInt("organizationId");
                this.organizationWithdraw(flowId, organizationId, from);
            }
            //执行成功后，确认消息
            message.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 投资人提现
     * @param flowId
     * @param lenderId
     * @param from
     */
    private void lenderWithdraw(final int flowId,final int lenderId,final String from) throws Exception{

        final LenderAccountFlowDO flow = lenderAccountFlowManager.getById(flowId);
        if (flow == null){
            return;
        }

        if (lenderId != flow.getLenderId()){
            return;
        }

        Integer flowStatus = flow.getStatus();
        if (flowStatus == FlowStatusEnum.SUCCESS.code || flowStatus == FlowStatusEnum.FAILURE.code){
            return;
        }

        //事务操作
        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {
                try {
                    String orderId = flow.getOrderId();
                    JSONObject response = TZTUtils.queryWithdraw(orderId);
                    lenderWithdrawLogger.error(response.toString());
                    if (response.containsKey("error_code")){
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code",response.getString("error_code"));
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message", response.getString("error_msg"));
                        flowLogger.error(json.toString());
                        return false;
                    }

                    if (response.containsKey("clientSignError")){
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code","clientSignError");
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message",response.getString("clientSignError"));
                        flowLogger.error(json.toString());
                        return false;
                    }

                    //易宝返回的商家订单号
                    String requestId = response.getString("requestid").trim();
                    if (StringUtils.isBlank(requestId) || !orderId.equals(requestId)) {
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code","inconsistent");
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message","返回订单号不一致：" + requestId);
                        flowLogger.error(json.toString());
                        return false;
                    }

                    //提现状态：DOING：处理中; FAILURE：提现失败; REFUND：提现退回; SUCCESS：提现成功; UNKNOW：未知;
                    String status = response.getString("status");
                    if (status.equals("DOING") || status.equals("UNKNOW")){
                        return false;
                    }

                    LenderAccountDO account = lenderAccountManager.getByLenderId(lenderId);
                    Double amount = flow.getAmount();
                    Double frozenAfter = CalculatorUtils.format(account.getBalanceFrozen() - amount);
                    if (status.equals("FAILURE") || status.equals("REFUND")){
                        //修改投资人账户
                        Double balanceAfter = CalculatorUtils.format(account.getBalance() + amount);
                        account.setBalance(balanceAfter);
                        account.setBalanceFrozen(frozenAfter);
                        lenderAccountManager.updateBalance(account);

                        //将流水状态设为提现失败
                        flow.setStatus(FlowStatusEnum.FAILURE.code);
                        flow.setDescription("提现失败，已退回账户");
                        lenderAccountFlowManager.update(flow);

                    } else if (status.equals("SUCCESS")){
                        //修改投资人账户
                        account.setBalanceFrozen(frozenAfter);
                        lenderAccountManager.updateBalance(account);

                        //将流水状态设为提现成功
                        flow.setStatus(FlowStatusEnum.SUCCESS.code);
                        lenderAccountFlowManager.update(flow);
                    }

                    //构建投资人提现账户流水日志
                    buildLenderWithdrawFlowLog(flow, from);

                } catch (Exception e) {
                    logger.error("query lender withdraw error." + e.getMessage());
                    throw new RuntimeException("query lender withdraw error.", e);
                }

                return true;
            }
        });

        return;
    }

    /**
     * 构建投资人提现账户流水日志
     * @param flow
     * @param from
     */
    private void buildLenderWithdrawFlowLog(LenderAccountFlowDO flow,String from){
        Long time = System.currentTimeMillis();
        //mongodb里面写入一条记录
        try {
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
            Document document = new Document();
            document.append("operation", OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
            document.append("flowId",flow.getId());
            document.append("lenderId", flow.getLenderId());
            document.append("orderId", flow.getOrderId());
            document.append("type", flow.getType());
            document.append("amount", flow.getAmount());
            document.append("fee", flow.getFee());
            document.append("withdraw", flow.getWithdraw());
            document.append("status", flow.getStatus());
            document.append("description", flow.getDescription());
            document.append("created", time);
            dbCollection.insertOne(document);
        } catch (Exception e) {
            logger.error("write to mongodb error", e);
        }

        //log4j自定义日志文件里面写入一条记录
        JSONObject json = new JSONObject();
        json.put("flowId",flow.getId());
        json.put("lenderId",flow.getLenderId());
        json.put("type",flow.getType());
        json.put("orderId",flow.getOrderId());
        json.put("amount",flow.getAmount());
        json.put("withdraw",flow.getWithdraw());
        json.put("fee",flow.getFee());
        json.put("from",from);
        json.put("listener",true);
        json.put("status",flow.getStatus());
        json.put("created", time);

        flowLogger.error(json.toString());
    }

    /**
     * 构建学好贷账户流水和mongodb
     */
    private AccountFlowDO buildAdminAccountFlow(LenderAccountFlowDO flow,Double amount,Integer type,String description){
        AccountDO adminAccount = accountManager.get();
        Double balanceAfter = CalculatorUtils.format(adminAccount.getBalance() + amount);
        adminAccount.setBalance(balanceAfter);
        accountManager.update(adminAccount);
        AccountFlowDO adminAccountFlow = new AccountFlowDO();
        adminAccountFlow.setBalanceAfter(balanceAfter);
        adminAccountFlow.setLenderId(flow.getLenderId());
        adminAccountFlow.setLenderName(flow.getLenderName());
        adminAccountFlow.setBiddingId(flow.getBiddingId());
        adminAccountFlow.setAmount(amount);
        adminAccountFlow.setType(type);
        adminAccountFlow.setStatus(flow.getStatus());
        adminAccountFlow.setDescription(description);
        accountFlowManager.insert(adminAccountFlow);
        return adminAccountFlow;
    }


    /**
     * 机构提现
     * @param from
     */
    private void organizationWithdraw(final int flowId,final int organizationId,final String from){

        final OrganizationAccountFlowDO flow = organizationAccountFlowManager.getById(flowId);
        if (flow == null){
            return;
        }

        if (organizationId != flow.getOrganizationId()){
            return;
        }

        Integer flowStatus = flow.getStatus();
        if (flowStatus == FlowStatusEnum.SUCCESS.code || flowStatus == FlowStatusEnum.FAILURE.code){
            return;
        }

        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {
                try {
                    TransferQueryEntity entity = new TransferQueryEntity();
                    String batchNo = flow.getBatchNo();
                    String orderId = flow.getOrderId();
                    entity.setBatchNo(batchNo);
                    entity.setOrderId(orderId);
                    entity.setPageNo(1);
                    JSONObject response = TransferUtils.transferQuery(entity);
                    organizationWithdrawLogger.error(response.toString());

                    if (response.containsKey("error")){
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("batchNo",batchNo);
                        json.put("message",response.getString("error"));
                        json.put("listener",true);
                        json.put("from",from);
                        flowLogger.error(json.toString());
                        return false;
                    }

                    if (!response.getString("returnCode").equals("1")) {
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("batchNo",batchNo);
                        json.put("message",response.getString("errorMsg"));
                        json.put("listener",true);
                        json.put("from",from);
                        flowLogger.error(json.toString());
                        return false;
                    }

                    JSONArray items = response.getJSONArray("items");
                    Iterator<JSONObject> iterator = items.iterator();
                    while (iterator.hasNext()) {
                        JSONObject item = iterator.next();

                        //打款返回状态
                        String transferCode = item.getString("transferCode");
                        String bankStatus = item.getString("bankStatus");
                        if (transferCode.equals(TransferCodeEnum.RECEIVED.code) || transferCode.equals(TransferCodeEnum.CHECKING.code)
                                || transferCode.equals(TransferCodeEnum.UNKNOWN.code)
                                || (transferCode.equals(TransferCodeEnum.REMITTED.code) && bankStatus.equals(BankStatusEnum.ONGOING.code))){
                            return false;
                        }

                        String itemOrderId = item.getString("orderId").trim();
                        if (!orderId.equals(itemOrderId)){
                            JSONObject json = new JSONObject();
                            json.put("flowId",flowId);
                            json.put("orderId",orderId);
                            json.put("batchNo",batchNo);
                            json.put("message","返回订单号不一致：" + itemOrderId);
                            json.put("listener",true);
                            json.put("from",from);
                            flowLogger.error(json.toString());
                            break;
                        }

                        OrganizationAccountDO account = organizationAccountManager.getByOrganizationId(flow.getOrganizationId());
                        Integer withdrawId = flow.getWithdrawId();
                        Double amount = flow.getAmount();
                        double frozenAfter = CalculatorUtils.format(account.getBalanceFrozen() - amount);

                        if (transferCode.equals(TransferCodeEnum.REMITTED.code) && bankStatus.equals(BankStatusEnum.SUCCESS.code)){
                            //修改机构账户余额及冻结余额
                            account.setBalanceFrozen(frozenAfter);
                            organizationAccountManager.updateBalance(account);

                            //修改流水状态
                            flow.setStatus(FlowStatusEnum.SUCCESS.code);
                            organizationAccountFlowManager.update(flow.getId(), flow.getStatus());

                            //修改提现申请的状态
                            organizationWithdrawManager.updateAuditStatus(withdrawId, OrganizationWithdrawStatusEnum.SUCCESS.code);

                            buildOrganizationWithdrawFlowLog(flow, from);

                        } else if (
                                transferCode.equals(TransferCodeEnum.REJECTED.code) ||
                                transferCode.equals(TransferCodeEnum.REFUND.code) ||
                                (transferCode.equals(TransferCodeEnum.REMITTED.code) && bankStatus.equals(BankStatusEnum.FAILURE.code))) {//已拒绝或者已退款，业务上都按失败处理

                            //修改机构账户余额及冻结余额
                            account.setBalance(CalculatorUtils.format(account.getBalance() + amount));
                            account.setBalanceFrozen(frozenAfter);
                            organizationAccountManager.updateBalance(account);

                            //修改流水状态
                            flow.setStatus(FlowStatusEnum.FAILURE.code);
                            flow.setDescription("提现失败，已退回账户");
                            organizationAccountFlowManager.update(flow.getId(), flow.getStatus());

                            //修改提现申请的状态
                            organizationWithdrawManager.updateAuditStatus(withdrawId, OrganizationWithdrawStatusEnum.FAILED.code);

                            buildOrganizationWithdrawFlowLog(flow, from);
                        }

                    }

                }catch (Exception e) {
                    logger.error("query organization withdraw error." + e.getMessage());
                    throw new RuntimeException("query organization withdraw error.", e);
                }

                return true;
            }

        });
    }

    /**
     * 构建机构提现账户流水日志
     * @param flow
     * @param from
     */
    private void buildOrganizationWithdrawFlowLog(OrganizationAccountFlowDO flow,String from){
        Long time = System.currentTimeMillis();
        //mongodb里面写入一条记录
        try {
            MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ORGANIZATION_ACCOUNT_FLOW);
            Document document = new Document();
            document.append("operation", OperationsLogTypeEnum.ORGANIZATION_ACCOUNT_FLOW.code);
            document.append("flowId",flow.getId());
            document.append("organizationId", flow.getOrganizationId());
            document.append("orderId", flow.getOrderId());
            document.append("batchNo", flow.getBatchNo());
            document.append("type", flow.getType());
            document.append("amount", flow.getAmount());
            document.append("withdraw", flow.getWithdraw());
            document.append("fee", flow.getFee());
            document.append("status", flow.getStatus());
            document.append("description", flow.getDescription());
            document.append("created", time);
            dbCollection.insertOne(document);
        } catch (Exception e) {
            logger.error("write to mongodb error", e);
        }

        //log4j自定义日志文件里面写入一条记录
        JSONObject json = new JSONObject();
        json.put("flowId",flow.getId());
        json.put("organizationId",flow.getOrganizationId());
        json.put("type",flow.getType());
        json.put("orderId",flow.getOrderId());
        json.put("batchNo", flow.getBatchNo());
        json.put("amount",flow.getAmount());
        json.put("withdraw",flow.getWithdraw());
        json.put("fee",flow.getFee());
        json.put("from",from);
        json.put("listener",true);
        json.put("status",flow.getStatus());
        json.put("created", time);

        flowLogger.error(json.toString());
    }
}
