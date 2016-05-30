package com.chinacloud.isv.util;

import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

public class MSUtil {

	private static String [] synArray={CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY};
	private static String [][] listEvent = {{CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER,"申请事件"},
											{CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY,"查询事件"},
											{CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL,"销毁事件"},
											{CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND,"挂起事件"},
											{CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE,"激活事件"}};
	
	/**
	 * distinguish Synchronization 
	 * @param caseName
	 * @return Synchronization return true
	 */
	public static final boolean isSynchronzation(String caseName){
		boolean b = false;
		for (String name : synArray) {
			if(name.equals(caseName)){
				b = true;
			}
		}
		return b;
	}
	
	 public static CloseableHttpResponse httpClientPostUrl(Map<String, String> headers, String url, String data) throws  Exception {
	        HttpClientBuilder hcBuilder = HttpClients.custom();
	        hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
	        //CloseableHttpClient httpClient = HttpClients.createDefault();
	        CloseableHttpClient httpClient = hcBuilder.build();
	        //httpClient.getParams().setParameter("http.protocol.allow-circular-redirects", true);
	        HttpPost httpPost = new HttpPost(url);
	        for (Map.Entry<String, String> entry : headers.entrySet()) {
	            httpPost.addHeader(entry.getKey(), entry.getValue());
	        }
	        httpPost.setEntity(new StringEntity(data));
	        RequestConfig requestConfig = RequestConfig.custom()
	                //设置连接超时时间
	                .setConnectTimeout(30000)
	                        //设置从connect Manager获取Connection 超时时间
	                .setConnectionRequestTimeout(30000)
	                        //请求获取数据的超时时间
	                .setSocketTimeout(30000)
	                .build();
	        httpPost.setConfig(requestConfig);
	        return httpClient.execute(httpPost);
	    }
	 
	 public static CloseableHttpResponse httpClientPostUrl(Map<String, String> headers, String url, List<NameValuePair> data) throws  Exception{
	        HttpClientBuilder hcBuilder = HttpClients.custom();
	        hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
	        //CloseableHttpClient httpClient = HttpClients.createDefault();
	        CloseableHttpClient httpClient = hcBuilder.build();
	        //httpClient.getParams().setParameter("http.protocol.allow-circular-redirects", true);
	        HttpPost httpPost = new HttpPost(url);
	        for(Map.Entry<String, String> entry: headers.entrySet()) {
	            httpPost.addHeader(entry.getKey(), entry.getValue());
	        }
	        httpPost.setEntity(new UrlEncodedFormEntity(data));
	        RequestConfig requestConfig = RequestConfig.custom()
	                //设置连接超时时间
	                .setConnectTimeout(30000)
	                        //设置从connect Manager获取Connection 超时时间
	                .setConnectionRequestTimeout(30000)
	                        //请求获取数据的超时时间
	                .setSocketTimeout(30000)
	                .build();
	        httpPost.setConfig(requestConfig);
	        return httpClient.execute(httpPost);
	    }


	 public static String getChineseName(String caseName){
		 String cName = null;
		 for (String[] strings : listEvent) {
			if(strings[0].equals(caseName)){
				cName = strings[1];
			}
		}
		 return cName;
	 }

}
