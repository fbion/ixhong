package com.ixhong.support.web.service.job;

import com.ixhong.domain.enums.FlowStatusEnum;
import com.ixhong.domain.enums.LenderFlowTypeEnum;
import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.manager.LenderAccountFlowManager;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * Created by cuichengrui on 2015/5/29.
 * 理财人提现
 */
public class LenderWithdrawJob {

    @Autowired
    private LenderAccountFlowManager lenderAccountFlowManager;

    @Autowired
    private JmsTemplate withdrawQueueSender;

    private Logger logger = Logger.getLogger(LenderWithdrawJob.class);

    public void execute() {

        //把所有提现状态为'提现处理中'的订单查出来
        try {
            logger.error("---lender withdraw begin---");
            List<LenderAccountFlowDO> flows = lenderAccountFlowManager.getByStatus(FlowStatusEnum.ONGOING.code, LenderFlowTypeEnum.WITHDRAW.code);
            logger.error("lender withdraw ongoing size : " + flows.size());
            if(CollectionUtils.isEmpty(flows)) {
                return;
            }
            //重新将这些提现信息加入到提现消息队列中
            for(final LenderAccountFlowDO flow : flows) {
                withdrawQueueSender.send(new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        JSONObject json = new JSONObject();
                        json.put("flowId", flow.getId());
                        json.put("lenderId",flow.getLenderId());
                        json.put("from","quartz-job");
                        return session.createTextMessage(json.toString());
                    }
                });
            }
        }catch (Exception e){
            logger.error("LenderWithdrawJob error," + e.getMessage());
        }

    }

}
