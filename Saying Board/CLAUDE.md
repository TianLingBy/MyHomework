# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

"今天吃什么？" 留言板 — 全栈 Web 应用，用户以匿名或登录身份发布美食相关留言，支持点赞、评论、管理员后台。

## 技术栈

- **后端**: Spring Boot 3.5 / Java 17 / Spring Data JPA / MySQL 8 / JWT (JJWT 0.12) / BCrypt
- **前端**: Vue 3 (Composition API + `<script setup>`) / Vite / Axios
- **数据库**: MySQL `food_board`，用户 `root/root`，端口 3306

## 常用命令

### 一键启动（Windows）
```bash
start-server.bat    # 同时启动后端(:8080)和前端(:5173)
```

### 后端
```bash
cd backend
# 需要 Java 17（系统默认可能是 Java 8，需要设置 JAVA_HOME）
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```

### 前端
```bash
cd frontend
npm install          # 首次运行
npm run dev          # 启动开发服务器 → http://localhost:5173
```

### 数据库初始化
```bash
mysql -u root -proot < database/init.sql
```

## 架构

### 前后端通信

前端 Vite 开发服务器（:5173）通过 `vite.config.js` 中的 proxy 将 `/api` 请求转发到后端（:8080）。Axios 拦截器自动从 `localStorage` 读取 `food_token` 并附加 `Authorization: Bearer` 头。

### 认证流程

`AuthInterceptor`（MVC 拦截器，非 Spring Security Filter）拦截所有 `/api/**` 请求，解析 JWT 后将 `currentUser`（用户名）和 `isAdmin`（布尔值）设为 Request Attribute。Controller 通过 `@RequestAttribute(required = false) String currentUser` 获取当前用户名，**不是 token**。`SecurityConfig` 仅用于暴露 `BCryptPasswordEncoder` Bean，已禁用所有 HTTP 安全过滤。

### 路由

前端使用 hash 路由（`App.vue` 中监听 `hashchange`）：
- `#/` → 首页（MessageBoard + DailyTopSidebar）
- `#/message/{id}` → 留言详情（MessageDetail）
- `#/user/{username}` → 用户资料（UserProfile）
- `#/admin` → 管理后台（AdminPage）

### 数据模型

三张表：`user`（用户）、`message`（留言）、`comment`（评论）。Message 和 Comment 的 `author` 字段存储用户名字符串（非外键），通过 `userRepository.findByUsername()` 关联查询头像和管理员状态。

### 关键设计

- **匿名发帖**：未登录用户发帖时，`MessageService` 从前缀/后缀数组随机组合生成 "XX的XX" 昵称（256 种组合）
- **动态卡片尺寸**：`MessageCard.vue` 根据点赞数动态计算卡片宽度、内边距和字号
- **每日热评**：`MessageRepository.findDailyTopMessages()` 使用 MySQL 窗口函数 `ROW_NUMBER() OVER (PARTITION BY DATE)` 取每天点赞最高的留言
- **头像系统**：用户可选择 16 个食物 emoji 或上传图片（Canvas 裁切为 100×100 JPEG base64）

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | admin123 | 管理员 |

## API 基础路径

- 留言: `GET/POST /api/messages`，`GET /api/messages?today=true`，`GET /api/messages/daily-top`
- 评论: `GET/POST /api/messages/{id}/comments`
- 用户: `POST /api/users/register`，`POST /api/users/login`，`GET /api/users/me`
- 管理: `/api/admin/stats`，`/api/admin/messages`，`/api/admin/users`，`/api/admin/comments`
