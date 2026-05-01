package com.aquaculture.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Paginated result wrapper
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Current page records
     */
    private List<T> records;

    /**
     * Total record count
     */
    private long total;

    /**
     * Current page number
     */
    private long page;

    /**
     * Page size
     */
    private long size;

    /**
     * Total page count
     */
    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long page, long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

}
