<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="admin-panel">
        <button class="close-btn" @click="$emit('close')">✕</button>
        <h2>🛠️ 管理后台</h2>

        <!-- Tab 切换 -->
        <div class="tabs">
          <button :class="{ active: tab === 'messages' }" @click="tab = 'messages'">
            📝 留言管理
          </button>
          <button :class="{ active: tab === 'users' }" @click="tab = 'users'; loadUsers()">
            👥 用户统计
          </button>
        </div>

        <!-- 留言管理 -->
        <div v-if="tab === 'messages'" class="tab-content">
          <div v-if="loadingMessages" class="loading">加载中...</div>
          <div v-else-if="messages.length === 0" class="empty">暂无留言</div>
          <div v-else class="message-list">
            <div v-for="msg in messages" :key="msg.id" class="admin-msg">
              <div class="msg-info">
                <span class="msg-author">{{ msg.author }}</span>
                <span class="msg-text">{{ msg.content }}</span>
              </div>
              <div class="msg-stats">
                <span title="浏览量">👁️ {{ msg.viewCount || 0 }}</span>
                <span title="点赞">❤️ {{ msg.likes || 0 }}</span>
                <span title="评论">💬 {{ msg.commentCount || 0 }}</span>
                <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
              </div>
              <button class="delete-btn" @click="handleDelete(msg.id)" title="删除">🗑️</button>
            </div>
          </div>
        </div>

        <!-- 用户统计 -->
        <div v-if="tab === 'users'" class="tab-content">
          <div v-if="loadingUsers" class="loading">加载中...</div>
          <div v-else-if="users.length === 0" class="empty">暂无用户</div>
          <div v-else class="user-list">
            <div v-for="u in users" :key="u.id" class="admin-user">
              <div class="user-avatar">{{ u.avatar || u.username.charAt(0).toUpperCase() }}</div>
              <div class="user-details">
                <div class="user-name">
                  {{ u.nickname || u.username }}
                  <span v-if="u.isAdmin" class="admin-badge">官方</span>
                </div>
                <div class="user-meta">
                  留言 {{ u.messageCount }} 条
                </div>
              </div>
              <div class="user-time">{{ formatTime(u.createdAt) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMessages, getAdminUsers, deleteMessage } from '../api'

defineEmits(['close', 'deleted'])

const tab = ref('messages')
const messages = ref([])
const users = ref([])
const loadingMessages = ref(true)
const loadingUsers = ref(false)

onMounted(() => {
  loadMessages()
})

async function loadMessages() {
  loadingMessages.value = true
  try {
    const res = await getMessages()
    messages.value = res.data
  } catch (err) {
    console.error('加载留言失败:', err)
  } finally {
    loadingMessages.value = false
  }
}

async function loadUsers() {
  loadingUsers.value = true
  try {
    const res = await getAdminUsers()
    users.value = res.data
  } catch (err) {
    console.error('加载用户失败:', err)
  } finally {
    loadingUsers.value = false
  }
}

async function handleDelete(id) {
  if (!confirm('确定要删除这条留言吗？')) return
  try {
    await deleteMessage(id)
    messages.value = messages.value.filter(m => m.id !== id)
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
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
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  cursor: pointer;
  user-select: none;
}

.admin-panel {
  background: white;
  border-radius: 20px;
  padding: 28px;
  width: 700px;
  max-width: 95vw;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.close-btn {
  position: absolute;
  top: 12px; right: 16px;
  background: none; border: none;
  font-size: 1.2em; color: #b2bec3;
  cursor: pointer;
}
.close-btn:hover { color: #e17055; }

h2 {
  text-align: center;
  color: #e17055;
  margin-bottom: 16px;
}

.tabs {
  display: flex;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 16px;
}

.tabs button {
  flex: 1;
  padding: 10px;
  background: none; border: none;
  font-size: 1em; font-weight: bold;
  color: #b2bec3;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s;
}

.tabs button.active {
  color: #e17055;
  border-bottom-color: #e17055;
}
.tabs button:hover { color: #e17055; }

.tab-content {
  overflow-y: auto;
  flex: 1;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #b2bec3;
}

/* 留言管理 */
.admin-msg {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  margin-bottom: 8px;
  background: #fffef9;
  border: 1px solid #ffeaa7;
  transition: background 0.2s;
}
.admin-msg:hover { background: #fff8e1; }

.msg-info {
  flex: 1;
  min-width: 0;
}

.msg-author {
  font-weight: bold;
  color: #e17055;
  margin-right: 8px;
  font-size: 0.9em;
}

.msg-text {
  color: #636e72;
  font-size: 0.9em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-stats {
  display: flex;
  gap: 10px;
  font-size: 0.8em;
  color: #b2bec3;
  flex-shrink: 0;
}

.msg-time {
  font-size: 0.75em;
}

.delete-btn {
  background: none; border: none;
  font-size: 1.1em;
  cursor: pointer;
  opacity: 0.4;
  transition: all 0.2s;
  flex-shrink: 0;
}
.delete-btn:hover {
  opacity: 1;
  transform: scale(1.2);
}

/* 用户统计 */
.admin-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  margin-bottom: 8px;
  background: #fffef9;
  border: 1px solid #ffeaa7;
}

.user-avatar {
  width: 40px; height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.1em;
  flex-shrink: 0;
}

.user-details { flex: 1; }

.user-name {
  font-weight: bold;
  color: #2d3436;
  display: flex;
  align-items: center;
  gap: 6px;
}

.admin-badge {
  font-size: 0.7em;
  background: #e17055;
  color: white;
  padding: 1px 6px;
  border-radius: 8px;
}

.user-meta {
  font-size: 0.85em;
  color: #b2bec3;
  margin-top: 2px;
}

.user-time {
  font-size: 0.8em;
  color: #b2bec3;
  flex-shrink: 0;
}
</style>
