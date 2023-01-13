/*
 * Copyright (c) 2020-2030 江苏丰尚
 * 不能修改和删除上面的版权声明
 * 此代码属于江苏丰尚深圳研究院编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.share;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.kylin.research.investment.dao.FinanceDao;
import org.kylin.research.investment.dao.ShareDao;
import org.kylin.research.investment.entity.da.DataAccess;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请加入注释
 *
 * @author chenqilin
 * @date 2022-11-10 21:39:17
 */
@Slf4j
public class ShareCatcher {
    private static FinanceDao financeDao=new FinanceDao();
    private static ShareDao shareDao=new ShareDao();
    private static File dir=new File("D:\\invest\\finance");
    public static void main(String[] args) {
        List<String> codes=financeDao.getAllStock();
        for(String code:codes){
            try{
                Map<String, Long> map = shareDao.getShare(code);
                File lrbFile = new File(dir, code + "_LRB.json");
                File zyzbFile = new File(dir, code + "_ZYZB.json");
                if(FileUtil.exist(lrbFile) && CollUtil.isNotEmpty(map) && map.size()>=5){
                    JSONObject lrbJson = JSON.parseObject(FileUtil.readString(lrbFile, Charset.forName("utf-8")));
                    JSONObject zyzbJson = JSON.parseObject(FileUtil.readString(zyzbFile, Charset.forName("utf-8")));
                    String name="";

                    List<String> years = new ArrayList<>(map.keySet());
                    years.sort((a,b)->a.compareTo(b));
                    years = years.subList(0,5);
                    boolean flg=true;
                    StringBuffer sb = new StringBuffer("");
                    for(String year:years){
                        double jlr = lrbJson.getJSONObject(year + "-12-31").getDouble("NETPROFIT");
                        name = lrbJson.getJSONObject(year + "-12-31").getString("SECURITY_NAME_ABBR");
                        double roe = zyzbJson.getJSONObject(year + "-12-31").getDouble("ROEKCJQ");
                        Long fh = map.get(year);
                        double gxzfl=fh/jlr;
                        if(gxzfl<0.5 || jlr<0 || roe<6){
                            flg=false;
                            break;
                        }else{
                            sb.append(year+":息支率="+new BigDecimal(gxzfl*100).setScale(2, RoundingMode.HALF_UP) +"%，ROE="+roe);
                        }
                    }
                    if(flg){
                        log.info(code+"\t"+name+"\t"+sb);
                    }
                }
            }catch (Throwable t){
                log.error(code+"分析失败,"+t.getMessage());
            }
        }

    }
}
