/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.entity.da;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.kylin.research.investment.dao.BaseDao;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据访问
 *
 * @author chenqilin
 * @date 2022-01-04 10:58:38
 */
@Data
@Accessors(chain = true)
public class DataAccess {
    public static final JSONObject DATA_ACCESS_JSON = JSON.parseObject( FileUtil.readString("data_access.json", Charset.forName("utf-8")));

    private String url;
    private String type= BaseDao.HTTP_TYPE_GET;
    private Map<String,String> header=new HashMap<>();
    private Map<String,String> param=new HashMap<>();

    @Override
    public String toString() {
        return url+"?"+param.keySet().stream().map(p->p+"="+param.get(p)).reduce((a,b)->a+"&"+b).get();
    }

    /**
     * 获取配置的连接信息
     * @param key
     * @return
     */
    public DataAccess load(String key){
        String getAnnouncement = DATA_ACCESS_JSON.getJSONObject(key).toJSONString();
        DataAccess dataAccess = JSON.parseObject(getAnnouncement, DataAccess.class);
        dataAccess.setHeader(getHeader(key));
        BeanUtil.copyProperties(dataAccess,this,false);
        return this;
    }

    /**
     * URL转化为DataAccess
     * @return
     */
    public  DataAccess load(URL url){
        this.setUrl(url.getProtocol()+"://"+url.getHost()+url.getPath());
        String query = url.getQuery();
        if(StrUtil.isNotBlank(query)){
            String[] params = query.split("&");
            for(String p:params){
                String[] pstr = p.split("=");
                if(pstr.length==2){
                    this.getParam().put(pstr[0], URLUtil.decode(pstr[1],"utf-8"));
                }
            }
        }
        return this;
    }

    /**
     * 读取配置的resources/header目录下header信息
     * @param key
     * @return
     */
    public static Map<String,String> getHeader(String key) {
        Map<String,String> header=new HashMap<>();
        File headFile = FileUtil.file(MessageFormat.format("header/{0}.txt", key));
        if(FileUtil.exist(headFile)){
            List<String> lines = FileUtil.readLines(headFile, "utf-8");
            if(CollUtil.isNotEmpty(lines)){
                for(String line:lines){
                    int i = line.indexOf(": ");
                    String headName = line.substring(0, i).trim();
                    String headValue = line.substring(i+1).trim();
                    if(StrUtil.isNotBlank(headName) ){
                        header.put(headName,headValue);
                    }
                }
            }
        }
        return header;
    }

}
