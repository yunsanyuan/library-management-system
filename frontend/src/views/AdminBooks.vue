<template>
  <div class="admin-books-page">
    <el-card shadow="hover">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:600">图书管理</span>
          <el-button type="success" @click="openAddDialog" size="small"><el-icon><Plus /></el-icon> 新增图书</el-button>
        </div>
      </template>
      <div class="filter-bar">
        <el-input v-model="keyword" placeholder="搜索书名/作者/ISBN" clearable @clear="loadBooks" @keyup.enter="loadBooks" style="width:280px" size="default">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadBooks">搜索</el-button>
      </div>
      <el-table :data="books" stripe border highlight-current-row v-loading="loading">
        <el-table-column prop="title" label="书名" min-width="130" />
        <el-table-column prop="author" label="作者" width="90" />
        <el-table-column prop="category" label="分类" width="90">
          <template #default="{ row }"><el-tag v-if="row.category" size="small" type="info">{{ row.category }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="130" />
        <el-table-column prop="publisher" label="出版社" width="110" />
        <el-table-column label="库存" width="70" align="center">
          <template #default="{ row }"><span :style="{color: row.available > 0 ? '#67c23a' : '#f56c6c'}">{{ row.available }}/{{ row.total }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="170" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="warning" size="small" link @click="openStockDialog(row)">库存</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-area">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="loadBooks" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '新增图书'" width="580px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="书名" prop="title"><el-input v-model="form.title" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="作者" prop="author"><el-input v-model="form.author" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="ISBN"><el-input v-model="form.isbn" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出版社"><el-input v-model="form.publisher" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="分类"><el-input v-model="form.category" placeholder="如：编程语言" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出版日期"><el-date-picker v-model="form.publicationDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="总库存" prop="total"><el-input-number v-model="form.total" :min="1" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialogVisible" title="调整库存" width="360px" destroy-on-close>
      <p style="margin-bottom:8px">图书：<b>{{ stockBook?.title }}</b></p>
      <p style="color:#909399;font-size:13px;margin-bottom:16px">当前：可借 {{ stockBook?.available }}/{{ stockBook?.total }}</p>
      <el-form-item label="总库存" label-width="60px"><el-input-number v-model="stockTotal" :min="1" /></el-form-item>
      <template #footer>
        <el-button @click="stockDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleStockUpdate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getBooks, addBook, updateBook, deleteBook, updateStock } from '../api/book'
import { useDisposed } from '../composables/useDisposed'
import type { Book } from '../types'

const disposed = useDisposed()
const books = ref<Book[]>([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(0)
const submitting = ref(false)
const formRef = ref()
const form = reactive({ title:'',author:'',isbn:'',publisher:'',publicationDate:'',description:'',coverUrl:'',category:'',total:1 })
const formRules = { title:[{required:true,message:'请输入书名',trigger:'blur'}], author:[{required:true,message:'请输入作者',trigger:'blur'}], total:[{required:true,message:'请输入总库存',trigger:'blur'}] }

async function loadBooks() {
  loading.value = true
  try { const res = await getBooks(page.value, size.value, keyword.value||undefined); if(disposed.value)return; books.value = res.data.records; total.value = res.data.total } catch {} finally { if(!disposed.value) loading.value = false }
}

function openAddDialog() { isEdit.value=false; Object.assign(form,{title:'',author:'',isbn:'',publisher:'',publicationDate:'',description:'',coverUrl:'',category:'',total:1}); dialogVisible.value=true }

function openEditDialog(book: Book) { isEdit.value=true; editId.value=book.id; Object.assign(form,{title:book.title,author:book.author,isbn:book.isbn||'',publisher:book.publisher||'',publicationDate:book.publicationDate||'',description:book.description||'',coverUrl:book.coverUrl||'',category:book.category||'',total:book.total}); dialogVisible.value=true }

async function handleSubmit() {
  try { await formRef.value.validate() } catch { return }
  submitting.value=true
  try { if(isEdit.value){await updateBook(editId.value,form);ElMessage.success('修改成功')}else{await addBook(form);ElMessage.success('新增成功')} dialogVisible.value=false;loadBooks() } catch {} finally { submitting.value=false }
}

async function handleDelete(book: Book) { try { await ElMessageBox.confirm(`确定删除《${book.title}》吗？`, '删除确认',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'}); await deleteBook(book.id); ElMessage.success('删除成功'); loadBooks() } catch {} }

const stockDialogVisible=ref(false), stockBook=ref<Book|null>(null), stockTotal=ref(1)
function openStockDialog(book:Book){stockBook.value=book;stockTotal.value=book.total;stockDialogVisible.value=true}
async function handleStockUpdate(){if(!stockBook.value)return;try{await updateStock(stockBook.value.id,stockTotal.value);ElMessage.success('库存调整成功');stockDialogVisible.value=false;loadBooks()}catch{}}

onMounted(()=>loadBooks())
</script>

<style scoped>
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; align-items: center; }
.pagination-area { margin-top: 16px; display: flex; justify-content: center; }
</style>
