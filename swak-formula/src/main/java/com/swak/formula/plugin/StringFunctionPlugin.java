package com.swak.formula.plugin;

import com.swak.formula.spi.FormulaPlugin;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 字符串函数
 *
 * @author colley.ma
 * @since 2.4.0
 **/
public class StringFunctionPlugin implements FormulaPlugin {

	/**
	 * Concat string.
	 *
	 * @param value1 the value 1
	 * @param value2 the value 2
	 * @return the string
	 */
	public String concat(String value1, String value2) {
		if (value1 == null) {
			return value2;
		}
		if (value2 == null) {
			return value1;
		}
		return value1.concat(value2);
	}

	public Boolean startsWith(String str, String prefix) {
		if (str == null || prefix == null) {
			return false;
		}
		return str.startsWith(prefix);
	}

	public Boolean endsWith(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		return str.endsWith(suffix);
	}

	public String substring(String str, int beginIndex, int endIndex) {
		if (str == null) {
			return null;
		}
		if (beginIndex < 0) {
			beginIndex = 0;
		}
		if (endIndex > str.length()) {
			endIndex = str.length();
		}
		if (endIndex <= 0 || beginIndex >= endIndex) {
			return "";
		}
		return str.substring(beginIndex, endIndex);
	}

	public String getCurrentTime(String format) {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
	}

	public Long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}

	public Boolean pos(String str, String suffix) {
		if (str == null || suffix == null) {
			return false;
		}
		return str.contains(suffix);
	}

	public Integer indexOf(String str, String suffix) {
		if (str == null || suffix == null) {
			return -1;
		}
		return str.indexOf(suffix);
	}

	public Integer lastIndexOf(String str, String suffix) {
		if (str == null || suffix == null) {
			return -1;
		}
		return str.lastIndexOf(suffix);
	}

	public Integer length(String str) {
		if (StringUtils.isEmpty(str)) {
			return 0;
		}
		return str.length();
	}
	public String replaceAll(String str, String regex, String replacement) {
		if (str == null || regex == null || replacement == null) {
			return null;
		}
		return str.replaceAll(regex, replacement);
	}
}
