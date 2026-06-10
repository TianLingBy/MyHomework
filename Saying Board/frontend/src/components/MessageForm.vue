<template>
  <section class="input-section">
    <h2>💬 发表你的想法</h2>
    <p v-if="isLoggedIn" class="tip login-tip">
      以 <span v-if="user.avatar" class="form-avatar">{{ user.avatar }}</span> <strong>{{ user.nickname || user.username }}</strong>
      <span v-if="user.isAdmin" class="form-official">官方</span> 身份发表留言
    </p>
    <p v-else class="tip">系统会自动为你分配一个「XX的XX」格式的昵称哦~</p>
    <div class="form-group">
      <label for="content">今天想吃什么？</label>
      <textarea
        id="content"
        v-model="content"
        placeholder="比如：好想吃火锅啊！推荐一家好吃的店~"
        @keydown.ctrl.enter="submit"
      ></textarea>
    </div>
    <button class="submit-btn" @click="submit" :disabled="loading">
      {{ loading ? '发布中...' : '🍲 发布留言' }}
    </button>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { createMessage } from '../api'
import { useAuth } from '../composables/useAuth'

const emit = defineEmits(['posted'])
const { user, isLoggedIn } = useAuth()

const content = ref('')
const loading = ref(false)

async function submit() {
  const text = content.value.trim()

  if (!text) {
    alert('请输入留言内容哦~')
    return
  }

  loading.value = true
  try {
    await createMessage(text)
    content.value = ''
    emit('posted')
  } catch (err) {
    alert('发布失败，请检查后端是否启动')
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.input-section {
  max-width: 600px;
  margin: 0 auto 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.input-section h2 {
  color: #e17055;
  margin-bottom: 8px;
  font-size: 1.4em;
}

.tip {
  color: #b2bec3;
  font-size: 0.9em;
  margin-bottom: 18px;
}

.login-tip {
  color: #e17055;
  font-weight: bold;
}

.login-tip strong {
  color: #d63031;
}

.form-avatar {
  font-size: 1.1em;
}

.form-official {
  font-size: 0.7em;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  padding: 1px 8px;
  border-radius: 8px;
  font-weight: bold;
  letter-spacing: 1px;
  vertical-align: middle;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #636e72;
  font-weight: bold;
}

.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #ffeaa7;
  border-radius: 12px;
  font-size: 1em;
  font-family: inherit;
  transition: border-color 0.3s;
  background: #fffef9;
  height: 100px;
  resize: vertical;
}

.form-group textarea:focus {
  outline: none;
  border-color: #e17055;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #e17055, #fdcb6e);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1.1em;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  letter-spacing: 2px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(225, 112, 85, 0.4);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
