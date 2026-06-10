<template>
  <div class="admin-page">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div class="admin-header-left">
        <span class="admin-logo">🛠️</span>
        <h1>管理后台</h1>
      </div>
      <div class="admin-header-right">
        <button class="header-btn back" @click="goHome">🏠 返回首页</button>
        <span class="admin-user-info">{{ user?.nickname || user?.username }}</span>
        <button class="header-btn logout" @click="handleLogout">退出</button>
      </div>
    </header>

    <div class="admin-body">
      <!-- 侧边栏 -->
      <nav class="admin-sidebar">
        <button
          v-for="item in menuItems"
          :key="item.key"
          :class="['sidebar-item', { active: activeTab === item.key }]"
          @click="activeTab = item.key"
        >
          <span class="sidebar-icon">{{ item.icon }}</span>
          <span class="sidebar-label">{{ item.label }}</span>
        </button>
      </nav>

      <!-- 内容区 -->
      <main class="admin-content">

        <!-- ===== 概览 ===== -->
        <div v-if="activeTab === 'dashboard'" class="tab-panel">
          <h2 class="panel-title">📊 数据概览</h2>
          <div class="stats-grid">
            <div class="stat-card users">
              <div class="stat-icon">👥</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
              <div class="stat-today">今日 +{{ stats.todayUsers }}</div>
            </div>
            <div class="stat-card messages">
              <div class="stat-icon">📝</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalMessages }}</div>
                <div class="stat-label">总留言数</div>
              </div>
              <div class="stat-today">今日 +{{ stats.todayMessages }}</div>
            </div>
            <div class="stat-card comments">
              <div class="stat-icon">💬</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalComments }}</div>
                <div class="stat-label">总评论数</div>
              </div>
              <div class="stat-today">今日 +{{ stats.todayComments }}</div>
            </div>
          </div>

          <div class="dashboard-section">
            <h3>📋 最近留言</h3>
            <div v-if="recentMessages.length === 0" class="empty-hint">暂无留言</div>
            <div v-else class="recent-list">
              <div v-for="msg in recentMessages.slice(0, 5)" :key="msg.id" class="recent-item">
                <span class="recent-author">{{ msg.author }}</span>
                <span class="recent-content">{{ msg.content }}</span>
                <span class="recent-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== 留言管理 ===== -->
        <div v-if="activeTab === 'messages'" class="tab-panel">
          <h2 class="panel-title">📝 留言管理</h2>

          <div class="toolbar">
            <div class="search-box">
              <input
                v-model="messageKeyword"
                placeholder="搜索留言内容或作者..."
                @keyup.enter="loadMessages"
              />
              <button class="search-btn" @click="loadMessages">🔍</button>
            </div>
            <button
              v-if="selectedMessages.length > 0"
              class="batch-delete-btn"
              @click="handleBatchDelete"
            >
              🗑️ 删除选中 ({{ selectedMessages.length }})
            </button>
          </div>

          <div v-if="loadingMessages" class="loading">加载中...</div>
          <div v-else-if="messages.length === 0" class="empty">暂无留言</div>
          <div v-else class="data-table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th class="col-check">
                    <input type="checkbox" @change="toggleAllMessages" :checked="allMessagesSelected" />
                  </th>
                  <th class="col-id">ID</th>
                  <th class="col-author">作者</th>
                  <th class="col-content">内容</th>
                  <th class="col-stats">浏览</th>
                  <th class="col-stats">点赞</th>
                  <th class="col-stats">评论</th>
                  <th class="col-time">时间</th>
                  <th class="col-actions">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="msg in messages" :key="msg.id">
                  <td class="col-check">
                    <input type="checkbox" :value="msg.id" v-model="selectedMessages" />
                  </td>
                  <td class="col-id">{{ msg.id }}</td>
                  <td class="col-author">{{ msg.author }}</td>
                  <td class="col-content">
                    <template v-if="editingId === msg.id">
                      <div class="edit-inline">
                        <input v-model="editContent" class="edit-input" @keyup.enter="saveEdit(msg.id)" @keyup.escape="cancelEdit" />
                        <button class="edit-save" @click="saveEdit(msg.id)">✓</button>
                        <button class="edit-cancel" @click="cancelEdit">✕</button>
                      </div>
                    </template>
                    <template v-else>
                      <span class="content-text" :title="msg.content">{{ msg.content }}</span>
                    </template>
                  </td>
                  <td class="col-stats">{{ msg.viewCount || 0 }}</td>
                  <td class="col-stats">{{ msg.likes || 0 }}</td>
                  <td class="col-stats">{{ msg.commentCount || 0 }}</td>
                  <td class="col-time">{{ formatTime(msg.createdAt) }}</td>
                  <td class="col-actions">
                    <button class="action-btn edit" @click="startEdit(msg)" title="编辑">✏️</button>
                    <button class="action-btn delete" @click="handleDeleteMessage(msg.id)" title="删除">🗑️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ===== 用户管理 ===== -->
        <div v-if="activeTab === 'users'" class="tab-panel">
          <h2 class="panel-title">👥 用户管理</h2>

          <div class="toolbar">
            <div class="search-box">
              <input
                v-model="userKeyword"
                placeholder="搜索用户名或昵称..."
                @keyup.enter="loadUsers"
              />
              <button class="search-btn" @click="loadUsers">🔍</button>
            </div>
          </div>

          <div v-if="loadingUsers" class="loading">加载中...</div>
          <div v-else-if="users.length === 0" class="empty">暂无用户</div>
          <div v-else class="data-table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th class="col-id">ID</th>
                  <th class="col-avatar">头像</th>
                  <th>用户名</th>
                  <th>昵称</th>
                  <th>身份</th>
                  <th class="col-stats">留言数</th>
                  <th class="col-time">注册时间</th>
                  <th class="col-actions">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in users" :key="u.id">
                  <td class="col-id">{{ u.id }}</td>
                  <td class="col-avatar">
                    <span class="user-avatar-cell">{{ u.avatar || u.username.charAt(0).toUpperCase() }}</span>
                  </td>
                  <td>{{ u.username }}</td>
                  <td>{{ u.nickname }}</td>
                  <td>
                    <span v-if="u.isAdmin" class="role-badge admin">管理员</span>
                    <span v-else class="role-badge normal">普通用户</span>
                  </td>
                  <td class="col-stats">{{ u.messageCount }}</td>
                  <td class="col-time">{{ formatTime(u.createdAt) }}</td>
                  <td class="col-actions">
                    <button
                      class="action-btn toggle-admin"
                      @click="handleToggleAdmin(u)"
                      :title="u.isAdmin ? '取消管理员' : '设为管理员'"
                    >
                      {{ u.isAdmin ? '⬇️' : '⬆️' }}
                    </button>
                    <button
                      class="action-btn delete"
                      @click="handleDeleteUser(u)"
                      title="删除用户"
                    >🗑️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ===== 评论管理 ===== -->
        <div v-if="activeTab === 'comments'" class="tab-panel">
          <h2 class="panel-title">💬 评论管理</h2>

          <div class="toolbar">
            <div class="search-box">
              <input
                v-model="commentKeyword"
                placeholder="搜索评论内容或作者..."
                @keyup.enter="loadComments"
              />
              <button class="search-btn" @click="loadComments">🔍</button>
            </div>
          </div>

          <div v-if="loadingComments" class="loading">加载中...</div>
          <div v-else-if="comments.length === 0" class="empty">暂无评论</div>
          <div v-else class="data-table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th class="col-id">ID</th>
                  <th class="col-id">留言ID</th>
                  <th class="col-author">评论者</th>
                  <th class="col-content">内容</th>
                  <th class="col-time">时间</th>
                  <th class="col-actions">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in comments" :key="c.id">
                  <td class="col-id">{{ c.id }}</td>
                  <td class="col-id">
                    <a class="msg-link" @click.prevent="goToMessage(c.messageId)">#{{ c.messageId }}</a>
                  </td>
                  <td class="col-author">{{ c.author }}</td>
                  <td class="col-content">
                    <span class="content-text" :title="c.content">{{ c.content }}</span>
                  </td>
                  <td class="col-time">{{ formatTime(c.createdAt) }}</td>
                  <td class="col-actions">
                    <button class="action-btn delete" @click="handleDeleteComment(c.id)" title="删除">🗑️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- ===== 每日热评 ===== -->
        <div v-if="activeTab === 'dailyTop'" class="tab-panel">
          <h2 class="panel-title">🏆 每日热评</h2>
          <p class="panel-subtitle">每一天点赞数最高的留言</p>

          <div v-if="loadingDailyTop" class="loading">加载中...</div>
          <div v-else-if="dailyTopList.length === 0" class="empty">暂无热评数据</div>
          <div v-else class="data-table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th class="col-id">ID</th>
                  <th class="col-date">日期</th>
                  <th class="col-author">作者</th>
                  <th class="col-content">内容</th>
                  <th class="col-stats">点赞</th>
                  <th class="col-stats">浏览</th>
                  <th class="col-stats">评论</th>
                  <th class="col-actions">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="msg in dailyTopList" :key="msg.id">
                  <td class="col-id">{{ msg.id }}</td>
                  <td class="col-date">
                    <span class="date-badge">{{ formatDateFull(msg.createdAt) }}</span>
                  </td>
                  <td class="col-author">{{ msg.author }}</td>
                  <td class="col-content">
                    <span class="content-text" :title="msg.content">{{ msg.content }}</span>
                  </td>
                  <td class="col-stats">
                    <span class="like-highlight">❤️ {{ msg.likes }}</span>
                  </td>
                  <td class="col-stats">{{ msg.viewCount || 0 }}</td>
                  <td class="col-stats">{{ msg.commentCount || 0 }}</td>
                  <td class="col-actions">
                    <button class="action-btn view" @click="goToMessage(msg.id)" title="查看原留言">👁️</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAuth } from '../composables/useAuth'
