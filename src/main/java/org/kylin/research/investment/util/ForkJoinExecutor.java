package org.kylin.research.investment.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: ForkJoinExecutor
 * Description:
 * Author: aierxuan
 * Date: 2018-12-28 17:28
 * History:
 * <author> <time> <version>    <desc>
 * 作者姓名 修改时间    版本号 描述
 */
@Slf4j
public class ForkJoinExecutor {

    public  static <R> R exec(RecursiveTask<R> task,Integer parallelism,long timeoutSec){
        ForkJoinPool baseInfoPool=new ForkJoinPool(parallelism);
        R r=baseInfoPool.invoke(task);
        baseInfoPool.shutdown();
        try {
            boolean b = baseInfoPool.awaitTermination(timeoutSec, TimeUnit.SECONDS);
            if(!b) log.error("ForkJoin在终止前超时");
        } catch (InterruptedException e) {
            log.error("ForckJoinPool在等待时被中断：",e);
        }
        return r;
    }
}
