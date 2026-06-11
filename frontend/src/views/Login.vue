<template>
  <div class="login-page">
    <!-- Left Panel: Animated Characters -->
    <div class="left-panel">
      <div class="panel-brand">
        <div class="brand-icon"><img src="/logo.png" alt="logo" /></div>
        <div class="brand-copy">
          <span class="brand-text">ArxFolio</span>
          <span class="brand-subtitle">AI Resume Studio</span>
        </div>
      </div>

      <div class="stage">
        <div class="char-wrap">
          <!-- Purple tall rectangle (back) -->
          <div ref="purpleRef" class="char purple" :style="purpleBodyStyle">
            <div class="face purple-face" :style="purpleFacePos">
              <div class="eyeball" :class="{ blink: purpleBlink }" style="width:20px;height:20px;">
                <div v-if="!purpleBlink" class="pupil" :style="purplePupilStyle"></div>
              </div>
              <div class="eyeball" :class="{ blink: purpleBlink }" style="width:20px;height:20px;">
                <div v-if="!purpleBlink" class="pupil" :style="purplePupilStyle"></div>
              </div>
            </div>
          </div>

          <!-- Black tall rectangle (middle) -->
          <div ref="blackRef" class="char black" :style="blackBodyStyle">
            <div class="face black-face" :style="blackFacePos">
              <div class="eyeball" :class="{ blink: blackBlink }" style="width:16px;height:16px;">
                <div v-if="!blackBlink" class="pupil" :style="blackPupilStyle"></div>
              </div>
              <div class="eyeball" :class="{ blink: blackBlink }" style="width:16px;height:16px;">
                <div v-if="!blackBlink" class="pupil" :style="blackPupilStyle"></div>
              </div>
            </div>
          </div>

          <!-- Orange semicircle (front left) -->
          <div ref="orangeRef" class="char orange" :style="orangeBodyStyle">
            <div class="face orange-face" :style="orangeFacePos">
              <div class="dot" :style="orangePupilStyle"></div>
              <div class="dot" :style="orangePupilStyle"></div>
            </div>
          </div>

          <!-- Yellow rounded rectangle (front right) -->
          <div ref="yellowRef" class="char yellow" :style="yellowBodyStyle">
            <div class="face yellow-face" :style="yellowFacePos">
              <div class="dot" :style="yellowPupilStyle"></div>
              <div class="dot" :style="yellowPupilStyle"></div>
            </div>
            <div class="mouth" :style="yellowMouthPos"></div>
          </div>
        </div>
      </div>

      <!-- Decorative -->
      <div class="grid-bg"></div>
      <div class="blur-orb blur-orb-1"></div>
      <div class="blur-orb blur-orb-2"></div>
    </div>

    <!-- Right Panel: Login Form -->
    <div class="right-panel">
      <div class="right-panel-inner">
        <div class="form-box">
          <div class="mobile-brand">
            <div class="brand-icon-sm"><img src="/logo.png" alt="logo" /></div>
            <div class="brand-copy">
              <span class="brand-text">ArxFolio</span>
              <span class="brand-subtitle">AI Resume Studio</span>
            </div>
          </div>

          <div class="form-header">
            <h1>Welcome back!</h1>
            <p>Please enter your details</p>
          </div>

          <form @submit.prevent="handleLogin" class="auth-form">
            <div class="field">
              <label for="account">账号</label>
              <input
                id="account"
                v-model="form.userAccount"
                type="text"
                placeholder="请输入账号"
                autocomplete="username"
                @focus="isTyping = true"
                @blur="isTyping = false"
                required
              />
            </div>

            <div class="field">
              <label for="password">密码</label>
              <div class="pw-wrap">
                <input
                  id="password"
                  v-model="form.userPassword"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                  @focus="isTyping = true"
                  @blur="isTyping = false"
                  required
                />
                <button type="button" class="pw-toggle" @click="showPassword = !showPassword">
                  <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </svg>
                  <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                </button>
              </div>
            </div>

            <div class="remember-row">
              <label class="remember-check" for="rememberMe">
                <input
                  id="rememberMe"
                  v-model="form.rememberMe"
                  type="checkbox"
                />
                <span>自动登录</span>
              </label>
            </div>

            <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

            <button type="submit" class="btn-submit" :disabled="isLoading">
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
          </form>

          <p class="signup-link">
            还没有账号？<router-link :to="registerLink">立即注册</router-link>
          </p>
        </div>

        <RecordFooter compact />
      </div>
    </div>

    <Teleport to="body">
      <Transition name="toast">
        <div
          v-if="showLoginIntentToast && loginIntentHint"
          class="login-intent-toast login-intent-toast--success"
          role="status"
          aria-live="polite"
        >
          {{ loginIntentHint }}
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRuntimeLoader } from '../composables/useRuntimeLoader'
import RecordFooter from '../components/RecordFooter.vue'
import { useUserStore } from '../stores/user'
import { login } from '../api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const LOGIN_INTENT_HINTS = {
  save: '登录后即可保存当前简历',
  share: '登录后即可分享当前简历',
  'share-manage': '登录后即可管理分享记录',
  version: '登录后即可查看历史版本',
  notice: '登录后即可开启邮件通知',
  'ai-chat': '登录后即可使用智能助手',
  'ai-match': '登录后即可使用岗位匹配',
  'ai-self-intro': '登录后即可生成自我介绍',
  'ai-score': '登录后即可使用简历打分',
  'ai-proofread': '登录后即可使用语法纠错',
}
const registerLink = computed(() => ({
  path: '/register',
  query: route.query.redirect ? { redirect: route.query.redirect } : {},
}))
const loginIntentHint = computed(() => {
  const intent = typeof route.query.intent === 'string' ? route.query.intent : ''
  return LOGIN_INTENT_HINTS[intent] || ''
})
const showLoginIntentToast = ref(false)
let loginIntentToastTimer = null

