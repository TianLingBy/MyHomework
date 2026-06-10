<template>
  <div :class="['app', { 'full-width': isAdminPage }]">
    <!-- 管理后台页面 -->
    <template v-if="isAdminPage">
      <AdminPage />
    </template>

    <!-- 用户主页 -->
    <template v-else-if="profileUser">
      <UserProfile :username="profileUser" />
    </template>

    <!-- 详情页 -->
    <template v-else-if="detailId">
      <MessageDetail :message-id="detailId" />
    </template>

    <!-- 首页 -->
    <template v-else>
      <MessageHeader @show-login="showLogin = true" />
      <div class="main-layout">
        <DailyTopSidebar ref="sidebarRef" />
        <div class="main-content">
          <MessageForm @posted="refreshMessages" />
          <MessageBoard ref="boardRef" />
        </div>
      </div>
      <footer class="footer">
        <p>🍕 吃货留言板 · 每一天都要好好吃饭 🍜</p>
      </footer>
    </template>

    <!-- 登录弹窗 -->
    <LoginModal v-if="showLogin" @close="showLogin = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import MessageHeader from './components/MessageHeader.vue'
import MessageForm from './components/MessageForm.vue'
import MessageBoard from './components/MessageBoard.vue'
import MessageDetail from './components/MessageDetail.vue'
import UserProfile from './components/UserProfile.vue'
import LoginModal from './components/LoginModal.vue'
import AdminPage from './components/AdminPage.vue'
import DailyTopSidebar from './components/DailyTopSidebar.vue'
import { useAuth } from './composables/useAuth'

const boardRef = ref(null)
const sidebarRef = ref(null)
const showLogin = ref(false)
const detailId = ref(null)
const profileUser = ref(null)
const isAdminPage = ref(false)
const { initAuth } = useAuth()

function parseHash() {
  const hash = location.hash
  const userMatch = hash.match(/^#\/user\/(.+)$/)
  const msgMatch = hash.match(/^#\/message\/(\d+)$/)

  if (hash === '#/admin') {
    isAdminPage.value = true
    profileUser.value = null
    detailId.value = null
  } else if (userMatch) {
    const username = decodeURIComponent(userMatch[1])
    profileUser.value = username
    detailId.value = null
    isAdminPage.value = false
    console.log('[路由] 用户主页:', username)
  } else if (msgMatch) {
    detailId.value = Number(msgMatch[1])
    profileUser.value = null
    isAdminPage.value = false
  } else {
    detailId.value = null
    profileUser.value = null
    isAdminPage.value = false
  }
}

onMounted(() => {
  initAuth()
  parseHash()
  window.addEventListener('hashchange', parseHash)
})

onUnmounted(() => {
  window.removeEventListener('hashchange', parseHash)
})

function refreshMessages() {
  boardRef.value?.loadMessages()
  sidebarRef.value?.loadDailyTop()
}
</script>

<style scoped>
.app {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.app.full-width {
  max-width: 100%;
  padding: 0;
}

.main-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.footer {
  text-align: center;
  padding: 30px;
  color: #b2bec3;
  font-size: 0.9em;
}

@media (max-width: 900px) {
  .main-layout {
    flex-direction: column;
  }
}
</style>
