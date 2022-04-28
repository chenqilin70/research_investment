/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.dao;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.kylin.research.investment.entity.Announcement;
import org.kylin.research.investment.entity.da.DataAccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * xueqiu.com 数据访问
 *
 * @author chenqilin
 * @date 2022-01-04 11:04:13
 */
@Slf4j
public class SnowballDao extends BaseDao{

    /**
     * 获取全部公告列表
     * @return
     */
    public  List<Announcement> getAnnouncement(String stock) {
        TimeInterval timer = DateUtil.timer();
        ExecutorService pool = Executors.newFixedThreadPool(50);
        CompletionService<List<Announcement>> service = new ExecutorCompletionService<>(pool);
        List<Announcement> anns=new ArrayList<>();
        try {
            Integer maxPage=getMaxPage(stock);
            for(int page=1;page<=maxPage;page++){
                final Integer pageFinal=page;
                service.submit(()->getAnnouncementPage(stock,pageFinal));
            }
            for(int page=1;page<=maxPage;page++){
                anns.addAll(service.take().get());
            }
        }catch (Throwable e){
            log.error("getAnnouncement 报错："+ ExceptionUtil.stacktraceToString(e));
        }finally {
            pool.shutdown();
        }
        log.info("公告查询完毕，条数："+anns.size()+",耗时："+timer.intervalSecond()+"秒");
        return anns;
    }

    /**
     * 获取公告列表最大页数
     * @return
     */
    private Integer getMaxPage(String stock ){
        DataAccess dataAccess = new DataAccess().load("getAnnouncement");
        dataAccess.getParam().put("page","1");
        dataAccess.getParam().put("symbol_id",stock);
        JSONObject body = JSON.parseObject(getBodyText(dataAccess));
        return body.getInteger("maxPage");
    }



    /**
     * 获取公告列表的某一页
     * @return
     */
    public  List<Announcement> getAnnouncementPage(String stock , int page){
        List<Announcement> anns=new ArrayList<>();
        DataAccess dataAccess = new DataAccess().load("getAnnouncement");
        dataAccess.getParam().put("page",page+"");
        dataAccess.getParam().put("symbol_id",stock);
        JSONObject body = JSON.parseObject(getBodyText(dataAccess));

        JSONArray list = body.getJSONArray("list");
        for (Object o : list) {
            String description = ((JSONObject) o).getString("description");
            String uploadDate = DateUtil.format(new Date( ((JSONObject) o).getLong("created_at") ),"yyyyMMdd");
            Long id = ((JSONObject) o).getLong("id");

            String href = Jsoup.parse(description).select("a").attr("href");
            if(!description.contains("<a ")) continue;
            String fileName=description.substring(0,description.indexOf("<a ")).trim()
                    .replaceAll("：","_")
                    .replaceAll("PDF文件下载:","")
                    .trim()
                    .replaceAll(" ","_")
                    .replaceAll("/","_")
                    +"_"+id+"."+href.substring(href.lastIndexOf(".")+1).toLowerCase().trim();

            fileName=uploadDate+"_"+fileName;
            anns.add(new Announcement(id,fileName,href,uploadDate,null,null,stock,DateUtil.formatDateTime(new Date()),null));
        }
        return anns;
    }

}
