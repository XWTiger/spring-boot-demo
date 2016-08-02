package com.chinacloud.isv.persistance;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.chinacloud.isv.domain.OrderRecord;

public interface OrderRecordDao {
	public List<OrderRecord> getList(@Param("serviceTemplateId")String serviceTemplateId,@Param("farmId")String farmId, @Param("offset") int offset,
            @Param("limit") int limit, @Param("orderBy") String orderBy, @Param("order") String order);
	public Integer deleteByCloneFarmId(String clonedFarmId);
	public Integer addRecord(OrderRecord or);
	public Integer cout(@Param("serviceTemplateId")String serviceTemplateId,@Param("farmId")String farmId);
}
