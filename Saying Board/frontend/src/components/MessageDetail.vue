<template>
  <div class="detail-page">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="goBack">← 返回首页</button>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else class="detail-card">
      <!-- 留言详情 -->
      <div class="msg-header">
        <div class="msg-avatar" :style="{ background: avatarColor }">
          <img v-if="message.avatar && message.avatar.startsWith('data:')" :src="message.avatar" alt="头像" class="msg-avatar-img" />
          <span v-else>{{ message.avatar || avatarEmoji }}</span>
        </div>
        <div class="msg-header-info">
          <div class="msg-author">
            {{ message.author }}
            <span v-if="message.isAdmin" class="official-badge">官方</span>
          </div>
          <div class="msg-date">📅 发布于 {{ formatFullDate(message.createdAt) }}</div>
        </div>
      </div>

      <div class="msg-content">{{ message.content }}</div>

      <div class="msg-stats">
        <span>👁️ 浏览 {{ message.viewCount || 0 }}</span>
        <span>❤️ 点赞 {{ message.likes || 0 }}</span>
        <span>💬 评论 {{ comments.length }}</span>
        <button
          v-if="isLoggedIn && (isAdmin || user.username === message.author)"
          class="delete-msg-btn"
          @click="handleDeleteMessage"
        >🗑️ 删除</button>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3>💬 评论 ({{ comments.length }})</h3>

        <div v-if="loadingComments" class="loading">加载中...</div>
        <div v-else-if="comments.length === 0" class="no-comments">暂无评论，快来抢沙发~</div>
        <div v-else class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-avatar" :class="{ 'has-custom': c.avatar }">
              <img v-if="c.avatar && c.avatar.startsWith('data:')" :src="c.avatar" alt="头像" class="comment-avatar-img" />
              <span v-else>{{ c.avatar || c.author.charAt(0) }}</span>
            </div>
            <div class="comment-body">
              <div class="comment-meta">
                <span class="comment-author">
                  {{ c.author }}
                  <span v-if="c.isAdmin" class="official-badge-sm">官方</span>
                </span>
                <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
                <button
                  v-if="isLoggedIn && (isAdmin || user.username === c.author)"
                  class="delete-comment-btn"
                  @click="handleDeleteComment(c)"
                >🗑️</button>
              </div>
              <div class="comment-text">{{ c.content }}</div>
            </div>
          </div>
        </div>

        <!-- 发表评论 -->
        <div class="comment-form">
          <textarea
            v-model="newComment"
            placeholder="写下你的评论..."
            @keydown.ctrl.enter="submitComment"
            rows="3"
          ></textarea>
          <button class="comment-btn" @click="submitComment" :disabled="submitting">
            {{ submitting ? '发送中...' : '💬 发送评论' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMessageDetail, getComments, addComment, deleteMessageByOwner, deleteComment } from '../api'
import { useAuth } from '../composables/useAuth'

const props = defineProps({
  messageId: { type: [Number, String], required: true }
})

const { user, isLoggedIn, isAdmin } = useAuth()
const message = ref({})
const comments = ref([])
const newComment = ref('')
const loading = ref(true)
const loadingComments = ref(true)
const submitting = ref(false)

// 头像
const foodAvatars = ['🍔','🍕','🍣','🍜','🍛','🍲','🥗','🍝','🌮','🍱','🥞','🧁','🍩','🍰','🥐','🍗']
const avatarColors = ['#ff767520','#74b9ff20','#55efc420','#ffeaa720','#a29bfe20','#fd79a820','#00cec920','#e1705520']

function hashCode(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) h = str.charCodeAt(i) + ((h << 5) - h)
  return Math.abs(h)
}

const avatarEmoji = computed(() => foodAvatars[hashCode(message.value.author || '') % foodAvatars.length])
const avatarColor = computed(() => avatarColors[hashCode(message.value.author || '') % avatarColors.length])

function goBack() {
  location.hash = ''
}

onMounted(async () => {
  // 加载详情（浏览量+1）
  try {
    const res = await getMessageDetail(props.messageId)
    message.value = res.data
  } catch (err) {
    console.error('加载详情失败:', err)
  } finally {
    loading.value = false
  }

  // 加载评论
  try {
    const res = await getComments(props.messageId)
    comments.value = res.data
  } catch (err) {
    console.error('加载评论失败:', err)
  } finally {
    loadingComments.value = false
  }
})

