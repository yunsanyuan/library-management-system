package com.library.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String role;
    private String avatarUrl;
    private String bio;
    private String createTime;
}
