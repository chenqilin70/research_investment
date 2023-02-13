/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;


/**
 * 请加入注释
 *
 * @author chenqilin
 * @date 2022-05-31 11:41:27
 */
public class TestCenter {
    /**
     * 企业年化收益计算
     */
    @Test
    public void annualizedRateOfReturn(){
        Double start= 1d;
        Double end =1.55d;
        Double year = 2d;

        String rate = new BigDecimal((Math.pow(end / start, 1.000/year)-1)*100).setScale(4, RoundingMode.HALF_UP).toString();
        System.out.println(rate);

    }
    /**
     * 收入增長比例
     */
    @Test
    public void incomeUpRate(){
        Double inconme=100.0000;
        Double save=inconme;
        for(int i=2;i<100;i++){
            int year=i/12;
            year++;
            int month=i%12;
            System.out.println("第"+year+"年，第"+month+"月收入增长："+new BigDecimal((inconme/save)*100.0000).setScale(2,RoundingMode.HALF_UP).doubleValue()+"%");
            save=save+inconme;
        }

    }

    /**
     * 收入增长模型
     */
    @Test
    public void incre(){
        double start = 70;
        double rate = 0.20;
        double shouru=2.5*12;
        for(int i=1;i<=10;i++){
            start=start*(1+rate)+shouru*(1+(rate/2));
            System.out.println(DateUtil.year(new Date()) +i+("年"+  (DateUtil.month(new Date())+1)  )+"月\t"+ NumberUtil.decimalFormat("0.00",start));
        }
    }

    /**
     * 股息复投模型
     */
    @Test
    public void dividend(){
        double start = 200;
        double rate = 0.07;
        double shouru=0.5*12;
        for(int i=0;i<10;i++){
            start=start*(1+rate)+shouru*(1+(rate/2));
            System.out.println(1+i+"\t"+start);
        }
    }

    /**
     * 收回本金模型
     */
    @Test
    public void principalRecovery(){
        double start = 1;
        double rate = 0.07;
        double shouyi=0;
        for(int i=0;i<20;i++){
            shouyi=shouyi+(start*rate);
            System.out.println(1+i+"\t"+shouyi);
        }
    }

    /**
     * 目标股息模型
     */
    @Test
    public void targetDivvy(){
        double targetDivvy=20;
        for(int b=100;b<500;b=b+10){
            double  rate = new BigDecimal((targetDivvy/b)*100).setScale(2,RoundingMode.HALF_UP).doubleValue();
            System.out.println(b+"w本金，需A股股息率"+rate+"%,需港股股息率"+new BigDecimal((rate/0.8)).setScale(2,RoundingMode.HALF_UP)+"%");
        }
    }
}
