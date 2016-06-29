package com.chinacloud.isv.persistance;

import com.chinacloud.isv.domain.TaskResult;

public interface TaskResultDao {

	public Integer addResult(TaskResult taskResult);
	public TaskResult getOrderTaskResultById(String id);
	
	public int getCount();
	//count bigger than a number that was configurated by user
	public Integer deleteResult(int num);
	public Integer deleteResultById(String id);
}
