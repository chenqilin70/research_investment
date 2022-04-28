/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import cn.hutool.core.thread.ThreadUtil;
import jodd.http.HttpException;
import jodd.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.entity.da.DataAccess;

/**
 * 基础数据访问
 *
 * @author chenqilin
 * @date 2022-01-04 11:07:03
 */
@Slf4j
public class BaseDao {

    public static final String HTTP_TYPE_GET="GET";
    public static final String HTTP_TYPE_POST="POST";



    public String getBodyText(DataAccess dataAccess){


        String bodyText="";
        int tryTime=0;
        while(true){
            try {
                tryTime++;

                HttpRequest httpRequest=null;
                if(HTTP_TYPE_GET.equals(dataAccess.getType())){
                    httpRequest = HttpRequest.get(dataAccess.getUrl());
                }else if(HTTP_TYPE_POST.equals(dataAccess.getType())){
                    httpRequest = HttpRequest.post(dataAccess.getUrl());
                }
                bodyText = httpRequest.query(dataAccess.getParam())
                        .header(dataAccess.getHeader()) .timeout(60*1000).send() .charset("utf-8").bodyText();
                if(bodyText.contains("请联系管理员")){
                    throw new HttpException("对方系统报错，要求联系管理员");
                }

                break;
            }catch (HttpException e){
                if(tryTime>10){
                    log.error(e.getMessage()+",尝试三次后均失败，即将放弃:"+dataAccess);
                    break;
                }else{
//                    log.error("http请求报错，即将重试");
//                    log.error(dataAccess);
                    ThreadUtil.sleep(5*1000);
                }
            }
        }
        return bodyText;
    }





}