const form = reactive({ userAccount: '', userPassword: '', rememberMe: false })
const showPassword = ref(false)
const errorMsg = ref('')
const isLoading = ref(false)
const LOGIN_MIN_LOADING_MS = 800
const { show: showRuntimeLoader, hide: hideRuntimeLoader } = useRuntimeLoader()

const isTyping = ref(false)
const isLookingAtEachOther = ref(false)
const purpleBlink = ref(false)
const blackBlink = ref(false)
const isPurplePeeking = ref(false)
const isBlackPeeking = ref(false)
const isOrangePeeking = ref(false)
const isYellowPeeking = ref(false)

const mouseX = ref(0)
const mouseY = ref(0)

const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

// Mouse
function onMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}
onMounted(() => window.addEventListener('mousemove', onMouseMove))
onUnmounted(() => window.removeEventListener('mousemove', onMouseMove))

// Blinking
const blinkTimers = []
function startBlink(setter) {
  const schedule = () => {
    const id = setTimeout(() => {
      setter(true)
      const id2 = setTimeout(() => { setter(false); schedule() }, 150)
      blinkTimers.push(id2)
    }, Math.random() * 4000 + 3000)
    blinkTimers.push(id)
  }
  schedule()
}
onMounted(() => {
  startBlink(v => purpleBlink.value = v)
  startBlink(v => blackBlink.value = v)
})
onUnmounted(() => blinkTimers.forEach(clearTimeout))

