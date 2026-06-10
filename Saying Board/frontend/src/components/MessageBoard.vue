<template>
  <section class="board-section">
    <h2>📢 大家都在说什么</h2>
    <p class="message-count">今日 {{ messages.length }} 条留言</p>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="messages.length === 0" class="empty-state">
      <div class="empty-icon">🍽️</div>
      <p>今天还没有人留言，来抢沙发吧！</p>
    </div>

    <!-- 留言列表 -->
    <TransitionGroup v-else name="list" tag="div" class="message-board">
      <MessageCard
        v-for="msg in messages"
        :key="msg.id"
        :message="msg"
        :is-liked="likedIds.includes(msg.id)"
        @like="handleLike"
        @unlike="handleUnlike"
        @open-detail="openDetail"
      />
    </TransitionGroup>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTodayMessages, likeMessage, unlikeMessage } from '../api'
import MessageCard from './MessageCard.vue'

const messages = ref([])
const likedIds = ref([])
const loading = ref(true)
const LIKED_KEY = 'food_liked_ids'

onMounted(() => {
  const saved = localStorage.getItem(LIKED_KEY)
  if (saved) likedIds.value = JSON.parse(saved)
  loadMessages()
})

async function loadMessages() {
  loading.value = true
  try {
    const res = await getTodayMessages()
    messages.value = res.data
  } catch (err) {
    console.error('加载留言失败:', err)
  } finally {
    loading.value = false
  }
}

function openDetail(message) {
  location.hash = `#/message/${message.id}`
}

async function handleLike(id) {
  try {
    await likeMessage(id)
    likedIds.value.push(id)
    localStorage.setItem(LIKED_KEY, JSON.stringify(likedIds.value))
    const msg = messages.value.find(m => m.id === id)
    if (msg) msg.likes++
  } catch (err) {
    console.error('点赞失败:', err)
  }
}

async function handleUnlike(id) {
  try {
    await unlikeMessage(id)
    const idx = likedIds.value.indexOf(id)
    if (idx > -1) likedIds.value.splice(idx, 1)
    localStorage.setItem(LIKED_KEY, JSON.stringify(likedIds.value))
    const msg = messages.value.find(m => m.id === id)
    if (msg) msg.likes = Math.max(0, msg.likes - 1)
  } catch (err) {
    console.error('取消点赞失败:', err)
  }
}

// 暴露给父组件调用
defineExpose({ loadMessages })
</script>

<style scoped>
.board-section {
  max-width: 1200px;
  margin: 0 auto;
}

.board-section h2 {
  text-align: center;
  color: #e17055;
  font-size: 1.8em;
  margin-bottom: 30px;
}

.message-count {
  text-align: center;
  color: #b2bec3;
  margin-bottom: 20px;
}

.message-board {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  align-items: flex-start;
  padding-bottom: 20px;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #b2bec3;
}

.empty-icon {
  font-size: 4em;
  margin-bottom: 15px;
}

.empty-state p,
.loading-state p {
  font-size: 1.1em;
}

/* TransitionGroup 动画 */
.list-enter-active {
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}
.list-leave-active {
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}
.list-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.9);
}
.list-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.85);
}
.list-move {
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}
</style>
