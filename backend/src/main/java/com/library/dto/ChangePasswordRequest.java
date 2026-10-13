package com.library.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
