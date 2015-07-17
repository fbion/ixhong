package com.ixhong.support.web.service.listener;

import com.mongodb.client.MongoCollection;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.yeepay.YeepayResult;
import com.ixhong.common.yeepay.YeepayUtils;
import com.ixhong.common.yeepay.tzt.TZTUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.FlowStatusEnum;
import com.ixhong.domain.enums.OperationsLogTypeEnum;
import com.ixhong.domain.pojo.LenderAccountDO;
import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.domain.pojo.StudentAccountDO;
import com.ixhong.domain.pojo.StudentAccountFlowDO;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.LenderAccountFlowManager;
import com.ixhong.manager.LenderAccountManager;
import com.ixhong.manager.StudentAccountFlowManager;
import com.ixhong.manager.StudentAccountManager;
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

/**
 * Created by liuguanqing on 15/5/18.
 */
@Service
public class RechargeQueueListener implements MessageListener {

    protected Logger flowLogger = Logger.getLogger("flow-data-logger");

    protected Logger logger = Logger.getLogger(RechargeQueueListener.class);

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private StudentAccountFlowManager studentAccountFlowManager;

    @Autowired
    private StudentAccountManager studentAccountManager;

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private LenderAccountManager lenderAccountManager;

    @Autowired
    private MongoDBClient mongoDBClient;

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
            if (json.has("studentId")) {
                int studentId = json.getInt("studentId");
                this.studentRecharge(studentId, flowId, from);
            } else if (json.has("lenderId")) {
                int lenderId = json.getInt("lenderId");
                this.lenderRecharge(lenderId, flowId, from);
            }
            //执行成功后，确认消息
            message.acknowledge();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //学生充值
    private void studentRecharge(final int studentId,final int flowId, final String from) throws Exception{

        final StudentAccountFlowDO flow = studentAccountFlowManager.getById(flowId);
        if(studentId != flow.getStudentId()) {
            return;
        }
        Integer flowStatus = flow.getStatus();
        if (flowStatus == FlowStatusEnum.SUCCESS.code || flowStatus == FlowStatusEnum.FAILURE.code){
            return;
        }

        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {

                String orderId = flow.getOrderId();
                StudentAccountDO account = studentAccountManager.getByStudentIdForUpdate(studentId);
                try {
                    YeepayResult qr = YeepayUtils.queryByOrderId(orderId);
                    //查询结果 1:查询正常； 50:订单不存在；
                    String code = qr.getR1Code();
                    if(StringUtils.isBlank(code) || !code.equals("1")) {
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code",code);
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message","code不合法");
                        flowLogger.error(json.toString());
                        return false;
                    }
                    // 确保易宝返回的商户订单号 与 我们系统的订单号一致
                    if(!qr.getR6Order().equals(orderId)) {
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code",code);
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message","订单号不一致：" + qr.getR6Order());
                        flowLogger.error(json.toString());
                        return false;
                    }

                    // 订单支付状态 INIT:未支付; CANCELED:已取消；SUCCESS:已支付；
                    String payStatus = qr.getRbPayStatus();
                    if (payStatus.equals("INIT")) {
                        return false;
                    }

                    if (payStatus.equals("CANCELED")) {
                        flow.setStatus(FlowStatusEnum.FAILURE.code);
                        studentAccountFlowManager.update(flow);
                        return true;
                    }

                    if (payStatus.equals("SUCCESS")) {
                        //TODO 更新学生账户余额
                        Double before = account.getBalance();
                        Double current = CalculatorUtils.format(account.getBalance() + flow.getRecharge());
                        account.setBalance(current);
                        studentAccountManager.updateBalance(account);

                        flow.setBalanceBefore(before);
                        flow.setBalanceAfter(current);
                        flow.setStatus(FlowStatusEnum.SUCCESS.code);
                        studentAccountFlowManager.update(flow);
                    }

                    //账户流水日志处理
                    buildStudentRechargeFlowLog(flow,from);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
        });

    }

    /**
     * 学生充值账户流水日志处理
     * @param flow
     * @param from
     */
    private void buildStudentRechargeFlowLog(StudentAccountFlowDO flow,String from) throws Exception {
        Long time = System.currentTimeMillis();
        //mongodb里面写入一条记录
        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_STUDENT_ACCOUNT_FLOW);
        Document document = new Document();
        document.append("operation", OperationsLogTypeEnum.STUDENT_ACCOUNT_FLOW.code);
        document.append("flowId",flow.getId());
        document.append("studentId", flow.getStudentId());
        document.append("orderId", flow.getOrderId());
        document.append("type", flow.getType());
        document.append("amount", flow.getAmount());
        document.append("recharge", flow.getRecharge());
        document.append("fee", flow.getFee());
        document.append("status", FlowStatusEnum.SUCCESS.code);
        document.append("description", flow.getDescription());
        document.append("created", time);
        dbCollection.insertOne(document);

