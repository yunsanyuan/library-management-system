# 图书馆管理系统

一个功能完善的图书馆管理系统，支持多角色权限管理、黑白主题切换、用户个人主页，前后端分离，Docker一键部署。

## 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + TypeScript + Element Plus |
| 后端 | Spring Boot 2.7.x + MyBatis-Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 部署 | Docker + Docker Compose |
| 接口文档 | springdoc-openapi-ui (Swagger 3) |

## 功能亮点

### 核心功能
- **多角色权限**：super_admin / admin / user / guest 四级角色，基于JWT无状态鉴权，路由守卫和后端权限双重保障
- **角色权限控制**：仅 super_admin 可修改用户角色（含提升为 admin/super_admin）；admin 不可删除 admin/super_admin 用户
- **仪表盘统计**：首页展示图书总数、可借数量、借阅中、逾期、用户数、已归还等统计数据，含热门图书TOP10和阅读达人TOP10排名
- **借阅管理**：借书/还书/强制归还，30天借期，逾期标识与剩余天数，库存自动扣减与恢复，已归还记录可删除
- **库存管理**：管理员可独立调整图书总库存，可借数量自动计算
- **用户管理**：管理员可查看用户列表、重置密码、删除用户；仅超级管理员可修改用户角色
- **密码管理**：用户可修改密码，管理员可重置用户密码
- **库存一致性**：借还书操作使用事务保障，借书扣减库存、还书恢复库存，避免超借

### 搜索与浏览
- **模糊搜索**：支持按书名、作者、ISBN进行模糊搜索
- **图书分类**：支持按分类筛选图书，管理员可设置图书分类
- **好书推荐**：基于借阅热度智能推荐图书
- **未登录浏览**：未登录用户可浏览图书列表和详情，借阅时提示登录
- **分页查询**：所有列表接口支持分页，管理员借阅记录支持多条件筛选

### 个性化
- **黑白主题切换**：右上角太阳/月亮图标一键切换，全局暗色适配，状态持久化到 localStorage，0.3s 过渡动画
- **用户头像**：支持上传头像图片（Base64 存储，限制 2MB）
- **个人简介**：用户可编辑/保存个人简介（bio 字段，最多200字）
- **公开用户主页**：点击用户排名或其他用户头像可查看其公开主页

### 部署
- **一键部署**：Docker Compose编排，`docker-compose up -d`即可启动全部服务

## 快速开始

### 前置条件

