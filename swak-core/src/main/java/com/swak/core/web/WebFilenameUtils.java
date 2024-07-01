package com.swak.core.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author colley.ma
 * @since 3.0.0
 **/
@Slf4j
public class WebFilenameUtils {

	private static final String DISPOSITION_FORMAT = "attachment; filename=\"%s\"; filename*=utf-8''%s";

	/**
	 * 未编码文件名转Content-Disposition值
	 *
	 * @param filename 未编码的文件名(包含文件后缀)
	 * @return Content-Disposition值
	 */
	public static String disposition(String filename) {
		String codedFilename = filename;
		try {
			if (StringUtils.isNotBlank(filename)) {
				codedFilename = java.net.URLEncoder.encode(filename, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			log.error("不支持的编码:", e);
		}
		return String.format(DISPOSITION_FORMAT, codedFilename, codedFilename);
	}

	public static void disposition(HttpServletResponse response,String filename) {
		response.reset();
		response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
		response.setHeader("content-disposition", disposition(filename));
		response.setContentType("text/html;application/vnd.ms-excel");
	}
}
