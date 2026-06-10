<template>
  <header class="header">
    <!-- 右上角用户区域 -->
    <div class="user-area">
      <template v-if="isLoggedIn">
        <a class="user-info" :href="'#/user/' + user.username" @click.prevent="goToProfile">
          <img v-if="user.avatar && user.avatar.startsWith('data:')" :src="user.avatar" alt="头像" class="user-avatar-img" />
          <span v-else class="user-avatar-sm">{{ user.avatar || '👤' }}</span>
          {{ user.nickname || user.username }}
          <span v-if="user.isAdmin" class="official-badge-header">官方</span>
        </a>
        <button v-if="isAdmin" class="user-btn admin" @click="goToAdmin">管理后台</button>
        <button class="user-btn logout" @click="handleLogout">退出</button>
      </template>
      <template v-else>
        <button class="user-btn login" @click="$emit('showLogin')">登录 / 注册</button>
      </template>
    </div>

    <div class="food-emojis">🍜 🍛 🍣 🍔 🍲</div>
    <h1>今天吃什么？</h1>
    <p class="subtitle">分享你的美食灵感，看看大家都在吃什么！</p>
  </header>
</template>

<script setup>
import { useAuth } from '../composables/useAuth'

defineEmits(['showLogin'])

const { user, isLoggedIn, isAdmin, logout } = useAuth()

function handleLogout() {
  logout()
}

function goToProfile() {
  location.hash = '#/user/' + user.value.username
}

function goToAdmin() {
  location.hash = '#/admin'
}
</script>

<style scoped>
.header {
  text-align: center;
  padding: 40px 20px 30px;
  position: relative;
}

.user-area {
  position: absolute;
  top: 16px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  color: #636e72;
  font-size: 0.95em;
  font-weight: bold;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: color 0.2s;
}

.user-info:hover {
  color: #e17055;
}

.user-avatar-sm {
  font-size: 1.2em;
}

.user-avatar-img {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  object-fit: cover;
  vertical-align: middle;
}

.official-badge-header {
  font-size: 0.65em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 1px 8px;
  border-radius: 8px;
  font-weight: bold;
  letter-spacing: 1px;
}

.user-btn {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  font-family: inherit;
}

.user-btn.login {
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border: none;
}

.user-btn.login:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(225, 112, 85, 0.4);
}

.user-btn.logout {
  background: white;
  color: #e17055;
  border: 2px solid #e17055;
}

.user-btn.logout:hover {
  background: #ffeaea;
}

.user-btn.admin {
  background: linear-gradient(135deg, #6c5ce7, #a29bfe);
  color: white;
  border: none;
}

.user-btn.admin:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.4);
}

.header h1 {
  font-size: 3em;
  color: #e17055;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
}

.subtitle {
  font-size: 1.2em;
  color: #d63031;
  opacity: 0.8;
}

.food-emojis {
  font-size: 2em;
  margin: 10px 0;
  letter-spacing: 10px;
}

@media (max-width: 768px) {
  .header h1 {
    font-size: 2em;
  }

  .user-area {
    position: static;
    justify-content: center;
    margin-bottom: 12px;
  }
}
</style>
