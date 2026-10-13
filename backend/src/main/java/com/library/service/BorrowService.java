package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.BorrowRecordVO;
import com.library.entity.BorrowRecord;

/**
 * 借阅服务接口
 */
public interface BorrowService {
    BorrowRecord borrowBook(Long userId, Long bookId);
    BorrowRecord returnBook(Long recordId, Long userId, String role);
    Page<BorrowRecordVO> getMyRecords(Long userId, int page, int size);
    Page<BorrowRecordVO> getAllRecords(int page, int size, String username, String bookTitle, String status);
    void deleteRecord(Long recordId, Long userId, String role);
}
