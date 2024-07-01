package com.swak.jdbc.segments;

import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSegmentList  extends ArrayList<SqlSegment> implements SqlSegment {
    /**
     * 最后一个值
     */
    SqlSegment lastValue = null;
    /**
     * 刷新 lastValue
     */
    boolean flushLastValue = false;

    String sqlSegment = StringPool.EMPTY;
    /**
     * 是否缓存过结果集
     */
    private boolean cacheSqlSegment = true;

    /**
     * 重写方法,做个性化适配
     *
     * @param c 元素集合
     * @return 是否添加成功
     */
    @Override
    public boolean addAll(Collection<? extends SqlSegment> c) {
        List<SqlSegment> list = new ArrayList<>(c);
        boolean goon = transformList(list, list.get(0), list.get(list.size() - 1));
        if (goon) {
            cacheSqlSegment = false;
            if (flushLastValue) {
                this.flushLastValue(list);
            }
            return super.addAll(list);
        }
        return false;
    }

    /**
     * 在其中对值进行判断以及更改 list 的内部元素
     *
     * @param list         传入进来的 ISqlSegment 集合
     * @param firstSegment SqlSegment 集合里第一个值
     * @param lastSegment  SqlSegment 集合里最后一个值
     * @return true 是否继续向下执行; false 不再向下执行
     */
    protected abstract boolean transformList(List<SqlSegment> list, SqlSegment firstSegment, SqlSegment lastSegment);

    /**
     * 刷新属性 lastValue
     */
    private void flushLastValue(List<SqlSegment> list) {
        lastValue = list.get(list.size() - 1);
    }

    /**
     * 删除元素里最后一个值</br>
     * 并刷新属性 lastValue
     */
    void removeAndFlushLast() {
        remove(size() - 1);
        flushLastValue(this);
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        if (cacheSqlSegment) {
            return sqlSegment;
        }
        cacheSqlSegment = true;
        sqlSegment = childrenSqlSegment(valuePairs);
        return sqlSegment;
    }

    /**
     * 只有该类进行过 addAll 操作,才会触发这个方法
     * <p>
     * 方法内可以放心进行操作
     *
     * @return sqlSegment
     */
    protected abstract String childrenSqlSegment(ParamNameValuePairs valuePairs);

    @Override
    public void clear() {
        super.clear();
        lastValue = null;
        sqlSegment = StringPool.EMPTY;
        cacheSqlSegment = true;
    }
}
