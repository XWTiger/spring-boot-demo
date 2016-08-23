package com.chinacloud.isv.persistance;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Param;

import com.chinacloud.isv.domain.TaskStack;

public interface TaskStackDao {
	public ArrayList<TaskStack> getTasks();
	public void addTask(TaskStack task);
	public void deleteTask(String id);
	public Integer lockTask(String id);
	public Integer unLockTask(String id);
	public Integer getRepeatTimesById(String id);
	public Integer addRepeageTimesById(@Param("id") String id,@Param("repeatTimes") int repeatTimes);
	public ArrayList<TaskStack> getTasksByTime(int time);
}
