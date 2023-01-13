package org.kylin.research.investment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public abstract class BaseRecursiveTask<D,R> extends RecursiveTask<R> {
	private List<D> datas;
	private int THRESHOLD_NUM;
	
	
	public BaseRecursiveTask(List<D> datas,int THRESHOLD_NUM) {
		this.datas=datas;
		this.THRESHOLD_NUM=THRESHOLD_NUM;
	}

	private static Logger log = LoggerFactory.getLogger(BaseRecursiveTask.class);
	
	public abstract R run(List<D> perIncrementList) ;
	protected abstract R reduce(R leftResult, R rightResult);
	public abstract BaseRecursiveTask getBaseRecursiveTask(List<D> dataList,int THRESHOLD_NUM);
	
	
	
	@Override
    protected R compute() {
    	
        //如果任务足够小就计算任务
        boolean canCompute = datas.size() <= THRESHOLD_NUM;
        if (canCompute) {
            return run(datas);//业务逻辑
        } else {
            // 如果任务大于阈值，就分裂成两个子任务计算
            long middle = datas.size() / 2;
            List<D> leftList = new ArrayList();
            List<D> rightList = new ArrayList();

            long i = 0;
            for (D data : datas) {
                if (i < middle) {
                    leftList.add(data);
                } else {
                    rightList.add(data);
                }
                i++;
            }
            
            BaseRecursiveTask leftTask = getBaseRecursiveTask(leftList,THRESHOLD_NUM);
            BaseRecursiveTask rightTask = getBaseRecursiveTask(rightList,THRESHOLD_NUM );

            // 执行子任务
            invokeAll(leftTask,rightTask);
            R leftResult = (R) leftTask.join();
            R rightResult = (R) rightTask.join();
            R result=reduce(leftResult,rightResult);
            return result;
        }
    }


	
	
	
	
	
	
	
	
	

}
