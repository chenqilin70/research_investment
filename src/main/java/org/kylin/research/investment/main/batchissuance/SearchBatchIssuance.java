/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.main.batchissuance;

import cn.hutool.db.DbUtil;
import cn.hutool.db.meta.MetaUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.kylin.research.investment.dao.BatchIssuanceDao;
import org.kylin.research.investment.entity.BatchIssuance;
import org.kylin.research.investment.entity.da.BatchIssuanceDataAccess;
import org.kylin.research.investment.entity.da.MechanismPageDataAccess;

import java.util.List;

/**
 * 生物制品批签发管理系统
 *
 * @author chenqilin
 * @date 2022-01-18 22:54:22
 */
@Slf4j
public class SearchBatchIssuance {

    private static BatchIssuanceDao batchIssuanceDao=new BatchIssuanceDao();

    public static void main(String[] args) {
        log.info("start");
        List<MechanismPageDataAccess> allMechanismPageDA = batchIssuanceDao.getAllMechanismPageDA();
        List<BatchIssuanceDataAccess>   allBatchIssuanceDatasetDA=batchIssuanceDao.getAllBatchIssuanceDatasetDA(allMechanismPageDA);
        List<BatchIssuance> allBatchIssuance = batchIssuanceDao.getAllBatchIssuance(allBatchIssuanceDatasetDA);
        batchIssuanceDao.insertBatchInssuance(allBatchIssuance);
        batchIssuanceDao.updateBatchIssuanceDataset();
        log.info("over");
    }





}
