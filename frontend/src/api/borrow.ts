import request from './request'
import type { ApiResult, PageResult, BorrowRecord } from '../types'

/**
 * 借阅API：借书、还书、查询记录
 */

export function borrowBook(bookId: number) {
  return request.post<any, ApiResult<any>>('/borrow', { bookId })
}

export function returnBook(id: number) {
  return request.put<any, ApiResult<any>>(`/borrow/${id}/return`)
}

export function getMyRecords(page: number, size: number) {
  return request.get<any, ApiResult<PageResult<BorrowRecord>>>('/borrow/my', {
    params: { page, size }
  })
}

export function getAllRecords(page: number, size: number, username?: string, bookTitle?: string, status?: string) {
  return request.get<any, ApiResult<PageResult<BorrowRecord>>>('/borrow', {
    params: { page, size, username, bookTitle, status }
  })
}

export function deleteRecord(id: number) {
  return request.delete<any, ApiResult<void>>(`/borrow/records/${id}`)
}
