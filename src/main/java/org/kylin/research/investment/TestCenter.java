/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.kylin.research.investment.entity.Announcement;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author chenqilin
 * @date 2022-01-12 21:52:22
 */
@Slf4j
public class TestCenter {
    public static void main(String[] args) {
        File file = new File("D:\\invest\\research_investment_result\\SearchAnnouncement\\20220412-224943_度报告\\fileName");
        List<String> deleteName = Arrays.asList("正文", "摘要", "督导","披露","独立意见","提示性","更正公告","补充");
        Arrays.stream(file.listFiles()).filter(f->deleteName.stream().map(s->f.getName().contains(s)).reduce((a,b)->a || b).get()).forEach(f->{
            System.out.println("删除"+f.getName());
            f.deleteOnExit();
        });
    }

    @SneakyThrows
    @Test
    public void test(){
        List<Entity> all = Db.use().findAll(Announcement.class.getSimpleName());
        List<Announcement> collect = all.stream().map(e -> e.toBeanIgnoreCase(Announcement.class)).collect(Collectors.toList());
        collect.stream().forEach(a->{
            if(!FileUtil.exist(new File(a.getDir(),a.getFileName()))){
                System.out.println(a.getFileName());
            }
        });
    }

    @SneakyThrows
    @Test
    public void test2(){
        List<Entity> query = Db.use().query("select  content,REPLACE( replace(content , x'0A','') ,x'0D','') as result from announcement limit 1");
        log.info(query.get(0).get("result").toString());

    }
}