// Looking at each other when typing
let lookTimer = null
watch(isTyping, val => {
  clearTimeout(lookTimer)
  if (val) {
    isLookingAtEachOther.value = true
    lookTimer = setTimeout(() => { isLookingAtEachOther.value = false }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})
onUnmounted(() => clearTimeout(lookTimer))

// Purple sneaky peek when password visible
let peekTimer = null
function schedulePeek() {
  peekTimer = setTimeout(() => {
    isPurplePeeking.value = true
    setTimeout(() => {
      isPurplePeeking.value = false
      if (form.userPassword.length > 0 && showPassword.value) schedulePeek()
    }, 800)
  }, Math.random() * 3000 + 2000)
}
watch([() => form.userPassword, showPassword], () => {
  clearTimeout(peekTimer)
  isPurplePeeking.value = false
  if (form.userPassword.length > 0 && showPassword.value) schedulePeek()
})
onUnmounted(() => clearTimeout(peekTimer))

// Black sneaky peek when password visible
let blackPeekTimer = null
function scheduleBlackPeek() {
  blackPeekTimer = setTimeout(() => {
    isBlackPeeking.value = true
    setTimeout(() => {
      isBlackPeeking.value = false
      if (form.userPassword.length > 0 && showPassword.value) scheduleBlackPeek()
    }, 600)
  }, Math.random() * 3500 + 2500)
}
watch([() => form.userPassword, showPassword], () => {
  clearTimeout(blackPeekTimer)
  isBlackPeeking.value = false
  if (form.userPassword.length > 0 && showPassword.value) scheduleBlackPeek()
})
onUnmounted(() => clearTimeout(blackPeekTimer))

// Orange peek when password visible
let orangePeekTimer = null
function scheduleOrangePeek() {
  orangePeekTimer = setTimeout(() => {
    isOrangePeeking.value = true
    setTimeout(() => {
      isOrangePeeking.value = false
      if (form.userPassword.length > 0 && showPassword.value) scheduleOrangePeek()
    }, 700)
  }, Math.random() * 4000 + 3000)
}
watch([() => form.userPassword, showPassword], () => {
  clearTimeout(orangePeekTimer)
  isOrangePeeking.value = false
  if (form.userPassword.length > 0 && showPassword.value) scheduleOrangePeek()
})
onUnmounted(() => clearTimeout(orangePeekTimer))

// Yellow peek when password visible
let yellowPeekTimer = null
function scheduleYellowPeek() {
  yellowPeekTimer = setTimeout(() => {
    isYellowPeeking.value = true
    setTimeout(() => {
      isYellowPeeking.value = false
      if (form.userPassword.length > 0 && showPassword.value) scheduleYellowPeek()
    }, 650)
  }, Math.random() * 4500 + 3500)
}
watch([() => form.userPassword, showPassword], () => {
  clearTimeout(yellowPeekTimer)
  isYellowPeeking.value = false
  if (form.userPassword.length > 0 && showPassword.value) scheduleYellowPeek()
})
onUnmounted(() => clearTimeout(yellowPeekTimer))

watch(loginIntentHint, hint => {
  clearTimeout(loginIntentToastTimer)
  if (!hint) {
    showLoginIntentToast.value = false
    return
  }
  showLoginIntentToast.value = true
  loginIntentToastTimer = setTimeout(() => {
    showLoginIntentToast.value = false
  }, 2000)
}, { immediate: true })
onUnmounted(() => clearTimeout(loginIntentToastTimer))

// Position helpers
function charPos(cRef) {
  if (!cRef.value) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const r = cRef.value.getBoundingClientRect()
  const dx = mouseX.value - (r.left + r.width / 2)
  const dy = mouseY.value - (r.top + r.height / 3)
  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-6, Math.min(6, -dx / 120)),
  }
}

function pupilFromChar(cRef, maxD = 5) {
  if (!cRef.value) return { x: 0, y: 0 }
  const r = cRef.value.getBoundingClientRect()
  const dx = mouseX.value - (r.left + r.width / 2)
  const dy = mouseY.value - (r.top + r.height / 4)
  const dist = Math.min(Math.hypot(dx, dy), maxD)
  const a = Math.atan2(dy, dx)
  return { x: Math.cos(a) * dist, y: Math.sin(a) * dist }
}

// Computed positions
const pp = computed(() => charPos(purpleRef))
const bp = computed(() => charPos(blackRef))
const yp = computed(() => charPos(yellowRef))
const op = computed(() => charPos(orangeRef))

const po = computed(() => pupilFromChar(purpleRef, 5))
const bo = computed(() => pupilFromChar(blackRef, 4))
const yo = computed(() => pupilFromChar(yellowRef, 5))
const oo = computed(() => pupilFromChar(orangeRef, 5))

// Derived
const pwdShown = computed(() => form.userPassword.length > 0 && showPassword.value)
const pwdHidden = computed(() => form.userPassword.length > 0 && !showPassword.value)
const active = computed(() => isTyping.value || pwdHidden.value)
const hasError = computed(() => errorMsg.value.length > 0)

// --- Character body styles ---

