/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.announcement;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.setting.dialect.PropsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.dao.SnowballDao;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.entity.Stock;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqlGetter;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * 公告标题快速检索
 * @author chenqilin
 * @date 2022-01-03 16:36:01
 */
@Slf4j
public class SearchAnnouncement {
    public static final String resultDir="D:\\invest\\research_investment_result\\SearchAnnouncement";
    public static final SnowballDao snowballDao=new SnowballDao();
    public static List<Stock> stockList= new ArrayList(){{
        PropsUtil.get("stock.properties").forEach((k,v)->add(new Stock(k.toString(),v.toString())));
    }};
    public static final SqlGetter sqlGetter = new SqlGetter("announcement");


    @SneakyThrows
    public static void main(String[] args) {
        while(true){
            Stock stock = getStock();

            while(true){
                log.info("["+stock.getName()+"]Please enter a query keywords：");
                final String searchStr = new Scanner(System.in).nextLine();
                if(StrUtil.isBlank(searchStr)) break;
                String sql =  sqlGetter.getSql("searchAnnouncement", MapCreator.SS.create("stockCode", stock.getCode(), "searchStr", searchStr)).getSql();
                List<Announcement> ann = Db.use().query(sql).stream().map(e->e.toBeanIgnoreCase(Announcement.class)).collect(Collectors.toList());
                String timeTag = DateUtil.format(new Date(), "yyyyMMdd-HHmmss");
                ann.stream().forEach(a->{
                    File targetDir=null;
                    if(a.getFileName().contains(searchStr)){
                        targetDir = new File(resultDir,  timeTag+ "_" + searchStr+"/fileName");
                    }else{
                        targetDir = new File(resultDir, timeTag + "_" + searchStr+"/content");
                    }

                    File targetFile = new File(targetDir, a.getFileName());
                    FileUtil.copyFile(new File(a.getDir(),a.getFileName()),targetFile, StandardCopyOption.REPLACE_EXISTING);
                    log.info(targetFile.getAbsolutePath());
                });
            }
            log.info("exit SearchAnnouncement of "+stock.getName());
        }
    }



    /**
     * Get the user required securities
     * @return
     */
    public static Stock getStock() {
        StringBuffer sb=new StringBuffer("\n");
        for(int i =0;i<stockList.size();i++){
            if(i%5==0) sb.append("\n");
            sb.append(i+"."+stockList.get(i).getName()+"("+stockList.get(i).getCode()+")\t\t");
        }
        log.info(sb.toString());
        log.info("Please select a stock ：");
        String index= new Scanner(System.in).next();
        Stock selected = stockList.get(Integer.parseInt(index));
        log.info("Have you choose for "+selected.getName());
        return selected;
    }








}
