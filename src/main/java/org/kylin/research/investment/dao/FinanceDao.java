/*
 * Copyright (c) 2020-2030 江苏丰尚
 * 不能修改和删除上面的版权声明
 * 此代码属于江苏丰尚深圳研究院编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.kylin.research.investment.entity.ReportDateType;
import org.kylin.research.investment.entity.da.DataAccess;
import org.kylin.research.investment.util.TryUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 请加入注释
 *
 * @author chenqilin
 * @date 2022-08-14 13:31:51
 */
public class FinanceDao  extends BaseDao{

    public List<String> getReportPeriod(String code, ReportDateType type){
        DataAccess getReportPeriod = new DataAccess().load("getReportPeriod");
        getReportPeriod.setUrl(MessageFormat.format(getReportPeriod.getUrl(),type.getName()));
        getReportPeriod.getParam().put("code",code);
        getReportPeriod.getParam().put("reportDateType",type.getCode().toString());

//        if(code.equals("SH601838")){
//            System.out.println("");
//        }else{
//            return new ArrayList<>();
//        }

        JSONObject json =  execHttp(getReportPeriod);

        if("股票代码不合法".equals(json.getString("message"))){
            return new ArrayList<>();
        }
        return json.getJSONArray("data").stream().map(o->((JSONObject)o).getString("REPORT_DATE").substring(0,10)).collect(Collectors.toList());
    }

    public JSONObject getFinanceData(String code,List<String> periods, ReportDateType type){
        List<List<String>> periodsList = ListUtil.split(periods, 5);
        JSONArray datas=new JSONArray();
        for(List<String> pl:periodsList){
            DataAccess getFinanceData = new DataAccess().load("getFinanceData");
            getFinanceData.setUrl(MessageFormat.format(getFinanceData.getUrl(),type.getName()));
            getFinanceData.getParam().put("code",code);
            getFinanceData.getParam().put("dates",pl.stream().reduce((a,b)->a+","+b).get());
            getFinanceData.getParam().put("reportDateType",type.getCode().toString());
            JSONObject json = execHttp(getFinanceData);
            datas.addAll(json.getJSONArray("data"));
        }
        JSONObject result=new JSONObject();
        for(Object o:datas){
            String report_date = ((JSONObject) o).getString("REPORT_DATE").substring(0, 10);
            result.put(report_date,o);
        }
        return result;
    }

    public JSONObject getFinanceZYZBData(String code){
        DataAccess getFinanceZYZBData = new DataAccess().load("getFinanceZYZBData");
        getFinanceZYZBData.getParam().put("code",code);
        JSONObject json = execHttp(getFinanceZYZBData);
        JSONArray dataJson = json.getJSONArray("data");

        JSONObject result=new JSONObject();
        for(Object o:dataJson){
            String report_date = ((JSONObject) o).getString("REPORT_DATE").substring(0, 10);
            result.put(report_date.substring(0,10),o);
        }
        return result;
    }


    public JSONObject execHttp( DataAccess access){
        return TryUtil.runTry(5,5,new JSONObject(),()->{
            JSONObject json = JSON.parseObject(getBodyText(access));
            int companyType=3;
            while( json.containsKey("$type") && companyType > 0 ){
                access.getParam().put("companyType",companyType+"" );
                json = JSON.parseObject(getBodyText(access));
                companyType--;
            }
            return json;
        },access.toString());
    }


    public List<String> getAllStock() {
        DataAccess getAllStock = new DataAccess().load("getAllStock");
        String body = getBodyText(getAllStock);
        JSONObject json = JSON.parseObject(body.substring(body.indexOf("(")+1,body.lastIndexOf(")")));
        JSONArray jsonArray = json.getJSONObject("data").getJSONArray("diff");
        return jsonArray.stream()
                .map(a -> ((JSONObject)a).getString("f12") )
                .filter(a-> Arrays.asList("600","601","603","605","688","300").contains(a.substring(0,3) )    || a.startsWith("00")  )
                .map(a->{
                    if(a.startsWith("00") || a.startsWith("300")){
                        return "SZ"+a;
                    }else{
                        return "SH"+a;
                    }
                }).collect(Collectors.toList());
    }
}
