package com.chinacloud.isv.entity;

public class Data {

		private String type;
		private String eventId;
		MarketPlace marketplace;
		Creator creator;
		Payload payload;
		private String callBackUrl;
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getEventId() {
			return eventId;
		}
		public void setEventId(String eventId) {
			this.eventId = eventId;
		}
		public MarketPlace getMarketplace() {
			return marketplace;
		}
		public void setMarketplace(MarketPlace marketplace) {
			this.marketplace = marketplace;
		}
		public Creator getCreator() {
			return creator;
		}
		public void setCreator(Creator creator) {
			this.creator = creator;
		}
		public Payload getPayload() {
			return payload;
		}
		public void setPayload(Payload payload) {
			this.payload = payload;
		}
		public String getCallBackUrl() {
			return callBackUrl;
		}
		public void setCallBackUrl(String callBackUrl) {
			this.callBackUrl = callBackUrl;
		}
}
