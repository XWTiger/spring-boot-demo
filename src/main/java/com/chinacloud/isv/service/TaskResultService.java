package com.chinacloud.isv.service;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;

@Service
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "service")
@Transactional
public class TaskResultService {

	@Autowired
	TaskResultDao taskResultDao;
	@Autowired
	TaskStackDao taskStackDao;

	@Value("${service.taskResultMaxNumber}")
	private int taskResultNumber;
	@Value("${service.deleteTime}")
	private int Time;
	private static final Logger logger = LogManager.getLogger(TaskResultService.class);

	@Scheduled(fixedRate = 50000)
	private void manageTaskResult() {

		int number = taskResultDao.getCount();
		if (number > taskResultNumber) {
			logger.info("real number --->" + number);
			logger.info("delete max number --->" + taskResultNumber);
			taskResultDao.deleteResult(Time);
		}
		ArrayList<TaskStack> taskList = taskStackDao.getTasksByTime(Time * 2);
		if (null != taskList && 0 != taskList.size()) {
			for (TaskStack taskStack : taskList) {
				String farmId = "0";
				String dFarmId = null;
				if (null != taskStack.getFarmId() && !"".equals(taskStack.getFarmId())) {
					farmId = taskStack.getFarmId();
				} else if (null != taskStack.getDestinationFarmId() && !"".equals(taskStack.getDestinationFarmId())) {
					dFarmId = taskStack.getDestinationFarmId();
				}else{
					logger.warn("garbage recover, the farm id is absent");
				}
				TaskResult taskResult = MSUtil.getResultInstance(taskStack.getId(), CaseProvider.STATUS_DEAD_LOCK, taskStack.getRequestMethod(),
						"", farmId, taskStack.getCallBackUrl(), taskStack.getParams(), dFarmId,taskStack.getEventType());
				taskStackDao.deleteTask(taskStack.getId());
				taskResultDao.addResult(taskResult);
			}
		}
		taskList = null;
	}

}
