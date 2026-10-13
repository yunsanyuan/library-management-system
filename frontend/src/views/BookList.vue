<template>
  <div class="book-list-page">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <div class="stat-card" style="border-left:3px solid #409eff"><div class="stat-val">{{ total }}</div><div class="stat-label">图书总数</div></div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card" style="border-left:3px solid #67c23a"><div class="stat-val">{{ availableBookCount }}</div><div class="stat-label">当前可借</div></div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card" style="border-left:3px solid #e6a23c"><div class="stat-val">{{ borrowedBookCount }}</div><div class="stat-label">已借出</div></div>
      </el-col>
    </el-row>

    <div class="filter-bar">
      <el-input v-model="keyword" placeholder="搜索书名 / 作者 / ISBN" clearable @clear="loadBooks" @keyup.enter="loadBooks" style="width:280px" size="default">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="selectedCategory" placeholder="全部分类" clearable @change="loadBooks" style="width:140px" size="default">
        <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
      </el-select>
      <el-button type="primary" @click="loadBooks">搜索</el-button>
      <el-radio-group v-model="viewMode" size="small" style="margin-left:auto">
        <el-radio-button value="card"><el-icon><Grid /></el-icon></el-radio-button>
        <el-radio-button value="table"><el-icon><List /></el-icon></el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="viewMode === 'card'" class="book-grid" v-loading="loading">
      <div class="book-card" v-for="book in books" :key="book.id" @click="router.push(`/books/${book.id}`)">
        <div class="book-cover">
          <img v-if="book.coverUrl" :src="book.coverUrl" alt="封面" />
          <div v-else class="cover-placeholder"><el-icon :size="32" color="#c0c4cc"><Reading /></el-icon></div>
          <div class="stock-badge" :class="{ out: book.available <= 0 }">{{ book.available > 0 ? `${book.available}/${book.total}` : '已借完' }}</div>
        </div>
        <div class="book-info">
          <div class="book-title" :title="book.title">{{ book.title }}</div>
          <div class="book-author">{{ book.author }}</div>
          <div class="book-meta">
            <el-tag v-if="book.category" size="small" type="info" effect="plain">{{ book.category }}</el-tag>
          </div>
          <div class="book-bottom">
            <el-tag :type="book.available > 0 ? 'success' : 'danger'" size="small" effect="dark">{{ book.available > 0 ? '可借' : '已借完' }}</el-tag>
            <el-button type="primary" size="small" :disabled="book.available <= 0" @click.stop="handleBorrow(book)">借阅</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && books.length === 0" description="暂无匹配图书" />
    </div>

    <el-table v-else :data="books" stripe border highlight-current-row v-loading="loading" @row-click="(row:any) => router.push(`/books/${row.id}`)" style="cursor:pointer">
      <el-table-column prop="title" label="书名" min-width="140" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="{ row }"><el-tag v-if="row.category" size="small" type="info">{{ row.category }}</el-tag><span v-else>-</span></template>
      </el-table-column>
      <el-table-column prop="publisher" label="出版社" width="120" />
      <el-table-column label="库存" width="80" align="center">
        <template #default="{ row }"><span :style="{color: row.available > 0 ? '#67c23a' : '#f56c6c'}">{{ row.available }}/{{ row.total }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }"><el-tag :type="row.available > 0 ? 'success' : 'danger'" size="small">{{ row.available > 0 ? '可借' : '已借完' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" :disabled="row.available <= 0" @click.stop="handleBorrow(row)">借阅</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-area">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="loadBooks" />
    </div>

    <div class="recommend-section" v-if="recommended.length > 0">
      <h3 class="section-title"><el-icon color="#e6a23c"><Medal /></el-icon> 好书推荐</h3>
      <el-row :gutter="12">
        <el-col :span="4" v-for="book in recommended" :key="book.id">
          <div class="rec-card" @click="router.push(`/books/${book.id}`)">
            <div class="rec-cover">
              <img v-if="book.coverUrl" :src="book.coverUrl" alt="封面" />
              <div v-else class="cover-placeholder-sm"><el-icon :size="20" color="#c0c4cc"><Reading /></el-icon></div>
            </div>
            <div class="rec-title">{{ book.title }}</div>
            <div class="rec-author">{{ book.author }}</div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getBooks, getCategories, getRecommended } from '../api/book'
import { borrowBook } from '../api/borrow'
import { getStatistics } from '../api/statistics'
import { useDisposed } from '../composables/useDisposed'
import type { Book } from '../types'

