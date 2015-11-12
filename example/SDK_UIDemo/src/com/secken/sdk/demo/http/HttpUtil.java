package com.secken.sdk.demo.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpUtil
{
	
	private static int REQUEST_TIME_OUT = 10 * 1000;

	/**
	 * 普通请求网络
	 * 
	 * @param sensetime_url
	 * @param map
	 * @return
	 */
	public static String post(String service_url, Map<String, String> map)
	{
		String CHARSET = "UTF-8";
		String result = "";
		try
		{
			URL url = new URL(service_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");// 提交模式
			conn.setReadTimeout(REQUEST_TIME_OUT);// 读取超时 单位毫秒
			conn.setConnectTimeout(REQUEST_TIME_OUT);// 连接超时
			conn.setUseCaches(false);
			conn.setDoOutput(true);// 是否输入参数

			StringBuffer params = new StringBuffer();
			for (Map.Entry<String, String> entry : map.entrySet())
			{
				params.append("&").append(entry.getKey()).append("=").append(entry.getValue());
			}
			byte[] bypes = params.toString().substring(1, params.length()).getBytes();
			conn.getOutputStream().write(bypes);
			int responseCode = conn.getResponseCode();
			if (responseCode != 200)
			{
				return null;
			}
			InputStream in = conn.getInputStream();
			InputStreamReader isReader = new InputStreamReader(in, CHARSET);
			BufferedReader bufReader = new BufferedReader(isReader);
			result = bufReader.readLine();
			conn.disconnect();
			return result;
		} catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}// 输入参数
	}
	
}
