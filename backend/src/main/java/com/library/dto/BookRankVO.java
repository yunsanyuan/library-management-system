package com.library.dto;

import lombok.Data;

@Data
public class BookRankVO {
    private Long bookId;
    private String title;
    private String author;
    private long borrowCount;
}
