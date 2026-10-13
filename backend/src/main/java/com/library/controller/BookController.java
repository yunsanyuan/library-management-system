package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.Result;
import com.library.dto.BookRequest;
import com.library.dto.StockRequest;
import com.library.entity.Book;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "图书管理", description = "图书浏览和管理接口")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "图书列表", description = "分页查询图书，支持按书名/作者/ISBN模糊搜索和分类筛选")
    public Result<Page<Book>> listBooks(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String category) {
        Page<Book> result = bookService.listBooks(page, size, keyword, category);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "图书详情", description = "根据ID获取图书详细信息")
    public Result<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return Result.success(book);
    }

    @GetMapping("/categories")
    @Operation(summary = "分类列表", description = "获取所有图书分类")
    public Result<List<String>> listCategories() {
        return Result.success(bookService.listCategories());
    }

    @GetMapping("/recommended")
    @Operation(summary = "好书推荐", description = "根据借阅热度推荐图书")
    public Result<List<Book>> getRecommended(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(bookService.getRecommended(limit));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "新增图书", description = "管理员新增图书，available默认等于total")
    public Result<Book> addBook(@Valid @RequestBody BookRequest request) {
        Book book = bookService.addBook(request);
        return Result.success(book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "修改图书", description = "管理员修改图书信息，不可直接修改库存")
    public Result<Book> updateBook(@PathVariable Long id,
                                   @Valid @RequestBody BookRequest request) {
        Book book = bookService.updateBook(id, request);
        return Result.success(book);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "删除图书", description = "管理员删除图书")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "调整库存", description = "管理员调整图书总库存")
    public Result<Book> updateStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        Book book = bookService.updateStock(id, request);
        return Result.success(book);
    }
}
