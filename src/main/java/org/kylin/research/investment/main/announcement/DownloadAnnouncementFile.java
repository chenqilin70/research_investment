/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.announcement;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import jodd.http.HttpRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.entity.da.DataAccess;
import org.kylin.research.investment.util.BaseRecursiveTask;
import org.kylin.research.investment.util.ForkJoinExecutor;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqlGetter;
import org.kylin.research.investment.util.TryUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Download the announcement file, deposited in the disk
 *
 * @author chenqilin
 * @date 2022-01-23 15:36:26
 */
@Slf4j
public class DownloadAnnouncementFile {
    private static SqlGetter sqlGetter = new SqlGetter("announcement");

    @SneakyThrows
    public static void main(String[] args) {
        log.info("start download the announcement file");
        List<Entity> entitys = Db.use().query(sqlGetter.getSql("undownloadAnn").getSql());
        List<Announcement> anns = entitys.stream().map(e -> e.toBeanIgnoreCase(Announcement.class)).collect(Collectors.toList());
        //不能高并发下载，不然会有很多文件下载失败
        Integer result = ForkJoinExecutor.exec(new DownloadAnnTask(anns, 50), 1, 60 * 60*5);
        log.info("end download the announcement file，search count:" + anns.size() + "，download count:" + result );

    }

    public static class DownloadAnnTask extends BaseRecursiveTask<Announcement, Integer> {

        public DownloadAnnTask(List<Announcement> datas, int THRESHOLD_NUM) {
            super(datas, THRESHOLD_NUM);
        }

        @Override
        public Integer run(List<Announcement> perIncrementList) {
            return perIncrementList.stream().map(a ->{
                Integer integer = TryUtil.runTry(5, 5, 0, () -> {
                    int result = 0;
                    File targetFile = new File(FileUtil.mkdir(a.getDir()), a.getFileName().replaceAll("\\\\","-").replaceAll("/","-")
                            .replaceAll("\\?","-").replaceAll(">","-").replaceAll("<","-").replaceAll("\\*","-").replaceAll(":","-"));
                    byte[] fileBytes = HttpRequest.get(a.getUrl()).timeout(10 * 60 * 1000).connectionTimeout(10 * 60 * 1000).send().bodyBytes();

                    if (FileUtil.exist(targetFile)) {
                        ThreadUtil.sleep(RandomUtil.randomLong(100,200));
                        targetFile = new File(FileUtil.mkdir(a.getDir()), FileUtil.mainName(targetFile)+"_"+new Date().getTime()+"."+FileUtil.getSuffix(targetFile));
                        log.info("发现相同文件名，已修改为："+targetFile.getName());
                        synchronized (DownloadAnnouncementFile.class) {
                            Db.use().execute(sqlGetter.getSql("setAnnFilename", MapCreator.SS.create("id", a.getId(),"fileName",targetFile.getName())).getSql());
                        }
                    }
                    if(!a.getFileName().equals(targetFile.getName())){
                        log.info("文件名已被修改");
                        Db.use().execute(sqlGetter.getSql("setAnnFilename", MapCreator.SS.create("id", a.getId(),"fileName",targetFile.getName())).getSql());
                    }


                    while(true){
                        try {
                            if(fileBytes.length!=0 && fileBytes.length < 600 && targetFile.getName().endsWith("pdf") && new String(fileBytes).contains("</a>")){
                                log.info("数据过少，怀疑是html："+new String(fileBytes));
                                Elements as = Jsoup.parse(new String(fileBytes)).getElementsByTag("a");
                                if(as!=null && as.size()>0){
                                    Element element = as.get(0);
                                    String href = element.attr("HREF");
                                    if(StrUtil.isNotBlank(href)){
                                        log.info("访问新链接："+href);
                                        fileBytes = HttpRequest.get(href).timeout(10 * 60 * 1000).connectionTimeout(10 * 60 * 1000).send().bodyBytes();
                                        log.info("新数据为："+fileBytes.length);
                                    }else{
                                        break;
                                    }
                                }else{
                                    break;
                                }
                            }else{
                                break;
                            }
                        }catch (Throwable t){
                            log.error("解析html类型pdf报错，打断循环",t);
                            break;
                        }

                    }



                    FileUtil.writeBytes(fileBytes, targetFile);
                    log.info("下载文件："+a.getUrl());
                    log.info("文件名："+targetFile.getName());

                    synchronized (DownloadAnnouncementFile.class) {
                        result = Db.use().execute(sqlGetter.getSql("setAnnExist", MapCreator.SS.create("id", a.getId())).getSql());
                    }

                    return result;
                }, a.getUrl());
                if(integer==0){
                    synchronized (DownloadAnnouncementFile.class) {
                        try {
                            Db.use().execute(sqlGetter.getSql("setAnnDownloadError", MapCreator.SS.create("id", a.getId())).getSql());
                        } catch (SQLException e) {
                            log.error("",e);
                        }
                    }
                }
                return integer;
            }).reduce((a, b) -> a + b).orElse(0);
        }

        @Override
        protected Integer reduce(Integer leftResult, Integer rightResult) {
            return leftResult + rightResult;
        }

        @Override
        public BaseRecursiveTask getBaseRecursiveTask(List<Announcement> dataList, int THRESHOLD_NUM) {
            return new DownloadAnnTask(dataList, THRESHOLD_NUM);
        }
    }
}
