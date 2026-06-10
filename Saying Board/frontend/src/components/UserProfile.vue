<template>
  <div class="profile-page">
    <button class="back-btn" @click="goBack">← 返回首页</button>

    <div v-if="loading" class="loading-state">加载中...</div>

    <div v-else-if="error" class="error-state">{{ error }}</div>

    <div v-else>
      <!-- 用户信息卡片 -->
      <div class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar-wrap">
            <div class="profile-avatar" :style="{ background: avatarBg }">
              <img v-if="profile.avatar && profile.avatar.startsWith('data:')" :src="profile.avatar" alt="头像" class="avatar-img" />
              <span v-else>{{ profile.avatar || avatarEmoji }}</span>
            </div>
          </div>
          <div class="profile-info">
            <div class="profile-name">
              {{ profile.nickname || profile.username }}
              <span v-if="profile.isAdmin" class="official-badge">官方</span>
            </div>
            <div class="profile-username">@{{ profile.username }}</div>
            <div class="profile-meta">
              <span>📅 {{ formatJoinDate(profile.createdAt) }} 加入</span>
              <span>📝 {{ profile.messageCount }} 条留言</span>
            </div>
          </div>
          <button v-if="isSelf" class="edit-btn" @click="showEdit = true">✏️ 编辑资料</button>
        </div>
      </div>

      <!-- 用户留言列表 -->
      <div class="user-messages">
        <h3>📝 {{ isSelf ? '我的' : 'TA的' }}留言</h3>
        <div v-if="profile.messages.length === 0" class="no-messages">暂无留言</div>
        <div v-else class="message-list">
          <div
            v-for="msg in profile.messages"
            :key="msg.id"
            class="msg-item"
            @click="goToMessage(msg.id)"
          >
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-footer">
              <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
              <div class="msg-stats">
                <span>❤️ {{ msg.likes || 0 }}</span>
                <span>👁️ {{ msg.viewCount || 0 }}</span>
                <button
                  v-if="isSelf"
                  class="delete-msg-btn"
                  @click.stop="handleDeleteMessage(msg)"
                >🗑️ 删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <Teleport to="body">
      <div v-if="showEdit" class="modal-overlay" @click.self="showEdit = false">
        <div class="edit-modal">
          <button class="close-btn" @click="showEdit = false">✕</button>
          <h3>✏️ 编辑资料</h3>

          <div class="edit-section">
            <label>自定义头像</label>
            <div class="avatar-upload-area">
              <div class="avatar-preview-edit" :class="{ 'has-image': editAvatar && editAvatar.startsWith('data:') }">
                <img v-if="editAvatar && editAvatar.startsWith('data:')" :src="editAvatar" alt="头像预览" />
                <span v-else>{{ editAvatar || '👤' }}</span>
              </div>
              <div class="upload-btns">
                <label class="upload-label">
                  📷 上传图片
                  <input
                    type="file"
                    accept="image/png,image/jpeg,image/gif,image/webp"
                    @change="handleAvatarUpload"
                    style="display: none"
                  />
                </label>
                <button v-if="editAvatar && editAvatar.startsWith('data:')" class="clear-img-btn" @click="editAvatar = ''">恢复默认</button>
              </div>
            </div>
            <div class="avatar-grid">
              <div
                v-for="emoji in avatarOptions"
                :key="emoji"
                class="avatar-option"
                :class="{ selected: editAvatar === emoji }"
                @click="editAvatar = emoji"
              >
                {{ emoji }}
              </div>
            </div>
          </div>

          <div class="edit-section">
            <label>昵称</label>
            <input
              v-model="editNickname"
              type="text"
              maxlength="30"
              placeholder="输入新昵称"
            />
          </div>

          <p v-if="editError" class="error-msg">{{ editError }}</p>

          <div class="edit-actions">
            <button class="cancel-btn" @click="showEdit = false">取消</button>
            <button class="save-btn" @click="saveProfile" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getUserProfile, updateProfile, deleteMessageByOwner } from '../api'
import { useAuth } from '../composables/useAuth'

const props = defineProps({
  username: { type: String, required: true }
})

const { user, isLoggedIn, setAuth } = useAuth()

