import request from './request'
import type { ApiResult, BookRank, UserRank } from '../types'

export interface Statistics {
  bookCount: number
  availableBookCount: number
  borrowedBookCount: number
  userCount: number
  borrowingCount: number
  overdueCount: number
  returnedCount: number
  bookRank: BookRank[]
  userRank: UserRank[]
}

export function getStatistics() {
  return request.get<any, ApiResult<Statistics>>('/borrow/statistics')
}
