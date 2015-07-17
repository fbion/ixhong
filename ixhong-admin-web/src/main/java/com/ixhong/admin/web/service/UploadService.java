package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;

import java.io.InputStream;

/**
 * Created by shenhongxi on 15/5/12.
 */
public interface UploadService {

    public Result uploadImage(InputStream inputStream);

    public Result uploadFile(InputStream inputStream, String format);
}