const profile = ref({})
const loading = ref(true)
const error = ref('')

// 监听 username 变化（从个人主页跳转到另一个用户主页）
watch(() => props.username, () => {
  loadProfile()
})
const showEdit = ref(false)
const editNickname = ref('')
const editAvatar = ref('')
const editError = ref('')
const saving = ref(false)

const isSelf = computed(() => isLoggedIn.value && user.value?.username === props.username)

const avatarOptions = [
  '🍔', '🍕', '🍣', '🍜', '🍛', '🍲', '🥗', '🍝',
  '🌮', '🍱', '🥞', '🧁', '🍩', '🍰', '🥐', '🍗'
]

function handleAvatarUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    alert('图片大小不能超过 2MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const size = 100
      canvas.width = size
      canvas.height = size
      const ctx = canvas.getContext('2d')
      const min = Math.min(img.width, img.height)
      const sx = (img.width - min) / 2
      const sy = (img.height - min) / 2
      ctx.drawImage(img, sx, sy, min, min, 0, 0, size, size)
      editAvatar.value = canvas.toDataURL('image/jpeg', 0.8)
    }
    img.src = e.target.result
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

function hashCode(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = str.charCodeAt(i) + ((h << 5) - h)
  }
  return Math.abs(h)
}

const avatarEmoji = computed(() => {
  return avatarOptions[hashCode(profile.value.username || '') % avatarOptions.length]
})

const avatarBg = computed(() => {
  const colors = [
    '#ff7675', '#74b9ff', '#55efc4', '#ffeaa7',
    '#a29bfe', '#fd79a8', '#00cec9', '#e17055'
  ]
  return colors[hashCode(profile.value.username || '') % colors.length] + '30'
})

async function loadProfile() {
  if (!props.username) {
    error.value = '用户不存在'
    loading.value = false
    return
  }
  loading.value = true
  error.value = ''
  console.log('[UserProfile] 正在加载用户资料:', props.username)
  try {
    const res = await getUserProfile(props.username)
    console.log('[UserProfile] API 返回:', res.data)
    if (res.data.error) {
      error.value = res.data.error
    } else {
      profile.value = res.data
    }
  } catch (err) {
    console.error('[UserProfile] 加载失败:', err.message, err.response?.status, err.response?.data)
    if (err.response?.status === 404) {
      error.value = '接口不存在，请确认后端已重启（需重新编译 Java 代码）'
    } else if (err.response?.status === 400) {
      error.value = err.response?.data?.error || '请求错误'
    } else if (!err.response) {
      error.value = '网络错误：无法连接后端，请确认后端服务已启动'
    } else {
      error.value = err.response?.data?.error || `加载失败 (${err.response?.status})`
    }
  } finally {
    loading.value = false
  }
}

watch(showEdit, (val) => {
  if (val) {
    editNickname.value = profile.value.nickname || ''
    editAvatar.value = profile.value.avatar || ''
    editError.value = ''
  }
})

async function handleDeleteMessage(msg) {
  if (!confirm('确定要删除这条留言吗？删除后不可恢复。')) return
  try {
    await deleteMessageByOwner(msg.id)
    profile.value.messages = profile.value.messages.filter(m => m.id !== msg.id)
    profile.value.messageCount = (profile.value.messageCount || 1) - 1
  } catch (err) {
    alert('删除失败: ' + (err.response?.data?.error || err.message))
  }
}

async function saveProfile() {
  editError.value = ''
  saving.value = true
  try {
    const res = await updateProfile(editNickname.value, editAvatar.value)
    if (res.data.error) {
      editError.value = res.data.error
      return
    }
    // 更新本地 profile 数据
    profile.value.nickname = res.data.nickname
    profile.value.avatar = res.data.avatar
    // 更新全局 auth 状态
    setAuth({
      token: localStorage.getItem('food_token'),
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
      isAdmin: res.data.isAdmin
    })
    showEdit.value = false
  } catch (err) {
    editError.value = err.response?.data?.error || '保存失败'
  } finally {
    saving.value = false
  }
}

function goBack() {
  location.hash = ''
}

function goToMessage(id) {
  location.hash = '#/message/' + id
}

function formatJoinDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

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

onMounted(loadProfile)
</script>

