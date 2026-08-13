package com.talented.buttie.common.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;

@Builder
public record PageResponse<T>(
    @ApiModelProperty(value = "현재 페이지 번호 (1부터 시작)", example = "1")
    int page,

    @ApiModelProperty(value = "페이지 당 항목 수", example = "10")
    int size,

    @ApiModelProperty(value = "전체 요소 수", example = "42")
    long totalElements,

    @ApiModelProperty(value = "전체 페이지 수", example = "5")
    int totalPages,

    @ApiModelProperty(value = "다음 페이지 존재 여부", example = "true")
    boolean hasNext,

    @ApiModelProperty(value = "이전 페이지 존재 여부", example = "false")
    boolean hasPrevious,

    @ApiModelProperty(value = "조회된 데이터 리스트")
    List<T> content
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return PageResponse.<T>builder()
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .hasNext(page < totalPages)
            .hasPrevious(page > 1)
            .content(content != null ? content : List.of())
            .build();
    }
}