const purpleBodyStyle = computed(() => {
  if (pwdShown.value) return { transform: 'skewX(0deg)' }
  if (active.value) return {
    height: '440px',
    transform: `skewX(${(pp.value.bodySkew || 0) - 12}deg) translateX(40px)`,
  }
  return { height: '400px', transform: `skewX(${pp.value.bodySkew || 0}deg)` }
})

const purpleFacePos = computed(() => {
  if (pwdShown.value) return { left: '20px', top: '35px' }
  if (isLookingAtEachOther.value) return { left: '55px', top: '65px' }
  return { left: `${45 + pp.value.faceX}px`, top: `${40 + pp.value.faceY}px` }
})

const purplePupilStyle = computed(() => {
  if (hasError.value) return { transform: 'translate(0px, 2px)' }
  if (pwdShown.value) return { transform: `translate(${isPurplePeeking.value ? 4 : -4}px, ${isPurplePeeking.value ? 5 : -4}px)` }
  if (isLookingAtEachOther.value) return { transform: 'translate(3px, 4px)' }
  return { transform: `translate(${po.value.x}px, ${po.value.y}px)` }
})

const blackBodyStyle = computed(() => {
  if (pwdShown.value) {
    if (isBlackPeeking.value) return {
      height: '340px',
      transform: `skewX(${(bp.value.bodySkew || 0) * 0.5}deg) translateX(-8px)`,
    }
    return { transform: 'skewX(0deg)' }
  }
  if (isLookingAtEachOther.value) return {
    height: '350px',
    transform: `skewX(${(bp.value.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`,
  }
  if (active.value) return {
    height: '350px',
    transform: `skewX(${(bp.value.bodySkew || 0) * 1.5 - 8}deg) translateX(15px)`,
  }
  return { height: '310px', transform: `skewX(${bp.value.bodySkew || 0}deg)` }
})

const blackFacePos = computed(() => {
  if (pwdShown.value && isBlackPeeking.value) return { left: '38px', top: '8px' }
  if (pwdShown.value) return { left: '10px', top: '28px' }
  if (isLookingAtEachOther.value) return { left: '32px', top: '12px' }
  return { left: `${26 + bp.value.faceX}px`, top: `${32 + bp.value.faceY}px` }
})

const blackPupilStyle = computed(() => {
  if (hasError.value) return { transform: 'translate(0px, 2px)' }
  if (pwdShown.value) return { transform: `translate(${isBlackPeeking.value ? 4 : -4}px, ${isBlackPeeking.value ? 4 : -4}px)` }
  if (isLookingAtEachOther.value) return { transform: 'translate(0px, -4px)' }
  return { transform: `translate(${bo.value.x}px, ${bo.value.y}px)` }
})

const orangeBodyStyle = computed(() => {
  if (hasError.value) return { transform: 'translateY(8px)' }
  if (pwdShown.value) {
    if (isOrangePeeking.value) return { transform: 'translateY(-12px)' }
    return { transform: 'skewX(0deg)' }
  }
  if (isLookingAtEachOther.value) return { transform: `skewX(${op.value.bodySkew || 0}deg) translateX(10px)` }
  if (active.value) return { transform: `skewX(${op.value.bodySkew || 0}deg) translateX(-12px)` }
  return { transform: `skewX(${op.value.bodySkew || 0}deg)` }
})

const orangeFacePos = computed(() => {
  if (pwdShown.value && isOrangePeeking.value) return { left: '95px', top: '75px' }
  if (pwdShown.value) return { left: '50px', top: '85px' }
  if (isLookingAtEachOther.value) return { left: '95px', top: '80px' }
  return { left: `${82 + op.value.faceX}px`, top: `${90 + op.value.faceY}px` }
})

const orangePupilStyle = computed(() => {
  if (hasError.value) return { transform: 'translate(0px, 2px)' }
  if (pwdShown.value) return { transform: `translate(${isOrangePeeking.value ? 4 : -5}px, ${isOrangePeeking.value ? -3 : -4}px)` }
  if (isLookingAtEachOther.value) return { transform: 'translate(-3px, -2px)' }
  return { transform: `translate(${oo.value.x}px, ${oo.value.y}px)` }
})

