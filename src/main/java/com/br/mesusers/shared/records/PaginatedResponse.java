package com.br.mesusers.shared.records;

import java.util.List;

public record PaginatedResponse<T>(
        List<?> items,
        int page,
        int totalCount,
        int pageSize,
        int totalPages) {

    public PaginatedResponse(List<?> items, int page, int totalCount, int pageSize, int totalPages) {
        this.items = items;
        this.page = page;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

}
