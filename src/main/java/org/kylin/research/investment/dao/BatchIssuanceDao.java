/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.meta.MetaUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kylin.research.investment.entity.BatchIssuance;
import org.kylin.research.investment.entity.da.BatchIssuanceDataAccess;
import org.kylin.research.investment.entity.da.DataAccess;
import org.kylin.research.investment.entity.da.MechanismPageDataAccess;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqlGetter;
import org.kylin.research.investment.util.SqliteUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 生物制品批签发管理系统  数据访问类
 *
 * @author chenqilin
 * @date 2022-01-18 22:58:29
 */
@Slf4j
public class BatchIssuanceDao extends BaseDao{

    public static final SqlGetter sqlGetter = new SqlGetter("batch_issuance");

    /**
     * 获取所有机构页面的DataAccess
     * @return
     */
    public List<MechanismPageDataAccess> getAllMechanismPageDA()  {
        log.info("开始：获取所有机构页面的DataAccess");
        TimeInterval timer = DateUtil.timer();
        List<MechanismPageDataAccess> result=new ArrayList();
        DataAccess mechanismListDA = new DataAccess().load("getBatchIssuance");
        Document body = Jsoup.parse(getBodyText(mechanismListDA));
        Elements tds = body.select("body>table>tbody>tr>td>table>tbody>tr>td>table>tbody>tr>td");
        for(Element td:tds){
            String name = td.select("strong").get(0).text();
            Elements as = td.select("a");
            String href = as.get(as.size() - 1).attr("href");
            MechanismPageDataAccess mechanismPageDA= ((MechanismPageDataAccess) new MechanismPageDataAccess()
                    .load(URLUtil.url(mechanismListDA.getUrl()+href.substring(href.indexOf("?")))))
                    .setMechanismName(name);
            result.add(mechanismPageDA);
        }
        log.info("结束：获取所有机构页面的DataAccess，耗时："+timer.intervalSecond()+"秒");
        return result;
    }

    /**
     * 获取所有批签发数据集的DataAccess
     * @param allMechanismPageDA
     * @return
     */
    @SneakyThrows
    public List<BatchIssuanceDataAccess> getAllBatchIssuanceDatasetDA(List<MechanismPageDataAccess> allMechanismPageDA) {
        log.info("开始：获取所有批签发数据集的DataAccess");
        TimeInterval timer = DateUtil.timer();

        List<Entity> allMd5 = Db.use().query(sqlGetter.getSql("getDownloadedBatchIssuanceDataset").getSql());
        Set<String> md5s = allMd5.stream().map(e -> e.getStr("datasetMd5")).collect(Collectors.toSet());

        List<BatchIssuanceDataAccess> result=allMechanismPageDA.parallelStream().map(da->{
            Document body = Jsoup.parse(getBodyText(da));
            Elements as = body.select("a[href^=\"search.do?\"]");
            List<BatchIssuanceDataAccess> subResult=new ArrayList();
            for(Element a:as){
                BatchIssuanceDataAccess batchIssuanceDataAccess = ((BatchIssuanceDataAccess) new BatchIssuanceDataAccess()
                        .load(URLUtil.url(da.getUrl() + "" + a.attr("href").substring(a.attr("href").indexOf("?")))))
                        .setTitle(a.text());
                batchIssuanceDataAccess.setMechanismName(da.getMechanismName());
                boolean downloaded = md5s.contains(dsUrlMd5(batchIssuanceDataAccess.toString()));
                if(!downloaded){
                    subResult.add(batchIssuanceDataAccess);
                }
            }
            return subResult;
        }).reduce((a,b)->new ArrayList<BatchIssuanceDataAccess>(){{addAll(a);addAll(b);}}).orElse(new ArrayList());
        log.info("结束：获取所有批签发数据集的DataAccess，耗时："+timer.intervalSecond()+"秒");
        return result;
    }

