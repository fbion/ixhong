package com.ixhong.admin.web.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.ixhong.admin.web.service.StudentService;
import com.ixhong.admin.web.service.UploadService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.common.utils.VelocityUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.BiddingStatusEnum;
import com.ixhong.domain.enums.HouseTypeEnum;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.pojo.StudentDO;
import com.ixhong.domain.query.BiddingQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuichengrui on 15/4/22.
 */
@Controller
@RequestMapping("/student")
@Permission(roles = {UserTypeEnum.ADMIN})
public class StudentController extends BaseController{

    private static final String CHARSET_UTF8 = "UTF-8";

    @Autowired
    private StudentService studentService;

    @Autowired
    private UploadService uploadService;

    @RequestMapping("/list_bidding")
    public ModelAndView listBidding(@RequestParam(value = "status",required = false,defaultValue = "-1")Integer status,
                                    @RequestParam(value = "begin_date",required = false)String beginDate,
                                    @RequestParam(value = "end_date",required = false)String endDate,
                                    @RequestParam(value = "student_name",required = false)String studentName,
                                    @RequestParam(value = "student_phone",required = false)String studentPhone,
                                    @RequestParam(value = "organization_name",required = false)String organizationName,
                                    @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        BiddingQuery query = new BiddingQuery();
        query.setCurrentPage(page);
        if (StringUtils.isNotBlank(studentName)){
            query.setStudentName(studentName);
        }
        if (StringUtils.isNotBlank(studentPhone)){
            query.setStudentPhone(studentPhone);
        }
        if (StringUtils.isNotBlank(organizationName)){
            query.setOrganizationName(organizationName);
        }
        try {
            if (StringUtils.isNotBlank(beginDate)){
                query.setBeginDate(DateUtils.parseDate(beginDate, Constants.DATE_PATTERN));
                query.setQueryBeginDate(beginDate);
            }
            if (StringUtils.isNotBlank(endDate)){
                query.setEndDate(DateUtils.addDays(DateUtils.parseDate(endDate, Constants.DATE_PATTERN), 1));
                query.setQueryEndDate(endDate);
            }
        } catch (ParseException e) {
            mav.setViewName("redirect:/error.jhtml?messages=parameter-error");
            return mav;
        }


        List<Integer> _status = new ArrayList<Integer>();
        if(status == -1) {
            _status = null;
        }else {
            _status.add(status);
        }
        query.setStatus(_status);
        query.setQueryStatus(status);
        Result result = this.studentService.list(query);
        mav.addObject("biddingAdminChecking",BiddingStatusEnum.ADMIN_AUDITING.code);
        mav.addObject("biddingFullChecking",BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
        mav.addObject("biddingStaging",BiddingStatusEnum.STAGING.code);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/list_full_bidding")
    public ModelAndView listFullBidding(@RequestParam(value = "status",required = false,defaultValue = "-1")Integer status,
                                        @RequestParam(value = "begin_date",required = false)String beginDate,
                                        @RequestParam(value = "end_date",required = false)String endDate,
                                        @RequestParam(value = "student_name",required = false)String studentName,
                                        @RequestParam(value = "student_phone",required = false)String studentPhone,
                                        @RequestParam(value = "organization_name",required = false)String organizationName,
                                        @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) throws ParseException {
        ModelAndView mav = new ModelAndView();
        BiddingQuery query = new BiddingQuery();
        query.setCurrentPage(page);
        if (StringUtils.isNotBlank(studentName)){
            query.setStudentName(studentName);
        }
        if (StringUtils.isNotBlank(studentPhone)){
            query.setStudentPhone(studentPhone);
        }
        if (StringUtils.isNotBlank(organizationName)){
            query.setOrganizationName(organizationName);
        }
        if (StringUtils.isNotBlank(beginDate)){
            query.setBeginDate(DateUtils.parseDate(beginDate, Constants.DATE_PATTERN));
            query.setQueryBeginDate(beginDate);
        }
        if (StringUtils.isNotBlank(endDate)){
            query.setEndDate(DateUtils.addDays(DateUtils.parseDate(endDate, Constants.DATE_PATTERN), 1));
            query.setQueryEndDate(endDate);
        }


        List<Integer> _status = new ArrayList<Integer>();
        if(status == -1) {
            _status = null;
        }else {
            _status.add(status);
        }
        query.setStatus(_status);
        query.setQueryStatus(status);
        Result result = this.studentService.list(query);
        mav.addObject("biddingAdminChecking",BiddingStatusEnum.ADMIN_AUDITING.code);
        mav.addObject("biddingFullChecking",BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
        mav.addObject("biddingStaging",BiddingStatusEnum.STAGING.code);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/detail")
    public ModelAndView detail( @RequestParam(value = "id")Integer id,
                                @RequestParam(value = "student_id")Integer studentId
                                ) {
        ModelAndView mav = new ModelAndView();
        Result result = this.studentService.detail(id, studentId);
        mav.addAllObjects(result.getAll());
        mav.addObject("otherHouseType", HouseTypeEnum.OTHERS.code);
        mav.addObject("rentHouseType", HouseTypeEnum.RENT.code);
        return mav;
    }

    @RequestMapping("/audit_bidding")
    public ModelAndView auditBidding(@RequestParam(value = "id")Integer id,
                                @RequestParam(value = "student_id")Integer studentId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        Result result = this.studentService.detail(id, studentId);
        //要添加的token
        mav.addObject("token", this.createToken(request, response));
        //标的id
        mav.addObject("id", id);
        mav.addObject("auditedFailure",BiddingStatusEnum.ADMIN_AUDITED_FAILURE.code);
        mav.addObject("ongoing",BiddingStatusEnum.ONGOING.code);
        mav.addAllObjects(result.getAll());
        mav.addObject("otherHouseType", HouseTypeEnum.OTHERS.code);
        mav.addObject("rentHouseType", HouseTypeEnum.RENT.code);
        return mav;
    }

    @RequestMapping("/audit_full_bidding")
    public ModelAndView auditFullBidding(@RequestParam(value = "id")Integer id,
                                    @RequestParam(value = "student_id")Integer studentId,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        Result result = this.studentService.detail(id, studentId);
        //要添加的token
        mav.addObject("token", this.createToken(request, response));
        //标的id
        mav.addObject("id", id);
        mav.addObject("auditedFailure",BiddingStatusEnum.FULL_ADMIN_AUDITED_FAILURE.code);
        mav.addObject("staging",BiddingStatusEnum.STAGING.code);
        mav.addAllObjects(result.getAll());
        mav.addObject("otherHouseType", HouseTypeEnum.OTHERS.code);
        mav.addObject("rentHouseType", HouseTypeEnum.RENT.code);
        return mav;
    }

    @RequestMapping(value = "/audit_bidding_action",method = {RequestMethod.POST})
    @ResponseBody
    public String auditBiddingAction(@RequestParam(value = "token")String token,
                                     @RequestParam(value = "id")Integer id,
                                     @RequestParam(value = "status")Integer status,
                                     @RequestParam(value = "audit_note")String auditNote,
                                     HttpServletRequest request,
                                     HttpServletResponse response){
        //验证token
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单过期，请刷新页面");
        }
        //验证标的状态
        if (BiddingStatusEnum.value(status) == null){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR);
        }
        Result result = this.studentService.auditBidding(id, status, auditNote);
        result.setResponseUrl("/student/list_bidding.jhtml");
        this.removeToken(request, response);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/audit_full_bidding_action",method = {RequestMethod.POST})
    @ResponseBody
    public String auditFullBiddingAction(@RequestParam(value = "token")String token,
                                         @RequestParam(value = "id")Integer id,
                                         @RequestParam(value = "status")Integer status,
                                         @RequestParam(value = "audit_note")String auditNote,
                                         HttpServletRequest request,
                                         HttpServletResponse response){
        //验证token
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单过期，请刷新页面");
        }
        //验证标的状态
        if (BiddingStatusEnum.value(status) == null){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR);
        }
        Result result = this.studentService.auditFullBidding(id, status, auditNote);
        this.removeToken(request, response);
        result.setResponseUrl("/student/list_full_bidding.jhtml");
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/certify")
    public ModelAndView certify(@RequestParam(value = "student_id")Integer studentId,
                                RedirectAttributes attrs
                                ) {
        ModelAndView mav = new ModelAndView();
        Result result = this.studentService.getById(studentId);
        StudentDO student = (StudentDO) result.getModel("student");
        if (student.getCertified() == 1) {
            attrs.addAttribute("result", "00");
            mav.setViewName("redirect:/student/certify_result.jhtml");
            return mav;
        }
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping(value = "/certify_action",method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String certifyAction(@RequestParam(value = "student_id")Integer studentId,
                                     @RequestParam(value = "name")String name,
                                     @RequestParam(value = "card_id")String cardId,
                                     @RequestParam(value = "photo_url", required = false)String photoUrl
                                     ) {
        Result result = this.studentService.certify(studentId, name, cardId, photoUrl);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "upload", method = {RequestMethod.POST})
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        Result result = null;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            result = this.uploadService.uploadImage(inputStream);
            return ResultHelper.renderAsJson(result);
        } catch (Exception e) {
            return ResultHelper.renderAsJson(ResultCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    return ResultHelper.renderAsJson(ResultCode.SYSTEM_ERROR, e.getMessage());
                }
            }
        }
    }

    @RequestMapping("/certify_result")
    public ModelAndView certifyResult(@RequestParam(value = "result")String result,
                                        @RequestParam(value = "score", required = false)String score) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("result", result);
        mav.addObject("score", score);
        return mav;
    }

