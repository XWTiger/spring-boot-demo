package com.chinacloud.isv.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.persistance.TaskResultDao;

@Service
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "service")
public class TaskResultService {

	@Autowired
	TaskResultDao taskResultDao;
	
	@Value("${service.taskResultMaxNumber}")
	private int taskResultNumber;
	@Value("${service.deleteTime}")
	private int Time;
	private static final Logger logger = LogManager.getLogger(TaskResultService.class);
	@Scheduled(fixedRate = 50000)
	private void manageTaskResult(){
		
	
		int number = taskResultDao.getCount();
		if(number > taskResultNumber){
			logger.info("real number --->"+number);
			logger.info("delete max number --->"+taskResultNumber);
			taskResultDao.deleteResult(Time);
		}
	}
	
}
