<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="close">
      <div class="modal">
        <button class="close-btn" @click="close">✕</button>

        <!-- Tab 切换 -->
        <div class="tabs">
          <button
            :class="{ active: tab === 'login' }"
            @click="tab = 'login'; error = ''"
          >登录</button>
          <button
            :class="{ active: tab === 'register' }"
            @click="tab = 'register'; error = ''"
          >注册</button>
        </div>

        <!-- 表单 -->
        <form @submit.prevent="handleSubmit" class="form">
          <div class="form-group">
            <label>用户名</label>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              maxlength="30"
              autocomplete="username"
            />
          </div>
          <div class="form-group">
            <label>密码</label>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </div>

          <p v-if="error" class="error-msg">{{ error }}</p>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '处理中...' : (tab === 'login' ? '登录' : '注册') }}
          </button>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import { login, register } from '../api'
import { useAuth } from '../composables/useAuth'

const emit = defineEmits(['close'])
const { setAuth } = useAuth()

const tab = ref('login')
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

function close() {
  emit('close')
}

async function handleSubmit() {
  error.value = ''

  if (!username.value.trim()) {
    error.value = '请输入用户名'
    return
  }
  if (!password.value) {
    error.value = '请输入密码'
    return
  }
  if (tab.value === 'register' && password.value.length < 4) {
    error.value = '密码长度不能少于4位'
    return
  }

  loading.value = true
  try {
    const fn = tab.value === 'login' ? login : register
    const res = await fn(username.value.trim(), password.value)

    if (res.data.error) {
      error.value = res.data.error
      return
    }

    setAuth(res.data)
    close()
  } catch (err) {
    error.value = err.response?.data?.error || '操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
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
  cursor: pointer;
  user-select: none;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal {
  background: white;
  border-radius: 20px;
  padding: 30px;
  width: 360px;
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

.tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 2px solid #f0f0f0;
}

.tabs button {
  flex: 1;
  padding: 10px;
  background: none;
  border: none;
  font-size: 1.1em;
  font-weight: bold;
  color: #b2bec3;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
}

.tabs button.active {
  color: #e17055;
  border-bottom-color: #e17055;
}

.tabs button:hover {
  color: #e17055;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #636e72;
  font-weight: bold;
  font-size: 0.9em;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 2px solid #ffeaa7;
  border-radius: 10px;
  font-size: 1em;
  font-family: inherit;
  transition: border-color 0.3s;
  background: #fffef9;
}

.form-group input:focus {
  outline: none;
  border-color: #e17055;
}

.error-msg {
  color: #d63031;
  font-size: 0.9em;
  margin-bottom: 12px;
  text-align: center;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.05em;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  letter-spacing: 2px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(225, 112, 85, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
