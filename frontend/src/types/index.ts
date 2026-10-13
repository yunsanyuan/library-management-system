export interface User {
  id: number
  username: string
  role: string
  avatarUrl: string
  bio: string
  createTime: string
}

export interface Book {
  id: number
  title: string
  author: string
  isbn: string
  publisher: string
  publicationDate: string
  description: string
  coverUrl: string
  category: string
  total: number
  available: number
  createTime: string
  updateTime: string
}

export interface BorrowRecord {
  id: number
  userId: number
  username: string
  bookId: number
  bookTitle: string
  borrowTime: string
  dueTime: string
  returnTime: string
  status: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface BookRank {
  bookId: number
  title: string
  author: string
  borrowCount: number
}

export interface UserRank {
  userId: number
  username: string
  borrowCount: number
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
