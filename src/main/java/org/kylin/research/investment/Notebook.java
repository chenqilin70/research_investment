/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * 文本查询
 * @author chenqilin
 * @date 2022-01-06 15:24:35
 */
@Slf4j
public class Notebook {
    public static void main(String[] args) {
        List<String> lines = FileUtil.readLines("notebook.txt", "utf-8");
        lines.stream().forEach(l->{
            log.info("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            log.info(l);
            new Scanner(System.in).nextLine();
        });
    }
}
