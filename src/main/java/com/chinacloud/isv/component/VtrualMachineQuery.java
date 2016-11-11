package com.chinacloud.isv.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.persistance.OrderRecordDao;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.CaseProvider;

@Component
@Scope
public class VtrualMachineQuery extends Thread {

	@Autowired
	TaskStackDao riskStackDao;
	@Autowired
	TaskResultDao taskResultDao;
	@Autowired
	Configuration configuration;
	@Autowired
	OrderRecordDao orderRecordDao;
	@Autowired
	VirtualMachineStatusCheck virtualMachineStatusCheck;
	@Autowired
	RebootEvent rebootEvent;
	@Autowired
	ActiveEvent activeEvent;
	@Autowired
	CancelEvent cancelEvent;
	@Autowired
	LaunchEvent launchEvent;

	private static Set<VMQeuryParam> queryList = new HashSet<VMQeuryParam>();
	private static ArrayList<VMQeuryParam> taskList = new ArrayList<VMQeuryParam>();
	private static final Logger logger = LogManager.getLogger(VtrualMachineQuery.class);
	private int status = 0;

	@Override
	public void run() {
		super.run();
		Runtime rt = Runtime.getRuntime();
		logger.info("jvm free memery ====>"+rt.freeMemory());
		long mem = rt.freeMemory();
		try {
			currentThread();
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				
				
				
				if(mem/rt.freeMemory() < configuration.getGcPercent()){
					logger.warn("the jvm free memery: "+rt.freeMemory());
					System.gc();
				}
				ArrayList<VMQeuryParam> task_list = getQueryTaskLine();
				if (task_list.size() > 0) {
					logger.debug("=========begin a task query==============");
					logger.debug("queue size:" + queryList.size());
				} else {
					try {
						currentThread();
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				for (VMQeuryParam vp : task_list) {
					// create url
					logger.debug("-------------------i am alived--------------------");
					String queryUrl = configuration.getMirBaseUrl() + "/mir/proxy/servers/xListServers/?farmId="
							+ vp.getcFarmId() + "&imageId=&limit=10&page=1&query=&start=0";
					Map<String, String> headerMap = new HashMap<String, String>();
					headerMap.put("X-Secure-Key", vp.getxSecurityKey());
					headerMap.put("X-Requested-Token", vp.getSpecialToken());
					// String requestResponse = null;
					if (CaseProvider.EVENT_NUMBER_TYPE_REBOOT == vp.getType()) {// reboot
																				// case
						rebootEvent.addParameters(vp);
						rebootEvent.go();
					} else if (CaseProvider.EVENT_NUMBER_TYPE_ACTIVE == vp.getType()) {// active
																						// case
						activeEvent.addParameters(vp, headerMap, queryUrl);
						activeEvent.go();
					} else if (CaseProvider.EVENT_NUMBER_TYPE_CANCEL == vp.getType()) {// cancel
																						// case
						cancelEvent.addParameters(vp, headerMap, queryUrl);
						cancelEvent.go();
					} else if (CaseProvider.EVENT_NUMBER_TYPE_LAUNCH == vp.getType()) {//launch case
						launchEvent.addParameters(vp, headerMap, queryUrl);
						launchEvent.go();
					}
				}
				logger.debug("================end query================");
				System.out.println("\n");
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("thread error\n" + e.getLocalizedMessage());
				e.printStackTrace();

			}

		}
	}

	public synchronized void addQueryTask(VMQeuryParam vmQeuryParam) {
		logger.info("add a query task,farm id:" + vmQeuryParam.getcFarmId());
		logger.info("line size before:" + queryList.size());
		queryList.add(vmQeuryParam);
		logger.info("line size after:" + queryList.size());
	}

	public synchronized void removeQueryTask(VMQeuryParam vp) {
		queryList.remove(vp);
	}

	private ArrayList<VMQeuryParam> getQueryTaskLine() {
		taskList.clear();
		Iterator<VMQeuryParam> its = queryList.iterator();
		while (its.hasNext()) {
			taskList.add(its.next());
		}
		return taskList;
	}

	/**
	 * 
	 * @param vmQeuryParam
	 * @return time out return true
	 */
	public boolean timeOutCheck(VMQeuryParam vmQeuryParam) {
		boolean b = false;
		long time = new Date().getTime() - vmQeuryParam.getBeginTime();
		logger.debug(time + "ms have gone.");
		if (time > configuration.getTimeOut() * 60000) {
			b = true;
			logger.debug("time out remove node id:" + vmQeuryParam.getTaskId());
		}
		return b;
	}

	@Override
	public synchronized void start() {
		if (0 == this.status) {
			this.status = 1;
			super.start();
		}
	}
}