import {
  getAdminStats,
  getAdminMessages,
  getAdminUsers,
  getAdminComments,
  getAdminDailyTop,
  editMessage,
  deleteMessage,
  batchDeleteMessages,
  toggleAdmin,
  deleteUser,
  deleteAdminComment
} from '../api'

const { user, isAdmin, logout } = useAuth()

// ===== 菜单配置 =====
const menuItems = [
  { key: 'dashboard', icon: '📊', label: '数据概览' },
  { key: 'messages', icon: '📝', label: '留言管理' },
  { key: 'dailyTop', icon: '🏆', label: '每日热评' },
  { key: 'users', icon: '👥', label: '用户管理' },
  { key: 'comments', icon: '💬', label: '评论管理' }
]

const activeTab = ref('dashboard')

// ===== 概览 =====
const stats = ref({
  totalUsers: 0,
  totalMessages: 0,
  totalComments: 0,
  todayUsers: 0,
  todayMessages: 0,
  todayComments: 0
})
const recentMessages = ref([])

async function loadStats() {
  try {
    const res = await getAdminStats()
    stats.value = res.data
  } catch (err) {
    console.error('加载统计失败:', err)
  }
}

async function loadRecentMessages() {
  try {
    const res = await getAdminMessages()
    recentMessages.value = res.data
  } catch (err) {
    console.error('加载最近留言失败:', err)
  }
}

