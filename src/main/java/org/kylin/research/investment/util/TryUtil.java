/*
 * Copyright (c) 2020-2030 丰尚·深圳
 * 不能修改和删除上面的版权声明
 * 此代码属于丰尚智慧农牧科技有限公司部门编写，在未经允许的情况下不得传播复制
 */
package org.kylin.research.investment.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 重试工具
 *
 * @author chenqilin
 * @date 2022-01-23 14:18:04
 */
@Slf4j
public class TryUtil {

    public static <T> T runTry(int limitTimes,int sleepSecond,T defaultResult,TryRunnable<T>  runnable,String taskKeyWord){
        int tryTimes=0;
        T t=defaultResult;
        while(true){
            try{
                tryTimes++;
                t=runnable.run();
                break;
            }catch (Throwable e){
                if(tryTimes>=limitTimes){
                    log.error("尝试"+tryTimes+"次后仍失败,任务关键词:"+taskKeyWord+"，即将放弃",e);
                    break;
                }else{
                    log.error("尝试"+tryTimes+"次后仍失败,任务关键词"+taskKeyWord+"，睡眠"+sleepSecond+"秒后将重试,报错内容：\n"+ ExceptionUtil.stacktraceToString(e));
                    ThreadUtil.sleep(1000*sleepSecond);
                }
            }
        }
        return t;
    }


    public interface TryRunnable<T>{
        T run() throws Throwable;
    }
}
