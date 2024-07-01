package com.swak.common.chain;

/**
 * 拦截器链
 * ClassName: FilterChain.java 
 * @author colley.ma
 * @date 2021年3月19日 下午6:04:39
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
