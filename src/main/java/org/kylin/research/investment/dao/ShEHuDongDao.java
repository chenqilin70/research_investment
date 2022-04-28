/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import cn.hutool.core.util.ReUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
 * 上证e互动数据访问
 *
 * @author chenqilin
 * @date 2022-02-21 16:08:31
 */
@Slf4j
public class ShEHuDongDao extends BaseDao{

    public List<HuDongYiQA> getStockAllQA(Stock stock){
        List<HuDongYiQA> qas = new ArrayList<>();
        DataAccess dataAccess=new DataAccess();
        String uid = getUid(stock);
        dataAccess.load("getSHQA").getParam().putAll(MapCreator.SS.create( "uid" , uid ));
        int page=1;
        while(true){

            try{
                dataAccess.getParam().put("page",""+page);
                dataAccess.getParam().put("_",new Date().getTime()+"");
                String body = getBodyText(dataAccess);
                Document bodyDoc = Jsoup.parse(body);
                Elements feedItems = bodyDoc.select(".m_feed_txt");
                for(Element e : feedItems){
                    log.info(e.text());
                }
                break;
            }catch (Exception e){
                log.error("获取问答报错：",e);
            }
            page++;

        }
        return  qas;
    }

    public String getUid(Stock stock){
        DataAccess dataAccess=new DataAccess();
        dataAccess.load("getUid").getParam().put("data",stock.getName());
        return  getBodyText(dataAccess);
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
