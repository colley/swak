package com.swak.common.chain;


/**
 * 责任Filter
 * ClassName: Filter.java 
 * @author colley.ma
 * @date 2021年3月19日 下午6:03:53
 */
public interface Filter<T> {

	/**
	 *  doFilter
	 * @Param [context, nextFilter]
	 * @return void
	 **/
	void doFilter(T context, FilterInvoker<T> nextFilter);

}