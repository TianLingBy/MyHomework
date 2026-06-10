import { ref, computed } from 'vue'
import { getCurrentUser } from '../api'

const TOKEN_KEY = 'food_token'
const USER_KEY = 'food_user'

const token = ref(localStorage.getItem(TOKEN_KEY) || '')
const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

const isLoggedIn = computed(() => !!user.value)
const isAdmin = computed(() => user.value?.isAdmin === true)

/** 初始化：页面刷新时恢复登录状态，静默刷新用户信息 */
async function initAuth() {
  if (!token.value) return
  // 先用 localStorage 缓存，保证刷新不丢状态
  // 然后静默请求后端更新最新数据
  try {
    const res = await getCurrentUser()
    user.value = res.data
    localStorage.setItem(USER_KEY, JSON.stringify(res.data))
  } catch {
    // 任何错误都不清除登录状态（包括 401、网络错误）
    // 用户需要主动退出才会清除
  }
}

/** 登录/注册成功后调用 */
function setAuth(data) {
  token.value = data.token
  user.value = { username: data.username, nickname: data.nickname, avatar: data.avatar || '', isAdmin: data.isAdmin }
  localStorage.setItem(TOKEN_KEY, data.token)
  localStorage.setItem(USER_KEY, JSON.stringify(user.value))
}

/** 退出登录 */
function logout() {
  token.value = ''
  user.value = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function useAuth() {
  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    initAuth,
    setAuth,
    logout
  }
}
