package com.chinacloud.isv.component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.entity.VMQeuryParam;

@Component
@Scope
public class VtrualMachineQuery extends Thread{

	private static Set<VMQeuryParam> queryList = new HashSet<VMQeuryParam>();
	private static final Logger logger = LogManager.getLogger(VtrualMachineQuery.class);
	private int status = 0;
	@Override
	public void run() {
		super.run();
		while(true){
			logger.debug("=========begin a task query========");
			logger.debug("queue size:"+queryList.size());
			logger.debug("------------------------------------");
			try {
				Thread.currentThread();
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.debug("================end================");
		}
	}

	public synchronized void addQueryTask(VMQeuryParam vmQeuryParam){
		queryList.add(vmQeuryParam);
	}
	
	public void removeQueryTask(VMQeuryParam vp){
		queryList.remove(vp);
	}
	
	private VMQeuryParam getQueryTask(){
		Iterator<VMQeuryParam> its = queryList.iterator();
		if(its.hasNext()){
			VMQeuryParam vp = its.next();
			return vp;
		}else{
			return null;
		}
	}
	
	@Override
	public synchronized void start(){
		if(0 == this.status){
			this.status = 1;
			super.start();
		}
	}
}