    /**
     * 管理员满标审核时可以下载合同
     * @param biddingId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/download_contract")
    public ModelAndView downloadContract(@RequestParam("bidding_id")Integer biddingId,
                                         HttpServletRequest request,
                                         HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        try {
            Result result = this.studentService.contractDetail(biddingId);
            if (!result.isSuccess()){
                mav.setViewName("redirect:/error.jhtml?messages=" + URLEncoder.encode(result.getMessage(), "utf-8"));
                return mav;
            }

            OutputStream os = response.getOutputStream();
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("借款协议.pdf", "utf-8"));
            response.setContentType("application/pdf");

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, os);
            document.open();

            //生成借款合同
            Map<String,Object> model = result.getAll();
            String text = VelocityUtils.render("contract_loan.vm", model);
            InputStream inputStream = new ByteArrayInputStream(text.getBytes(CHARSET_UTF8));
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, inputStream, null, Charset.forName(CHARSET_UTF8), new PdfFontProvider());

            //生成服务合同
            document.newPage();
            text = VelocityUtils.render("contract_service.vm", model);
            inputStream = new ByteArrayInputStream(text.getBytes(CHARSET_UTF8));
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, inputStream, null, Charset.forName(CHARSET_UTF8), new PdfFontProvider());
            document.close();
        } catch (Exception e) {
            mav.setViewName("redirect:/error.jhtml?messages=system-error");
            return mav;
        }

        return null;
    }

    /**
     * 解决linux环境下中文支持
     */
    public static class PdfFontProvider extends XMLWorkerFontProvider {

