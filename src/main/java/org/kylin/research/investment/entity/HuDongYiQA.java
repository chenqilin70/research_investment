/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 互动易问答
 *
 * @author chenqilin
 * @date 2022-02-21 17:18:36
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class HuDongYiQA {
    private Long indexId;
    private String question;
    private String answer;
    private String stockCode;
    private Long pubDate;
    private Long updateDate;
}
