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
 * 标的催款发短信   最迟还款日前10/7/3/1天
 * Created by duanxiangchao on 2015/6/8.
 */
public class BiddingRepaySMSJob {

    @Autowired
    private StudentBillStageManager studentBillStageManager;

    @Autowired
    private StudentManager studentManager;

    private Logger logger = Logger.getLogger(BiddingRepaySMSJob.class);

    private static final int PAGE_SIZE = 30;

    private static final int[] DAYS_OF_REPAY = {1,5};

    public void execute(){
            for(Integer day: DAYS_OF_REPAY){
                try{
                    sendMessage(day);
                }catch (Exception e){
                    logger.error("BiddingRepaySMSJob error," + e.getMessage());
                }
            }
    }

    private void sendMessage(int days){
        Date date = DateUtils.addDays(new Date(),days);
        Date deadline = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
        //查询待还的记录
        int page = 1;
        while (true) {
            int startRow = (page -1) * PAGE_SIZE;
            List<StudentBillStageDO> billList = studentBillStageManager.getForSMS(deadline, 0, startRow, PAGE_SIZE);
            if(CollectionUtils.isEmpty(billList)) {
                break;
            }

            for (StudentBillStageDO billStage : billList) {

                try {
                    StudentDO student = studentManager.getById(billStage.getStudentId());
                    StringBuilder sb = buildSMS(billStage);
                    smsSend(student.getPhone(), sb.toString());
                } catch (Exception e) {
                    logger.error("BiddingRepaySMSJob,error" + e);
                }
            }
            page++;
        }
    }

    private StringBuilder buildSMS(StudentBillStageDO billStage) {
        StringBuilder sb = new StringBuilder();
        sb.append("您需要在"+ DateFormatUtils.format(billStage.getDeadline(), "yyyy-MM-dd"));
        sb.append("前还款"+ CalculatorUtils.format(billStage.getAmount() + billStage.getFee()));
        sb.append("元，到期不还款将产生罚金且影响信用记录，请您按时还款。已还");
        sb.append((billStage.getStage() - 1)+"期，总");
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
