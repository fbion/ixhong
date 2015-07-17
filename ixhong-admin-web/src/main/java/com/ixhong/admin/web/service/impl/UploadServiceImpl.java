package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.UploadService;
import com.ixhong.common.CommonConstants;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.DESUtils;
import com.ixhong.common.utils.HttpUtils;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.misc.SystemConfig;
import com.ixhong.domain.pojo.UserDO;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shenhongxi on 15/5/12.
 */
@Service
public class UploadServiceImpl implements UploadService {
    private Logger logger = Logger.getLogger(UploadService.class);

    @Autowired
    private SystemConfig systemConfig;

    @Override
    public Result uploadImage(InputStream inputStream) {
        Result result = new Result();
        try {
            UserDO user = LoginContextHolder.getLoginUser();
            String uid = user.getId() + "/admin";
            String token = DESUtils.encrypt(uid, CommonConstants.FS_SECURITY_KEY);
            HttpEntity entity = new InputStreamEntity(inputStream);
            Map<String,String> params = new HashMap<String, String>();
            params.put("token", token);
            params.put("uid", uid);
            String response = HttpUtils.httpPost("http://" + systemConfig.getImageHostName() + "/image_upload", params, entity);
            if(StringUtils.isBlank(response)) {
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                result.setMessage("图片上传失败，文件系统访问异常");
                return result;
            }
            JSONObject responseJson = JSONObject.fromObject(response);
            if (responseJson.get("code").equals(ResultCode.SUCCESS.getCode())) {
                result.setSuccess(true);

                JSONObject _data = (JSONObject) responseJson.get("data");
                result.addModel("filename", _data.get("filename"));
                result.addModel("imgHost", systemConfig.getImageHostName());
                result.addModel("url","http://" +systemConfig.getImageHostName()+"/ds/x8/"+_data.get("filename"));
            } else {
                result.setMessage(responseJson.get("message").toString());
            }
        } catch (Exception e) {
            logger.error("image upload error,",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("图片上传失败，文件系统访问异常");
        }
        return result;
    }

    @Override
    public Result uploadFile(InputStream inputStream, String format) {
        Result result = new Result();

        try {
            UserDO user = LoginContextHolder.getLoginUser();
            String uid = user.getId() + "/organization";//来自organization
            String token = DESUtils.encrypt(uid, CommonConstants.FS_SECURITY_KEY);
            HttpEntity entity = new InputStreamEntity(inputStream);
            Map<String,String> params = new HashMap<String, String>();
            params.put("token", token);
            params.put("uid", uid);
            params.put("format", format);
            String response = HttpUtils.httpPost("http://" + systemConfig.getImageHostName() + "/file_upload", params, entity);
            if(StringUtils.isBlank(response)) {
                result.setResultCode(ResultCode.SYSTEM_ERROR);
                result.setMessage("文件上传失败，文件系统访问异常");
                return result;
            }
            JSONObject responseJson = JSONObject.fromObject(response);
            if (responseJson.get("code").equals(ResultCode.SUCCESS.getCode())) {
                result.setSuccess(true);
                JSONObject _data = (JSONObject) responseJson.get("data");
                result.addModel("filename", _data.get("filename"));
                result.addModel("imgHost", systemConfig.getImageHostName());
            } else {
                result.setMessage(responseJson.get("message").toString());
            }
        } catch (Exception e) {
            logger.error("file upload error,",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("文件上传失败，文件系统访问异常");
        }
        return result;
    }
}
