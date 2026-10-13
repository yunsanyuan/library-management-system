package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.dto.BorrowRecordVO;
import com.library.dto.BorrowRequest;
import com.library.dto.StatisticsVO;
import com.library.dto.UserVO;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import com.library.service.StatisticsService;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 借阅控制器
 * 处理借书、还书、借阅记录查询、统计数据、用户管理
 */
@RestController
@RequestMapping("/api/borrow")
@Tag(name = "借阅管理", description = "借书、还书、统计和用户管理接口")
public class BorrowController {

    private final BorrowService borrowService;
    private final StatisticsService statisticsService;
    private final UserService userService;

    public BorrowController(BorrowService borrowService, StatisticsService statisticsService,
                          UserService userService) {
        this.borrowService = borrowService;
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "借书", description = "借阅图书，校验库存，生成借阅记录，available-1")
    public Result<BorrowRecord> borrowBook(@Valid @RequestBody BorrowRequest request,
                                           HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        BorrowRecord record = borrowService.borrowBook(userId, request.getBookId());
        return Result.success(record);
    }

    @PutMapping("/{id}/return")
    @Operation(summary = "还书", description = "归还图书，普通用户只能还自己的，管理员可还任意")
    public Result<BorrowRecord> returnBook(@PathVariable Long id,
                                           HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        BorrowRecord record = borrowService.returnBook(id, userId, role);
        return Result.success(record);
    }

    @GetMapping("/my")
    @Operation(summary = "我的借阅", description = "分页查询当前用户借阅记录")
    public Result<Page<BorrowRecordVO>> getMyRecords(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Page<BorrowRecordVO> result = borrowService.getMyRecords(userId, page, size);
        return Result.success(result);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'super_admin')")
    @Operation(summary = "所有借阅记录", description = "管理员分页查看所有借阅记录，支持筛选")
    public Result<Page<BorrowRecordVO>> getAllRecords(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String username,
                                                      @RequestParam(required = false) String bookTitle,
                                                      @RequestParam(required = false) String status) {
        Page<BorrowRecordVO> result = borrowService.getAllRecords(page, size, username, bookTitle, status);
        return Result.success(result);
    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "统计数据", description = "获取图书、用户、借阅等统计数据")
    public Result<StatisticsVO> getStatistics() {
        return Result.success(statisticsService.getStatistics());
    }

    /**
     * 用户列表（仅管理员），支持搜索
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('admin', 'super_admin')")
    @Operation(summary = "用户列表", description = "管理员分页查看用户列表")
    public Result<Page<UserVO>> listUsers(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(page, size, keyword));
    }

    /**
     * 修改用户角色（仅管理员）
     */
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('super_admin')")
    @Operation(summary = "修改角色", description = "修改用户角色，仅super_admin可操作")
    public Result<UserVO> updateUserRole(@PathVariable Long id, @RequestBody java.util.Map<String, String> body,
                                         HttpServletRequest httpRequest) {
        String operatorRole = (String) httpRequest.getAttribute("role");
        return Result.success(userService.updateUserRole(id, body.get("role"), operatorRole));
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('admin', 'super_admin')")
    @Operation(summary = "删除用户", description = "删除用户，admin不能删除admin/super_admin")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operatorRole = (String) httpRequest.getAttribute("role");
        userService.deleteUser(id, operatorRole);
        return Result.success();
    }

    /**
     * 管理员重置用户密码
     */
    @PutMapping("/users/{id}/password")
    @PreAuthorize("hasAnyRole('admin', 'super_admin')")
    @Operation(summary = "重置密码", description = "管理员重置用户密码，admin不可重置super_admin密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body,
                                      HttpServletRequest httpRequest) {
        String operatorRole = (String) httpRequest.getAttribute("role");
        userService.resetPassword(id, body.get("newPassword"), operatorRole);
        return Result.success();
    }

    @DeleteMapping("/records/{id}")
    @Operation(summary = "删除借阅记录", description = "删除已归还的借阅记录，普通用户只能删自己的")
    public Result<Void> deleteRecord(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        borrowService.deleteRecord(id, userId, role);
        return Result.success();
    }

    @GetMapping("/users/{id}/profile")
    @Operation(summary = "用户公开主页", description = "查看任意用户的公开信息")
    public Result<UserVO> getPublicProfile(@PathVariable Long id) {
        return Result.success(userService.getPublicProfile(id));
    }
}
