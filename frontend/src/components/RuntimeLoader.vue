<template>
  <Transition name="runtime-loader-fade">
    <div v-if="visible" id="app-runtime-loader" class="runtime-loader" aria-live="polite" aria-busy="true">
      <div class="runtime-loader__inner">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <path d="M12 2v4" />
          <path d="M12 18v4" />
          <path d="M4.93 4.93l2.83 2.83" />
          <path d="M16.24 16.24l2.83 2.83" />
          <path d="M2 12h4" />
          <path d="M18 12h4" />
          <path d="M4.93 19.07l2.83-2.83" />
          <path d="M16.24 7.76l2.83-2.83" />
        </svg>
        <p>{{ message }}</p>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useRuntimeLoader } from '../composables/useRuntimeLoader'

const { visible, message } = useRuntimeLoader()
</script>

<style scoped>
.runtime-loader {
  position: fixed;
  inset: 0;
  z-index: 100000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(243, 244, 246, 0.96);
  backdrop-filter: blur(2px);
}

.runtime-loader__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  transform: translateY(-60px);
  color: #4f72f6;
}

.runtime-loader__inner svg {
  animation: runtime-loader-spin 1s linear infinite;
}

.runtime-loader__inner p {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
}

.runtime-loader-fade-enter-active,
.runtime-loader-fade-leave-active {
  transition: opacity 0.2s ease;
}

.runtime-loader-fade-enter-from,
.runtime-loader-fade-leave-to {
  opacity: 0;
}

@keyframes runtime-loader-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
