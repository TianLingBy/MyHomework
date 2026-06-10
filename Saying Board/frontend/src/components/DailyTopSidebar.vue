<template>
  <aside class="daily-top-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-icon">🏆</span>
      <h3>每日热评</h3>
    </div>

    <div v-if="loading" class="sidebar-loading">加载中...</div>

    <div v-else-if="topMessages.length === 0" class="sidebar-empty">
      <div class="empty-icon">📭</div>
      <p>暂无热评数据</p>
    </div>

    <div v-else class="top-list">
      <div
        v-for="msg in topMessages"
        :key="msg.id"
        class="top-item"
        @click="openDetail(msg.id)"
      >
        <div class="item-date">
          <span class="date-day">{{ formatDate(msg.createdAt) }}</span>
          <span class="date-weekday">{{ formatWeekday(msg.createdAt) }}</span>
        </div>
        <div class="item-body">
          <div class="item-author">
            <span class="author-avatar">{{ msg.avatar || getAvatarEmoji(msg.author) }}</span>
            <span class="author-name">{{ msg.author }}</span>
            <span v-if="msg.isAdmin" class="admin-tag">官方</span>
          </div>
          <div class="item-content">{{ msg.content }}</div>
          <div class="item-stats">
            <span class="stat like">❤️ {{ msg.likes }}</span>
            <span class="stat view">👀 {{ msg.viewCount || 0 }}</span>
            <span v-if="msg.commentCount > 0" class="stat comment">💬 {{ msg.commentCount }}</span>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDailyTopMessages } from '../api'

const topMessages = ref([])
const loading = ref(true)

const foodAvatars = [
  '🍔', '🍕', '🍣', '🍜', '🍛', '🍲', '🥗', '🍝',
  '🌮', '🍱', '🥞', '🧁', '🍩', '🍰', '🥐', '🍗'
]

function hashCode(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = str.charCodeAt(i) + ((h << 5) - h)
  }
  return Math.abs(h)
}

function getAvatarEmoji(author) {
  return foodAvatars[hashCode(author) % foodAvatars.length]
}

async function loadDailyTop() {
  loading.value = true
  try {
    const res = await getDailyTopMessages()
    topMessages.value = res.data
  } catch (err) {
    console.error('加载每日热评失败:', err)
  } finally {
    loading.value = false
  }
}

function openDetail(id) {
  location.hash = `#/message/${id}`
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}/${day}`
}

function formatWeekday(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[d.getDay()]
}

onMounted(() => {
  loadDailyTop()
})

defineExpose({ loadDailyTop })
</script>

<style scoped>
.daily-top-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 20px;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
}

.daily-top-sidebar::-webkit-scrollbar {
  width: 4px;
}

.daily-top-sidebar::-webkit-scrollbar-thumb {
  background: #e1705540;
  border-radius: 4px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #ffeaa7;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1em;
  color: #e17055;
}

.sidebar-icon {
  font-size: 1.3em;
}

.sidebar-loading,
.sidebar-empty {
  text-align: center;
  padding: 30px 10px;
  color: #b2bec3;
  font-size: 0.9em;
}

.empty-icon {
  font-size: 2em;
  margin-bottom: 8px;
}

.top-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.top-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: 12px;
  background: #fffef9;
  border: 1px solid #ffeaa740;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
}

.top-item:hover {
  background: #fff8e1;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(225, 112, 85, 0.12);
}

.item-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  padding: 6px 4px;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  border-radius: 10px;
  color: white;
  flex-shrink: 0;
}

.date-day {
  font-size: 0.85em;
  font-weight: bold;
  line-height: 1.2;
}

.date-weekday {
  font-size: 0.65em;
  opacity: 0.9;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-author {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.author-avatar {
  font-size: 0.85em;
}

.author-name {
  font-size: 0.8em;
  font-weight: bold;
  color: #e17055;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-tag {
  font-size: 0.55em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 1px 5px;
  border-radius: 4px;
  font-weight: bold;
  flex-shrink: 0;
}

.item-content {
  font-size: 0.8em;
  color: #636e72;
  line-height: 1.4;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.item-stats {
  display: flex;
  gap: 8px;
}

.stat {
  font-size: 0.7em;
  color: #b2bec3;
}

.stat.like {
  color: #e17055;
}

/* 响应式：窄屏隐藏 */
@media (max-width: 900px) {
  .daily-top-sidebar {
    display: none;
  }
}
</style>
