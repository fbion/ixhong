    package com.ixhong.lender.web.controller;

    import com.ixhong.common.pojo.Result;
    import com.ixhong.common.pojo.ResultCode;
    import com.ixhong.common.utils.ResultHelper;
    import com.ixhong.common.utils.ValidateUtils;
    import com.ixhong.domain.enums.*;
    import com.ixhong.domain.misc.LoginContextHolder;
    import com.ixhong.domain.permission.Permission;
    import com.ixhong.domain.pojo.LenderAccountDO;
    import com.ixhong.domain.pojo.LenderDO;
    import com.ixhong.domain.pojo.UserDO;
    import com.ixhong.domain.query.BiddingItemQuery;
    import com.ixhong.domain.query.LenderAccountFlowQuery;
    import com.ixhong.domain.query.LenderBillStageQuery;
    import com.ixhong.domain.utils.CalculatorUtils;
    import com.ixhong.lender.web.service.BiddingService;
    import com.ixhong.lender.web.service.LenderAccountService;
    import com.ixhong.lender.web.service.LenderService;
    import org.apache.commons.lang.StringUtils;
    import org.apache.commons.lang.math.NumberUtils;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.servlet.ModelAndView;

    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;

    /**
     * Created by jenny on 5/11/15.
     */
    @Controller
    @RequestMapping("/lender")
    @Permission(roles = {UserTypeEnum.INVESTOR})
    public class LenderController extends BaseController {

        private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LenderController.class);

        private static final String REG_WITHDRAW = "(?!^0$)(?!^[0]{2,})(?!^0[1-9]+)(?!^0\\.[0]*$)^(([1-9]+[0-9]*)|0)(\\.\\d{1,2})?$";

        @Autowired
        private LenderService lenderService;

        @Autowired
        private LenderAccountService lenderAccountService;

        @Autowired
        private BiddingService biddingService;


        @RequestMapping("/index")
        public ModelAndView index(){
            ModelAndView mav = new ModelAndView();
            UserDO user = LoginContextHolder.getLoginUser();
            Result result = lenderService.getDetailById(user.getId());
            mav.addObject("username",user.getName());
            mav.addObject("loginTime",user.getLoginTime());
            mav.addObject("flowTypeEnum", LenderFlowTypeEnum.values());
            mav.addAllObjects(result.getAll());
            mav.addObject("flowStatusEnum", FlowStatusEnum.values());
            return mav;
        }

        @RequestMapping("/update_password")
        public ModelAndView updatePassword(HttpServletRequest request, HttpServletResponse response) {
            ModelAndView mav = new ModelAndView();
            LenderDO lender = (LenderDO)lenderService.getById(LoginContextHolder.getLoginUser().getId()).getModel("lender");
            mav.addObject("token", this.createToken(request, response));
            mav.addObject("phone", lender.getPhone());
            return mav;
        }

        @RequestMapping("/update_password_action")
        @ResponseBody
        public String updatePasswordAction(@RequestParam("old_password")String oldPassword,
                                           @RequestParam("password")String password,
                                           @RequestParam("confirm_password")String confirmPassword,
                                           @RequestParam("phone")String phone,
                                           @RequestParam("token")String token,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
            String source = this.getToken(request);
            if(!source.equals(token)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单过期，请重新刷新页面");
            }
            if(StringUtils.isBlank(oldPassword) || StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码不能为空");
            }
            if(oldPassword.length() < 6 || oldPassword.length() > 20) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码必须是6-20个字符");
            }
            if(password.length() < 6 || password.length() > 20) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码必须是6-20个字符");
            }
            if(confirmPassword.length() < 6 || confirmPassword.length() > 20) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码必须是6-20个字符");
            }
            if(!ValidateUtils.validatePassword(oldPassword) || !ValidateUtils.validatePassword(password) || !ValidateUtils.validatePassword(confirmPassword)) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码格式为英文字母（区分大小写）、数字或下划线两两混合");
            }
            if(!password.equals(confirmPassword)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "密码与确认密码不一致");
            }
            Result result = lenderService.updatePassword(phone, oldPassword, password);
            if (!result.isSuccess()) {
                return ResultHelper.renderAsJson(result);
            }
            this.removeToken(request, response);
            result.setResponseUrl("/lender/update_password_success.jhtml");
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/protocol")
        public ModelAndView protocol(){
            ModelAndView mav = new ModelAndView();
            return mav;
        }

        @RequestMapping("/update_password_success")
        public ModelAndView updatePasswordSuccess() {
            ModelAndView mav = new ModelAndView();
            return mav;
        }

        @RequestMapping("/get_cities")
        @ResponseBody
        public String getCities(@RequestParam("pid")String pid) {
            Result result = lenderService.getCities(pid);
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/bank_card_list")
        public ModelAndView bankCardList() {
            ModelAndView mav = new ModelAndView();
            Result result = lenderService.bankCardList();
            LenderAccountDO lenderAccount = (LenderAccountDO)result.getModel("lenderAccount");
            LenderDO lender = (LenderDO)result.getModel("lender");

            if(lender != null && lender.getCertified() == 0) {
                mav.setViewName("redirect:/lender/certify.jhtml");
            }else if(lenderAccount != null && StringUtils.isBlank(lenderAccount.getDealPassword())) {
                mav.setViewName("redirect:/lender/add_deal_password.jhtml");
            }else {
                mav.addObject("realName", lender.getRealName());
                if(lenderAccount != null && StringUtils.isNotBlank(lenderAccount.getBankCardId())){
                    String bankCardId = lenderAccount.getBankCardId();
                    mav.addObject("bankName", lenderAccount.getBankName());
                    int length = bankCardId.length();
                    String cardTop = bankCardId.substring(0, 6);
                    String cardLast = bankCardId.substring(length - 4, length);
                    mav.addObject("bankCardId", StringUtils.rightPad(cardTop, length - 4, "*") + cardLast);
                }
            }
            return mav;
        }

        @RequestMapping("/bank_card")
        public ModelAndView addBankCardPage() {
            ModelAndView mav = new ModelAndView();
            return mav;
        }

        @RequestMapping("/certify")
        public ModelAndView certify(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value = "flag",required = false)String flag) {
            ModelAndView mav = new ModelAndView();
            LenderDO lender = (LenderDO)lenderService.getById(LoginContextHolder.getLoginUser().getId()).getModel("lender");
            if(lender.getCertified() == 1) {
                mav.setViewName("redirect:/lender/certify_success.jhtml");
            }else {
                mav.addObject("token", this.createToken(request, response));
            }
            mav.addObject("flag",flag);
            return mav;
        }

        @RequestMapping("/certify_success")
        public ModelAndView certifySuccess() {
            ModelAndView mav = new ModelAndView();
            LenderDO lender = (LenderDO)lenderService.getById(LoginContextHolder.getLoginUser().getId()).getModel("lender");
            String cardId = lender.getCardId();
            cardId = cardId.substring(0, 3) + "*********" + cardId.substring(cardId.length()-4, cardId.length());
            mav.addObject("realName", lender.getRealName());
            mav.addObject("cardId", cardId);
            return mav;
        }

        @RequestMapping("/certify_action")
        @ResponseBody
        public String getCities(@RequestParam("real_name")String realName,
                                @RequestParam("card_id")String cardId,
                                @RequestParam("token")String token,
                                @RequestParam(value = "flag",required = false)String flag,
                                HttpServletRequest request,
                                HttpServletResponse response) {
            if (!this.getToken(request).equals(token)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单过期，请重新刷新页面");
            }
            if(StringUtils.isBlank(realName) || !ValidateUtils.validateChineseText(realName)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "名字输入有误");
            }
            if(!ValidateUtils.validateCardId(cardId)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "身份证号输入有误");
            }
            Result result = lenderService.certify(realName.trim(), cardId.trim());
            if(result.isSuccess()) {
                result.addModel("realName", realName);
                result.addModel("cardId", cardId);
                if(flag != null && "bidding".equals(flag)){
                    result.setResponseUrl("/lender/add_deal_password.jhtml?flag="+flag);
                }else{
                    result.setResponseUrl("/lender/certify_success.jhtml");
                }
            }
            this.removeToken(request, response);
            return ResultHelper.renderAsJson(result);
        }

        /**
         * 流水账明细
         */
        @RequestMapping("/list_account_flow")
        @ResponseBody
        public String list(@RequestParam(value = "begin_date",required = false)String beginDate,
                                 @RequestParam(value = "end_date",required = false)String endDate,
                                 @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) {
            ModelAndView mav = new ModelAndView();
            LenderAccountFlowQuery query = new LenderAccountFlowQuery();
            try {
                if (StringUtils.isNotBlank(beginDate)){
                    query.setBeginDate(beginDate);
                    query.setQueryBeginDate(beginDate);
                }
                if (StringUtils.isNotBlank(endDate)){
                    query.setEndDate(endDate);
                    query.setQueryEndDate(endDate);
                }
            } catch (Exception e) {
                mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
                Result result = new Result();
                result.setSuccess(false);
                result.setMessage("查询条件没通过审核");
                return ResultHelper.renderAsJson(result);
            }
            Result result = this.lenderAccountService.listFlow(query);
           return ResultHelper.renderAsJson(result);
        }

        //发送添加银行卡短信
        @RequestMapping("/send_phone_code_action")
        @ResponseBody
        public String sendPhoneCodeAction(@RequestParam("phone") String phone) {
            Result result = this.lenderService.sendPhoneCode(phone, null, "lenderAddBankCardPhoneCode");
            return ResultHelper.renderAsJson(result);
        }

        /**
         * 学生还款账单
         * @return
         */
        @RequestMapping("/bill_stage")
        public ModelAndView billStage(@RequestParam(value = "begin_date",required = false)String beginDate,
                                      @RequestParam(value = "end_date",required = false)String endDate,
                                      @RequestParam(value = "key_word",required = false) String keyword,
                                      @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) {
            ModelAndView mav = new ModelAndView();
            LenderBillStageQuery query = new LenderBillStageQuery();
            query.setType(LenderFlowTypeEnum.RECEIVE.code);
            query.setLenderId(LoginContextHolder.getLoginUser().getId());
            query.setCurrentPage(page);
            query.setPageSize(10);
            try {
                if (StringUtils.isNotBlank(beginDate)){
                    query.setBeginDate(beginDate);
                    query.setQueryBeginDate(beginDate);
                }
                if (StringUtils.isNotBlank(endDate)){
                    query.setEndDate(endDate);
                    query.setQueryEndDate(endDate);
                }

                if(StringUtils.isNotBlank(keyword)){
                    query.setKeyWord(keyword);
                }
            } catch (Exception e) {
                mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
                Result result = new Result();
                result.setSuccess(false);
                result.setMessage("查询条件没通过审核");
            }

            Result result = this.lenderService.billStageList(query);
            result.addModel("biiStageStatus", BillStageStatusEnum.getLenderMap());
            mav.addAllObjects(result.getAll());
            return mav;
        }

        /**
         * 投标记录
         */
        @RequestMapping("/bidding_item")
        public ModelAndView biddingItem(@RequestParam(value = "begin_date",required = false)String beginDate,
                                        @RequestParam(value = "end_date",required = false)String endDate,
                                        @RequestParam(value = "page",required = false,defaultValue = "1")Integer page,
                                        @RequestParam(value = "status",required = false,defaultValue = "-1")Integer status) {
            ModelAndView mav = new ModelAndView();

            BiddingItemQuery query = new BiddingItemQuery();
            query.setLenderId(LoginContextHolder.getLoginUser().getId());
            query.setCurrentPage(page);
            query.setPageSize(10);
            try {
                if (StringUtils.isNotBlank(beginDate)){
                    query.setBeginDate(beginDate);
                    query.setQueryBeginDate(beginDate);
                }
                if (StringUtils.isNotBlank(endDate)){
                    query.setEndDate(endDate);
                    query.setQueryEndDate(endDate);
                }
               query.setStatus(status);
            } catch (Exception e) {
                mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
                Result result = new Result();
                result.setSuccess(false);
                result.setMessage("查询条件没通过审核");
            }

            Result result = this.lenderAccountService.listItem(query);
            mav.addAllObjects(result.getAll());
            mav.addObject("biddingItemEnum", BiddingItemEnum.values());
            return mav;
        }

        @RequestMapping("/register_success")
        public ModelAndView registerSuccess(@RequestParam(value = "id")Integer id){
            ModelAndView mav = new ModelAndView();
            Result result = this.lenderService.getById(id);
            LenderDO lender = (LenderDO)result.getModel("lender");
            if(lender != null ){
                String phone = lender.getPhone();
                String newPhone = phone.substring(0,3)+"****"+phone.substring(7,11);
                lender.setPhone(newPhone);
                mav.addObject("user", lender);
            }

            return mav;
        }

        @RequestMapping("/add_deal_password")
        public ModelAndView addDealPassword(HttpServletRequest request,
                                            HttpServletResponse response,
                                            @RequestParam(value = "flag",required = false)String flag){
            ModelAndView mav = new ModelAndView();
            Result result = lenderService.bankCardList();
            LenderDO lender = (LenderDO)result.getModel("lender");
            LenderAccountDO lenderAccount = (LenderAccountDO)result.getModel("lenderAccount");
            if(lender != null && lender.getCertified() == 0) {
                mav.setViewName("redirect:/lender/certify.jhtml");
            }else if(lenderAccount != null && StringUtils.isNotBlank(lenderAccount.getDealPassword())) {
                mav.setViewName("redirect:/lender/update_deal_password.jhtml");
            }else {
                mav.addObject("token", this.createToken(request, response));
            }
            mav.addObject("flag",flag);
            return mav;
        }

        @RequestMapping("/add_deal_password_action")
        @ResponseBody
        public String addDealPasswordAction(@RequestParam("password1")String password1,
                                            @RequestParam("password2")String password2,
                                            @RequestParam("token")String token,
                                            @RequestParam(value = "flag",required = false)String flag,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
            if(!this.getToken(request).equals(token)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单已过期，请刷新页面");
            }
            if(password1.length() < 6 || password1.length() > 20) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码必须是6-20个字符");
            }
            if(!ValidateUtils.validatePassword(password1)) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码格式为英文字母（区分大小写）、数字或下划线两两混合");
            }
            if(!password1.equals(password2)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "交易密码与确认密码不一致");
            }
            Result result = lenderAccountService.addDealPassword(password1);
            if (result.isSuccess()) {
                if(flag != null && "bidding".equals(flag)){
                    result.setResponseUrl("/lender/add_bank_card.jhtml?flag="+flag);
                }else{
                    result.setResponseUrl("/lender/deal_password_success.jhtml?type=add");
                }
            }
            this.removeToken(request, response);
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/deal_password_success")
        public ModelAndView addDealPasswordSuccess(@RequestParam("type")String type){
            ModelAndView mav = new ModelAndView();
            mav.addObject("type", type);
            return mav;
        }

        @RequestMapping("/update_deal_password")
        public ModelAndView updateDealPassword(HttpServletRequest request, HttpServletResponse response){
            ModelAndView mav = new ModelAndView();
            mav.addObject("token", this.createToken(request, response));
            return mav;
        }

        @RequestMapping("/update_deal_password_action")
        @ResponseBody
        public String updateDealPasswordAction(@RequestParam("old_password")String oldPassword,
                                                @RequestParam("password1")String password1,
                                                @RequestParam("password2")String password2,
                                                @RequestParam("token")String token,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
            if(!this.getToken(request).equals(token)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单已过期，请刷新页面");
            }
            if(oldPassword.length() < 6 || oldPassword.length() > 20) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码必须是6-20个字符");
            }
            if(password1.length() < 6 || password1.length() > 20) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码必须是6-20个字符");
            }
            if(!ValidateUtils.validatePassword(oldPassword) || !ValidateUtils.validatePassword(password1)) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码格式为英文字母（区分大小写）、数字或下划线两两混合");
            }
            if(!password1.equals(password2)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "交易密码与确认密码不一致");
            }
            Result result = lenderAccountService.updateDealPassword(oldPassword, password1);
            if (result.isSuccess()) {
                result.setResponseUrl("/lender/deal_password_success.jhtml?type=update");
            }
            this.removeToken(request, response);
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/add_bank_card")
        public ModelAndView addBankCard(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(value = "flag",required = false)String flag) {
            ModelAndView mav = new ModelAndView();
            Result result = lenderService.lenderAccountDetail();
            mav.addObject("flag",flag);
            mav.addObject("token", this.createToken(request, response));
            mav.addAllObjects(result.getAll());
            return mav;
        }

        @RequestMapping("/bind_bank_card")
        @ResponseBody
        public String bindBankCard(@RequestParam("bank_card_id") String bankCardId,
                                   @RequestParam("phone") String phone,
                                   HttpServletRequest request) {
            Result result = this.lenderService.bindBankCard(bankCardId, phone, this.getClientIP(request));
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/add_bank_card_action")
        @ResponseBody
        public String addBankCardAction(@RequestParam("bank_card_id")String bankCardId,
                                        @RequestParam("bank_name")String bankName,
                                        @RequestParam("phone_code")String phoneCode,
                                        @RequestParam("token")String token,
                                        @RequestParam("request_id")String requestId,
                                        @RequestParam(value = "flag",required = false)String flag,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
            if(!this.getToken(request).equals(token)) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单过期，请刷新页面再操作");
            }
            if(bankCardId.length() < 16 || bankCardId.length() > 19 || !ValidateUtils.validateNonZeroPositiveInteger(bankCardId)) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "您输入的银行卡号有误");
            }
            LenderAccountDO lenderAccount = new LenderAccountDO();
            lenderAccount.setLenderId(LoginContextHolder.getLoginUser().getId());
            lenderAccount.setBankCardId(bankCardId);
            lenderAccount.setBankName(bankName);
            Result result = lenderService.addBankCard(lenderAccount, phoneCode, requestId);
            if(result.isSuccess()) {
                this.removeToken(request, response);
                String realName = (String)result.getModel("realName");
                if(flag != null && "bidding".equals(flag)){
                    result.setResponseUrl("/lender/recharge.jhtml?flag="+flag);
                }else{
                    result.setResponseUrl("/lender/bank_card_list.jhtml?bankName=" + bankName + "&bankCardId=" + bankCardId + "&realName=" + realName);
                }
            }
            return ResultHelper.renderAsJson(result);
        }


        @RequestMapping("/process")
        public ModelAndView process(@RequestParam("step")String step){
            ModelAndView mav = new ModelAndView();
            mav.addObject("step", step);
            return mav;
        }

        @RequestMapping("/recharge")
        public ModelAndView recharge(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam(value = "flag",required = false)String flag){
            ModelAndView mav = new ModelAndView();
            Result result = lenderService.accountDetail();
            if (!result.isSuccess()){
                String step = (String)result.getModel("step");
                if("certify".equals(step)) {
                    if(flag != null && "bidding".equals(flag)){
                        mav.setViewName("redirect:/lender/certify.jhtml?flag="+flag);
                    }else{
                        mav.setViewName("redirect:/lender/certify.jhtml");
                    }
                }else if("password".equals(step)) {
                    if(flag != null && "bidding".equals(flag)){
                        mav.setViewName("redirect:/lender/add_deal_password.jhtml?flag="+flag);
                    }else{
                        mav.setViewName("redirect:/lender/add_deal_password.jhtml");
                    }
                }else if("bank".equals(step)) {
                    if(flag != null && "bidding".equals(flag)){
                        mav.setViewName("redirect:/lender/add_bank_card.jhtml?flag="+flag);
                    }else{
                        mav.setViewName("redirect:/lender/add_bank_card.jhtml");
                    }
                }
                return mav;
            }
            mav.addObject("flag",flag);
            mav.addObject("token", this.createToken(request, response));
            mav.addAllObjects(result.getAll());
            mav.addObject("rechargeEnum", AdminFlowTypeEnum.RECHARGE.code);
            return mav;
        }

        @RequestMapping("/recharge_fee")
        @ResponseBody
        public String rechargeFee(@RequestParam("recharge")double recharge){
            //提现金额若为小数，小数点后只允许输入2位以内
            if (recharge < 10){
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "最少充值10元!");
            }
            if (recharge > 50000){
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "最多充值50000元!");
            }
            Result result = new Result();
            // 计算出提现实际交易金额（投资人目前免手续费）
            double fee = 0;
            result.addModel("fee", fee);
            result.addModel("amount", CalculatorUtils.format(recharge + fee));
            result.setSuccess(true);
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/recharge_action")
        @ResponseBody
        public String rechargeAction(@RequestParam("recharge")double recharge,
                                     @RequestParam("deal_password")String dealPassword,
                                     @RequestParam("token")String token,
                                     @RequestParam(value = "flag",required = false)String flag,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
            if (!token.equals(this.getToken(request))) {
                return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单过期，请重新刷新页面");
            }
            Result result = new Result();
            if (recharge < 10) {
                result.setMessage("最少充值10元!");
            } else if (recharge > 50000) {
                result.setMessage("最多充值50000元!");
            } else {
                result = this.lenderService.recharge(recharge, this.getClientIP(request), dealPassword);
            }
            if (result.isSuccess()){
                this.removeToken(request, response);
            }
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/recharge_success")
        public ModelAndView rechargeSuccess(@RequestParam("recharge")double recharge,
                                            @RequestParam("order_id")String orderId) {
            ModelAndView mav = new ModelAndView();
            mav.addObject("recharge", recharge);
            mav.addObject("orderId", orderId);
            mav.addObject("rechargeSuccess", FlowStatusEnum.SUCCESS.code);
            mav.addObject("rechargeFailure", FlowStatusEnum.FAILURE.code);
            mav.addObject("recharging", FlowStatusEnum.ONGOING.code);
            return mav;
        }

        @RequestMapping("/get_flow_status")
        @ResponseBody
        public String getFlowStatus(@RequestParam("order_id")String orderId) {
            Result result = lenderAccountService.getFlowStatus(orderId);
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/withdraw")
        public ModelAndView withdraw(HttpServletRequest request,
                                     HttpServletResponse response){
            ModelAndView mav = new ModelAndView();
            Result result = lenderService.accountDetail();
            if (!result.isSuccess()) {
                String step = (String)result.getModel("step");
                if("certify".equals(step)) {
                    mav.setViewName("redirect:/lender/certify.jhtml");
                }else if("password".equals(step)) {
                    mav.setViewName("redirect:/lender/deal_password.jhtml");
                }else if("bank".equals(step)) {
                    mav.setViewName("redirect:/lender/bank_card.jhtml");
                }
                return mav;
            }
            mav.addObject("token", this.createToken(request, response));
            mav.addObject("withdrawType", AdminFlowTypeEnum.WITHDRAW.code);
            mav.addAllObjects(result.getAll());
            return mav;
        }

        @RequestMapping("/withdraw_action")
        @ResponseBody
        public String withdrawAction(@RequestParam("token")String token,
                                     @RequestParam("withdraw")String inputWithdraw,
                                     @RequestParam("password")String dealPassword,
                                     @RequestParam("phone_code")String phoneCode,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
            if (!token.equals(this.getToken(request))) {
                return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单已过期，请刷新页面!");
            }

            double withdraw = NumberUtils.toDouble(inputWithdraw);
            if (StringUtils.isBlank(inputWithdraw) || withdraw <= 0){
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请输入提现金额!");
            }

            //提现金额应为正整数，且若有小数位最多只可以有两位小数
            Pattern regex = Pattern.compile(REG_WITHDRAW);
            Matcher matcher = regex.matcher(inputWithdraw);
            if (!matcher.matches()){
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请输入正确的金额(如果有小数位最多2位)!");
            }

            Result result = this.lenderService.withdraw(withdraw, this.getClientIP(request), dealPassword, phoneCode);
            if (result.isSuccess()){
                this.removeToken(request, response);
            }
            return ResultHelper.renderAsJson(result);
        }

        @RequestMapping("/recharge_callback")
        public ModelAndView rechargeCallback(@RequestParam("data")String data,
                                             @RequestParam("encryptkey")String encryptKey,
                                             HttpServletResponse response) {
            ModelAndView mav = new ModelAndView();
            Result result = this.lenderService.rechargeCallback(data, encryptKey);
            if (result.isSuccess()) {
                try {
                    response.getOutputStream().write("success".getBytes("utf-8"));
                    return null;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            } else {
                logger.error(result.getMessage());
            }
            return mav;
        }

        @RequestMapping("/flow_list")
        public ModelAndView flowList(@RequestParam(value = "type",required = false,defaultValue = "-1")Integer type) {
            ModelAndView mav = new ModelAndView();
            LenderAccountFlowQuery query = new LenderAccountFlowQuery();
            query.setPageSize(10);
            query.setLenderId(LoginContextHolder.getLoginUser().getId());
            if (type != null){
                query.setType(type);
            }
            Result result = this.lenderAccountService.listFlow(query);
            mav.addAllObjects(result.getAll());
            mav.addObject("flowStatusEnum", FlowStatusEnum.values());
            return mav;
        }

        @RequestMapping("/flow_list_query")
        @ResponseBody
        public String flowListQuery(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                    @RequestParam(value = "begin_date", required = false) String beginDate,
                                    @RequestParam(value = "end_date", required = false) String endDate,
                                    @RequestParam(value = "type",required = false,defaultValue = "-1") Integer type
                                   ){

            LenderAccountFlowQuery query =  buildLenderAccountFlowQuery(page, beginDate, endDate, type);
            Result result = this.lenderAccountService.listFlow(query);
            return ResultHelper.renderAsJson(result);
        }

        private LenderAccountFlowQuery buildLenderAccountFlowQuery(int page,String beginDate,String endDate,Integer type){
            LenderAccountFlowQuery query = new LenderAccountFlowQuery();
            if(StringUtils.isNotBlank(beginDate)){
                query.setBeginDate(beginDate);
            }
            if(StringUtils.isNotBlank(endDate)){
                query.setEndDate(endDate);
            }
            if(type !=null || type != -1 ){
                query.setType(type);
            }

            query.setCurrentPage(page);
            query.setPageSize(10);
            query.setLenderId(LoginContextHolder.getLoginUser().getId());
            return query;

        }

        @RequestMapping("/bidding_action")
        @ResponseBody
        public String biddingAction(@RequestParam("bidding_id")int biddingId,
                                    @RequestParam("money") double money,
                                    @RequestParam("deal_password")String dealPassword){

            if(money < 50){
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR,"投资金额低于最低投资金额");
            }
            Result result = biddingService.bidding(money,biddingId,dealPassword);
            if(result.isSuccess()){
                result.setResponseUrl("/lender/bidding_item.jhtml");
            }

            return ResultHelper.renderAsJson(result);
        }

    }
