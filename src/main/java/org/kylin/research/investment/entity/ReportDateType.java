/*
 * Copyright (c) 2020-2030 江苏丰尚
 * 不能修改和删除上面的版权声明
 * 此代码属于江苏丰尚深圳研究院编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.entity;

/**
 * 财务报表类型
 *
 * @author chenqilin
 * @date 2022-08-14 14:49:05
 */

public enum ReportDateType {
    ZYZB(0,"zyzb","主要指标"),
    ZCFZB(1,"zcfzb","资产负债表"),
    LRB(2,"lrb","利润表"),
    XJLLB(3,"xjllb","现金流量表");

    private Integer code;
    private String name;
    private String desc;

    ReportDateType(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
