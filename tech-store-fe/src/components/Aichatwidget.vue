<template>
  <div class="aichat-root">
    <transition name="bubble-pop">
      <button v-if="!isOpen" class="aichat-bubble" @click="openChat" :class="{ 'has-pulse': !hasInteracted }" aria-label="Chat với AI">
        <span class="bubble-icon">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none">
            <path d="M12 2C6.48 2 2 6.02 2 11c0 2.5 1.06 4.76 2.77 6.38L4 22l4.89-1.56A10.1 10.1 0 0 0 12 20.98c5.52 0 10-4.02 10-8.98S17.52 2 12 2z" fill="currentColor"/>
            <circle cx="8.5" cy="11" r="1.2" fill="white"/>
            <circle cx="12" cy="11" r="1.2" fill="white"/>
            <circle cx="15.5" cy="11" r="1.2" fill="white"/>
          </svg>
        </span>
        <span v-if="!hasInteracted" class="bubble-badge">AI</span>
      </button>
    </transition>

    <transition name="panel-slide">
      <div v-if="isOpen" class="aichat-panel">
        <div class="panel-header">
          <div class="panel-header__left">
            <div class="agent-avatar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M12 2C6.48 2 2 6.02 2 11c0 2.5 1.06 4.76 2.77 6.38L4 22l4.89-1.56A10.1 10.1 0 0 0 12 20.98c5.52 0 10-4.02 10-8.98S17.52 2 12 2z" fill="white"/>
              </svg>
              <span class="agent-online"></span>
            </div>
            <div class="agent-info">
              <div class="agent-name">RetailPro AI</div>
              <div class="agent-status">{{ isTyping ? 'Đang soạn tin...' : 'Trực tuyến' }}</div>
            </div>
          </div>
          <div class="panel-header__right">
            <button class="header-btn" @click="clearChat" title="Cuộc trò chuyện mới">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M3 3v5h5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <button class="header-btn header-btn--close" @click="isOpen = false">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M18 6 6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div v-if="messages.length === 0" class="panel-welcome">
          <div class="welcome-emoji">👋</div>
          <div class="welcome-title">Xin chào{{ customerName ? ', ' + customerName : '' }}!</div>
          <div class="welcome-sub">Tôi có thể giúp gì cho bạn hôm nay?</div>
          <div class="quick-chips">
            <button v-for="q in quickQuestions" :key="q" class="chip" @click="sendQuick(q)">{{ q }}</button>
          </div>
        </div>

        <div class="panel-messages" ref="messagesEl">
          <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.role === 'user' ? 'msg-row--user' : 'msg-row--ai'">
            <div v-if="msg.role !== 'user'" class="msg-avatar">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path d="M12 2C6.48 2 2 6.02 2 11c0 2.5 1.06 4.76 2.77 6.38L4 22l4.89-1.56A10.1 10.1 0 0 0 12 20.98c5.52 0 10-4.02 10-8.98S17.52 2 12 2z" fill="white"/>
              </svg>
            </div>
            <div class="msg-bubble" v-html="formatMsg(msg.content)"></div>
          </div>

          <div v-if="isTyping" class="msg-row msg-row--ai">
            <div class="msg-avatar">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path d="M12 2C6.48 2 2 6.02 2 11c0 2.5 1.06 4.76 2.77 6.38L4 22l4.89-1.56A10.1 10.1 0 0 0 12 20.98c5.52 0 10-4.02 10-8.98S17.52 2 12 2z" fill="white"/>
              </svg>
            </div>
            <div class="msg-bubble msg-bubble--typing">
              <span></span><span></span><span></span>
            </div>
          </div>

          <div v-if="showEscalate" class="escalate-card">
            <div class="escalate-card__icon">🎧</div>
            <div class="escalate-card__text">Bạn muốn được hỗ trợ bởi nhân viên?</div>
            <button class="escalate-card__btn" @click="showEscalate = false">Để AI xử lý</button>
          </div>
        </div>

        <div class="panel-input">
          <textarea ref="inputEl" v-model="inputText" class="input-field" placeholder="Nhập tin nhắn..." rows="1"
            @keydown.enter.exact.prevent="send" @input="autoResize"></textarea>
          <button class="send-btn" :disabled="!inputText.trim() || isTyping" @click="send">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M22 2 11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="panel-footer">Powered by RetailPro AI · <span>Groq LLaMA</span></div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import http from '../api/http'

const auth = useAuthStore()
const customerName = computed(() => auth.user?.name || auth.user?.username || '')

const isOpen = ref(false)
const hasInteracted = ref(false)
const isTyping = ref(false)
const inputText = ref('')
const sessionId = ref(null)
const messages = ref([])
const showEscalate = ref(false)
const messagesEl = ref(null)
const inputEl = ref(null)

