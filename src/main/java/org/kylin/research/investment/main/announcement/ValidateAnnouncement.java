/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.announcement;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.setting.dialect.PropsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.Test;
import org.kylin.research.investment.dao.SnowballDao;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.entity.Stock;
import org.kylin.research.investment.util.FileReader;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqlGetter;
import org.kylin.research.investment.util.SqliteUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证公告下载情况
 *
 * @author chenqilin
 * @date 2022-04-11 15:04:19
 */
@Slf4j
public class ValidateAnnouncement {

    public static List<Stock> stockList= new ArrayList(){{
        PropsUtil.get("stock.properties").forEach((k,v)->add(new Stock(k.toString(),v.toString())));
    }};
    public static final SqlGetter sqlGetter = new SqlGetter("announcement");

    public static SnowballDao snowballDao=new SnowballDao();

    public static File dir = new File("D:\\invest\\research_investment_result\\announcements");

    public static void main(String[] args) throws SQLException {
        for(Stock s:stockList){

            List<Announcement> announcement = snowballDao.getAnnouncement(s.getCode());
            long snowballCount = announcement.stream().map(a->a.getId()).distinct().count();

            SqlGetter.Sql sql = sqlGetter.getSql("annCountByStockCode", MapCreator.SS.create("stockCode",s.getCode()));
            Entity entity = Db.use().queryOne(sql.getSql());
            Long dbcount = entity.getLong("c");

            int fileCount = new File(dir, s.getName()).listFiles().length;

            StringBuffer sb=new StringBuffer(s.getName()+"\t");

            if(snowballCount!=dbcount || snowballCount!=fileCount){
                if(snowballCount!=dbcount){
                    sb.append("数据缺失"+snowballCount+">>"+dbcount+"\t");
                }
                if(snowballCount!=fileCount){
                    sb.append("文件缺失"+snowballCount+">>"+fileCount);
                }
                log.info(sb.toString());
            }
        }
    }

    /**
     * 查找所有在数据库中存在但在磁盘中不存在的文件
     * @throws SQLException
     */
    @Test
    public void fileDisable() throws SQLException {
        for(Stock s:stockList){
            SqlGetter.Sql sql = sqlGetter.getSql("fileDisable", MapCreator.SS.create("stockCode",s.getCode()));
            List<Entity> lines = Db.use().query(sql.getSql());
            for(Entity entity : lines){
                File f=new File(entity.getStr("file"));
                if(!FileUtil.exist(f)){
                    log.info(entity.getInt("id")+"==>"+f.getAbsolutePath());
                    Db.use().update(
                            Entity.create(Announcement.class.getSimpleName()).set("content",null).set("exist",0),
                            Entity.create(Announcement.class.getSimpleName()).set("id",entity.getInt("id"))
                    );
                }
            }
        }
    }

    /**
     * 处理文件名相同导致下载被覆盖的文件
     */
    @Test
    public void equalFilename() throws SQLException {
        SqlGetter.Sql sql = sqlGetter.getSql("equalFilename");
        List<Entity> lines = Db.use().query(sql.getSql());
        for(Entity e:lines){
            String dir = e.getStr("dir");
            String fileName = e.getStr("fileName");
            FileUtil.del(new File(new File(dir), fileName));

        }
    }

    /**
     * 解析错误的文件为下载失败，需要删掉重新下载
     * @throws SQLException
     */
    @Test
    public void unParse() throws SQLException {
//        String sql = sqlGetter.getSql("unParseAnn").getSql();
        String sql="SELECT * FROM Announcement a WHERE crawlTime like '2022-04-14%'";
        List<Entity> lines = Db.use().query(sql);
        for(Entity e:lines){
            Announcement a = e.toBeanIgnoreCase(Announcement.class);
            File file = new File(new File(a.getDir()), a.getFileName());
            log.info("删除："+file.getAbsolutePath());
            FileUtil.del(file);
            Db.use().update(
                    Entity.create(Announcement.class.getSimpleName()).set("content",null).set("exist",0),
                    Entity.create(Announcement.class.getSimpleName()).set("id",a.getId())
            );

        }

    }

    /**
     * 查询磁盘中的空文件
     */
    @Test
    public void nullFile(){
        System.out.println("a\\b/c");
        System.out.println("a\\b/c".replaceAll("\\\\","-").replaceAll("/","-"));
//        List<File> files = FileUtil.loopFiles(dir);
//        int count=0;
//        for(File f:files){
//            if(FileUtil.size(f)==0 || FileUtil.size(f)==1){
//                log.info("即将删除:"+f.getAbsolutePath());
//                FileUtil.del(f);
//                count++;
//            }
//        }
//        log.info("已成功删除"+count+"个文件");
    }

}
