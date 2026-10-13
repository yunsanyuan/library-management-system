package com.library.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 图书新增/修改请求DTO
 */
@Data
public class BookRequest {
    @NotBlank(message = "书名不能为空")
    private String title;

    @NotBlank(message = "作者不能为空")
    private String author;

    private String isbn;
    private String publisher;
    private LocalDate publicationDate;
    private String description;
    private String coverUrl;
    private String category;

    @NotNull(message = "总库存不能为空")
    private Integer total;
}
