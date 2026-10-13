package com.library.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.BookRequest;
import com.library.dto.StockRequest;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public BookServiceImpl(BookMapper bookMapper, BorrowRecordMapper borrowRecordMapper) {
        this.bookMapper = bookMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    @Override
    public Page<Book> listBooks(int page, int size, String keyword, String category) {
        Page<Book> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword)
            );
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Book::getCategory, category);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return bookMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Book getBookById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        return book;
    }

    @Override
    public Book addBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublicationDate(request.getPublicationDate());
        book.setDescription(request.getDescription());
        book.setCoverUrl(request.getCoverUrl());
        book.setCategory(request.getCategory());
        book.setTotal(request.getTotal());
        book.setAvailable(request.getTotal());
        bookMapper.insert(book);
        return book;
    }

    @Override
    public Book updateBook(Long id, BookRequest request) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublisher(request.getPublisher());
        book.setPublicationDate(request.getPublicationDate());
        book.setDescription(request.getDescription());
        book.setCoverUrl(request.getCoverUrl());
        book.setCategory(request.getCategory());
        bookMapper.updateById(book);
        return book;
    }

    @Override
    public void deleteBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        bookMapper.deleteById(id);
    }

    @Override
    public Book updateStock(Long id, StockRequest request) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        int borrowed = book.getTotal() - book.getAvailable();
        int newTotal = request.getTotal();
        int newAvailable = newTotal - borrowed;
        if (newAvailable < 0) {
            newAvailable = 0;
        }
        book.setTotal(newTotal);
        book.setAvailable(newAvailable);
        bookMapper.updateById(book);
        return book;
    }

    @Override
    public List<String> listCategories() {
        List<Book> allBooks = bookMapper.selectList(null);
        return allBooks.stream()
                .map(Book::getCategory)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getRecommended(int limit) {
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        Map<Long, Long> borrowCountMap = allRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        List<Book> allBooks = bookMapper.selectList(null);
        allBooks.sort((a, b) -> {
            long countA = borrowCountMap.getOrDefault(a.getId(), 0L);
            long countB = borrowCountMap.getOrDefault(b.getId(), 0L);
            if (countB != countA) return Long.compare(countB, countA);
            return a.getAvailable().compareTo(b.getAvailable());
        });

        return allBooks.stream().limit(limit).collect(Collectors.toList());
    }
}
