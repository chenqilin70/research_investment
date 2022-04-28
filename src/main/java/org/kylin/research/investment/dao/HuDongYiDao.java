/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.kylin.research.investment.entity.BatchIssuance;
import org.kylin.research.investment.entity.HuDongYiQA;
import org.kylin.research.investment.entity.Stock;
import org.kylin.research.investment.entity.da.DataAccess;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqliteUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 互动易数据访问
 *
 * @author chenqilin
 * @date 2022-02-21 16:08:31
 */
@Slf4j
public class HuDongYiDao  extends BaseDao{

    public List<HuDongYiQA> getStockAllQA(Stock stock){
        List<HuDongYiQA> qas = new ArrayList<>();
        DataAccess dataAccess=new DataAccess();
        dataAccess.load("getSZQA").getParam().putAll(MapCreator.SS.create("stockcode",stock.getCode().substring(2),"orgId",getOrgId(stock)));
        int page=1;
        while(true){

            try{
                dataAccess.getParam().put("pageNum",""+page);
                String body = getBodyText(dataAccess);
                JSONArray rows = JSON.parseObject(body).getJSONArray("rows");
                if(rows.size()==0) break;
                rows.stream().forEach(r->{
                    JSONObject row=((JSONObject)r);
                    String question = row.getString("mainContent");
                    Long pubDate = row.getLong("pubDate");
                    String answer = row.getString("attachedContent");
                    Long updateDate = row.getLong("updateDate");
                    Long indexId = row.getLong("indexId");
                    HuDongYiQA huDongYiQA = new HuDongYiQA(indexId, question, answer, stock.getCode(), pubDate, updateDate);
                    qas.add(huDongYiQA);
                });
            }catch (Exception e){
                log.error("获取问答报错：",e);
            }
            page++;

        }
        return  qas;
    }

    public String getOrgId(Stock stock){
        DataAccess dataAccess=new DataAccess();
        dataAccess.load("getOrgId").getParam().put("stockcode",stock.getCode().substring(2));
        String body = getBodyText(dataAccess);
        String script = Jsoup.parse(body).select("script").stream().filter(s -> s.toString().contains("var orgId =")).collect(Collectors.toList()).get(0).toString();
        String orgId = script.lines().filter(l -> l.contains("orgId")).map(s -> ReUtil.findAll("\"(.*)\"",s,1).get(0)).collect(Collectors.toList()).get(0);
        return orgId;
    }


    public void insertQA(List<HuDongYiQA> stockAllQA) {
        if(!SqliteUtil.existTable(HuDongYiQA.class)){
            SqliteUtil.createTable(HuDongYiQA.class);
        }
        stockAllQA.stream().forEach(q->{
            try {
                Db.use().insert(Entity.parse(q));
            } catch (SQLException e) {
                log.error("插入问答报错："+e);
            }
        });

    }
}
