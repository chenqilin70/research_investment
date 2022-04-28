/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公告
 *
 * @author chenqilin
 * @date 2022-01-04 09:54:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Announcement {
    /**
     * 唯一标识
     */
    private Long id;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 地址
     */
    private String url;
    /**
     * 上传时间
     */
    private String uploadDate;
    /**
     * 文字内容
     */
    private String content;
    /**
     * 磁盘地址
     */
    private String dir;
    /**
     * 代码
     */
    private String stockCode;
    /**
     * 采集时间
     */
    private String crawlTime;
    /**
     * 磁盘文件是否存在  1-存在 0-不存在
     */
    private Integer exist;
}