    public String dsUrlMd5(String url){
        String query = URLUtil.url(url).getQuery();
        Map<String, String> map = Arrays.stream(StrUtil.removePrefix(query, "?").split("&")).collect(Collectors.toMap(a -> a.split("=")[0], a -> a.split("=")[1], (a, b) -> a));
        String parameter1 = map.getOrDefault("parameter1", "");
        String parameter2 = map.getOrDefault("parameter2", "");
        String md5 = SecureUtil.md5(parameter1 + "_" + parameter2);
        return md5;

    }




    /**
     * 获取所有批签发数据项
     * @param allBatchIssuanceDatasetDA
     * @return
     */
    @SneakyThrows
    public List<BatchIssuance> getAllBatchIssuance(List<BatchIssuanceDataAccess> allBatchIssuanceDatasetDA) {
        log.info("开始：获取所有批签发数据项");
        TimeInterval timer = DateUtil.timer();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        List<Callable<List<BatchIssuance>>> allCallable = allBatchIssuanceDatasetDA.stream().map(d -> new BatchIssuanceCallable(d)).collect(Collectors.toList());
        List<Future<List<BatchIssuance>>> futures = executorService.invokeAll(allCallable);
        executorService.shutdown();
        List<BatchIssuance> result=new ArrayList();
        for(Future<List<BatchIssuance>> f : futures)  result.addAll(f.get());
        log.info("结束：获取所有批签发数据项，耗时："+timer.intervalSecond()+"秒");
        return result;
    }

    /**
     * 插入所有签批发数据项
     * @param allBatchIssuance
     */
    @SneakyThrows
    public void insertBatchInssuance(List<BatchIssuance> allBatchIssuance) {
        log.info("开始：插入所有签批发数据项");
        TimeInterval timer = DateUtil.timer();
        if(!SqliteUtil.existTable(BatchIssuance.class)){
            SqliteUtil.createTable(BatchIssuance.class);
        }
        int count=allBatchIssuance.stream().map(bi-> {
            try {
                bi.setSaveTime(new Date());
                return Db.use().insert(Entity.parse(bi));
            } catch (SQLException throwables) {
                log.error("",throwables);
                return 0;
            }
        }).reduce((a,b)->a+b).orElse(0);

        log.info("结束：获取所有批签发数据项，共"+count+"条，耗时："+timer.intervalSecond()+"秒");
    }

    public void updateBatchIssuanceDataset() {
        try {
            int i = DbUtil.use().execute(sqlGetter.getSql("dropBatchIssuanceDataset").getSql());
            int j = DbUtil.use().execute(sqlGetter.getSql("createBatchIssuanceDataset").getSql());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public  class BatchIssuanceCallable implements Callable<List<BatchIssuance>> {
        private BatchIssuanceDataAccess d;

        public BatchIssuanceCallable(BatchIssuanceDataAccess d) {
            super();
            this.d = d;
        }
        @Override
        public List<BatchIssuance> call() throws Exception {
            List<BatchIssuance> result=new ArrayList();
            String bodyText = getBodyText(d);
            Document doc = Jsoup.parse(bodyText);
            Elements columnElements = doc.select("thead>tr>td");
            if (columnElements.isEmpty()) {
                log.error("查询到列为空：" + d);
            } else {
                List<String> columns =  columnElements.subList(1, columnElements.size()).stream().map(e->e.text()).collect(Collectors.toList());
                Elements batchIssuanceElements = doc.select("body>center>div>div>table>tbody>tr>td>table>tbody>tr");
                for(Element tr:batchIssuanceElements){
                    Elements td = tr.select("td");
                    BatchIssuance bi=new BatchIssuance();
                    for(int i=0;i<columns.size();i++){
                        bi.setValueByChinese(columns.get(i),td.get(i).text());
                    }
                    bi.setUrl(d.toString());
                    bi.setBatchIssuanceDatasetTitle(d.getTitle());
                    bi.setDatasetMd5(dsUrlMd5(bi.getUrl()));
                    result.add(bi);
                }
            }
            return result;
        }
    }

}










