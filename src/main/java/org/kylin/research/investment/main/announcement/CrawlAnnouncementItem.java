/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.announcement;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.meta.MetaUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.dao.SnowballDao;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.entity.Stock;
import org.kylin.research.investment.util.BaseRecursiveTask;
import org.kylin.research.investment.util.ForkJoinExecutor;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqlGetter;
import org.kylin.research.investment.util.SqliteUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * Query all the basic information announcement, deposited in the sqlite
 *
 * @author chenqilin
 * @date 2022-01-23 10:37:52
 */
@Slf4j
@Data
public class CrawlAnnouncementItem {
    public static final String resultDir = "D:\\tempFile\\research_investment_result\\announcements";
    public static final SnowballDao snowballDao = new SnowballDao();
    public static final SqlGetter sqlGetter = new SqlGetter("announcement");

    public static void main(String[] args) {

        log.info("start query all the basic information announcement");

        String stockCode=args[0];
        String stockName=args[1];

        boolean exist = MetaUtil.getTables(DSFactory.get()).stream().map(t -> t.toLowerCase()).collect(Collectors.toList()).contains(Announcement.class.getSimpleName().toLowerCase());

        /**
         * 可以提前建好表和索引
         * CREATE TABLE Announcement( id INT  PRIMARY KEY,fileName TEXT,url TEXT,uploadDate TEXT,content TEXT,dir TEXT,stockCode TEXT,crawlTime TEXT,exist INT )
         * CREATE UNIQUE INDEX id_unique_index on Announcement (id);
         */
        if (!exist) SqliteUtil.createTable(Announcement.class);

        List<Announcement> ann = snowballDao.getAnnouncement(stockCode);

        List<Announcement> notexistAnn = ForkJoinExecutor.exec(new AnnNotExistTask(ann, 25, stockName), 50, 60);

        Integer count = notexistAnn.stream().map(a -> {
            int result = 0;
            try {
                result = Db.use().insert(Entity.parse(a));
            } catch (SQLException throwables) {
                log.error("", throwables);
            }
            return result;
        }).reduce((a, b) -> a + b).orElse(0);

        log.info("end query all the basic information announcement,query count:" + ann.size() + "，save count:" + count );

    }

    public static class AnnNotExistTask extends BaseRecursiveTask<Announcement,List<Announcement>>{
        private String stockName;

        public AnnNotExistTask(List<Announcement> datas, int THRESHOLD_NUM,String stockName) {
            super(datas, THRESHOLD_NUM);
            this.stockName=stockName;
        }

        @Override
        public List<Announcement> run(List<Announcement> ann)  {
            String inStr = ann.stream().map(a -> "'" + a.getId() + "'").reduce((a, b) -> a + "," + b).orElse("");
            String batchExistAnnSql = sqlGetter.getSql("batchExistAnn", MapCreator.SS.create("inStr", inStr)).getSql();
            Entity entity = null;
            try {
                entity = Db.use().queryOne(batchExistAnnSql);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            Long batchExistCount = entity.getLong("c");
            if(batchExistCount>=ann.stream().map(a->a.getId()).distinct().count()){
                return new ArrayList<>();
            }else{
                return ann.stream().map(a -> {
                    List<Announcement> result = new ArrayList<>();
                    try {
                        long annCount = Db.use().count(Entity.create(Announcement.class.getSimpleName()).set("id", a.getId()));
                        if(annCount==0){
                            a.setDir(new File(resultDir,stockName).getAbsolutePath());
                            a.setExist(0);
                            result.add(a);
                        }
                    } catch (SQLException throwables) {
                        log.error("", throwables);
                    }
                    return result;
                }).reduce((a, b) -> new ArrayList<Announcement>(){{addAll(a);addAll(b);}}).orElse(new ArrayList<Announcement>());
            }

        }

        @Override
        protected List<Announcement> reduce(List<Announcement> leftResult, List<Announcement> rightResult) {
            return new ArrayList<Announcement>(){{addAll(leftResult);addAll(rightResult);}};
        }

        @Override
        public BaseRecursiveTask getBaseRecursiveTask(List<Announcement> dataList, int THRESHOLD_NUM) {
            return new AnnNotExistTask(dataList,THRESHOLD_NUM,stockName);
        }
    }




}
