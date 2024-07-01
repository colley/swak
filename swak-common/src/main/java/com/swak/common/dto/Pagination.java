package com.swak.common.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author yuanchao.ma
 * @since  2022/05/16
 */
public class Pagination<T> implements java.io.Serializable {
    private static final long serialVersionUID = 2655888519198509118L;
    private List<T> list;
    private PageInfo page;

    public Pagination() {
    }

    public Pagination(List<T> list) {
        this(list, null);
    }

    public Pagination(List<T> list, PageInfo pape) {
        this.page = pape;
        this.list = list;
    }

    public static <T> Pagination<T> builder(List<T> list, PageInfo pape) {
        return new Pagination<T>(list, pape);

    }

    public static <T> Pagination<T> empty() {
        return new Pagination<>(Collections.emptyList());

    }

    public List<T> getList() {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public PageInfo getPage() {
        return page;
    }

    public void setPage(PageInfo page) {
        this.page = page;
    }
}
