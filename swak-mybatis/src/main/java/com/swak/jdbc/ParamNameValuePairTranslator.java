
package com.swak.jdbc;


import com.swak.jdbc.common.IbsStringHelper;
import com.swak.jdbc.metadata.TableList;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class ParamNameValuePairTranslator implements ParamNameValuePairs {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, Object> paramNameValuePairs = new LinkedHashMap<>();
    private final AtomicInteger maxIndex = new AtomicInteger(1);

    private TableList tableList;

    public ParamNameValuePairTranslator() {}

    public ParamNameValuePairTranslator(TableList tableList){
        this.tableList = tableList;
    }

    @Override
    public Map<String, Object> getParameter() {
        return paramNameValuePairs;
    }

    @Override
    public String addParameter(String parameterName, Object parameterValue) {
        lock.lock();
        try {
            if (StringUtils.isNotEmpty(parameterName)) {
                String parameterN = IbsStringHelper.replace(parameterName);
                if (containParameter(parameterN)) {
                    parameterN = parameterN + maxIndex.incrementAndGet();
                }
                paramNameValuePairs.put(parameterN, parameterValue);
                return parameterN;
            }
        } finally {
            lock.unlock();
        }
        return parameterName;
    }


    @Override
    public boolean containParameter(String parameterName) {
        if (StringUtils.isNotEmpty(parameterName)) {
            return paramNameValuePairs.containsKey(parameterName);
        }
        return false;
    }

    @Override
    public TableList getTableList() {
        return tableList;
    }


    @Override
    public void clear() {
        paramNameValuePairs.clear();
        Optional.ofNullable(tableList).ifPresent(TableList::clear);
    }
}
