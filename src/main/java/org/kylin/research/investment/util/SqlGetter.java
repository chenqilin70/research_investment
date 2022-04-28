package org.kylin.research.investment.util;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 读取influxQL文件的工具类
 * @author chenqilin
 */
@Slf4j
public class SqlGetter {
    private static final String BASE_PATH = "sql/";
    private Map<String,Node> nodes = new HashMap<>();;
    private String type = null;
    private static final String PREFIX = "\\{", SUFFIX = "\\}";

    public SqlGetter(String type) {
        this.type = type;
        SAXReader reader = new SAXReader();
        Document doc=null;
        try (InputStream bizin = SqlGetter.class.getClassLoader().getResourceAsStream(BASE_PATH + type + ".xml");){
            doc = reader.read(bizin);
        } catch (Exception e1) {
            throw new RuntimeException("ERROR:", e1);
        }

        List<Node> allElements = doc.selectNodes("/sqls/*");
        for(Node e:allElements){
            nodes.put(e.getName(),e);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public Sql getSql(String id) {
        return buildInfluxQL(id,null);
    }
    public Sql getSql(String id,Map<String,Object> params) {
        return buildInfluxQL(id,params);
    }
    public Sql getSql(String id,Object[] params) {
        return buildInfluxQL(id,params);
    }
    public Sql getSql(String id,Object params) {
        return buildInfluxQL(id,params);
    }

    private Sql buildInfluxQL(String id,Object params) {
        Node e = nodes.get(id);
        Sql task=new Sql();
        task.setId(id);
        task.setNode(e);
        String sql = e.getText().replaceAll("\\n", " ").replaceAll("\\t", " ");;
        sql = ReUtil.replaceAll(sql," +"," ");
        task.setSql(fillParam(sql,params));
        log.info("构建Sql："+ task.getSql());
        return task;
    }

    /**
     * 将src中用大括号括起来的key替换成map中key所对应的value
     *
     * @param src
     * @param map
     * @return
     */
    private static String replace(String src, Map<String, String> map) {
        for (String k : map.keySet()) {
            if (map.get(k) != null) {
                src = src.replaceAll(pack(k), ObjectUtil.toString(map.get(k)));
            }
        }
        return src;
    }

    private static String pack(String src) {
        return PREFIX.concat(src).concat(SUFFIX);
    }

    private static String fillParam(String src, Object params) {
        String result=null;
        if (params==null) {
            result = src;
        } else {
            if (params instanceof Map) {
                result = replace(src, (Map<String, String>) params);
            } else if (params instanceof Object[]) {
                result = MessageFormat.format(src, (Object[]) params);
            } else {
                Map<String, String> param = Arrays.stream(ReflectUtil.getFields(params.getClass())).collect(Collectors.toMap(a -> ReflectUtil.getFieldName(a), a -> StrUtil.toString(ReflectUtil.getFieldValue(params, a))));
                result = replace(src, param);
            }
        }
        return result;
    }

    @Data
    public  static  class Sql  {
        private Node node;
        private String id;
        private String sql;
    }
}
