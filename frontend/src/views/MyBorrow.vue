<template>
  <div class="my-borrow-page">
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600">我的借阅记录</span>
          <div class="summary">
            <el-tag type="primary" effect="plain" size="small">借阅中: {{ borrowingCount }}</el-tag>
            <el-tag type="danger" effect="plain" size="small">逾期: {{ overdueCount }}</el-tag>
            <el-tag type="success" effect="plain" size="small">已归还: {{ returnedCount }}</el-tag>
          </div>
        </div>
      </template>
      <el-table :data="records" stripe border highlight-current-row v-loading="loading">
        <el-table-column prop="bookTitle" label="书名" min-width="140" />
        <el-table-column prop="borrowTime" label="借阅时间" width="155">
          <template #default="{ row }">{{ formatTime(row.borrowTime) }}</template>
        </el-table-column>
        <el-table-column prop="dueTime" label="应还日期" width="105">
          <template #default="{ row }">{{ formatDate(row.dueTime) }}</template>
        </el-table-column>
        <el-table-column label="剩余天数" width="85" align="center">
          <template #default="{ row }">
            <span v-if="row.status==='RETURNED'" style="color:#909399">-</span>
            <span v-else-if="getDaysLeft(row) < 0" style="color:#f56c6c;font-weight:600">{{ getDaysLeft(row) }}天</span>
            <span v-else style="color:#67c23a">{{ getDaysLeft(row) }}天</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }"><el-tag :type="getStatusType(row)" size="small">{{ getStatusText(row) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="returnTime" label="归还时间" width="155">
          <template #default="{ row }">{{ row.returnTime ? formatTime(row.returnTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status==='BORROWED'" type="warning" size="small" @click="handleReturn(row)">归还</el-button>
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
import { getMyRecords, returnBook, deleteRecord } from '../api/borrow'
import { useDisposed } from '../composables/useDisposed'
import type { BorrowRecord } from '../types'

const disposed = useDisposed()
const records = ref<BorrowRecord[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const borrowingCount = computed(() => records.value.filter(r => r.status === 'BORROWED' && new Date(r.dueTime) >= new Date()).length)
const overdueCount = computed(() => records.value.filter(r => r.status === 'BORROWED' && new Date(r.dueTime) < new Date()).length)
const returnedCount = computed(() => records.value.filter(r => r.status === 'RETURNED').length)

async function loadRecords() {
  loading.value = true
  try {
    const res = await getMyRecords(page.value, size.value)
    if (disposed.value) return
    records.value = res.data.records
    total.value = res.data.total
  } catch {} finally { if (!disposed.value) loading.value = false }
}

async function handleReturn(record: BorrowRecord) {
  try {
    await ElMessageBox.confirm(`确定归还《${record.bookTitle}》吗？`, '归还确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await returnBook(record.id); ElMessage.success('归还成功'); loadRecords()
  } catch {}
}

async function handleDelete(record: BorrowRecord) {
  try {
    await ElMessageBox.confirm(`确定删除《${record.bookTitle}》的借阅记录吗？`, '删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteRecord(record.id); ElMessage.success('删除成功'); loadRecords()
  } catch {}
}

function getDaysLeft(row: BorrowRecord): number { return Math.ceil((new Date(row.dueTime).getTime() - Date.now()) / 86400000) }
function getStatusText(row: BorrowRecord): string { if (row.status === 'RETURNED') return '已归还'; if (new Date(row.dueTime) < new Date()) return '已逾期'; return '借阅中' }
function getStatusType(row: BorrowRecord): string { if (row.status === 'RETURNED') return 'success'; if (new Date(row.dueTime) < new Date()) return 'danger'; return 'primary' }
function formatTime(val: string): string { if (!val) return '-'; return val.replace('T', ' ').substring(0, 16) }
function formatDate(val: string): string { if (!val) return '-'; return val.substring(0, 10) }

onMounted(() => loadRecords())
</script>

<style scoped>
.summary { display: flex; gap: 8px; }
.pagination-area { margin-top: 16px; display: flex; justify-content: center; }
</style>
