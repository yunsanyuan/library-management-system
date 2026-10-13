package com.library.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.BorrowRecordVO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.BorrowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 借阅服务实现
 * 处理借书、还书、强制归还及借阅记录查询
 * 核心逻辑：借书时校验库存并扣减，还书时恢复库存
 */
@Service
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;

    public BorrowServiceImpl(BorrowRecordMapper borrowRecordMapper,
                            BookMapper bookMapper,
                            UserMapper userMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
    }

    /**
     * 借书操作
     * 1. 校验图书是否存在且有库存
     * 2. 校验用户是否已借阅此书且未归还
     * 3. 创建借阅记录，到期时间30天
     * 4. 扣减可借数量（available-1）
     */
    @Override
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 查询图书
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        if (book.getAvailable() <= 0) {
            throw new BusinessException(ErrorCode.BOOK_NO_STOCK);
        }

        // 校验是否已借阅且未归还
        LambdaQueryWrapper<BorrowRecord> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BorrowRecord::getUserId, userId)
                    .eq(BorrowRecord::getBookId, bookId)
                    .eq(BorrowRecord::getStatus, "BORROWED");
        if (borrowRecordMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(ErrorCode.BORROW_DUPLICATE);
        }

        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowTime(LocalDateTime.now());
        record.setDueTime(LocalDateTime.now().plusDays(30)); // 借期30天
        record.setStatus("BORROWED");
        borrowRecordMapper.insert(record);

        // 扣减可借数量
        book.setAvailable(book.getAvailable() - 1);
        bookMapper.updateById(book);

        return record;
    }

    /**
     * 还书操作
     * 1. 校验借阅记录存在且未归还
     * 2. 普通用户只能还自己的书，管理员可还任意
     * 3. 标记归还状态，记录归还时间
     * 4. 恢复可借数量（available+1）
     */
    @Override
    @Transactional
    public BorrowRecord returnBook(Long recordId, Long userId, String role) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }
        if ("RETURNED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.BORROW_ALREADY_RETURNED);
        }

        // 权限校验：普通用户只能归还自己借阅的图书
        if (!"admin".equals(role) && !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BORROW_NOT_YOURS);
        }

        // 标记归还
        record.setStatus("RETURNED");
        record.setReturnTime(LocalDateTime.now());
        borrowRecordMapper.updateById(record);

        // 恢复可借数量
        Book book = bookMapper.selectById(record.getBookId());
        if (book != null) {
            book.setAvailable(book.getAvailable() + 1);
            bookMapper.updateById(book);
        }

        return record;
    }

    /**
     * 查询当前用户借阅记录（分页）
     */
    @Override
    public Page<BorrowRecordVO> getMyRecords(Long userId, int page, int size) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getUserId, userId)
               .orderByDesc(BorrowRecord::getBorrowTime);
        Page<BorrowRecord> result = borrowRecordMapper.selectPage(pageParam, wrapper);

        // 转换为VO
        Page<BorrowRecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(convertToVO(result.getRecords()));
        return voPage;
    }

    /**
     * 管理员查询所有借阅记录，支持按用户名、书名、状态筛选
     */
    @Override
    public Page<BorrowRecordVO> getAllRecords(int page, int size, String username,
                                              String bookTitle, String status) {
        Page<BorrowRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();

        // 按状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(BorrowRecord::getStatus, status);
        }

        // 按用户名筛选：先查用户ID
        if (StringUtils.hasText(username)) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getUsername, username);
            List<User> users = userMapper.selectList(userWrapper);
            if (users.isEmpty()) {
                // 无匹配用户，返回空结果
                Page<BorrowRecordVO> emptyPage = new Page<>(page, size, 0);
                emptyPage.setRecords(List.of());
                return emptyPage;
            }
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            wrapper.in(BorrowRecord::getUserId, userIds);
        }

        // 按书名筛选：先查图书ID
        if (StringUtils.hasText(bookTitle)) {
            LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
            bookWrapper.like(Book::getTitle, bookTitle);
            List<Book> books = bookMapper.selectList(bookWrapper);
            if (books.isEmpty()) {
                Page<BorrowRecordVO> emptyPage = new Page<>(page, size, 0);
                emptyPage.setRecords(List.of());
                return emptyPage;
            }
            List<Long> bookIds = books.stream().map(Book::getId).collect(Collectors.toList());
            wrapper.in(BorrowRecord::getBookId, bookIds);
        }

        wrapper.orderByDesc(BorrowRecord::getBorrowTime);
        Page<BorrowRecord> result = borrowRecordMapper.selectPage(pageParam, wrapper);

        Page<BorrowRecordVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(convertToVO(result.getRecords()));
        return voPage;
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId, Long userId, String role) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND);
        }
        if ("BORROWED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.BORROW_NOT_RETURNED);
        }
        if (!"admin".equals(role) && !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BORROW_NOT_YOURS);
        }
        borrowRecordMapper.deleteById(recordId);
    }

    /**
     * 将借阅记录转换为VO，附带用户名和书名
     */
    private List<BorrowRecordVO> convertToVO(List<BorrowRecord> records) {
        return records.stream().map(record -> {
            BorrowRecordVO vo = new BorrowRecordVO();
            vo.setId(record.getId());
            vo.setUserId(record.getUserId());
            vo.setBookId(record.getBookId());
            vo.setBorrowTime(record.getBorrowTime());
            vo.setDueTime(record.getDueTime());
            vo.setReturnTime(record.getReturnTime());
            vo.setStatus(record.getStatus());

            // 查询用户名
            User user = userMapper.selectById(record.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }

            // 查询书名
            Book book = bookMapper.selectById(record.getBookId());
            if (book != null) {
                vo.setBookTitle(book.getTitle());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
