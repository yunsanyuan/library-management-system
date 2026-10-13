package com.library.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 库存调整请求DTO
 */
@Data
public class StockRequest {
    @NotNull(message = "总库存不能为空")
    private Integer total;
}
