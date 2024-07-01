
package com.swak.common.util;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;


/**
 * ClassName: PlaceholderUtils.java 
 * @Description: {keywordId}占位符填充   Map->{keywordId:'11'}
 * @author colley.ma
 * @date 2021年3月15日
 */
@Slf4j
public class PlaceholderUtils {
    /**
     * Prefix for system property placeholders: "{"
     */
    public static final String PLACEHOLDER_PREFIX = "{";

    /**
     * Suffix for system property placeholders: "}"
     */
    public static final String PLACEHOLDER_SUFFIX = "}";

    public static String resolvePlaceholders(String text, Map<String, String> parameter) {
    	
    	if(StringUtils.isEmpty(text) || MapUtils.isEmpty(parameter)) {
    		return text;
    	}

        StringBuilder buf = new StringBuilder(text);

        int startIndex = buf.indexOf(PLACEHOLDER_PREFIX);

        while (startIndex != -1) {
            int endIndex = buf.indexOf(PLACEHOLDER_SUFFIX, startIndex + PLACEHOLDER_PREFIX.length());

            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + PLACEHOLDER_PREFIX.length(), endIndex);

                int nextIndex = endIndex + PLACEHOLDER_SUFFIX.length();
                try {
					String propVal = parameter.get(placeholder);
					if (propVal == null) {
						if (placeholder.startsWith("encode_")) {
							propVal = URLBuilder.encodeURIComponent(parameter.get(placeholder.substring(7)));
						}
					}
					if (propVal != null) {
						buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(), propVal);

						nextIndex = startIndex + propVal.length();
					} else {
						log.warn("Could not resolve placeholder '" + placeholder + "' in [" + text + "] ");
					}
				} catch (Exception ex) {
					log.warn("Could not resolve placeholder '" + placeholder + "' in [" + text + "]: " + ex);
				}

				startIndex = buf.indexOf(PLACEHOLDER_PREFIX, nextIndex);
			} else {
				startIndex = -1;
			}
		}

		return buf.toString();
	}
    
 
    
    public static void main(String[] args) {
    	Map<String,String> params = Maps.newHashMap();
    	params.put("LandingPage", "https://item.yhd.com/5417598.html");
		String text = "https://union-click.yhd.com/sem.php?source=baidu-nks&unionId=262767352&siteId=baidunks_{keywordid}&to={LandingPage}";
		System.out.println(PlaceholderUtils.resolvePlaceholders(text, params));
    }
}
