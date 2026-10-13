import request from './request'
import type { ApiResult, PageResult, Book } from '../types'

export function getBooks(page: number, size: number, keyword?: string, category?: string) {
  return request.get<any, ApiResult<PageResult<Book>>>('/books', {
    params: { page, size, keyword, category }
  })
}

export function getBookById(id: number) {
  return request.get<any, ApiResult<Book>>(`/books/${id}`)
}

export function getCategories() {
  return request.get<any, ApiResult<string[]>>('/books/categories')
}

export function getRecommended(limit: number = 6) {
  return request.get<any, ApiResult<Book[]>>('/books/recommended', { params: { limit } })
}

export function addBook(data: Partial<Book>) {
  return request.post<any, ApiResult<Book>>('/books', data)
}

export function updateBook(id: number, data: Partial<Book>) {
  return request.put<any, ApiResult<Book>>(`/books/${id}`, data)
}

export function deleteBook(id: number) {
  return request.delete<any, ApiResult<void>>(`/books/${id}`)
}

export function updateStock(id: number, total: number) {
  return request.put<any, ApiResult<Book>>(`/books/${id}/stock`, { total })
}