        public PdfFontProvider(){
            super(null,null);
        }

        @Override
        public Font getFont(final String fontName, String encoding, float size, final int style) {

            Font FontChinese = null;
            try {
                BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                FontChinese = new Font(bfChinese, 12, Font.NORMAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(FontChinese==null)
                FontChinese = super.getFont(fontName, encoding, size, style);

            return FontChinese;
        }
    }

    @RequestMapping("/list_ongoing_bidding")
    public ModelAndView listOngoingBidding(@RequestParam(value = "page",required = false,defaultValue = "1")Integer page,
                                           @RequestParam(value = "student_name",required = false)String studentName,
                                           @RequestParam(value = "student_phone",required = false)String studentPhone,
                                           @RequestParam(value = "organization_name",required = false)String organizationName){
        ModelAndView mav = new ModelAndView();
        BiddingQuery query = new BiddingQuery();
        query.setCurrentPage(page);
        if (StringUtils.isNotBlank(studentName)){
            query.setStudentName(studentName);
        }
        if (StringUtils.isNotBlank(studentPhone)){
            query.setStudentPhone(studentPhone);
        }
        if (StringUtils.isNotBlank(organizationName)){
            query.setOrganizationName(organizationName);
        }
        List statusList = new ArrayList<Integer>();
        statusList.add(BiddingStatusEnum.ONGOING.code);
        query.setStatus(statusList);
        Result result = this.studentService.list(query);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/bidding_delay_action")
    @ResponseBody
    public String biddingDelayAction(@RequestParam(value = "id")Integer id){
        Result result = studentService.biddingInvalidDelay(id);
        result.setResponseUrl("/student/list_ongoing_bidding.jhtml");
        return ResultHelper.renderAsJson(result);
    }

}