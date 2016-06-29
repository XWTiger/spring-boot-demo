package com.chinacloud.isv.util;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;

import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;

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

	 /**
	  * get the case chinese name by case type
	  * @param caseName
	  * @return name
	  */
	 public static String getChineseName(String caseName){
		 String cName = null;
		 for (String[] strings : listEvent) {
			if(strings[0].equals(caseName)){
				cName = strings[1];
			}
		}
		 return cName;
	 }

	 public static String getFarmNameFromResult(String resultMsg){
		 String sp[] = resultMsg.split("'");
		 return sp[1];
	 }
	 
	 public static CloseableHttpResponse httpClientGetUrl(Map<String, String> headers, String url) throws  Exception{
		 HttpClientBuilder hcBuilder = HttpClients.custom();
	        hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
	        CloseableHttpClient httpClient = hcBuilder.build();
	        HttpGet httpGet = new HttpGet(url);
	        for(Map.Entry<String, String> entry: headers.entrySet()) {
	        	httpGet.addHeader(entry.getKey(), entry.getValue());
	        }
	        RequestConfig requestConfig = RequestConfig.custom()
	                //设置连接超时时间
	                .setConnectTimeout(30000)
	                        //设置从connect Manager获取Connection 超时时间
	                .setConnectionRequestTimeout(30000)
	                        //请求获取数据的超时时间
	                .setSocketTimeout(30000)
	                .build();
	        httpGet.setConfig(requestConfig);
	        return httpClient.execute(httpGet);
	 }

	 /**
	  * generate a local result
	  * @param type 1 success  else failed
	  * @param task 
	  * @return
	  */
	 public static TaskResult getTaskResult(int type,TaskStack task,String r,String comebackResult){
		 TaskResult result = new TaskResult();
		 if(1 == type){
			 result.setResultStatus("SUCCESS");
			 result.setId(task.getId());
			 result.setRequestMethod(task.getRequestMethod());
			 result.setParams(r);
			 result.setErrorInfo(comebackResult);
			 result.setRequestUrl(task.getCallBackUrl());
		 }else{
			 result.setResultStatus("FAILED");
			 result.setId(task.getId());
			 result.setRequestMethod(task.getRequestMethod());
			 result.setParams(r);
			 result.setRequestUrl(task.getCallBackUrl());
			 result.setErrorInfo("call back return result failed:"+comebackResult);
		 }
		 return result;
	 }
	 /**
	  * if is a formal request url
	  * @param url
	  * @return true if yes otherwise return false 
	  */
	 public static boolean httpUrlCheck(String url){
		 boolean b = true;
		 String regex = "^(http|https)://([\\w-]+.)+[\\w-]+(/[\\w-./?%&=]*)?$";
		 b = url.matches(regex);
		 return b;
	 }
	 /**
	  * is test request?
	  * @param p
	  * @return true ,if is test request
	  */
	 public static boolean isTestParameter(String p){
		 boolean b = true;
		 if(!p.equals("{eventUrl}")){
			 b = false;
		 }
		 return b;
	 }
	 /**
	  * encode string to ISO-8859-1
	  * @param s
	  * @return string
	  */
	 public static String encode(String s){
		 String newString = null;
		try {
			newString = new String(s.getBytes(),"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		return newString;
	 }
}
