package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.BookRequest;
import com.library.dto.StockRequest;
import com.library.entity.Book;
import java.util.List;

public interface BookService {
    Page<Book> listBooks(int page, int size, String keyword, String category);
    Book getBookById(Long id);
    Book addBook(BookRequest request);
    Book updateBook(Long id, BookRequest request);
    void deleteBook(Long id);
    Book updateStock(Long id, StockRequest request);
    List<String> listCategories();
    List<Book> getRecommended(int limit);
}
