/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.entity;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 批签发量
 * [序号, 产品名称, 批号, 有效期至, 生产企业, 证书编号, 签发结论, 批签发机构]
 * [序号, 产品名称, 规格, 批号, 签发量, 有效期至, 生产企业, 收检编号, 证书编号, 报告编号, 签发日期, 签发结论, 批签发机构]
 * @author chenqilin
 * @date 2022-01-19 20:51:32
 */
@Data
@Accessors(chain = true)
public class BatchIssuance {

    public static final Map<String,Field> CHINESE_MAP=Arrays.stream(ReflectUtil.getFields(BatchIssuance.class)).filter(f -> f.getAnnotation(Chinese.class)!=null).collect(Collectors.toMap(f -> f.getAnnotation(Chinese.class).value(), f -> f));

    @Chinese("序号")
    private String number;

    @Chinese("产品名称")
    private String productName;

    @Chinese("规格")
    private String specifications;

    @Chinese("批号")
    private String batchNumber;

    @Chinese("签发量")
    private String issuedQuantity;

    @Chinese("有效期至")
    private String validUntil;

    @Chinese("生产企业")
    private String productCompany;

    @Chinese("收检编号")
    private String receivingInspectionNo;

    @Chinese("证书编号")
    private String certificateNo;

    @Chinese("报告编号")
    private String reportNo;

    @Chinese("签发日期")
    private String dateOfIssue;

    @Chinese("签发结论")
    private String issuingConclusion;

    @Chinese("批签发机构")
    private String batchIssuingAuthority;

    @Chinese("数据地址")
    private String url;

    @Chinese("批签发数据集标题")
    private String batchIssuanceDatasetTitle;

    @Chinese("数据集MD5")
    private String datasetMd5;

    @Chinese("保存时间")
    private Date saveTime;

    public  void setValueByChinese(String name,String value){
        Field field = CHINESE_MAP.get(name);
        if(field!=null){
            ReflectUtil.setFieldValue(this,field,value);
        }
    }



}
