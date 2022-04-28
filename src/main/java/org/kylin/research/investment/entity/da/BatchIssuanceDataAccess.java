/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.entity.da;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 批签发数据集-数据访问
 *
 * @author chenqilin
 * @date 2022-01-19 16:08:32
 */
@Data
@Accessors(chain = true)
public class BatchIssuanceDataAccess  extends MechanismPageDataAccess{

    /**
     * 标题
     */
    private String title;

    @Override
    public String toString() {
        return super.toString();
    }
}
