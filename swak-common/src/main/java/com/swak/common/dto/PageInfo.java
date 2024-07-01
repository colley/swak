
package com.swak.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class PageInfo implements java.io.Serializable {
    private static final long serialVersionUID = -8141622693773776209L;
    public static int DEFAULT_PAGE_SIZE = 15;

    private Integer currentPage = 1;
    private Integer pageSize = 10;
    private Long totalSize = 0L;

    /**
     * 增加排序字段
     * [{"column":"name"，"asc":true,"usePinyin":false}]
     * 使用方式：xxxService.page(new Page<Line>(query.getCurrentPage(), query.getPageSize()).addOrder(query.orders()),
     * queryWrapper);
     */
    private List<ExtOrderItem> orders = new ArrayList<>();

    public PageInfo() {
    }

    public PageInfo(Integer currentPage, Integer pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;

    }

    public PageInfo(Integer currentPage, Integer pageSize, Long totalSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalSize = totalSize;

    }

    public static PageInfo newPageInfo(Integer currentPage, Integer pageSize) {
        return new PageInfo(currentPage, pageSize, pageSize.longValue());
    }

    public static PageInfo page(PageInfo from, Long totalSize) {
        return new PageInfo(from.currentPage, from.pageSize, totalSize);
    }

    public static PageInfo newPageInfo(Integer currentPage, Integer pageSize, Long totalSize) {
        return new PageInfo(currentPage, pageSize, totalSize);
    }

    public PageInfo addOrder(ExtOrderItem... items) {
        orders.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * 支持mybatis-plus中的page分页查询排序
     */
    public ExtOrderItem[] orders() {
        return orders.stream().map(i -> {
            ExtOrderItem item = new ExtOrderItem();
            if (i.isUsePinyin()) {
                item.setColumn(String.format(" convert(%s using gbk) ", i.getColumn()));
            } else {
                item.setColumn(i.getColumn());
            }
            item.setAsc(i.isAsc());
            return item;
        }).collect(Collectors.toList()).toArray(new ExtOrderItem[0]);
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalPage() {
        return new Integer((new Double(Math.ceil(new Double(totalSize) / new Double(pageSize)))).intValue());
    }

    public Integer getOffset() {
        return currentPage > 0 ? ((currentPage - 1) * pageSize) : 0;
    }

    @Data
    @Accessors(chain = true)
    public static class ExtOrderItem {
        /**
         * 需要进行排序的字段
         */
        private String column;

        /**
         * 是否正序排列，默认 true
         */
        private boolean asc = true;

        /**
         * 是否使用pinyin排序
         */
        private boolean usePinyin = false;

        public static ExtOrderItem asc(String column) {
            return build(column, true);
        }

        public static ExtOrderItem desc(String column) {
            return build(column, false);
        }

        private static ExtOrderItem build(String column, boolean asc) {
            ExtOrderItem item = new ExtOrderItem();
            item.setColumn(column);
            item.setAsc(asc);
            return item;
        }
    }
}