async function submitComment() {
  const text = newComment.value.trim()
  if (!text) {
    alert('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const res = await addComment(props.messageId, text)
    comments.value.push(res.data)
    newComment.value = ''
  } catch (err) {
    alert('评论失败: ' + (err.response?.data?.error || err.message))
  } finally {
    submitting.value = false
  }
}

async function handleDeleteMessage() {
  if (!confirm('确定要删除这条留言吗？删除后不可恢复。')) return
  try {
    await deleteMessageByOwner(props.messageId)
    alert('删除成功')
    location.hash = ''
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
}

async function handleDeleteComment(comment) {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    await deleteComment(props.messageId, comment.id)
    comments.value = comments.value.filter(c => c.id !== comment.id)
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
}

/** 完整日期格式：2024-06-09 15:30 */
function formatFullDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

/** 相对时间 */
function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}-${day} ${h}:${min}`
}
</script>

<style scoped>
.detail-page {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.85);
  border: 2px solid #ffeaa7;
  border-radius: 12px;
  padding: 8px 20px;
  font-size: 1em;
  font-weight: bold;
  color: #e17055;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  margin-bottom: 20px;
}

.back-btn:hover {
  background: #fff;
  transform: translateX(-4px);
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #b2bec3;
  font-size: 1.1em;
}

.detail-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.msg-avatar {
  width: 52px; height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
  flex-shrink: 0;
  overflow: hidden;
}

.msg-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.msg-header-info { flex: 1; }

.msg-author {
  font-weight: bold;
  color: #e17055;
  font-size: 1.2em;
}

.msg-date {
  font-size: 0.85em;
  color: #b2bec3;
  margin-top: 3px;
}

.msg-content {
  font-size: 1.15em;
  color: #2d3436;
  line-height: 1.7;
  margin-bottom: 18px;
  word-break: break-word;
}

.msg-stats {
  display: flex;
  gap: 20px;
  font-size: 0.95em;
  color: #b2bec3;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 24px;
}

/* 评论区 */
.comments-section h3 {
  color: #e17055;
  font-size: 1.1em;
  margin-bottom: 16px;
}

.loading, .no-comments {
  text-align: center;
  padding: 30px;
  color: #b2bec3;
}

.comment-list {
  margin-bottom: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fdcb6e, #e17055);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9em;
  flex-shrink: 0;
  overflow: hidden;
}

.comment-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-body { flex: 1; min-width: 0; }

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-author {
  font-weight: bold;
  color: #636e72;
}

.comment-time {
  font-size: 0.8em;
  color: #b2bec3;
}

.comment-text {
  color: #2d3436;
  line-height: 1.6;
  word-break: break-word;
}

/* 评论输入 */
.comment-form {
  border-top: 2px solid #f0f0f0;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-form textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #ffeaa7;
  border-radius: 12px;
  font-size: 1em;
  font-family: inherit;
  resize: vertical;
  background: #fffef9;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #e17055;
}

.comment-btn {
  align-self: flex-end;
  padding: 10px 28px;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 1px;
}

.comment-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(225, 112, 85, 0.4);
}

.comment-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.official-badge {
  font-size: 0.6em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: bold;
  letter-spacing: 1px;
  vertical-align: middle;
  margin-left: 6px;
}

.official-badge-sm {
  font-size: 0.7em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 1px 8px;
  border-radius: 8px;
  font-weight: bold;
  letter-spacing: 1px;
  vertical-align: middle;
  margin-left: 4px;
}

.comment-avatar.has-custom {
  font-size: 1.2em;
  background: linear-gradient(135deg, #ffeaa7, #fdcb6e);
}

.delete-msg-btn {
  margin-left: auto;
  background: none;
  border: 1px solid #ff7675;
  color: #d63031;
  font-size: 0.85em;
  padding: 2px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.delete-msg-btn:hover {
  background: #ffeaea;
}

.delete-comment-btn {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 0.85em;
  cursor: pointer;
  opacity: 0.4;
  transition: opacity 0.2s;
  padding: 0 4px;
}

.delete-comment-btn:hover {
  opacity: 1;
}
</style>