// ===== 留言管理 =====
const messages = ref([])
const loadingMessages = ref(false)
const messageKeyword = ref('')
const selectedMessages = ref([])
const editingId = ref(null)
const editContent = ref('')

const allMessagesSelected = computed(() =>
  messages.value.length > 0 && selectedMessages.value.length === messages.value.length
)

async function loadMessages() {
  loadingMessages.value = true
  try {
    const res = await getAdminMessages(messageKeyword.value)
    messages.value = res.data
    selectedMessages.value = []
  } catch (err) {
    console.error('加载留言失败:', err)
  } finally {
    loadingMessages.value = false
  }
}

function toggleAllMessages(e) {
  if (e.target.checked) {
    selectedMessages.value = messages.value.map(m => m.id)
  } else {
    selectedMessages.value = []
  }
}

function startEdit(msg) {
  editingId.value = msg.id
  editContent.value = msg.content
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function saveEdit(id) {
  if (!editContent.value.trim()) return
  try {
    await editMessage(id, editContent.value.trim())
    const msg = messages.value.find(m => m.id === id)
    if (msg) msg.content = editContent.value.trim()
    cancelEdit()
  } catch (err) {
    alert('编辑失败: ' + (err.response?.data?.error || err.message))
  }
}

async function handleDeleteMessage(id) {
  if (!confirm('确定要删除这条留言吗？')) return
  try {
    await deleteMessage(id)
    messages.value = messages.value.filter(m => m.id !== id)
    selectedMessages.value = selectedMessages.value.filter(sid => sid !== id)
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
}

async function handleBatchDelete() {
  if (!confirm(`确定要删除选中的 ${selectedMessages.value.length} 条留言吗？`)) return
  try {
    await batchDeleteMessages(selectedMessages.value)
    messages.value = messages.value.filter(m => !selectedMessages.value.includes(m.id))
    selectedMessages.value = []
  } catch (err) {
    alert('批量删除失败: ' + (err.response?.data?.error || err.message))
  }
}

// ===== 用户管理 =====
const users = ref([])
const loadingUsers = ref(false)
const userKeyword = ref('')

async function loadUsers() {
  loadingUsers.value = true
  try {
    const res = await getAdminUsers(userKeyword.value)
    users.value = res.data
  } catch (err) {
    console.error('加载用户失败:', err)
  } finally {
    loadingUsers.value = false
  }
}

async function handleToggleAdmin(u) {
  const action = u.isAdmin ? '取消管理员' : '设为管理员'
  if (!confirm(`确定要${action} "${u.nickname || u.username}" 吗？`)) return
  try {
    await toggleAdmin(u.id, !u.isAdmin)
    u.isAdmin = !u.isAdmin
  } catch (err) {
    alert('操作失败: ' + (err.response?.data?.error || err.message))
  }
}

async function handleDeleteUser(u) {
  if (u.isAdmin) {
    alert('不能删除管理员账号！')
    return
  }
  if (!confirm(`确定要删除用户 "${u.nickname || u.username}" 吗？该操作不可恢复！`)) return
  try {
    await deleteUser(u.id)
    users.value = users.value.filter(item => item.id !== u.id)
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
}

// ===== 评论管理 =====
const comments = ref([])
const loadingComments = ref(false)
const commentKeyword = ref('')

// ===== 每日热评 =====
const dailyTopList = ref([])
const loadingDailyTop = ref(false)

async function loadDailyTop() {
  loadingDailyTop.value = true
  try {
    const res = await getAdminDailyTop()
    dailyTopList.value = res.data
  } catch (err) {
    console.error('加载每日热评失败:', err)
  } finally {
    loadingDailyTop.value = false
  }
}

async function loadComments() {
  loadingComments.value = true
  try {
    const res = await getAdminComments(null, commentKeyword.value)
    comments.value = res.data
  } catch (err) {
    console.error('加载评论失败:', err)
  } finally {
    loadingComments.value = false
  }
}

async function handleDeleteComment(id) {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    await deleteAdminComment(id)
    comments.value = comments.value.filter(c => c.id !== id)
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
}

function goToMessage(msgId) {
  location.hash = '#/message/' + msgId
}

// ===== 通用 =====
function goHome() {
  location.hash = ''
}

function handleLogout() {
  logout()
  location.hash = ''
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}-${day} ${h}:${min}`
}

function formatDateFull(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${y}-${m}-${day} ${weekdays[d.getDay()]}`
}

// ===== Tab 切换时自动加载数据 =====
watch(activeTab, (tab) => {
  if (tab === 'dashboard') {
    loadStats()
    loadRecentMessages()
  } else if (tab === 'messages') {
    loadMessages()
  } else if (tab === 'dailyTop') {
    loadDailyTop()
  } else if (tab === 'users') {
    loadUsers()
  } else if (tab === 'comments') {
    loadComments()
  }
})

onMounted(() => {
  if (!isAdmin.value) {
    alert('需要管理员权限！')
    location.hash = ''
    return
  }
  loadStats()
  loadRecentMessages()
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航 ===== */
.admin-header {
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  flex-shrink: 0;
}

.admin-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-logo {
  font-size: 1.5em;
}

.admin-header h1 {
  font-size: 1.2em;
  margin: 0;
}

.admin-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-user-info {
  font-size: 0.9em;
  opacity: 0.9;
}

.header-btn {
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 0.85em;
  font-weight: bold;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  font-family: inherit;
}

.header-btn.back {
  background: rgba(255,255,255,0.25);
  color: white;
}
.header-btn.back:hover {
  background: rgba(255,255,255,0.4);
}

.header-btn.logout {
  background: white;
  color: #e17055;
}
.header-btn.logout:hover {
  background: #fff0ed;
}

/* ===== 主体布局 ===== */
.admin-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.admin-sidebar {
  width: 180px;
  background: white;
  border-right: 1px solid #eee;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: none;
  border: none;
  font-size: 0.95em;
  color: #636e72;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  font-family: inherit;
  border-left: 3px solid transparent;
}

.sidebar-item:hover {
  background: #fff8f0;
  color: #e17055;
}

.sidebar-item.active {
  background: #fff0ed;
  color: #e17055;
  font-weight: bold;
  border-left-color: #e17055;
}

.sidebar-icon {
  font-size: 1.2em;
}

/* ===== 内容区 ===== */
.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.tab-panel {
  max-width: 1100px;
}

.panel-title {
  font-size: 1.4em;
  color: #2d3436;
  margin: 0 0 20px 0;
}

/* ===== 统计卡片 ===== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
}

.stat-card.users::before { background: linear-gradient(90deg, #6c5ce7, #a29bfe); }
.stat-card.messages::before { background: linear-gradient(90deg, #e17055, #fdcb6e); }
.stat-card.comments::before { background: linear-gradient(90deg, #00b894, #55efc4); }

.stat-icon {
  font-size: 2em;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 1.8em;
  font-weight: bold;
  color: #2d3436;
}

.stat-label {
  font-size: 0.85em;
  color: #b2bec3;
}

.stat-today {
  font-size: 0.8em;
  color: #00b894;
  background: #e8faf5;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: bold;
}

/* ===== 最近留言 ===== */
.dashboard-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.dashboard-section h3 {
  margin: 0 0 14px 0;
  color: #2d3436;
  font-size: 1.1em;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fffef9;
  border-radius: 10px;
  border: 1px solid #ffeaa7;
}

.recent-author {
  font-weight: bold;
  color: #e17055;
  font-size: 0.9em;
  flex-shrink: 0;
}

.recent-content {
  flex: 1;
  color: #636e72;
  font-size: 0.9em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-time {
  font-size: 0.8em;
  color: #b2bec3;
  flex-shrink: 0;
}

/* ===== 工具栏 ===== */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: white;
  border: 2px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  flex: 1;
  max-width: 400px;
  transition: border-color 0.2s;
}

.search-box:focus-within {
  border-color: #e17055;
}

.search-box input {
  flex: 1;
  border: none;
  padding: 10px 14px;
  font-size: 0.95em;
  outline: none;
  font-family: inherit;
}

.search-btn {
  background: none;
  border: none;
  padding: 10px 14px;
  font-size: 1.1em;
  cursor: pointer;
  transition: transform 0.2s;
}
.search-btn:hover { transform: scale(1.1); }

.batch-delete-btn {
  background: #ff6b6b;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 12px;
  font-size: 0.9em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.batch-delete-btn:hover {
  background: #ee5a24;
  transform: translateY(-1px);
}

/* ===== 数据表格 ===== */
.data-table-wrapper {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #fafafa;
  padding: 12px 14px;
  text-align: left;
  font-size: 0.85em;
  color: #b2bec3;
  font-weight: bold;
  border-bottom: 2px solid #f0f0f0;
  white-space: nowrap;
}

.data-table td {
  padding: 12px 14px;
  font-size: 0.9em;
  color: #2d3436;
  border-bottom: 1px solid #f5f5f5;
}

.data-table tr:hover td {
  background: #fffdf5;
}

.col-check { width: 40px; text-align: center; }
.col-id { width: 50px; color: #b2bec3; }
.col-author { white-space: nowrap; font-weight: bold; color: #e17055; }
.col-content { min-width: 200px; }
.col-stats { width: 60px; text-align: center; color: #b2bec3; }
.col-time { width: 100px; white-space: nowrap; font-size: 0.8em; color: #b2bec3; }
.col-actions { width: 100px; text-align: center; white-space: nowrap; }
.col-avatar { width: 50px; text-align: center; }

.content-text {
  display: inline-block;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-avatar-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px; height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  font-weight: bold;
  font-size: 0.9em;
}

.msg-link {
  color: #6c5ce7;
  cursor: pointer;
  text-decoration: none;
}
.msg-link:hover {
  text-decoration: underline;
}

.role-badge {
  font-size: 0.75em;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: bold;
}
.role-badge.admin {
  background: #e17055;
  color: white;
}
.role-badge.normal {
  background: #dfe6e9;
  color: #636e72;
}

/* ===== 行内编辑 ===== */
.edit-inline {
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-input {
  flex: 1;
  border: 2px solid #e17055;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 0.9em;
  outline: none;
  font-family: inherit;
}

.edit-save, .edit-cancel {
  background: none;
  border: none;
  font-size: 1.1em;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.2s;
}
.edit-save:hover { background: #e8faf5; }
.edit-cancel:hover { background: #fff0ed; }

/* ===== 操作按钮 ===== */
.action-btn {
  background: none;
  border: none;
  font-size: 1em;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 6px;
  transition: all 0.2s;
  opacity: 0.5;
}
.action-btn:hover {
  opacity: 1;
  transform: scale(1.2);
}
.action-btn.delete:hover {
  background: #fff0ed;
}
.action-btn.edit:hover {
  background: #fff8e1;
}
.action-btn.toggle-admin:hover {
  background: #f0f0ff;
}
.action-btn.view:hover {
  background: #e8faf5;
}

/* ===== 每日热评 ===== */
.panel-subtitle {
  color: #b2bec3;
  font-size: 0.9em;
  margin: -12px 0 20px 0;
}

.col-date {
  white-space: nowrap;
  width: 130px;
}

.date-badge {
  display: inline-block;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 0.8em;
  font-weight: bold;
}

.like-highlight {
  color: #e17055;
  font-weight: bold;
}

/* ===== 状态提示 ===== */
.loading, .empty, .empty-hint {
  text-align: center;
  padding: 40px;
  color: #b2bec3;
  font-size: 0.95em;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 60px;
  }
  .sidebar-label {
    display: none;
  }
  .sidebar-item {
    justify-content: center;
    padding: 12px;
  }
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .admin-header h1 {
    display: none;
  }
  .data-table-wrapper {
    overflow-x: auto;
  }
}
</style>
