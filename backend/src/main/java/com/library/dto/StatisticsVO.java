package com.library.dto;

import lombok.Data;
import java.util.List;

@Data
public class StatisticsVO {
    private long bookCount;
    private long availableBookCount;
    private long borrowedBookCount;
    private long userCount;
    private long borrowingCount;
    private long overdueCount;
    private long returnedCount;
    private List<BookRankVO> bookRank;
    private List<UserRankVO> userRank;
}
