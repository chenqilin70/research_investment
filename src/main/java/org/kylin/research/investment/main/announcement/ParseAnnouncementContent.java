/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.announcement;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.poi.word.WordUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.util.BaseRecursiveTask;
import org.kylin.research.investment.util.ForkJoinExecutor;
import org.kylin.research.investment.util.FileReader;
import org.kylin.research.investment.util.SqlGetter;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parse the contents of the announcement file, deposited in the mysql
 *
 * @author chenqilin
 * @date 2022-01-24 10:16:44
 */
@Slf4j
public class ParseAnnouncementContent {

    public static final SqlGetter sqlGetter = new SqlGetter("announcement");

    @SneakyThrows
    public static void main(String[] args) {
        log.info("start parse the contents of the announcement file");
        List<Announcement> anns = Db.use().query(sqlGetter.getSql("noneContentAnn").getSql()).stream().map(e -> e.toBeanIgnoreCase(Announcement.class)).collect(Collectors.toList());
        Integer result = ForkJoinExecutor.exec(new ParseContentTask(anns, 10), 20, 60 * 60*5);
        log.info("end parse the contents of the announcement file,search count" + anns.size() + "，parse count:" + result);
    }

    public static class ParseContentTask extends BaseRecursiveTask<Announcement, Integer> {

        public ParseContentTask(List<Announcement> datas, int THRESHOLD_NUM) {
            super(datas, THRESHOLD_NUM);
        }

        @Override
        public Integer run(List<Announcement> anns) {
            return anns.stream().map(a -> {
                int result = 0;
                try {
                    if (a.getFileName().endsWith("pdf")) {
                        result=setContent(FileReader.getPdfContent(new File(a.getDir(), a.getFileName())),a.getId());
                    } else if(a.getFileName().endsWith("doc") || a.getFileName().endsWith("docx")) {
                        result=setContent(FileReader.getWordContent(new File(a.getDir(), a.getFileName())),a.getId());
                    } else {
                        result=setContent(FileUtil.readString(new File(a.getDir(), a.getFileName()), Charset.forName("UTF-8")),a.getId());
                    }

                } catch (Exception e) {
                    log.error("解析文件报错,文件："+a.getFileName()+"。",e);
                    try {
                        Db.use().update(Entity.create(Announcement.class.getSimpleName()).set("content", "ERROR"),
                                Entity.create(Announcement.class.getSimpleName()).set("id", a.getId()));
                    } catch (SQLException throwables) {
                        log.error("",e);
                    }
                }
                return result;
            }).reduce((a, b) -> a + b).orElse(0);
        }

        @SneakyThrows
        public int setContent(String content, Long id){
            synchronized (ParseAnnouncementContent.class){
                if (StrUtil.isNotBlank(content)) {
                    return Db.use().update(Entity.create(Announcement.class.getSimpleName()).set("content", content),
                            Entity.create(Announcement.class.getSimpleName()).set("id", id));
                } else {
                    return Db.use().update(Entity.create(Announcement.class.getSimpleName()).set("content", "NONE"),
                            Entity.create(Announcement.class.getSimpleName()).set("id", id));
                }
            }
        }

        @Override
        protected Integer reduce(Integer leftResult, Integer rightResult) {
            return leftResult + rightResult;
        }

        @Override
        public BaseRecursiveTask getBaseRecursiveTask(List<Announcement> dataList, int THRESHOLD_NUM) {
            return new ParseContentTask(dataList, THRESHOLD_NUM);
        }
    }

}
