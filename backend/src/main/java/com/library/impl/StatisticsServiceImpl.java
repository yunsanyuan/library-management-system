package com.library.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.dto.BookRankVO;
import com.library.dto.StatisticsVO;
import com.library.dto.UserRankVO;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowRecordMapper;
import com.library.mapper.UserMapper;
import com.library.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    public StatisticsServiceImpl(BookMapper bookMapper, UserMapper userMapper,
                                BorrowRecordMapper borrowRecordMapper) {
        this.bookMapper = bookMapper;
        this.userMapper = userMapper;
        this.borrowRecordMapper = borrowRecordMapper;
    }

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO vo = new StatisticsVO();

        vo.setBookCount(bookMapper.selectCount(null));

        LambdaQueryWrapper<Book> availWrapper = new LambdaQueryWrapper<>();
        availWrapper.gt(Book::getAvailable, 0);
        vo.setAvailableBookCount(bookMapper.selectCount(availWrapper));

        vo.setBorrowedBookCount(bookMapper.selectList(null).stream()
                .mapToLong(b -> b.getTotal() - b.getAvailable())
                .sum());

        vo.setUserCount(userMapper.selectCount(null));

        LambdaQueryWrapper<BorrowRecord> borrowingWrapper = new LambdaQueryWrapper<>();
        borrowingWrapper.eq(BorrowRecord::getStatus, "BORROWED");
        vo.setBorrowingCount(borrowRecordMapper.selectCount(borrowingWrapper));

        LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.eq(BorrowRecord::getStatus, "BORROWED")
                      .lt(BorrowRecord::getDueTime, LocalDateTime.now());
        vo.setOverdueCount(borrowRecordMapper.selectCount(overdueWrapper));

        LambdaQueryWrapper<BorrowRecord> returnedWrapper = new LambdaQueryWrapper<>();
        returnedWrapper.eq(BorrowRecord::getStatus, "RETURNED");
        vo.setReturnedCount(borrowRecordMapper.selectCount(returnedWrapper));

        vo.setBookRank(getBookRanking(10));
        vo.setUserRank(getUserRanking(10));

        return vo;
    }

    private List<BookRankVO> getBookRanking(int limit) {
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        Map<Long, Long> countMap = allRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        return countMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    BookRankVO vo = new BookRankVO();
                    vo.setBookId(entry.getKey());
                    vo.setBorrowCount(entry.getValue());
                    Book book = bookMapper.selectById(entry.getKey());
                    if (book != null) {
                        vo.setTitle(book.getTitle());
                        vo.setAuthor(book.getAuthor());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private List<UserRankVO> getUserRanking(int limit) {
        List<BorrowRecord> allRecords = borrowRecordMapper.selectList(null);
        Map<Long, Long> countMap = allRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getUserId, Collectors.counting()));

        return countMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    UserRankVO vo = new UserRankVO();
                    vo.setUserId(entry.getKey());
                    vo.setBorrowCount(entry.getValue());
                    User user = userMapper.selectById(entry.getKey());
                    if (user != null) {
                        vo.setUsername(user.getUsername());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
