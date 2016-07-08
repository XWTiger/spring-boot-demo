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
	public HashMap<Object, Object> getRecordList(String serviceTemplateId,int page, int pageSize, String orderBy, String order){
		if (page < 0) {
            throw new IllegalArgumentException("参数（page）不合法");
        }
        if (pageSize < 0) {
            throw new IllegalArgumentException("参数（pageSize）不合法");
        }
        if(null == orderBy || "".equals(orderBy)){
        	orderBy = "addTime";
        }
        if(null == order || "".equals(order)){
        	order = "desc";
        }
        double num = orderRecordDao.cout(serviceTemplateId);
        int pageNumber =  (int) Math.ceil(num/pageSize);
        List<OrderRecord> list = orderRecordDao.getList(serviceTemplateId, page * pageSize, pageSize, orderBy, order);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("total", pageNumber);
        map.put("list", list);
		return map;
	}
	/**
	 * get service template instance number 
	 * @param list
	 * @return hash map
	 */
	public ArrayList<HashMap<Object, Object>> getSTNumber(String [] list){
		if(null == list){
			throw new IllegalArgumentException("服务模板ID列表为空");
		}
		ArrayList<HashMap<Object, Object>> mapList = new ArrayList<>();
		HashMap<Object, Object> map = null;
		for (String string : list) {
			map =new HashMap<>();
			map.put("id", string);
			map.put("count", orderRecordDao.cout(string));
			mapList.add(map);
		}
		return mapList;
	}
	
	
	
	/**
	 * delete order service instance by cloned farm id
	 * @param cFarmId
	 * @return
	 */
	public boolean deleteRecordByCFarmId(int cFarmId){
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
