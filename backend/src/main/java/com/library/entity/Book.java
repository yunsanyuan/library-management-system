package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private LocalDate publicationDate;
    private String description;
    private String coverUrl;
    private String category;
    private Integer total;
    private Integer available;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
