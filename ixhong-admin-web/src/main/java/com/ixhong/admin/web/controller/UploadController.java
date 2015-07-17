package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.UploadService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.constants.Constants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by shenhongxi on 15/5/12.
 */
@Controller
@RequestMapping("/upload")
public class UploadController extends BaseController {
    private Logger logger = Logger.getLogger(BaseController.class);


    @Autowired
    private UploadService uploadService;

    @RequestMapping("/upload")
    public ModelAndView upload() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    @RequestMapping(value = "/upload_action", method = {RequestMethod.POST})
    @ResponseBody
    public String upload(@RequestParam("upfile") MultipartFile file, @RequestParam(value = "type",defaultValue = "img",required = false)String type) {
        Result result = null;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            String filename = file.getOriginalFilename();
            String format = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            if (type.equals("img")) {
                if (!ArrayUtils.contains(Constants.IMAGE_FORMAT, format)) {
                    return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请上传" + Arrays.toString(Constants.IMAGE_FORMAT) + "格式的文件");
                }
                result = this.uploadService.uploadImage(inputStream);
            } else {
                if (!ArrayUtils.contains(Constants.FILE_FORMAT, format)) {
                    return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请上传" + Arrays.toString(Constants.FILE_FORMAT) + "格式的文件");
                }
                result = this.uploadService.uploadFile(inputStream, format);
            }
            return ResultHelper.renderAsJson(result);
        } catch (Exception e) {

            return ResultHelper.renderAsJson(ResultCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.error("");
                    return ResultHelper.renderAsJson(ResultCode.SYSTEM_ERROR, e.getMessage());
                }
            }
        }
    }

    @RequestMapping(value = "/upload_actions", method = {RequestMethod.GET})
    @ResponseBody
    public String uploads(@RequestParam(value = "type",required = false)String type) {
        Result result = null;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/Users/jenny/Workspace/Applications/xuehaodai/ixhong-admin-web/src/main/webapp/js/ueditor1_4_2/ueditor.all.js");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String filename = "filename";
            String format = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            if (type.equals("img")) {
                if (!ArrayUtils.contains(Constants.IMAGE_FORMAT, format)) {
                    return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请上传" + Arrays.toString(Constants.IMAGE_FORMAT) + "格式的文件");
                }
                result = this.uploadService.uploadImage(inputStream);
            } else {
                if (!ArrayUtils.contains(Constants.FILE_FORMAT, format)) {
                    return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请上传" + Arrays.toString(Constants.FILE_FORMAT) + "格式的文件");
                }
                result = this.uploadService.uploadFile(inputStream, format);
            }
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
}