const quickQuestions = [
  '💎 Điểm thưởng của tôi',
  '🛍️ Gợi ý sản phẩm',
  '📦 Kiểm tra đơn hàng',
  '🎁 Ưu đãi VIP',
]

function openChat() {
  isOpen.value = true
  hasInteracted.value = true
  nextTick(() => inputEl.value?.focus())
}

function clearChat() {
  messages.value = []
  sessionId.value = null
  showEscalate.value = false
}

async function send() {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isTyping.value = true
  showEscalate.value = false
  await scrollBottom()
  if (inputEl.value) inputEl.value.style.height = 'auto'
  try {
    const res = await http.post('/api/chat/customer', {
      message: text,
      sessionId: sessionId.value,
    })
    const data = res.data
    sessionId.value = data.sessionId
    messages.value.push({ role: 'ai', content: data.reply })
    if (data.escalate) showEscalate.value = true
  } catch (e) {
    messages.value.push({ role: 'ai', content: 'Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.' })
  } finally {
    isTyping.value = false
    await scrollBottom()
  }
}

function sendQuick(q) {
  inputText.value = q.replace(/^[\p{Emoji}\s]+/u, '').trim()
  send()
}

function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function formatMsg(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

async function scrollBottom() {
  await nextTick()
  if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap');
.aichat-root { position: fixed; bottom: 28px; right: 28px; z-index: 9999; font-family: 'Nunito', sans-serif; }
.aichat-bubble { width: 58px; height: 58px; border-radius: 50%; background: linear-gradient(135deg, #6366f1, #4f46e5); border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; color: white; box-shadow: 0 8px 24px rgba(99,102,241,0.45); transition: transform 0.2s cubic-bezier(0.34,1.5,0.64,1), box-shadow 0.2s; position: relative; }
.aichat-bubble:hover { transform: scale(1.08); box-shadow: 0 12px 32px rgba(99,102,241,0.55); }
.aichat-bubble:active { transform: scale(0.95); }
.aichat-bubble.has-pulse::before { content: ''; position: absolute; inset: -4px; border-radius: 50%; border: 2px solid rgba(99,102,241,0.4); animation: pulse-ring 2s ease-out infinite; }
@keyframes pulse-ring { 0% { transform: scale(1); opacity: 0.6; } 100% { transform: scale(1.5); opacity: 0; } }
.bubble-icon { display: flex; align-items: center; justify-content: center; }
.bubble-badge { position: absolute; top: -4px; right: -4px; background: #ef4444; color: white; font-size: 9px; font-weight: 800; letter-spacing: 0.5px; padding: 2px 5px; border-radius: 100px; border: 2px solid white; }
.aichat-panel { width: 360px; height: 520px; background: #ffffff; border-radius: 24px; box-shadow: 0 24px 80px rgba(15,23,42,0.18), 0 4px 16px rgba(15,23,42,0.08); display: flex; flex-direction: column; overflow: hidden; border: 1px solid rgba(99,102,241,0.12); }
.panel-header { background: linear-gradient(135deg, #4f46e5, #6366f1); padding: 16px 18px; display: flex; align-items: center; justify-content: space-between; flex-shrink: 0; }
.panel-header__left { display: flex; align-items: center; gap: 12px; }
.agent-avatar { width: 38px; height: 38px; border-radius: 50%; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; position: relative; flex-shrink: 0; }
.agent-online { position: absolute; bottom: 1px; right: 1px; width: 9px; height: 9px; background: #4ade80; border-radius: 50%; border: 2px solid #4f46e5; }
.agent-name { font-size: 14px; font-weight: 800; color: white; line-height: 1.2; }
.agent-status { font-size: 11.5px; color: rgba(255,255,255,0.7); margin-top: 1px; }
.panel-header__right { display: flex; gap: 6px; }
.header-btn { width: 30px; height: 30px; border-radius: 8px; background: rgba(255,255,255,0.15); border: none; color: rgba(255,255,255,0.85); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background 0.15s; }
.header-btn:hover { background: rgba(255,255,255,0.25); }
.header-btn--close:hover { background: rgba(239,68,68,0.4); }
.panel-welcome { padding: 24px 20px 12px; text-align: center; flex-shrink: 0; }
.welcome-emoji { font-size: 36px; margin-bottom: 8px; }
.welcome-title { font-size: 16px; font-weight: 800; color: #0f172a; margin-bottom: 4px; }
.welcome-sub { font-size: 13px; color: #64748b; margin-bottom: 16px; }
.quick-chips { display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }
.chip { background: #f1f5f9; border: 1px solid rgba(99,102,241,0.15); color: #4f46e5; font-family: 'Nunito', sans-serif; font-size: 12.5px; font-weight: 700; padding: 7px 14px; border-radius: 100px; cursor: pointer; transition: background 0.15s, transform 0.15s, border-color 0.15s; white-space: nowrap; }
.chip:hover { background: #ede9fe; border-color: rgba(99,102,241,0.4); transform: translateY(-1px); }
.panel-messages { flex: 1; overflow-y: auto; padding: 12px 16px; display: flex; flex-direction: column; gap: 10px; scroll-behavior: smooth; }
.panel-messages::-webkit-scrollbar { width: 4px; }
.panel-messages::-webkit-scrollbar-track { background: transparent; }
.panel-messages::-webkit-scrollbar-thumb { background: rgba(99,102,241,0.2); border-radius: 4px; }
.msg-row { display: flex; align-items: flex-end; gap: 8px; animation: msg-in 0.3s cubic-bezier(0.34,1.3,0.64,1) both; }
@keyframes msg-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.msg-row--user { flex-direction: row-reverse; }
.msg-avatar { width: 26px; height: 26px; border-radius: 50%; background: linear-gradient(135deg, #4f46e5, #6366f1); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.msg-bubble { max-width: 78%; padding: 10px 14px; border-radius: 18px; font-size: 13.5px; line-height: 1.55; word-break: break-word; }
.msg-row--ai .msg-bubble { background: #f1f5f9; color: #1e293b; border-bottom-left-radius: 4px; }
.msg-row--user .msg-bubble { background: linear-gradient(135deg, #4f46e5, #6366f1); color: white; border-bottom-right-radius: 4px; }
.msg-bubble--typing { display: flex; align-items: center; gap: 5px; padding: 12px 16px; min-width: 56px; }
.msg-bubble--typing span { width: 7px; height: 7px; border-radius: 50%; background: #94a3b8; animation: typing-dot 1.2s ease-in-out infinite; }
.msg-bubble--typing span:nth-child(2) { animation-delay: 0.2s; }
.msg-bubble--typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing-dot { 0%, 60%, 100% { transform: translateY(0); opacity: 0.4; } 30% { transform: translateY(-5px); opacity: 1; } }
.escalate-card { background: linear-gradient(135deg, #fef3c7, #fffbeb); border: 1px solid rgba(245,158,11,0.25); border-radius: 14px; padding: 12px 16px; display: flex; align-items: center; gap: 10px; animation: msg-in 0.3s both; }
.escalate-card__icon { font-size: 20px; flex-shrink: 0; }
.escalate-card__text { font-size: 12.5px; color: #92400e; font-weight: 600; flex: 1; }
.escalate-card__btn { background: #f59e0b; color: white; border: none; font-family: 'Nunito', sans-serif; font-size: 12px; font-weight: 700; padding: 5px 12px; border-radius: 100px; cursor: pointer; white-space: nowrap; transition: background 0.15s; }
.escalate-card__btn:hover { background: #d97706; }
.panel-input { padding: 12px 14px; border-top: 1px solid rgba(15,23,42,0.07); display: flex; align-items: flex-end; gap: 10px; background: #ffffff; flex-shrink: 0; }
.input-field { flex: 1; border: 1.5px solid rgba(99,102,241,0.2); border-radius: 14px; padding: 9px 14px; font-family: 'Nunito', sans-serif; font-size: 13.5px; color: #0f172a; resize: none; outline: none; line-height: 1.5; transition: border-color 0.2s; background: #f8faff; min-height: 40px; max-height: 120px; }
.input-field::placeholder { color: #94a3b8; }
.input-field:focus { border-color: #6366f1; background: #ffffff; }
.send-btn { width: 40px; height: 40px; border-radius: 12px; background: linear-gradient(135deg, #4f46e5, #6366f1); border: none; color: white; display: flex; align-items: center; justify-content: center; cursor: pointer; flex-shrink: 0; transition: transform 0.15s, opacity 0.15s; }
.send-btn:hover:not(:disabled) { transform: scale(1.06); }
.send-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.panel-footer { text-align: center; font-size: 10.5px; color: #94a3b8; padding: 6px 0 10px; flex-shrink: 0; }
.panel-footer span { color: #6366f1; font-weight: 700; }
.bubble-pop-enter-active { transition: all 0.3s cubic-bezier(0.34,1.5,0.64,1); }
.bubble-pop-leave-active { transition: all 0.2s ease; }
.bubble-pop-enter-from, .bubble-pop-leave-to { opacity: 0; transform: scale(0.6); }
.panel-slide-enter-active { transition: all 0.35s cubic-bezier(0.34,1.3,0.64,1); }
.panel-slide-leave-active { transition: all 0.25s ease; }
.panel-slide-enter-from { opacity: 0; transform: translateY(20px) scale(0.95); }
.panel-slide-leave-to { opacity: 0; transform: translateY(16px) scale(0.97); }
@media (max-width: 480px) { .aichat-root { bottom: 16px; right: 16px; } .aichat-panel { width: calc(100vw - 32px); height: 480px; } }
</style>