const yellowBodyStyle = computed(() => {
  if (hasError.value) return { transform: 'translateY(6px)' }
  if (pwdShown.value) {
    if (isYellowPeeking.value) return { transform: 'skewX(4deg) scaleX(1.04)' }
    return { transform: 'skewX(0deg)' }
  }
  if (isLookingAtEachOther.value) return { transform: `skewX(${yp.value.bodySkew || 0}deg) translateX(8px)` }
  if (active.value) return { transform: `skewX(${yp.value.bodySkew || 0}deg) translateX(12px)` }
  return { transform: `skewX(${yp.value.bodySkew || 0}deg)` }
})

const yellowFacePos = computed(() => {
  if (pwdShown.value && isYellowPeeking.value) return { left: '30px', top: '25px' }
  if (pwdShown.value) return { left: '20px', top: '35px' }
  if (isLookingAtEachOther.value) return { left: '65px', top: '30px' }
  return { left: `${52 + yp.value.faceX}px`, top: `${40 + yp.value.faceY}px` }
})

const yellowPupilStyle = computed(() => {
  if (hasError.value) return { transform: 'translate(0px, 2px)' }
  if (pwdShown.value) return { transform: `translate(${isYellowPeeking.value ? 3 : -5}px, ${isYellowPeeking.value ? -3 : -4}px)` }
  if (isLookingAtEachOther.value) return { transform: 'translate(-2px, -3px)' }
  return { transform: `translate(${yo.value.x}px, ${yo.value.y}px)` }
})

const yellowMouthPos = computed(() => {
  if (hasError.value) return { left: '38px', top: '92px', transform: 'rotate(-25deg)' }
  if (pwdShown.value && isYellowPeeking.value) return { left: '18px', top: '80px', transform: 'rotate(0deg)' }
  if (pwdShown.value) return { left: '10px', top: '88px' }
  return { left: `${40 + yp.value.faceX}px`, top: `${88 + yp.value.faceY}px` }
})

// Login
async function handleLogin() {
  errorMsg.value = ''
  isLoading.value = true
  const startAt = Date.now()
  try {
    const data = await login(form)
    await ensureMinLoading(startAt)
    userStore.setUser(data)
    showRuntimeLoader('正在进入首页...')
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.trim()
      ? route.query.redirect
      : '/'
    router.push(redirect)
  } catch (e) {
    await ensureMinLoading(startAt)
    hideRuntimeLoader()
    errorMsg.value = e.message
  } finally {
    isLoading.value = false
  }
}

async function ensureMinLoading(startAt) {
  const elapsed = Date.now() - startAt
  const remain = LOGIN_MIN_LOADING_MS - elapsed
  if (remain > 0) {
    await new Promise(resolve => setTimeout(resolve, remain))
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

/* ====== Left Panel ====== */
.left-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  background: linear-gradient(135deg, rgba(70, 114, 242, 0.9), rgb(70, 114, 242), rgba(70, 114, 242, 0.8));
  color: #fff;
  overflow: hidden;
}

.panel-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 32px;
  font-weight: 600;
  position: relative;
  z-index: 20;
}

