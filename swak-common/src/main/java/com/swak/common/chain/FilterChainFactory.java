package com.swak.common.chain;

import java.util.List;

/**
 * 责任链模式工厂
 * @author colley.ma
 * @date 2018/04/17
 */
public class FilterChainFactory {	
	
	public static<T> FilterChain<T> buildFilterChain(List<? extends Filter<T>> filterClsList) {
        FilterInvoker<T> last = new FilterInvoker<T>(){};
        FilterChain<T> filterChain = new FilterChain<T>();
        for(int i = filterClsList.size() - 1; i >= 0; i--){
            FilterInvoker<T> next = last;
			Filter<T> filter = filterClsList.get(i);
            last = new FilterInvoker<T>(){
                @Override
                public void invoke(T context) {
                    filter.doFilter(context, next);
                }
            };
        }
        filterChain.setHeader(last);
        return filterChain;
    }

}