<style scoped>
.profile-page {
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

.loading-state, .error-state, .no-messages {
  text-align: center;
  padding: 60px 20px;
  color: #b2bec3;
  font-size: 1.1em;
}

.error-state {
  color: #d63031;
}

/* 用户信息卡片 */
.profile-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.profile-avatar-wrap {
  flex-shrink: 0;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5em;
  border: 3px solid #ffeaa7;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.profile-name {
  font-size: 1.5em;
  font-weight: bold;
  color: #2d3436;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.official-badge {
  font-size: 0.55em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: bold;
  letter-spacing: 1px;
}

.profile-username {
  color: #b2bec3;
  font-size: 0.9em;
  margin-top: 2px;
}

.profile-meta {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  font-size: 0.85em;
  color: #636e72;
  flex-wrap: wrap;
}

.edit-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 0.9em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  flex-shrink: 0;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(225, 112, 85, 0.4);
}

/* 用户留言列表 */
.user-messages {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.user-messages h3 {
  color: #e17055;
  font-size: 1.1em;
  margin-bottom: 16px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-item {
  padding: 16px;
  background: #fffef9;
  border: 1px solid #ffeaa7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.msg-item:hover {
  background: #fff8e1;
  transform: translateX(4px);
}

.msg-content {
  color: #2d3436;
  line-height: 1.6;
  margin-bottom: 8px;
  word-break: break-word;
}

.msg-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.8em;
  color: #b2bec3;
}

.msg-stats {
  display: flex;
  gap: 12px;
  align-items: center;
}

.delete-msg-btn {
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

/* 编辑弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.edit-modal {
  background: white;
  border-radius: 20px;
  padding: 30px;
  width: 420px;
  max-width: 90vw;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 16px;
  background: none;
  border: none;
  font-size: 1.2em;
  color: #b2bec3;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #e17055;
}

.edit-modal h3 {
  text-align: center;
  color: #e17055;
  margin-bottom: 20px;
}

.edit-section {
  margin-bottom: 20px;
}

.edit-section label {
  display: block;
  margin-bottom: 8px;
  color: #636e72;
  font-weight: bold;
  font-size: 0.9em;
}

.edit-section input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #ffeaa7;
  border-radius: 10px;
  font-size: 1em;
  font-family: inherit;
  transition: border-color 0.3s;
  background: #fffef9;
  box-sizing: border-box;
}

.edit-section input:focus {
  outline: none;
  border-color: #e17055;
}

.avatar-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
  margin-top: 10px;
}

.avatar-upload-area {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
}

.avatar-preview-edit {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 2px solid #ffeaa7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8em;
  overflow: hidden;
  flex-shrink: 0;
  background: #fffef9;
}

.avatar-preview-edit.has-image {
  border-color: #e17055;
}

.avatar-preview-edit img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-btns {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.upload-label {
  display: inline-block;
  padding: 6px 16px;
  background: linear-gradient(135deg, #74b9ff, #a29bfe);
  color: white;
  border-radius: 10px;
  font-size: 0.85em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-label:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(116, 185, 255, 0.4);
}

.clear-img-btn {
  padding: 4px 12px;
  background: #f0f0f0;
  color: #636e72;
  border: none;
  border-radius: 8px;
  font-size: 0.8em;
  cursor: pointer;
  transition: background 0.2s;
  font-family: inherit;
}

.clear-img-btn:hover {
  background: #dfe6e9;
}

.avatar-option {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
  border-radius: 10px;
  border: 2px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
  background: #fffef9;
}

.avatar-option:hover {
  border-color: #fdcb6e;
  transform: scale(1.1);
}

.avatar-option.selected {
  border-color: #e17055;
  background: #ffeaa7;
  transform: scale(1.1);
}

.error-msg {
  color: #d63031;
  font-size: 0.9em;
  margin-bottom: 12px;
  text-align: center;
}

.edit-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.cancel-btn {
  padding: 10px 24px;
  background: #f0f0f0;
  color: #636e72;
  border: none;
  border-radius: 12px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.cancel-btn:hover {
  background: #dfe6e9;
}

.save-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(225, 112, 85, 0.4);
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .profile-meta {
    justify-content: center;
  }

  .avatar-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
