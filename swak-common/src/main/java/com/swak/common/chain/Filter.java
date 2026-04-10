package com.swak.common.chain;


/**
 * 责任Filter
 * ClassName: Filter.java 
 * @author colley.ma
 * @since 2.4.0
 */
public interface Filter<T> {

	/**
	 * doFilter
	 **/
	void doFilter(T context, FilterInvoker<T> nextFilter);

}