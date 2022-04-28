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
 * 证券
 *
 * @author chenqilin
 * @date 2022-01-05 21:42:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    private String code;
    private String name;
}
