package com.chinacloud.isv.util;

import java.io.IOException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.mirtemplate.ComponentInfo;
import com.chinacloud.isv.entity.mirtemplate.MirTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MSUtil {

	private static final Logger logger = LogManager.getLogger(MSUtil.class);
	
	private static String[] synArray = { CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY };
	private static String[][] listEvent = { { CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER, "申请事件" },
			{ CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY, "查询事件" },
			{ CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL, "销毁事件" },
			{ CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND, "挂起事件" },
			{ CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE, "激活事件" },
			{ CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT,"重启事件"} };

	/**
	 * distinguish Synchronization
	 * 
	 * @param caseName
	 * @return Synchronization return true
	 */
	public static final boolean isSynchronzation(String caseName) {
		boolean b = false;
		for (String name : synArray) {
			if (name.equals(caseName)) {
				b = true;
			}
		}
		return b;
	}

	public static CloseableHttpResponse httpClientPostUrl(Map<String, String> headers, String url, String data)
			throws Exception {
		HttpClientBuilder hcBuilder = HttpClients.custom();
		hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
		// CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpClient httpClient = hcBuilder.build();
		// httpClient.getParams().setParameter("http.protocol.allow-circular-redirects",
		// true);
		HttpPost httpPost = new HttpPost(url);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}
		httpPost.setEntity(new StringEntity(data));
		RequestConfig requestConfig = RequestConfig.custom()
				// 设置连接超时时间
				.setConnectTimeout(30000)
				// 设置从connect Manager获取Connection 超时时间
				.setConnectionRequestTimeout(30000)
				// 请求获取数据的超时时间
				.setSocketTimeout(30000).build();
		httpPost.setConfig(requestConfig);
		return httpClient.execute(httpPost);
	}

	public static CloseableHttpResponse httpClientPostUrl(Map<String, String> headers, String url,
			List<NameValuePair> data) throws Exception {
		HttpClientBuilder hcBuilder = HttpClients.custom();
		hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
		// CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpClient httpClient = hcBuilder.build();
		// httpClient.getParams().setParameter("http.protocol.allow-circular-redirects",
		// true);
		HttpPost httpPost = new HttpPost(url);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}
		httpPost.setEntity(new UrlEncodedFormEntity(data));
		RequestConfig requestConfig = RequestConfig.custom()
				// 设置连接超时时间
				.setConnectTimeout(30000)
				// 设置从connect Manager获取Connection 超时时间
				.setConnectionRequestTimeout(30000)
				// 请求获取数据的超时时间
				.setSocketTimeout(30000).build();
		httpPost.setConfig(requestConfig);
		return httpClient.execute(httpPost);
	}

	/**
	 * get the case chinese name by case type
	 * 
	 * @param caseName
	 * @return name
	 */
	public static String getChineseName(String caseName) {
		String cName = null;
		for (String[] strings : listEvent) {
			if (strings[0].equals(caseName)) {
				cName = strings[1];
			}
		}
		return cName;
	}

	public static String getFarmNameFromResult(String resultMsg) {
		String sp[] = resultMsg.split("'");
		return sp[1];
	}

	public static CloseableHttpResponse httpClientGetUrl(Map<String, String> headers, String url) throws Exception {
		HttpClientBuilder hcBuilder = HttpClients.custom();
		hcBuilder.setRedirectStrategy(new LaxRedirectStrategy());
		CloseableHttpClient httpClient = hcBuilder.build();
		HttpGet httpGet = new HttpGet(url);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpGet.addHeader(entry.getKey(), entry.getValue());
		}
		RequestConfig requestConfig = RequestConfig.custom()
				// 设置连接超时时间
				.setConnectTimeout(30000)
				// 设置从connect Manager获取Connection 超时时间
				.setConnectionRequestTimeout(30000)
				// 请求获取数据的超时时间
				.setSocketTimeout(30000).build();
		httpGet.setConfig(requestConfig);
		return httpClient.execute(httpGet);
	}

	/**
	 * generate a local result
	 * 
	 * @param type
	 *            1 success else failed
	 * @param task
	 * @return
	 */
	public static TaskResult getTaskResult(int type, TaskStack task, String r, String comebackResult) {
		TaskResult result = new TaskResult();
		if (1 == type) {
			result.setResultStatus("SUCCESS");
			result.setId(task.getId());
			result.setRequestMethod(task.getRequestMethod());
			result.setParams(r);
			result.setErrorInfo(comebackResult);
			result.setRequestUrl(task.getCallBackUrl());
		} else {
			result.setResultStatus("FAILED");
			result.setId(task.getId());
			result.setRequestMethod(task.getRequestMethod());
			result.setParams(r);
			result.setRequestUrl(task.getCallBackUrl());
			result.setErrorInfo("call back return result failed:" + comebackResult);
		}
		return result;
	}

	/**
	 * if is a formal request url
	 * 
	 * @param url
	 * @return true if yes otherwise return false
	 */
	public static boolean httpUrlCheck(String url) {
		boolean b = true;
		String regex = "^(http|https)://([\\w-]+.)+[\\w-]+(/[\\w-./?%&=]*)?$";
		b = url.matches(regex);
		return b;
	}

	/**
	 * is test request?
	 * 
	 * @param p
	 * @return true ,if is test request
	 */
	public static boolean isTestParameter(String p) {
		boolean b = true;
		if (!p.equals("{eventUrl}")) {
			b = false;
		}
		return b;
	}

	/**
	 * encode string to ISO-8859-1
	 * 
	 * @param s
	 * @return string
	 */
	public static String encode(String s) {
		String newString = null;
		try {
			newString = new String(s.getBytes(), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return newString;
	}

	/**
	 * get new configuration string
	 * 
	 * @param mirTemplate
	 * @param farmConf
	 * @return string [] 0 -- farm, 1 -- roles, 2 -- changed
	 */
	public static String[] getConfiguratedString(MirTemplate mirTemplate, String farmConf) {
		String[] liStrings = new String[3];
		String farmInfo = null;
		String roles = null;
	
		// roles
		ObjectMapper oMapper = new ObjectMapper();
		try {
			JsonNode node = oMapper.readTree(farmConf);
			JsonNode nodeM = node.get("moduleParams");
			JsonNode farm = nodeM.get("farm");
			liStrings[2] = null;
			farmInfo = farm.get("farm").toString();
			System.out.println("farm info---->" + farmInfo);
			liStrings[0] = farmInfo;
			JsonNode cNode = farm.get("roles");
			if (cNode.isArray()) {
				System.out.println("node size--->" + cNode.size());
				String[] roleStrNode = new String[cNode.size()];
				int i = 0 ;
				for (JsonNode jsonNode : cNode) {
					System.out.println(jsonNode.toString());
					String buffer = jsonNode.toString();
					System.out.println(jsonNode.get("name").toString());
					ComponentInfo componentInfo = null;
					for (ComponentInfo c : mirTemplate.getComponentInfos()) {
						if (jsonNode.get("name").toString().equals("\""+c.getComponentName()+"\"")) {
							componentInfo = c;
							break;
						}
					}
					String realString = replaceValue(buffer,"scaling.min_instances","\""+componentInfo.getUnitInstanceNumber()+"\"");
					realString = replaceValue(realString, "openstack.flavor-id", "\""+componentInfo.getUnitFlavorId()+"\"");
					realString = replaceValue(realString, "openstack.networks", "\"[\\\""+componentInfo.getComponentNet()+"\\\"]\"");
					roleStrNode[i] = realString;
					i++;
				}
				roles ="[";
				for(int j= 0 ; j < cNode.size(); j++){
					if((j+1) == cNode.size()){
						roles =roles +roleStrNode[j]+"]";
					}else{
						roles =roles +roleStrNode[j]+",";
					}
				}
				logger.debug("roles: "+roles);
				liStrings[1] = roles;
			}
		} catch (JsonProcessingException e) {
			System.out.println("JsonProcessingException");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			return null;
		}
		return liStrings;
	}

	/**
	 * jsonStr old string , name is the key ,value is the new value
	 * 
	 * @param jsonStr
	 * @param name
	 * @param value
	 * @return new string
	 */
	public static String replaceValue(String jsonStr, String name, String value) {
		String string = null;
		String bufferHeader = null;
		String bufferTail = null;
		logger.debug("go into here?");
		int index = jsonStr.indexOf(name);
		System.out.println("head===>" + jsonStr.substring(0, index));
		// find ","
		bufferHeader = jsonStr.substring(0, index - 1);
		System.out.println("bufferHeader===>"+bufferHeader);
		int rIndex = index +name.length();
		int rEndIndex = 0;
		for (int i = rIndex; i < jsonStr.length(); i++) {
			String content = jsonStr.substring(i, i + 1);
			if (content.equals(",")) {
				System.out.println("the real index -->" + i);
				rEndIndex = i;
				break;
			}
		}
		if(0 == rEndIndex){
			logger.error("get index falied");
			return null;
		}
		bufferTail = jsonStr.substring(rEndIndex, jsonStr.length());
		logger.debug("buffer header ===>"+bufferHeader);
		logger.debug("buffer tail ====>"+bufferTail);
		string = bufferHeader +"\""+name+"\":"+value+bufferTail;
		logger.debug("final string =====>"+string);
		return string;
	}
}
