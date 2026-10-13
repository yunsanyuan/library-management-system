package com.library.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 借阅记录响应DTO（含图书和用户信息）
 */
@Data
public class BorrowRecordVO {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime borrowTime;
    private LocalDateTime dueTime;
    private LocalDateTime returnTime;
    private String status;
}
