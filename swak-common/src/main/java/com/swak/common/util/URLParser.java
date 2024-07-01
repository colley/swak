package com.swak.common.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
/**
 * 
 * ClassName: URLParser.java 
 * @Description: URL地址解析
 * @author colley.ma
 * @date 2021年3月15日
 */
public class URLParser {
	protected byte type;
	protected static final byte TYPE_URL = 1;
	protected static final byte TYPE_QUERY_STRING = 2;
	protected String url;
	protected String baseUrl;
	protected String queryString;
	protected String label;
	protected String charset = "utf-8";
 
	protected boolean compiled = false;
	public Map<String, String> parsedParams;
	protected URLDecoder urld = new URLDecoder();
 
	public static URLParser fromURL(String url) {
		URLParser parser = new URLParser();
 
		parser.type = 1;
		parser.url = url;
 
		String[] split = url.split("\\?", 2);
		parser.baseUrl = split[0];
		parser.queryString = (split.length > 1 ? split[1] : "");
 
		String[] split2 = url.split("#", 2);
		parser.label = (split2.length > 1 ? split2[1] : null);
 
		return parser;
	}
 
	public static URLParser fromQueryString(String queryString) {
		URLParser parser = new URLParser();
 
		parser.type = 2;
		parser.queryString = queryString;
 
		return parser;
	}
 
	public URLParser useCharset(String charset) {
		this.charset = charset;
		return this;
	}
 
	public URLParser compile(){
		if (this.compiled) {
			return this;
		}
		String paramString = this.queryString.split("#")[0];
		String[] params = paramString.split("&");
 
		this.parsedParams = Maps.newLinkedHashMap();
		for (String p : params) {
			String[] kv = p.split("=", 2); 
			if (kv.length == 2) {
				try {
					this.parsedParams.put(kv[0], URLDecoder.decode(kv[1], this.charset));
				} catch (UnsupportedEncodingException e) {
					
				}
			}
		}
		this.compiled = true;
 
		return this;
	}
 
	public String getParameter(String name) {
		if (this.compiled) {
			return (String) this.parsedParams.get(name);
		}
		String paramString = this.queryString.split("#")[0];
		Matcher match = Pattern.compile("(^|&)" + name + "=([^&]*)").matcher(paramString);
		match.lookingAt();
 
		return match.group(2);
	}
 
	public URLParser setParameter(String name, String value){
		if (!this.compiled) {
			compile();
		}
		this.parsedParams.put(name, value);
 
		return this;
	}
	public URLParser removeParameter(String name){
		if (!this.compiled) {
			compile();
		}
		if(this.parsedParams.get(name)!=null) {
			this.parsedParams.remove(name);
		} 
		return this;
	}
	
	
	public Map<String, String> getParsedParams(){
		if (!this.compiled) {
			compile();
		}
		
		return this.parsedParams;
	}
 
	public String toURL() {
		if (!this.compiled) {
			compile();
		}
		URLBuilder builder = new URLBuilder();
		if (this.type == 1) {
			builder.appendPath(this.baseUrl);
		}
		for (String k : this.parsedParams.keySet()) {
			builder.appendParamEncode(k, (String) this.parsedParams.get(k), this.charset);
		}
		if (this.label != null) {
			builder.appendLabel(this.label);
		}
		return builder.toString();
	}

	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		
	}
	
	public URLParser replaceBaseUrl(String searchString, String replacement) {
		String baseUrl = StringUtils.replace(this.baseUrl, searchString, replacement);
		this.baseUrl = baseUrl;
		return this;
	}
	

	public String getBaseUrl() {
		return baseUrl;
	}

	public static void main(String[] args) {
		try {
			
			System.out.println(
					fromURL("https://pro.m.jd.com/mall/active/2r3jVWUAMR4VCjEf4KVQ9yGBdSy9/index.html?wxAppName=kepler")
							.compile().setParameter("q", "tweaked")
							.removeParameter("wxAppName").replaceBaseUrl("/mall/", "/mini/").toURL());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
