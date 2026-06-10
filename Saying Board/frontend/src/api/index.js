import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 5000
})

// 请求拦截器：自动携带 token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('food_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ===== 留言相关 =====

/** 获取所有留言 */
export function getMessages() {
  return api.get('/messages')
}

/** 获取今日留言 */
export function getTodayMessages() {
  return api.get('/messages', { params: { today: true } })
}

/** 发布留言（昵称由后端自动生成，已登录则用用户名） */
export function createMessage(content) {
  return api.post('/messages', { content })
}

/** 点赞 */
export function likeMessage(id) {
  return api.put(`/messages/${id}/like`)
}

/** 取消点赞 */
export function unlikeMessage(id) {
  return api.put(`/messages/${id}/unlike`)
}

// ===== 用户相关 =====

/** 用户注册 */
export function register(username, password) {
  return api.post('/users/register', { username, password })
}

/** 用户登录 */
export function login(username, password) {
  return api.post('/users/login', { username, password })
}

/** 获取当前登录用户信息 */
export function getCurrentUser() {
  return api.get('/users/me')
}

/** 获取用户公开资料 */
export function getUserProfile(username) {
  return api.get(`/users/profile/${username}`)
}

/** 更新当前用户的昵称和头像 */
export function updateProfile(nickname, avatar) {
  return api.put('/users/profile', { nickname, avatar })
}

// ===== 评论相关 =====

/** 获取某条留言的评论 */
export function getComments(messageId) {
  return api.get(`/messages/${messageId}/comments`)
}

/** 发表评论 */
export function addComment(messageId, content) {
  return api.post(`/messages/${messageId}/comments`, { content })
}

/** 删除评论（管理员或作者本人） */
export function deleteComment(messageId, commentId) {
  return api.delete(`/messages/${messageId}/comments/${commentId}`)
}

/** 删除留言（管理员或作者本人） */
export function deleteMessageByOwner(id) {
  return api.delete(`/messages/${id}`)
}

/** 获取单条留言详情（浏览量+1） */
export function getMessageDetail(id) {
  return api.get(`/messages/${id}`)
}

// ===== 每日热评 =====

/** 获取每日最高点赞留言（公开） */
export function getDailyTopMessages() {
  return api.get('/messages/daily-top')
}

// ===== 管理员相关 =====

/** 获取仪表盘统计数据 */
export function getAdminStats() {
  return api.get('/admin/stats')
}

/** 搜索/获取所有留言（管理员） */
export function getAdminMessages(keyword) {
  const params = keyword ? { keyword } : {}
  return api.get('/admin/messages', { params })
}

/** 编辑留言内容（管理员） */
export function editMessage(id, content) {
  return api.put(`/admin/messages/${id}`, { content })
}

/** 批量删除留言（管理员） */
export function batchDeleteMessages(ids) {
  return api.delete('/admin/messages/batch', { data: { ids } })
}

/** 搜索/获取所有用户统计（管理员） */
export function getAdminUsers(keyword) {
  const params = keyword ? { keyword } : {}
  return api.get('/admin/users', { params })
}

/** 设置/取消管理员 */
export function toggleAdmin(userId, isAdmin) {
  return api.put(`/admin/users/${userId}/admin`, { isAdmin })
}

/** 删除用户（管理员） */
export function deleteUser(userId) {
  return api.delete(`/admin/users/${userId}`)
}

/** 获取每日最高点赞留言（管理员） */
export function getAdminDailyTop() {
  return api.get('/admin/daily-top')
}

/** 获取所有评论（管理员，支持筛选） */
export function getAdminComments(messageId, keyword) {
  const params = {}
  if (messageId) params.messageId = messageId
  if (keyword) params.keyword = keyword
  return api.get('/admin/comments', { params })
}

/** 删除评论（管理员） */
export function deleteAdminComment(commentId) {
  return api.delete(`/admin/comments/${commentId}`)
}

/** 删除留言（管理员） */
export function deleteMessage(id) {
  return api.delete(`/admin/messages/${id}`)
}
