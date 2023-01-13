/*
 * Copyright (c) 2020-2030 江苏丰尚
 * 不能修改和删除上面的版权声明
 * 此代码属于江苏丰尚深圳研究院编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.dao.FinanceDao;
import org.kylin.research.investment.entity.ReportDateType;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 请加入注释
 *
 * @author chenqilin
 * @date 2022-08-14 13:18:13
 */
@Slf4j
public class FinanceCatcher {
    private static  FinanceDao financeDao=new FinanceDao();
    public static final File resultDir=new File("D:\\invest\\finance");

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<String> codes=financeDao.getAllStock();
        for(String code:codes){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
//                    download(code,ReportDateType.ZCFZB);
//                    download(code,ReportDateType.LRB);
//                    download(code,ReportDateType.XJLLB);
                    downloadZYZB(code,ReportDateType.ZYZB);
                }
            });

        }
        executorService.shutdown();


    }

    public static void download(String code,ReportDateType type){
        List<String> reportPeriod = financeDao.getReportPeriod(code, type);
        if(CollUtil.isNotEmpty(reportPeriod)){
            JSONObject data = financeDao.getFinanceData(code, reportPeriod, type);
            File targetFile = new File(resultDir, code + "_"+type.getName().toUpperCase()+".json");
            if (FileUtil.exist(targetFile)) {
                FileUtil.del(targetFile);
            }
            FileUtil.writeUtf8String(data.toJSONString(),targetFile);
        }
    }
    public static void downloadZYZB(String code,ReportDateType type){
        JSONObject data = financeDao.getFinanceZYZBData(code);
        File targetFile = new File(resultDir, code + "_"+type.getName().toUpperCase()+".json");
        if (FileUtil.exist(targetFile)) {
            FileUtil.del(targetFile);
        }
        FileUtil.writeUtf8String(data.toJSONString(),targetFile);
    }


}
