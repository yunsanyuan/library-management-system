<template>
  <div class="admin-borrows-page">
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600">借阅管理</span>
          <div class="summary">
            <el-tag type="primary" effect="plain" size="small">借阅中: {{ borrowingCount }}</el-tag>
            <el-tag type="danger" effect="plain" size="small">逾期: {{ overdueCount }}</el-tag>
            <el-tag type="success" effect="plain" size="small">已归还: {{ returnedCount }}</el-tag>
          </div>
        </div>
      </template>
      <div class="filter-bar">
        <el-input v-model="filterUsername" placeholder="用户名" clearable size="default" style="width:130px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="filterBookTitle" placeholder="书名" clearable size="default" style="width:130px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterStatus" placeholder="状态" clearable size="default" style="width:110px">
          <el-option label="借阅中" value="BORROWED" />
          <el-option label="已归还" value="RETURNED" />
        </el-select>
        <el-button type="primary" @click="loadRecords" size="default">查询</el-button>
      </div>
      <el-table :data="records" stripe border highlight-current-row v-loading="loading">
        <el-table-column prop="username" label="用户" width="80" />
        <el-table-column prop="bookTitle" label="书名" min-width="130" />
        <el-table-column prop="borrowTime" label="借阅时间" width="155">
          <template #default="{ row }">{{ formatTime(row.borrowTime) }}</template>
        </el-table-column>
        <el-table-column prop="dueTime" label="应还日期" width="105">
          <template #default="{ row }">{{ formatDate(row.dueTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row)" size="small">{{ getStatusText(row) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="returnTime" label="归还时间" width="155">
          <template #default="{ row }">{{ row.returnTime ? formatTime(row.returnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status==='BORROWED'" type="warning" size="small" @click="handleForceReturn(row)">强制归还</el-button>
            <el-button v-if="row.status==='RETURNED'" type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-area">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="loadRecords" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAllRecords, returnBook, deleteRecord } from '../api/borrow'
import { useDisposed } from '../composables/useDisposed'
import type { BorrowRecord } from '../types'

const disposed = useDisposed()
const records = ref<BorrowRecord[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filterUsername = ref('')
const filterBookTitle = ref('')
const filterStatus = ref('')

const borrowingCount = computed(() => records.value.filter(r => r.status === 'BORROWED' && new Date(r.dueTime) >= new Date()).length)
const overdueCount = computed(() => records.value.filter(r => r.status === 'BORROWED' && new Date(r.dueTime) < new Date()).length)
const returnedCount = computed(() => records.value.filter(r => r.status === 'RETURNED').length)

async function loadRecords() {
  loading.value = true
  try {
    const res = await getAllRecords(page.value, size.value, filterUsername.value || undefined, filterBookTitle.value || undefined, filterStatus.value || undefined)
    if (disposed.value) return
    records.value = res.data.records
    total.value = res.data.total
  } catch {} finally { if (!disposed.value) loading.value = false }
}

async function handleForceReturn(record: BorrowRecord) {
  try {
    await ElMessageBox.confirm(`确定强制归还用户「${record.username}」借阅的《${record.bookTitle}》吗？`, '强制归还确认', { confirmButtonText:'确定', cancelButtonText:'取消', type:'warning' })
    await returnBook(record.id); ElMessage.success('强制归还成功'); loadRecords()
  } catch {}
}

async function handleDelete(record: BorrowRecord) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${record.username}」借阅《${record.bookTitle}》的记录吗？`, '删除确认', { confirmButtonText:'确定', cancelButtonText:'取消', type:'warning' })
    await deleteRecord(record.id); ElMessage.success('删除成功'); loadRecords()
  } catch {}
}

function getStatusText(row: BorrowRecord): string { if (row.status === 'RETURNED') return '已归还'; if (new Date(row.dueTime) < new Date()) return '已逾期'; return '借阅中' }
function getStatusType(row: BorrowRecord): string { if (row.status === 'RETURNED') return 'success'; if (new Date(row.dueTime) < new Date()) return 'danger'; return 'primary' }
function formatTime(val: string): string { if (!val) return '-'; return val.replace('T', ' ').substring(0, 16) }
function formatDate(val: string): string { if (!val) return '-'; return val.substring(0, 10) }

onMounted(() => loadRecords())
</script>

<style scoped>
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.summary { display: flex; gap: 8px; }
.pagination-area { margin-top: 16px; display: flex; justify-content: center; }
</style>