        //log4j自定义日志文件里面写入一条记录
        JSONObject json = new JSONObject();
        json.put("flowId",flow.getId());
        json.put("studentId",flow.getStudentId());
        json.put("type",flow.getType());
        json.put("orderId",flow.getOrderId());
        json.put("amount",flow.getAmount());
        json.put("recharge",flow.getRecharge());
        json.put("fee",flow.getFee());
        json.put("from",from);
        json.put("listener",true);
        json.put("status", FlowStatusEnum.SUCCESS.code);
        json.put("created", time);

        flowLogger.error(json.toString());
    }

    //投资人充值
    private void lenderRecharge(final int lenderId, final int flowId, final String from) throws Exception{
        final LenderAccountFlowDO flow = lenderAccountFlowManager.getById(flowId);
        if(lenderId != flow.getLenderId()) {
            return;
        }
        Integer flowStatus = flow.getStatus();
        if (flowStatus == FlowStatusEnum.SUCCESS.code || flowStatus == FlowStatusEnum.FAILURE.code){
            return;
        }

        transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus transactionStatus) {

                String orderId = flow.getOrderId();
                LenderAccountDO account = lenderAccountManager.getByLenderId(lenderId);
                try {
                    JSONObject responseJson = TZTUtils.queryOrder(orderId);

                    if (responseJson.containsKey("error_code")) {
                        logger.error(responseJson.getString("error_msg") + "(" + responseJson.getString("error_code") + ")");
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code",responseJson.getString("error_code"));
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message", responseJson.getString("error_msg"));
                        flowLogger.error(json.toString());
                        throw new RuntimeException("lenderRecharge error," + responseJson.toString());
                    }
                    if (responseJson.containsKey("clientSignError")) {
                        logger.error(responseJson.getString("clientSignError"));
                        JSONObject json = new JSONObject();
                        json.put("flowId",flowId);
                        json.put("orderId",orderId);
                        json.put("code","clientSignError");
                        json.put("listener",true);
                        json.put("from",from);
                        json.put("message", responseJson.getString("clientSignError"));
                        flowLogger.error(json.toString());
                        throw new RuntimeException("lenderRecharge error," + responseJson.toString());
                    }
                    logger.info("------------------query order from yeepay success------------------");

                    //status
                    //0：待付（创建的订单未支付成功）
                    //1：已付（订单已经支付成功）
                    //2：已撤销（待支付订单有效期为
                    //1 天，过期后自动撤销）
                    //3：阻断交易（订单因为高风险而被阻断）

                    int status = responseJson.getInt("status");
                    if (status == 1) {
                        double before = account.getBalance();
                        Double current = CalculatorUtils.format(before + flow.getRecharge());
                        account.setBalance(current);
                        lenderAccountManager.updateBalance(account);

                        flow.setBalanceAfter(current);
                        flow.setBalanceBefore(before);
                        flow.setStatus(FlowStatusEnum.SUCCESS.code);
                    } else if (status == 2 || status == 3) {
                        flow.setStatus(FlowStatusEnum.FAILURE.code);
                    }
                    lenderAccountFlowManager.update(flow);

                    buildLenderRechargeFlowLog(flow, from);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
        });
    }

    /**
     * 理财人充值账户流水日志处理
     * @param flow
     * @param from
     */
    private void buildLenderRechargeFlowLog(LenderAccountFlowDO flow, String from) throws Exception {
        long time = System.currentTimeMillis();
        //mongodb里面写入一条记录
        MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_LENDER_ACCOUNT_FLOW);
        Document document = new Document();
        document.append("operation", OperationsLogTypeEnum.LENDER_ACCOUNT_FLOW.code);
        document.append("lenderId", flow.getLenderId());
        document.append("orderId", flow.getOrderId());
        document.append("type", flow.getType());
        document.append("amount", flow.getAmount());
        document.append("fee", flow.getFee());
        document.append("flowId", flow.getId());
        document.append("recharge", flow.getRecharge());
        document.append("status", flow.getStatus());
        document.append("description", flow.getDescription());
        document.append("created", time);
        dbCollection.insertOne(document);

        //log4j自定义日志文件里面写入一条记录
        JSONObject json = new JSONObject();
        json.put("lenderId", flow.getLenderId());
        json.put("type", flow.getType());
        json.put("orderId", flow.getOrderId());
        json.put("recharge", flow.getRecharge());
        json.put("fee", flow.getFee());
        json.put("amount", flow.getAmount());
        json.put("from", from);
        json.put("flowId", flow.getId());
        json.put("listener", true);
        json.put("status", flow.getStatus());
        json.put("created", time);

        flowLogger.error(json.toString());
    }

}
