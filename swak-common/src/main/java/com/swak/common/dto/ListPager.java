/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-3-11 下午7:45
 * History:
 */
package com.swak.common.dto;

import com.swak.common.dto.base.VO;
import com.swak.common.util.GetterUtil;

import java.util.List;

/**
 * 本地分页数据结构
 *
 * @author colley.ma
 * @since 2022/9/9 18:38
 */
public class ListPager<T> implements VO {

    private static final long serialVersionUID = 421834782569405170L;
    private Integer size;
    private Integer pageSize;
    private Integer totalPage;
    private List<T> item;
    private int pageIndex;
    private int startIndex;
    private int endIndex;

    public ListPager(Integer size, Integer pageIndex, Integer pageSize) {
        this.size = size;
        this.pageSize = pageSize;
        totalPage = size / pageSize;
        this.totalPage = totalPage + (((size % pageSize) > 0) ? 1 : 0);
        this.pageIndex = pageIndex;
        this.startIndex = Math.min(getFirstResult(), size);
        this.endIndex = Math.min(startIndex + pageSize, size);
    }

    public ListPager(List<T> item, Integer pageSize) {
        this(item.size(), pageSize);
        this.item = item;
    }

    public ListPager(Integer size, Integer pageSize) {
        this.size = size;
        this.pageSize = pageSize;
        totalPage = size / pageSize;
        totalPage = totalPage + (size % pageSize > 0 ? 1 : 0);
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getSubList() {
        return GetterUtil.split(item, startIndex, endIndex);
    }

    public List<T> getPageList(int i) {
        this.setPageIndex(i);
        return getPageList();
    }

    public List<T> getPageList() {
        this.startIndex = Math.min(getFirstResult(), size);
        this.endIndex = Math.min(startIndex + pageSize, size);
        return GetterUtil.split(item, startIndex, endIndex);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public List<T> getItem() {
        return item;
    }

    public ListPager<T> setItem(List<T> item) {
        this.item = item;

        return this;
    }

    public int getFirstResult() {
        return (((pageIndex - 1) * pageSize) > 0) ? ((pageIndex - 1) * pageSize) : 0;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
