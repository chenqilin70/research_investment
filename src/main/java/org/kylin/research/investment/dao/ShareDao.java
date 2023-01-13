/*
 * Copyright (c) 2020-2030 江苏丰尚
 * 不能修改和删除上面的版权声明
 * 此代码属于江苏丰尚深圳研究院编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.kylin.research.investment.entity.da.DataAccess;

import java.util.HashMap;
import java.util.Map;

/**
 * 请加入注释
 *
 * @author chenqilin
 * @date 2022-11-10 21:53:55
 */
public class ShareDao  extends BaseDao{

    public Map<String,Long> getShare(String code) {
        Map<String,Long> shareMap=new HashMap<>();
        try{
            DataAccess getAllStock = new DataAccess().load("getShare");
            getAllStock.getParam().put("code", code );
            String body = getBodyText(getAllStock);
            JSONArray shareJson = JSON.parseObject(body).getJSONArray("lnfhrz");
            if(shareJson!=null){
                for(Object s:shareJson){
                    JSONObject share= (JSONObject) s;
                    Long total_dividend = share.getLong("TOTAL_DIVIDEND");
                    String statistics_year = share.getString("STATISTICS_YEAR");
                    if(total_dividend!=null && !"2022".equals(statistics_year)){
                        shareMap.put(statistics_year,total_dividend);
                    }
                }
            }
        }catch (Throwable t){
            System.err.println(code+" 获取分红失败 "+t.getMessage());
        }
        return shareMap;
    }
}
