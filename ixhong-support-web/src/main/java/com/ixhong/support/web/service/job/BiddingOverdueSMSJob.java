package com.ixhong.support.web.service.job;

import com.ixhong.common.utils.HttpUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.pojo.StudentBillStageDO;
import com.ixhong.domain.pojo.StudentDO;
import com.ixhong.domain.utils.CalculatorUtils;
import com.ixhong.manager.StudentBillStageManager;
import com.ixhong.manager.StudentManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by duanxiangchao on 2015/6/8.
 */
public class BiddingOverdueSMSJob {

    private Logger logger = Logger.getLogger(BiddingOverdueSMSJob.class);

    private static final int PAGE_SIZE = 30;

    @Autowired
    private StudentBillStageManager studentBillStageManager;

    @Autowired
    private StudentManager studentManager;

    private static final int[] DAYS_OF_OVERDUE = {-1,-3,-5,-7,-10};

    public void execute(){
            for(Integer day: DAYS_OF_OVERDUE){
                try {
                    sendMessage(day);
                }catch (Exception e){
                    logger.error("BiddingOverdueSMSJob error," + e.getMessage());
                }
            }
    }

    private void sendMessage(int days){
        Date date = DateUtils.addDays(new Date(),days);
        Date deadline = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);

        //查询逾期的记录
        int page = 1;
        while(true) {
            int startRow  = (page -1) * PAGE_SIZE;

            List<StudentBillStageDO> billList = studentBillStageManager.getForSMS(deadline, 1, startRow, PAGE_SIZE);
            if(CollectionUtils.isEmpty(billList)) {
                break;
            }
            for (StudentBillStageDO billStage : billList) {

                try {
                    StudentDO student = studentManager.getById(billStage.getStudentId());
                    StringBuilder sb = buildSMS(days, billStage);
                    smsSend(student.getPhone(), sb.toString());
                } catch (Exception e) {
                    logger.error("BiddingOverdueSMSJob,error:" + e);
                }

            }
            page++;
        }
    }

    private StringBuilder buildSMS(int days, StudentBillStageDO billStage) {
        StringBuilder sb = new StringBuilder();
        sb.append("您应于"+DateFormatUtils.format(billStage.getDeadline(), "yyyy-MM-dd"));
        sb.append("还款"+ CalculatorUtils.format(billStage.getAmount()+billStage.getFee())+"元，已逾期"+Math.abs(days));
        sb.append("天，产生逾期罚金共"+billStage.getOverdueFee());
        sb.append("元，请及时还款。逾期罚金=逾期金额*0.5%*逾期天数，已还");
        sb.append((billStage.getStage()-1)+"期，总");
        sb.append(billStage.getTotalStage()+"期【阳光E学】");
        return sb;
    }

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
