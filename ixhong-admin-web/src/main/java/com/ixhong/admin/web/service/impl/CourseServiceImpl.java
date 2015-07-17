package com.ixhong.admin.web.service.impl;

import com.mongodb.client.MongoCollection;
import com.ixhong.admin.web.service.CourseService;
import com.ixhong.common.mongo.MongoDBClient;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.CourseStatusEnum;
import com.ixhong.domain.enums.OperationsLogTypeEnum;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.CourseDO;
import com.ixhong.domain.pojo.UserDO;
import com.ixhong.domain.query.CourseQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.CourseManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by cuichengrui on 15/4/14.
 */
@Service
public class CourseServiceImpl implements CourseService {

    private Logger logger = Logger.getLogger(CourseService.class);

    @Autowired
    private CourseManager courseManager;

    @Autowired
    private MongoDBClient mongoDBClient;

    @Override
    public Result list(CourseQuery query) {
        Result result = new Result();
        try {
            QueryResult<CourseDO> qr = this.courseManager.query(null, query);
            result.addModel("query", query);
            result.addModel("queryResult", qr);
            result.addModel("courses", qr.getResultList());
            result.addModel("statusEnums", CourseStatusEnum.values());
            result.addModel("courseChecking", CourseStatusEnum.COURSE_CHECKING.code);
            result.addModel("courseDisabled", CourseStatusEnum.COURSE_DISABLED.code);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("course list error", e);
            result.setMessage("course list error");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result detail(Integer id) {
        Result result = new Result();
        try {
            CourseDO course = this.courseManager.getById(id);
            result.addModel("course", course);
            result.addModel("statusEnums", CourseStatusEnum.values());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("course detail error", e);
            result.setMessage("course detail error");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result audit(CourseDO course, String remoteIp) {
        Result result = new Result();
        try {
            this.courseManager.audit(course);
            try {
                //将课程的审核日志写入mongodb
                MongoCollection<Document> dbCollection = mongoDBClient.getCollection(Constants.MONGO_ADMIN_OPERATION_COLLECTION);
                UserDO user = LoginContextHolder.getLoginUser();
                Document document = new Document();
                document.append("operation", OperationsLogTypeEnum.AUDIT_COURSE.code);
                document.append("courseId", course.getId());
                document.append("name", course.getName()); //操作对象
                document.append("beforeStatus", CourseStatusEnum.COURSE_CHECKING.code);
                document.append("afterStatus", course.getStatus());
                document.append("operator", user.getName());
                document.append("description", course.getAuditNote());
                document.append("created", System.currentTimeMillis());
                document.append("remoteIp", remoteIp);
                dbCollection.insertOne(document);
            } catch (Exception e) {
                logger.error("write to mongodb error", e);
            }
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("course audit error,", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("审核失败，请重试");
        }
        return result;
    }
}
