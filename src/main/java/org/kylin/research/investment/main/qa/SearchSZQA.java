/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.qa;

import cn.hutool.db.meta.MetaUtil;
import cn.hutool.setting.dialect.PropsUtil;
import org.kylin.research.investment.dao.HuDongYiDao;
import org.kylin.research.investment.entity.HuDongYiQA;
import org.kylin.research.investment.entity.Stock;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取深证问答
 *
 * @author chenqilin
 * @date 2022-02-21 16:02:55
 */
public class SearchSZQA {

    public static List<Stock> stockList= new ArrayList<>(){{
        PropsUtil.get("stock.properties").forEach((k, v)->add(new Stock(k.toString(),v.toString())));
    }};
    public static HuDongYiDao huDongYiDao=new HuDongYiDao();

    public static void main(String[] args) {
        List<Stock> stocks = stockList.stream().filter(s -> s.getCode().startsWith("SZ")).collect(Collectors.toList());
        stocks.stream().forEach(s->{
            System.out.println("----------------------"+s.getName()+"----------------------------");
            List<HuDongYiQA> stockAllQA = huDongYiDao.getStockAllQA(s);
            huDongYiDao.insertQA(stockAllQA);
        });
    }

}
