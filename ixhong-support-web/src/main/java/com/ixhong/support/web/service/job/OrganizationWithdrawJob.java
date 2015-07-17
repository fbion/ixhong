package com.ixhong.support.web.service.job;

import com.ixhong.domain.enums.FlowStatusEnum;
import com.ixhong.domain.enums.OrganizationFlowTypeEnum;
import com.ixhong.domain.pojo.OrganizationAccountFlowDO;
import com.ixhong.manager.OrganizationAccountFlowManager;
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
 * Created by shenhongxi on 2015/6/13.
 * 机构提现
 */
public class OrganizationWithdrawJob {

    @Autowired
    private OrganizationAccountFlowManager organizationAccountFlowManager;

    @Autowired
    private JmsTemplate withdrawQueueSender;

    private Logger logger = Logger.getLogger(OrganizationWithdrawJob.class);

    public void execute() {

        try {
            //把所有提现状态为'提现处理中'的订单查出来
            logger.error("---organization withdraw begin---");
            List<OrganizationAccountFlowDO> flows = organizationAccountFlowManager.getByStatus(FlowStatusEnum.ONGOING.code, OrganizationFlowTypeEnum.WITHDRAW.code);
            logger.error("organization withdraw ongoing size : " + flows.size());
            if(CollectionUtils.isEmpty(flows)) {
                return;
            }
            //重新将这些提现信息加入到提现消息队列中
            for(final OrganizationAccountFlowDO flow : flows) {
                withdrawQueueSender.send(new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        JSONObject json = new JSONObject();
                        json.put("flowId", flow.getId());
                        json.put("organizationId",flow.getOrganizationId());
                        json.put("from","quartz-job");
                        return session.createTextMessage(json.toString());
                    }
                });
            }
        }catch (Exception e){
            logger.error("OrganizationWithdrawJob error," + e.getMessage());
        }

    }

}
