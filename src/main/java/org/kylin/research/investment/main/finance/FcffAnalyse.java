/*
 * Copyright (c) 2020-2030 江苏丰尚
 * 不能修改和删除上面的版权声明
 * 此代码属于江苏丰尚深圳研究院编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.finance;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.entity.ReportDateType;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 自由现金流
 *
 * @author chenqilin
 * @date 2022-08-14 18:28:20
 */
@Slf4j
public class FcffAnalyse {

    public static final File resultDir=new File("D:\\invest\\finance");

    /**
     * 𝑭𝑪𝑭𝑭 = 息税前经营利润 𝑬𝑩𝑰𝑻 ∗ (𝟏 − 所得税率 𝑻𝒄) + 折旧摊销 𝑫&𝑨 − 资本支出 𝑪𝒂𝒑𝑬𝒙 − 净营运资本增加 △ 𝑵𝑾𝑪
     * 𝑭𝑪𝑭𝑭 = 净利润+利息支出-所得税   +   固定资产和投资性房地产折旧+无形资产摊销+长期待摊费用摊销   -   购建固定资产、无形资产和其他长期资产支付的现金  -   净营运资本增加
     * @param args
     */
    public static void main(String[] args) {
        String code="SZ300601";
        JSONObject lrbs = JSON.parseObject(getFinanceData(code, ReportDateType.LRB));
        JSONObject xjllbs = JSON.parseObject(getFinanceData(code, ReportDateType.XJLLB));
        JSONObject zcfzbs = JSON.parseObject(getFinanceData(code, ReportDateType.ZCFZB));
        List<String> years = lrbs.keySet().stream().filter(r -> r.endsWith("12-31")).collect(Collectors.toList());
        years.sort((a,b)->a.compareTo(b));
        for(String year:years){
            if(years.indexOf(year)==0){
                log.warn(year+"为初始年，忽略");
                continue;
            }

            JSONObject lrb = lrbs.getJSONObject(year);
            if(lrb==null || lrb.isEmpty()){
                log.warn(year+"利润表为空，忽略");
                continue;
            }

            JSONObject xjllb = xjllbs.getJSONObject(year);
            if(xjllb==null || xjllb.isEmpty()){
                log.warn(year+"现金流量表为空，忽略");
                continue;
            }


            Double netprofit = getValue(lrb,"NETPROFIT");//净利润
            Double finance_expense = getValue(lrb,"FINANCE_EXPENSE");//财务费用（利息支出）
            Double income_tax = getValue(lrb,"INCOME_TAX");//所得税

            Double fa_ir_depr = getValue(xjllb,"FA_IR_DEPR");//固定资产和投资性房地产折旧
            Double ia_amortize = getValue(xjllb,"IA_AMORTIZE");//无形资产摊销
            Double lpe_amortize = getValue(xjllb,"LPE_AMORTIZE");//长期待摊费用摊销

            Double construct_long_asset = getValue(xjllb,"CONSTRUCT_LONG_ASSET");//购建固定资产、无形资产和其他长期资产支付的现金

            Double currentNwc = NWC(zcfzbs, year);
            Double lastNwc = NWC(zcfzbs,(Integer.parseInt(year.substring(0, 4)) - 1) + "-12-31");

            Double A=netprofit + finance_expense - income_tax;
            Double B=fa_ir_depr + ia_amortize + lpe_amortize;
            Double C=construct_long_asset;
            Double D=(currentNwc - lastNwc);

            double fcff = A  + B - C - D;
            log.info(year+"\t"+ MessageFormat.format("{0}(利润端)  + {1}(折旧摊销) - {2}(再投入) - {3}(运营资本增加) = ",format(A),format(B),format(C),format(D))+format(fcff)+"亿元");

        }

    }

    public static String format(Double v){
        return new BigDecimal(v).divide(new BigDecimal(100000000)).setScale(2, RoundingMode.HALF_DOWN).toString();
    }

    /**
     * 净营运资本
     * @param zcfzbs
     * @param year
     * @return
     */
    private static Double NWC(JSONObject zcfzbs, String year) {
        JSONObject currentZcfz = zcfzbs.getJSONObject(year);
        Double total_current_assets = getValue(currentZcfz,"TOTAL_CURRENT_ASSETS");//流动资产合计

        Double monetaryfunds = getValue(currentZcfz,"MONETARYFUNDS");//货币资金

        Double trade_finasset_notfvtpl = getValue(currentZcfz, "TRADE_FINASSET_NOTFVTPL");//交易性金融资产
        Double lend_fund = getValue(currentZcfz, "LEND_FUND");//拆出资金

        Double total_current_liab = getValue(currentZcfz,"TOTAL_CURRENT_LIAB");//流动负债合计

        Double short_loan = getValue(currentZcfz,"SHORT_LOAN");//短期借款

        Double noncurrent_liab_1YEAR = getValue(currentZcfz,"NONCURRENT_LIAB_1YEAR");//一年内到期的非流动负债

        Double crrentNwc=total_current_assets-monetaryfunds-trade_finasset_notfvtpl-lend_fund-(total_current_liab-short_loan-noncurrent_liab_1YEAR);

        return crrentNwc;
    }

    public static Double getValue(JSONObject json,String key){
        return json.getBigDecimal(key)==null?0.00:json.getBigDecimal(key).doubleValue();
    }

    public static String getFinanceData(String code, ReportDateType type){
        File file = new File(resultDir, code + "_" + type.getName().toUpperCase() + ".json");
        return FileUtil.readString(file,"utf-8");
    }



}
