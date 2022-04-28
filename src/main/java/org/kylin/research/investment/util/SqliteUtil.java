/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.meta.MetaUtil;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.entity.BatchIssuance;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sqlite 使用工具
 *
 * @author chenqilin
 * @date 2022-01-20 15:36:38
 */
@Slf4j
public class SqliteUtil {
    public static SqlGetter commonGetter=new SqlGetter("common");

    /**
     * 获取根据JavaBean建表的SQL
     * @param clazz
     * @return
     */
    public static  SqlGetter.Sql getCreateTableSql(Class clazz){
        Field[] fields = clazz.getDeclaredFields();
        String columns = Arrays.stream(fields).filter(f-> !Modifier.isStatic(f.getModifiers())).map(f -> {
            String subSql = f.getName() + " ";
            Class<?> type = f.getType();
            if (String.class == type) {
                subSql = subSql + "TEXT";
            } else if (Integer.class == type || Long.class == type) {
                subSql = subSql + "INT";
            } else if (Float.class == type || BigDecimal.class == type || Double.class == type) {
                subSql = subSql + "REAL";
            } else {
                subSql = subSql + "TEXT";
            }
            return subSql;
        }).reduce((a, b) -> a + "," + b).orElse("");
        return commonGetter.getSql("createTable", MapCreator.SS.create("tableName", clazz.getSimpleName(),"columns",columns));
    }

    @SneakyThrows
    public static void truncateTable(Class clazz){
        SqlGetter.Sql sql = commonGetter.getSql("truncateTable",MapCreator.SS.create("tableName",clazz.getSimpleName()));
        Db.use().execute(sql.getSql());
    }

    @SneakyThrows
    public static void createTable(Class clazz){
        SqlGetter.Sql sql = getCreateTableSql(clazz);
        Db.use().execute(sql.getSql());
    }
    public static boolean existTable(Class clazz){
        List<String> tables = MetaUtil.getTables(DSFactory.get());
        if(CollUtil.isNotEmpty(tables)){
            tables = tables.stream().map(t -> t.toLowerCase()).collect(Collectors.toList());
            return tables.contains(clazz.getSimpleName().toLowerCase());
        }else{
            return false;
        }
    }


    @SneakyThrows
    public static void main(String[] args) {
//        createTable(BatchIssuance.class);
        for(int i=0;i<10;i++){
            BatchIssuance bi=new BatchIssuance();
            bi.setBatchNumber(UUID.randomUUID().toString());
            bi.setReportNo(""+i);
            bi.setCertificateNo("abc"+i);
            int insert = Db.use().insert(Entity.parse(bi));
            log.info("插入"+insert+"条");
        }
        List<Entity> all = Db.use().findAll(BatchIssuance.class.getSimpleName());

        all.stream().forEach(e->{
            log.info(JSON.toJSONString(e.toBean(BatchIssuance.class)));
        });

    }

}
