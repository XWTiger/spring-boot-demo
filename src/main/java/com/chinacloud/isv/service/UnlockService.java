package com.chinacloud.isv.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.MSUtil;

@Service
public class UnlockService {
	
	@Autowired
	TaskStackDao taskStackDao;
	@Autowired
	TaskResultDao taskResultDao;
	
	public static final Logger logger = LogManager.getLogger(UnlockService.class);
	/**
	 * analyze the unlock times,if bigger than 5 return failed and add task result, otherwise unlock mission 
	 * @param ts
	 * @return
	 */
	public boolean  unlockMission(TaskStack ts){
		boolean b = true;
		//analyze the unlock times 
		Integer repeatTimes = taskStackDao.getRepeatTimesById(ts.getId());
		if(null == repeatTimes){
			logger.error("when unlock task, return repeatTimes is null");
		}else{
			//if bigger than 5 return failed and add task result, otherwise unlock mission 
			if(repeatTimes > 5){
				WhiteholeFactory wFactory =new WhiteholeFactory();
				Params params = null;
				try {
					params= wFactory.getEntity(Params.class, ts.getParams());
				} catch (Exception e) {
					logger.error("convert params string to entiy failed");
					b = false;
					e.printStackTrace();
					return b;
				}
				TaskResult taskResult = this.getResultInstance(ts.getId(), "FAILED", "POST", MSUtil.getChineseName(params.getData().getType())+" TIME OUT", ts.getFarmId(), ts.getCallBackUrl(),ts.getParams());
				//delete the row record of task and the order case result
				taskStackDao.deleteTask(ts.getId());
				taskResultDao.addResult(taskResult);
			}else{
				repeatTimes++;
				taskStackDao.unLockTask(ts.getId());
				taskStackDao.addRepeageTimesById(ts.getId(), repeatTimes);
			}
		}
		return b;
	}
	
	
	/**
	 * get result instance 
	 * type not 0 success, 0 filed
	 * @return
	 */
	private TaskResult getResultInstance(String id,String status,String requestMthod,String requestResponse,int cFarmId,String callBackUrl,String parameters){
		TaskResult tResult = new TaskResult();
		tResult.setResultStatus(status);
		tResult.setId(id);
		tResult.setRequestMethod(requestMthod);
		tResult.setParams(parameters);
		tResult.setErrorInfo(requestResponse);
		tResult.setcFarmId(cFarmId);
		tResult.setRequestUrl(callBackUrl);
		return tResult;
	}
}
