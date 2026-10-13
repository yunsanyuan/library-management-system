package com.library.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 借书请求DTO
 */
@Data
public class BorrowRequest {
    @NotNull(message = "图书ID不能为空")
    private Long bookId;
}
