package com.swak.common.chain;

/**
 * 拦截器链
 * ClassName: FilterChain.java 
 * @author colley.ma
 * @since 2.4.0
 */
public class FilterChain<T>{

    private FilterInvoker<T> header;

    public void doFilter(T context){
        header.invoke(context);
    }

    public void setHeader(FilterInvoker<T> header) {
        this.header = header;
    }
}
