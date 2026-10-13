package com.library.dto;

import lombok.Data;

@Data
public class UserRankVO {
    private Long userId;
    private String username;
    private long borrowCount;
}