- 安装 [Docker](https://www.docker.com/) 和 [Docker Compose](https://docs.docker.com/compose/)

### 启动步骤

1. 克隆项目并进入目录
2. 执行启动脚本：

```bash
# Linux / macOS
chmod +x start.sh
./start.sh

# Windows
start.bat
```

或直接执行：

```bash
docker-compose up -d
```

3. 等待服务启动完成（首次构建需要几分钟），访问 **http://localhost**

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | superadmin | super123 |
| 管理员 | admin | admin123 |

### API 接口文档

启动后访问：**http://localhost/swagger-ui/**

> 开发模式下后端直接访问：http://localhost:8080/swagger-ui.html

## 项目目录结构

```
├── backend/                        # 后端 Spring Boot 项目
│   ├── Dockerfile                  # 多阶段构建（Maven + JRE）
│   ├── pom.xml                     # Maven依赖配置
│   └── src/main/
│       ├── java/com/library/
│       │   ├── LibraryApplication.java    # 启动类
│       │   ├── config/                   # 配置类
│       │   │   ├── SecurityConfig.java   # Spring Security + JWT配置
│       │   │   ├── CorsConfig.java       # CORS跨域配置
│       │   │   ├── SwaggerConfig.java    # Swagger文档配置
│       │   │   ├── MyBatisPlusConfig.java # 分页插件
│       │   │   └── MyBatisPlusMetaObjectHandler.java # 自动填充
│       │   ├── security/                 # JWT鉴权
│       │   │   ├── JwtTokenProvider.java # JWT生成与校验
│       │   │   ├── JwtAuthenticationFilter.java # JWT过滤器
│       │   │   └── JwtAuthEntryPoint.java # 认证入口点
│       │   ├── common/                   # 公共模块
│       │   │   ├── Result.java           # 统一返回格式
│       │   │   └── PageResult.java       # 分页结果
│       │   ├── exception/                # 异常处理
│       │   │   ├── ErrorCode.java        # 错误码枚举
│       │   │   ├── BusinessException.java # 业务异常
│       │   │   └── GlobalExceptionHandler.java # 全局异常拦截
│       │   ├── entity/                   # 数据实体（User含avatarUrl/bio，Book含category）
│       │   ├── mapper/                   # MyBatis-Plus Mapper
│       │   ├── dto/                      # 请求/响应DTO（含BookRankVO/UserRankVO/StatisticsVO等）
│       │   ├── service/                  # 服务接口
│       │   ├── impl/                     # 服务实现
│       │   └── controller/              # 控制器（Auth/Book/Borrow三大Controller）
│       └── resources/
│           ├── application.yml           # Spring Boot配置
│           └── init.sql                  # 数据库初始化脚本（含superadmin账号+分类+头像+简介字段）
├── frontend/                       # 前端 Vue 3 项目
│   ├── Dockerfile                  # 多阶段构建（Node + Nginx）
│   ├── nginx.conf                  # Nginx配置（SPA + API代理）
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── main.ts                 # 入口文件
│       ├── App.vue
│       ├── api/                    # API封装（auth/book/borrow/statistics/user）
│       ├── router/                 # 路由配置与守卫（requireManage/requireLogin）
│       ├── stores/                 # Pinia状态管理（userStore含角色判断方法）
│       ├── types/                  # TypeScript类型定义
│       ├── views/                  # 页面组件（14个页面）
│       ├── components/             # 公共组件（MainLayout含主题切换）
│       └── styles/                 # 全局样式（含暗色CSS变量）
├── docker-compose.yml              # Docker Compose编排
├── start.sh                        # Linux启动脚本
├── start.bat                       # Windows启动脚本
└── README.md
```

## 关键设计说明

### 角色权限体系

| 角色 | 权限范围 |
|------|----------|
| super_admin | 全部权限，可修改任意用户角色，可删除任意用户 |
| admin | 图书/借阅/用户管理，不可修改角色，不可删除 admin/super_admin |
| user | 浏览图书、借阅/归还、个人中心 |
| guest | 仅浏览图书（前端状态，无数据库记录） |

### JWT 鉴权流程

1. 用户登录 → 后端校验密码 → 生成JWT Token（含userId、username、role）
2. 前端存储Token到localStorage，后续请求通过`Authorization: Bearer <token>`携带
3. 后端`JwtAuthenticationFilter`拦截请求 → 校验Token → 设置Security上下文
4. `SecurityConfig`根据角色配置接口权限：
   - 图书增删改：admin 或 super_admin
   - 修改用户角色：仅 super_admin
   - 用户管理（列表/删除/重置密码）：admin 或 super_admin
5. 前端路由守卫：未登录跳转登录页，非管理员拦截`/admin/*`路由
6. 前端 userStore 提供 `isSuperAdmin()`/`isAdmin()`/`canManage()`/`canBorrow()` 等角色判断方法

### 库存一致性处理

- 借书操作使用`@Transactional`事务保障：创建借阅记录 + 扣减可借数量在同一事务中
- 借书前校验`available > 0`，防止超借
- 借书前校验用户是否已借同一书且未归还，防止重复借阅
- 还书操作同样使用事务：标记归还 + 恢复可借数量
- 管理员修改图书信息时不可直接修改库存，库存由借还逻辑自动维护

### 借阅状态说明

- `BORROWED`：借阅中，可正常归还
- `RETURNED`：已归还，不可再次操作，可删除记录
- 逾期判断：`dueTime < 当前时间` 且 `status = BORROWED`，前端显示红色逾期标识

### 主题切换机制

- 通过 `document.documentElement.setAttribute('data-theme', 'dark')` 切换主题
- CSS 使用 `[data-theme="dark"]` 选择器覆盖浅色样式
- global.css 定义暗色CSS变量，各页面scoped样式中添加暗色适配
- 状态持久化到 localStorage，0.3s 过渡动画

---

## 页面布局与视觉表现

### 整体布局结构

系统采用经典的**侧边栏 + 顶部导航栏 + 内容区**三栏布局，使用 Element Plus 的 `el-container` 布局容器实现。

- **侧边栏**：深色背景（#304156），宽度 210px，支持折叠/展开（折叠后 64px 仅显示图标）。Logo 区域使用更深色（#263445）区分。菜单项选中时左侧出现 3px 蓝色竖线高亮，hover 时背景加深。管理员菜单以子菜单（系统管理）形式折叠展示。
- **顶部导航栏**：白色背景，高度 52px，左侧为折叠按钮 + 面包屑导航，右侧为主题切换开关（太阳/月亮）+ 角色标签 + 用户头像下拉菜单。
- **内容区**：浅灰色背景（#f0f2f5），padding 20px，内容卡片为白色，形成清晰的层次对比。暗色模式下背景变为深色（#141414），卡片变为深灰（#1d1e1f）。

### 色彩与主题

- **浅色主题**：内容区 #f0f2f5，侧边栏 #304156，卡片 #fff，主色 #409eff
- **暗色主题**：内容区 #141414，侧边栏 #1d1e1f，卡片 #1d1e1f，文字 #e0e0e0
- **功能色**：成功绿 #67c23a、警告橙 #e6a23c、危险红 #f56c6c、信息灰 #909399
- **渐变按钮**：主要操作使用渐变背景，hover 时渐变加深，disabled 时变浅

### 登录 / 注册页面

- **左右分栏布局**：左侧品牌展示区（深色渐变背景 #2b4c7e → #304156 → #263445），右侧登录/注册表单区（宽度 460px）
- 左侧包含圆形图标、系统名称、功能特色列表
- 右侧表单使用 `size="large"` 大号输入框，登录按钮全宽渐变蓝色带字间距
- 暗色模式下右侧表单区变为深色背景

### 首页（Dashboard）

- **欢迎栏**：蓝色渐变背景（#409eff → #66b1ff），白色文字，显示时间段问候语
- **逾期提醒**：若存在逾期图书，显示 warning 类型 Alert
- **统计卡片**：6 列等宽布局，白色卡片左侧 3px 彩色竖线区分类型
- **排名面板**：两列卡片布局，左侧"热门图书 TOP10"，右侧"阅读达人 TOP10"。前三名序号为彩色圆形（金/银/铜），点击可跳转
- **快捷操作**：6 列等宽卡片网格，图标+文字纵向排列，hover 时上浮

### 图书浏览页面

- **统计卡片**：3 列布局，显示图书总数/当前可借/已借出
- **筛选栏**：搜索框 + 分类下拉 + 搜索按钮 + 卡片/表格视图切换
- **卡片视图**：自适应网格，白色卡片，封面区域浅灰背景，右上角库存角标
- **表格视图**：el-table 带 border + stripe，点击行跳转详情
- **好书推荐**：底部推荐卡片区域

### 图书详情页面

- 白色卡片居中（max-width 800px），左图右详情布局
- el-descriptions 两列边框表格展示详细信息
- 底部大号借阅按钮

### 我的借阅页面

- 白色卡片包裹，header 显示标题 + 右侧三个统计标签
- 表格列：书名/借阅时间/应还日期/剩余天数/状态/归还时间/操作（归还/删除）

### 借阅管理页面（管理员）

- 筛选栏 + 表格，支持强制归还和删除记录

### 图书管理页面（管理员）

- header 右侧"新增图书"按钮，支持编辑/库存调整/删除

### 用户管理页面（管理员）

- 表格列：ID/用户名/角色标签/注册时间/操作
- **改角色按钮**：仅超级管理员可见，下拉选项包含 user/admin/super_admin
- 重置密码 / 删除按钮（不可删除 admin/super_admin）

### 个人中心页面

- 白色卡片居中（max-width 600px）
- 头部：渐变头像（支持上传）+ 用户名 + 角色标签 + 注册时间
- 个人简介区域：查看/编辑/保存
- 修改账号名 + 修改密码表单

### 公开用户主页

- 只读查看他人信息：头像 + 用户名 + 角色标签 + 注册时间 + 个人简介
- 从排名面板点击跳转

### 404 页面

- 全屏浅灰背景居中，80px 超大"404"数字 + 提示文字 + 返回首页按钮

### 全局组件规范

- **表格**：统一使用 border + stripe + highlight-current-row
- **弹窗**：圆角 8px，标题加粗，footer 按钮右对齐，destroy-on-close
- **搜索框**：圆角输入框 + 搜索图标前缀
- **按钮**：主操作 primary 渐变蓝色，危险操作 danger 渐变红色 + 二次确认
- **分页**：居中对齐
- **面包屑**：所有页面顶部导航栏显示
- **侧边栏折叠**：点击折叠按钮可收缩为仅图标模式
