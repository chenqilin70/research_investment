/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.announcement;

import cn.hutool.setting.dialect.PropsUtil;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.entity.Stock;

import java.util.ArrayList;
import java.util.List;

/**
 * init announcement
 *
 * @author chenqilin
 * @date 2022-01-24 15:04:48
 */
@Slf4j
public class InitAnnouncement {

    public static List<Stock> stockList= new ArrayList<>(){{
        PropsUtil.get("stock.properties").forEach((k, v)->add(new Stock(k.toString(),v.toString())));
    }};

    public static void main(String[] args) {
        for(Stock stock:stockList){
            CrawlAnnouncementItem.main(new String[]{stock.getCode(),stock.getName()});
        }
        DownloadAnnouncementFile.main(new String[]{});
        ParseAnnouncementContent.main(new String[]{});
    }

}
