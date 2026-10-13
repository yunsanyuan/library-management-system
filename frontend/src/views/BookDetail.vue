<template>
  <div class="book-detail-page" v-loading="loading">
    <el-card v-if="book" shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600;font-size:15px">图书详情</span>
          <el-button @click="router.back()" text type="primary" size="small"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
        </div>
      </template>
      <el-row :gutter="24">
        <el-col :span="8">
          <img v-if="book.coverUrl" :src="book.coverUrl" alt="封面" class="cover-img" />
          <div v-else class="cover-placeholder"><el-icon :size="48" color="#c0c4cc"><Reading /></el-icon></div>
        </el-col>
        <el-col :span="16">
          <h2 class="book-main-title">{{ book.title }}</h2>
          <div class="author-line"><el-icon :size="14" color="#909399"><User /></el-icon><span>{{ book.author }}</span></div>
          <el-descriptions :column="2" border size="default" style="margin:16px 0">
            <el-descriptions-item label="ISBN">{{ book.isbn || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ book.publisher || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ book.publicationDate || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ book.category || '未分类' }}</el-descriptions-item>
            <el-descriptions-item label="总库存">{{ book.total }} 册</el-descriptions-item>
            <el-descriptions-item label="可借数量"><el-tag :type="book.available > 0 ? 'success' : 'danger'" size="small">{{ book.available }} 册</el-tag></el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ book.description || '暂无简介' }}</el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" size="large" :disabled="book.available <= 0" @click="handleBorrow">
            <el-icon><Tickets /></el-icon> {{ book.available > 0 ? '立即借阅' : '暂无可借' }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookById } from '../api/book'
import { borrowBook } from '../api/borrow'
import { useDisposed } from '../composables/useDisposed'
import type { Book } from '../types'

const route = useRoute()
const router = useRouter()
const disposed = useDisposed()
const book = ref<Book | null>(null)
const loading = ref(false)

async function loadBook() {
  loading.value = true
  try {
    const res = await getBookById(Number(route.params.id))
    if (disposed.value) return
    book.value = res.data
  } catch {} finally { if (!disposed.value) loading.value = false }
}

async function handleBorrow() {
  if (!book.value) return
  if (!localStorage.getItem('token')) {
    ElMessageBox.confirm('请先登录后再借阅图书', '未登录', {
      confirmButtonText: '去登录', cancelButtonText: '取消', type: 'info'
    }).then(() => router.push('/login')).catch(() => {})
    return
  }
  try {
    await ElMessageBox.confirm(`确定借阅《${book.value.title}》吗？`, '借阅确认', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
    })
    await borrowBook(book.value.id)
    ElMessage.success('借阅成功')
    loadBook()
  } catch {}
}

onMounted(() => loadBook())
</script>

<style scoped>
.book-detail-page { max-width: 800px; margin: 0 auto; }
.cover-img { width: 100%; border-radius: 6px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.cover-placeholder { width: 100%; height: 240px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; border-radius: 6px; border: 1px dashed #dcdfe6; }
.book-main-title { color: #303133; font-size: 20px; font-weight: 600; margin-bottom: 8px; line-height: 1.4; }
.author-line { display: flex; align-items: center; gap: 4px; color: #909399; font-size: 13px; }

[data-theme="dark"] .cover-placeholder { background: #2a2a2a; border-color: #4a4a4a; }
[data-theme="dark"] .book-main-title { color: #e0e0e0; }
[data-theme="dark"] .author-line { color: #a0a0a0; }
</style>
