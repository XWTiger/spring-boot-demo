package com.chinacloud.isv.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.domain.OrderRecord;
import com.chinacloud.isv.persistance.OrderRecordDao;

@Service
public class OrderRecordService {

	@Autowired
	private OrderRecordDao orderRecordDao;
	/**
	 * get the ordered service instance
	 * @param page
	 * @param pageSize
	 * @param orderBy
	 * @param order
	 * @return ArrayList<OrderRecord>
	 */
	public HashMap<Object, Object> getRecordList(String serviceTemplateId,String farmId,Integer page, Integer pageSize, String orderBy, String order){
	
        if(null == orderBy || "".equals(orderBy)){
        	orderBy = "addTime";
        }
        if(null == order || "".equals(order)){
        	order = "desc";
        }
        if(null == serviceTemplateId || serviceTemplateId.equals("")){
        	serviceTemplateId = null;
        }
        if(null == farmId || farmId.equals("")){
        	farmId = null;
        }
        
    	double num = orderRecordDao.cout(serviceTemplateId,farmId);
        HashMap<Object, Object> map = new HashMap<>();
        List<OrderRecord> list = null;
    	if(page <= 0){
    		throw new IllegalArgumentException("页码不能小于0");
    	}
    	if(pageSize <= 0){
    		throw new IllegalArgumentException("每页显示数量不能小于0");
    	}
    	int pageNumber =  (int) Math.ceil(num/pageSize);
    	list = orderRecordDao.getList(serviceTemplateId,farmId, (page - 1) * pageSize, pageSize, orderBy, order);
    	 map.put("pages", pageNumber);
        map.put("totalCount",(int)num);
        map.put("list", list);
		return map;
	}
	/**
	 * get service template instance number 
	 * @param list
	 * @return hash map
	 */
	public HashMap<Object, Object> getSTNumber(String [] list){
		if(null == list){
			throw new IllegalArgumentException("服务模板ID列表为空");
		}
		HashMap<Object, Object> rmap = new HashMap<>();
		ArrayList<HashMap<Object, Object>> mapList = new ArrayList<>();
		HashMap<Object, Object> map = null;
		for (String string : list) {
			map =new HashMap<>();
			map.put("id", string);
			map.put("count", orderRecordDao.cout(string,null));
			mapList.add(map);
		}
		rmap.put("data", mapList);
		return rmap;
	}
	
	
	
	/**
	 * delete order service instance by cloned farm id
	 * @param cFarmId
	 * @return
	 */
	public boolean deleteRecordByCFarmId(String cFarmId){
		boolean b = true;
		Integer result = orderRecordDao.deleteByCloneFarmId(cFarmId);
		if(null == result){
			b = false;
		}
		return b;
	}
	/**
	 * add a order service instance
	 * @param orderRecord
	 * @return
	 */
	public boolean addRecord(OrderRecord orderRecord){
		boolean b = true;
		Integer result = orderRecordDao.addRecord(orderRecord);
		if(null == result){
			b = false;
		}
		return b;
	}
}
