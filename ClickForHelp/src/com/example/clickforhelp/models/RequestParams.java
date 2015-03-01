package com.example.clickforhelp.models;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RequestParams {
	private String URI;
	private String method;
	private Map<String, String> params = new HashMap<String, String>();

	public String getURI() {
		return URI;
	}

	public void setURI(String uRI) {
		URI = uRI;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public void setParam(String string, String value) {
		params.put(string, value);
	}

	public String getEncodedParams() {
		StringBuilder sb = new StringBuilder();
		for (String key : params.keySet()) {
			String value = null;
			try {
				value = URLEncoder.encode(params.get(key), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(key + "=" + value);
		}
		return sb.toString();
	}

}
