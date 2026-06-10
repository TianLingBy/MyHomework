<template>
  <div
    class="message-card"
    :style="cardStyle"
    @mouseenter="hovered = true"
    @mouseleave="hovered = false"
    @click="$emit('openDetail', message)"
  >
    <div class="card-header">
      <div class="avatar" :style="{ background: avatarColor }">
        <img v-if="message.avatar && message.avatar.startsWith('data:')" :src="message.avatar" alt="头像" class="avatar-img" />
        <span v-else>{{ message.avatar || avatarEmoji }}</span>
      </div>
      <div>
        <div class="card-author" :style="{ fontSize: fontSize + 'em' }">
          <span class="author-name">{{ message.author }}</span><span v-if="message.isAdmin" class="official-badge">官方</span>
        </div>
        <div class="card-time">{{ timeText }}</div>
      </div>
    </div>

    <div class="card-content" :style="{ fontSize: fontSize + 'em' }">
      {{ message.content }}
    </div>

    <div class="card-footer" @click.stop>
      <button
        class="like-btn"
        :class="{ liked: isLiked }"
        @click.stop="handleLike"
      >
        {{ isLiked ? '❤️' : '🤍' }}
        <span class="like-count">{{ message.likes }}</span>
      </button>
      <div class="footer-right">
        <span v-if="message.commentCount > 0" class="comment-count" title="评论">💬 {{ message.commentCount }}</span>
        <span v-if="message.likes > 0" class="likes-badge">🔥 热门</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  message: { type: Object, required: true },
  isLiked: { type: Boolean, default: false }
})

const emit = defineEmits(['like', 'unlike', 'openDetail'])
const hovered = ref(false)

// ===== 点赞 =====
function handleLike() {
  emit(props.isLiked ? 'unlike' : 'like', props.message.id)
}

// ===== 食物头像 =====
const foodAvatars = [
  '🍔', '🍕', '🍣', '🍜', '🍛', '🍲', '🥗', '🍝',
  '🌮', '🍱', '🥞', '🧁', '🍩', '🍰', '🥐', '🍗'
]
const avatarColors = [
  '#ff767520', '#74b9ff20', '#55efc420', '#ffeaa720',
  '#a29bfe20', '#fd79a820', '#00cec920', '#e1705520'
]

const cardColorPairs = [
  { bg: '#ffeaa7', text: '#6c3a00' },
  { bg: '#dff9fb', text: '#0a3d62' },
  { bg: '#f8cdda', text: '#6b1d3b' },
  { bg: '#d5f5e3', text: '#1a5632' },
  { bg: '#e8daef', text: '#4a1a6b' },
  { bg: '#fadbd8', text: '#7b1e2a' },
  { bg: '#d6eaf8', text: '#1b3a5c' },
  { bg: '#fdebd0', text: '#7e3b00' },
  { bg: '#d1f2eb', text: '#0e5e4a' },
  { bg: '#f9e79f', text: '#6b5b00' },
  { bg: '#f5cba7', text: '#783f04' },
  { bg: '#aed6f1', text: '#1a3c6e' },
  { bg: '#d2b4de', text: '#4a0e5c' },
  { bg: '#abebc6', text: '#0b5c30' },
  { bg: '#f5b7b1', text: '#78281f' },
  { bg: '#a3e4d7', text: '#0e4d40' },
]

function hashCode(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = str.charCodeAt(i) + ((h << 5) - h)
  }
  return Math.abs(h)
}

const avatarEmoji = computed(() => {
  return foodAvatars[hashCode(props.message.author) % foodAvatars.length]
})
const avatarColor = computed(() => {
  return avatarColors[hashCode(props.message.author) % avatarColors.length]
})

// ===== 动态尺寸 =====
const likes = computed(() => props.message.likes || 0)

const cardColor = computed(() => {
  return cardColorPairs[hashCode(props.message.author) % cardColorPairs.length]
})

// 估算中文字符宽度（1 中文 ≈ 1em，1 ASCII ≈ 0.55em）
function estimateTextWidth(text, fontSizeEm) {
  let w = 0
  for (const ch of text) {
    w += ch.charCodeAt(0) > 127 ? 1 : 0.55
  }
  return w * fontSizeEm * 16 // 16px ≈ 1em
}

const cardStyle = computed(() => {
  const basePadH = 10
  const basePadV = 8
  const padBonus = likes.value * 2
  const fs = fontSize.value

  // 内容最长行宽度
  const content = props.message.content || ''
  const lines = content.split('\n')
  let maxContentW = 0
  for (const line of lines) {
    if (line) maxContentW = Math.max(maxContentW, estimateTextWidth(line, fs))
  }

  // 作者名 + 官方徽章宽度
  const authorW = estimateTextWidth(props.message.author || '', fs) + (props.message.isAdmin ? 48 : 0)

  // 头像区占 30px，左右 padding 共 basePadH*2 + padBonus*2
  const padding = (basePadH + padBonus) * 2
  const avatarSpace = 30
  const needed = Math.max(maxContentW, authorW + avatarSpace) + padding + 12
  const dynWidth = 100 + likes.value * 20

  return {
    width: Math.max(dynWidth, needed) + 'px',
    padding: (basePadV + padBonus) + 'px ' + (basePadH + padBonus) + 'px',
    background: cardColor.value.bg,
    color: cardColor.value.text
  }
})

const fontSize = computed(() => {
  const base = 0.75
  const bonus = Math.min(likes.value * 0.03, 0.5)
  return base + bonus
})

// ===== 时间格式化 =====
const timeText = computed(() => {
  if (!props.message.createdAt) return ''
  const date = new Date(props.message.createdAt)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'

  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${m}-${d} ${h}:${min}`
})
</script>

<style scoped>
.message-card {
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  display: inline-flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
  cursor: pointer;
  user-select: none;
}

.message-card:hover {
  transform: translateY(-5px) scale(1.02);
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.15);
}

.message-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #e17055, #fdcb6e, #00b894, #6c5ce7);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9em;
  margin-right: 6px;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-author {
  font-weight: bold;
  color: inherit;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 3px;
}

.author-name {
  white-space: nowrap;
}

.card-time {
  font-size: 0.6em;
  opacity: 0.6;
  color: inherit;
  margin-top: 1px;
}

.card-content {
  color: inherit;
  line-height: 1.4;
  flex: 1;
  margin-bottom: 6px;
  word-break: break-word;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-top: 6px;
}

.like-btn {
  background: none;
  border: 2px solid #fdcb6e;
  border-radius: 12px;
  padding: 2px 8px;
  cursor: pointer;
  font-size: 0.7em;
  color: #e17055;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 3px;
}

.like-btn:hover {
  background: #ffeaa7;
  transform: scale(1.05);
}

.like-btn.liked {
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border-color: transparent;
}

.like-count {
  font-weight: bold;
}

.likes-badge {
  font-size: 0.6em;
  color: #fdcb6e;
  font-weight: bold;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-count {
  font-size: 0.6em;
  color: #6c5ce7;
}

.official-badge {
  font-size: 0.6em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 1px 6px;
  border-radius: 6px;
  font-weight: bold;
  letter-spacing: 1px;
  flex-shrink: 0;
  white-space: nowrap;
}
</style>