const router = useRouter()
const disposed = useDisposed()
const books = ref<Book[]>([])
const recommended = ref<Book[]>([])
const categories = ref<string[]>([])
const loading = ref(false)
const keyword = ref('')
const selectedCategory = ref('')
const viewMode = ref('card')
const page = ref(1)
const size = ref(12)
const total = ref(0)
const availableBookCount = ref(0)
const borrowedBookCount = ref(0)

async function loadBookStats() {
  try {
    const res = await getStatistics()
    if (disposed.value) return
    availableBookCount.value = res.data.availableBookCount
    borrowedBookCount.value = res.data.borrowedBookCount
  } catch {}
}

async function loadBooks() {
  loading.value = true
  try {
    const res = await getBooks(page.value, size.value, keyword.value || undefined, selectedCategory.value || undefined)
    if (disposed.value) return
    books.value = res.data.records
    total.value = res.data.total
  } catch {} finally { if (!disposed.value) loading.value = false }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    if (disposed.value) return
    categories.value = res.data
  } catch {}
}

async function loadRecommended() {
  try {
    const res = await getRecommended(6)
    if (disposed.value) return
    recommended.value = res.data
  } catch {}
}

async function handleBorrow(book: Book) {
  if (!localStorage.getItem('token')) {
    ElMessageBox.confirm('请先登录后再借阅图书', '未登录', {
      confirmButtonText: '去登录', cancelButtonText: '取消', type: 'info'
    }).then(() => router.push('/login')).catch(() => {})
    return
  }
  try {
    await ElMessageBox.confirm(`确定借阅《${book.title}》吗？`, '借阅确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })
    await borrowBook(book.id)
    ElMessage.success('借阅成功')
    loadBooks()
  } catch {}
}

onMounted(() => { loadBooks(); loadCategories(); loadRecommended(); loadBookStats() })
</script>

<style scoped>
.stats-row { margin-bottom: 16px; }
.stat-card { background: #fff; padding: 14px 20px; border-radius: 4px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.stat-val { font-size: 22px; font-weight: 700; color: #303133; line-height: 1.3; }
.stat-label { font-size: 12px; color: #909399; margin-top: 2px; }

.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }

.book-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; min-height: 200px; margin-bottom: 16px; }
.book-card { background: #fff; border-radius: 4px; overflow: hidden; cursor: pointer; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.25s; }
.book-card:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,0.1); }
.book-cover { height: 100px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; position: relative; }
.book-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { display: flex; align-items: center; justify-content: center; }
.stock-badge { position: absolute; top: 8px; right: 8px; padding: 2px 8px; background: #409eff; border-radius: 3px; color: #fff; font-size: 11px; font-weight: 600; }
.stock-badge.out { background: #f56c6c; }
.book-info { padding: 10px 12px 12px; }
.book-title { color: #303133; font-size: 14px; font-weight: 600; line-height: 1.3; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-author { color: #909399; font-size: 12px; margin-bottom: 4px; }
.book-meta { margin-bottom: 8px; }
.book-bottom { display: flex; align-items: center; justify-content: space-between; }

.pagination-area { margin: 16px 0; display: flex; justify-content: center; }

.recommend-section { margin-top: 20px; }
.section-title { color: #303133; font-size: 15px; font-weight: 600; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; }
.rec-card { background: #fff; border-radius: 4px; overflow: hidden; cursor: pointer; box-shadow: 0 1px 4px rgba(0,0,0,0.04); transition: all 0.2s; }
.rec-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.rec-cover { height: 80px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; overflow: hidden; }
.rec-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder-sm { display: flex; align-items: center; justify-content: center; }
.rec-title { font-size: 12px; font-weight: 600; color: #303133; padding: 6px 8px 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rec-author { font-size: 11px; color: #909399; padding: 0 8px 8px; }

[data-theme="dark"] .stat-card { background: #1d1e1f; }
[data-theme="dark"] .stat-val { color: #e0e0e0; }
[data-theme="dark"] .stat-label { color: #a0a0a0; }
[data-theme="dark"] .book-card { background: #1d1e1f; }
[data-theme="dark"] .book-cover { background: #2a2a2a; }
[data-theme="dark"] .book-title { color: #e0e0e0; }
[data-theme="dark"] .book-author { color: #a0a0a0; }
[data-theme="dark"] .section-title { color: #e0e0e0; }
[data-theme="dark"] .rec-card { background: #1d1e1f; }
[data-theme="dark"] .rec-cover { background: #2a2a2a; }
[data-theme="dark"] .rec-title { color: #e0e0e0; }
[data-theme="dark"] .rec-author { color: #a0a0a0; }
</style>
