package org.mentorship.reflectly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponseDto<T> {
    private List<T> content;
    private long total;
    private String nextLink;
}