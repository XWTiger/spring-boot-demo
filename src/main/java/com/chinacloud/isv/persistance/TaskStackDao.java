package com.chinacloud.isv.persistance;

import java.util.ArrayList;

import com.chinacloud.isv.domain.TaskStack;

public interface TaskStackDao {
	public ArrayList<TaskStack> getTasks();
	public void addTask(TaskStack task);
	public void deleteTask(String id);
	public Integer lockTask(String id);
	public Integer unLockTask(String id);
	
}