.brand-icon {
  width: 58px;
  height: 58px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-icon img {
  width: 42px;
  height: 42px;
  object-fit: contain;
  object-position: center;
  display: block;
  transform: translate(1px, -1.5px);
}

.brand-text {
  font-family: 'Chillax', sans-serif;
  font-size: 32px;
  letter-spacing: -0.5px;
  transform: translateY(2px);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0;
}

.brand-subtitle {
  font-family: 'Chillax', sans-serif;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
  transform: translateY(-2px);
}

/* Stage */
.stage {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.char-wrap {
  position: relative;
  width: 550px;
  height: 450px;
}

/* Characters */
.char {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  transform-origin: bottom center;
}

.char.purple {
  left: 70px;
  width: 180px;
  height: 400px;
  background: #6C3FF5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}

.char.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background: #2D2D2D;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

.char.orange {
  left: 0;
  width: 240px;
  height: 200px;
  background: #FF9B6B;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}

.char.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background: #E8D754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}

/* Face container */
.face {
  position: absolute;
  display: flex;
  transition: all 0.7s ease-in-out;
}

.purple-face { gap: 32px; }
.black-face { gap: 24px; }
.orange-face { gap: 32px; }
.yellow-face { gap: 24px; }

/* Eyeball (white circle with pupil) */
.eyeball {
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.15s ease;
}

.eyeball.blink {
  height: 2px !important;
}

.pupil {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2D2D2D;
  transition: transform 0.1s ease-out;
}

/* Dot pupils (orange & yellow - no white eyeball) */
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #2D2D2D;
  transition: transform 0.2s ease-out;
}

/* Mouth line on yellow */
.mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  background: #2D2D2D;
  border-radius: 2px;
  transition: all 0.7s ease-in-out;
}

/* Decorative */
.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
}

.blur-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.blur-orb-1 {
  top: 25%;
  right: 25%;
  width: 256px;
  height: 256px;
  background: rgba(255, 255, 255, 0.1);
  filter: blur(64px);
}

.blur-orb-2 {
  bottom: 25%;
  left: 25%;
  width: 384px;
  height: 384px;
  background: rgba(255, 255, 255, 0.05);
  filter: blur(64px);
}

/* ====== Right Panel ====== */
.right-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: var(--bg-page, #fff);
}

.right-panel-inner {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  gap: 24px;
}

.form-box {
  width: 100%;
}

.mobile-brand {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 48px;
}

.brand-icon-sm {
  width: 32px;
  height: 32px;
  border-radius: 7px;
  background: rgba(70, 114, 242, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-icon-sm img {
  width: 22px;
  height: 22px;
  object-fit: contain;
  object-position: center;
  display: block;
  transform: translate(0.5px, -1px);
}

.mobile-brand .brand-text {
  transform: translateY(1px);
}

.mobile-brand .brand-copy {
  gap: 0;
}

.mobile-brand .brand-subtitle {
  font-size: 11px;
  color: rgba(15, 23, 42, 0.56);
  transform: translateY(-1px);
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h1 {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-1, #111);
  letter-spacing: -0.3px;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: var(--text-3, #888);
}

.login-intent-toast {
  position: fixed;
  top: 120px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10020;
  padding: 10px 28px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  line-height: 1;
  box-shadow: var(--shadow-md);
  white-space: nowrap;
}

.login-intent-toast--success {
  background: var(--success);
}

.toast-enter-active {
  transition: all 0.25s ease;
}

.toast-leave-active {
  transition: all 0.18s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

/* Form */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-2, #444);
}

.field input {
  width: 100%;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.25rem;
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 8px;
  background: var(--bg-page, #fff);
  color: var(--text-1, #0f172a);
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.field input:focus {
  border-color: rgb(70, 114, 242);
  box-shadow: 0 0 0 2px rgba(70, 114, 242, 0.15);
}

.field input::placeholder {
  color: var(--text-3, #aaa);
}

.pw-wrap {
  position: relative;
}

.pw-wrap input {
  padding-right: 44px;
}

.pw-toggle {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-3, #888);
  transition: color 0.15s;
}

.pw-toggle:hover {
  color: var(--text-1, #333);
}

.error-msg {
  font-size: 13px;
  color: #f87171;
  background: rgba(239, 68, 68, 0.08);
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.remember-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-top: -4px;
  padding-left: 6px;
}

.remember-check {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-2, #444);
  font-size: 14px;
  font-weight: 500;
}

.remember-check input[type='checkbox'] {
  width: 16px;
  height: 16px;
  accent-color: rgb(70, 114, 242);
  cursor: pointer;
}

.btn-submit {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  background: rgb(70, 114, 242);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: rgba(70, 114, 242, 0.9);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(70, 114, 242, 0.3);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: none;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.signup-link {
  text-align: center;
  margin-top: 32px;
  font-size: 14px;
  color: var(--text-3, #888);
}

.signup-link a {
  color: rgb(70, 114, 242);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.15s;
}

.signup-link a:hover {
  color: rgba(70, 114, 242, 0.8);
}

/* ====== Responsive ====== */
@media (max-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .left-panel {
    display: none;
  }

  .mobile-brand {
    display: flex;
  }

  .right-panel {
    min-height: 100vh;
  }
}
</style>
