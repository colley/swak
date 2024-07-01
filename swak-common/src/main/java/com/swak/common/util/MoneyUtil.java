/**
 * 
 */
package com.swak.common.util;

import java.math.BigDecimal;

/**
 * The type Money util.
 *
 * @author ColleyMa
 * @version V1.0
 * @Title: MoneyUtil.java
 * @Description: 货币类型常用方法支持
 */
public class MoneyUtil {
	/**
	 * 获取元到分
	 *
	 * @param yuan the yuan
	 * @return yuan to cent
	 */
	public static Long getYuanToCent(String yuan) {
		Money money = new Money(yuan);
		return money.getCent();
	}

	/**
	 * 获取元到分
	 *
	 * @param yuan the yuan
	 * @return yuan to cent
	 */
	public static Long getYuanToCent(double yuan) {
		Money money = new Money(yuan);
		return money.getCent();
	}

	/**
	 * 获取分到元
	 *
	 * @param cent the cent
	 * @return cent to yuan
	 */
	public static String getCentToYuan(long cent) {
		Money m = new Money();
		m.setCent(cent);
		return m.toString();
	}

	/**
	 * 返回元BigDecimal
	 *
	 * @param cent the cent
	 * @return BigDecimal cent to decimal yuan
	 */
	public static BigDecimal getCentToDecimalYuan(long cent) {
		Money m = new Money();
		m.setCent(cent);
		return m.getAmount();
	}

	/**
	 * 返回元BigDecimal
	 *
	 * @param cent the cent
	 * @return BigDecimal cent to decimal yuan
	 */
	public static BigDecimal getCentToDecimalYuan(Integer cent) {
		Money m = new Money();
		m.setCent(cent);
		return m.getAmount();
	}

	/**
	 * Gets cent to yuan.
	 *
	 * @param cent the cent
	 * @return the cent to yuan
	 */
	public static String getCentToYuan(String cent) {
		Money m = new Money();
		m.setCent(GetterUtil.getLong(cent));
		return m.toString();
	}

	/**
	 * Gets cent to yuan.
	 *
	 * @param cent the cent
	 * @return the cent to yuan
	 */
	public static String getCentToYuan(Integer cent) {
		Money m = new Money();
		m.setCent(cent);
		return m.toString();
	}
}
