/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.qa;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.entity.HuDongYiQA;
import org.kylin.research.investment.entity.Stock;
import org.kylin.research.investment.main.announcement.SearchAnnouncement;
import org.kylin.research.investment.util.MapCreator;
import org.kylin.research.investment.util.SqlGetter;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * 导出问答
 *
 * @author chenqilin
 * @date 2022-03-29 13:49:44
 */
@Slf4j
public class ExportQA {
    public static final SqlGetter sqlGetter = new SqlGetter("qa");
    public static final File resultDir=new File("D:\\tempFile\\research_investment_result\\ExportQA");
    public static void main(String[] args) throws SQLException {
        while(true){
            Stock stock = SearchAnnouncement.getStock();
            while(true){
                log.info("["+stock.getName()+"]Please enter a query keywords：");
                final String searchStr = new Scanner(System.in).nextLine();
                if(StrUtil.isBlank(searchStr)) break;
                String sql =  sqlGetter.getSql("exportQA", MapCreator.SS.create("stockCode", stock.getCode(), "searchStr", searchStr)).getSql();
                List<HuDongYiQA> qaList = Db.use().query(sql).stream().map(e->e.toBeanIgnoreCase(HuDongYiQA.class)).collect(Collectors.toList());
                StringBuffer content=new StringBuffer("# "+searchStr+"\n");
                for(HuDongYiQA qa:qaList){

                    content.append("##### "+DateUtil.format(new Date(qa.getPubDate()),"yyyyMMdd")+"问："+qa.getQuestion()+"\n" );
                    content.append("\n");
                    content.append(">  答: "+qa.getAnswer()+"(更新时间："+DateUtil.formatDate(new Date(qa.getUpdateDate()))+")\n---\n");
                }
                content.append("over!");
                FileUtil.writeUtf8String(content.toString(),new File(resultDir, DateUtil.format(new Date(),"yyyyMMdd-HHmmss")+"_"+searchStr+".md"));
            }
        }

    }
}
