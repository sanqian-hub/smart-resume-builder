<template>
  <footer class="record-footer" :class="{ 'record-footer--compact': compact }" aria-label="备案信息">
    <div class="record-footer__inner">
      <div class="record-footer__meta">
        <span class="record-footer__copyright">
          <span class="record-footer__copyright-mark">©</span>
          <span>2026 sanqian</span>
        </span>
        <span class="record-footer__dot" aria-hidden="true">/</span>
        <a
          class="record-footer__meta-link"
          href="https://github.com/sanqian-hub"
          target="_blank"
          rel="noreferrer noopener"
        >
          GitHub · sanqian
        </a>
      </div>
      <div class="record-footer__beian">
        <a
          class="record-footer__link"
          href="https://beian.miit.gov.cn/"
          target="_blank"
          rel="noreferrer noopener"
        >
          粤ICP备2026028310号-2
        </a>

        <span class="record-footer__divider" aria-hidden="true">/</span>

        <a
          class="record-footer__link record-footer__link--public-security"
          href="https://beian.mps.gov.cn/#/query/webSearch?code=44088202000096"
          target="_blank"
          rel="noreferrer noopener"
        >
          <img
            class="record-footer__icon"
            src="/beian.png"
            alt="公安网备"
            width="16"
            height="16"
          />
          <span>粤公网安备44088202000096号</span>
        </a>
      </div>
    </div>
  </footer>
</template>

<script setup>
defineProps({
  compact: {
    type: Boolean,
    default: false,
  },
})
</script>

<style scoped>
.record-footer {
  width: 100%;
  padding: 16px 0 8px;
}

.record-footer--compact {
  padding-top: 6px;
  padding-bottom: 0;
}

.record-footer__inner {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(75, 85, 99, 0.82);
}

.record-footer__meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  font-size: 12px;
  line-height: 1.6;
  color: rgba(75, 85, 99, 0.82);
  font-weight: 400;
}

.record-footer__beian {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px 10px;
}

.record-footer__copyright {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.record-footer__copyright-mark {
  font-size: 16px;
  line-height: 1;
  transform: translateY(-1px);
}

.record-footer__dot {
  color: rgba(156, 163, 175, 0.9);
}

.record-footer__meta-link {
  color: inherit;
  text-decoration: none;
  transition: color 0.15s ease;
}

.record-footer__meta-link:hover {
  color: var(--primary);
}

.record-footer__link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: inherit;
  text-decoration: none;
  transition: color 0.15s ease, opacity 0.15s ease;
}

.record-footer__link:hover {
  color: var(--primary);
}

.record-footer__link--public-security {
  white-space: nowrap;
}

.record-footer__divider {
  color: rgba(156, 163, 175, 0.9);
}

.record-footer__icon {
  flex-shrink: 0;
  opacity: 0.92;
}

@media (max-width: 768px) {
  .record-footer {
    padding-top: 12px;
  }

  .record-footer__inner {
    font-size: 11px;
  }

  .record-footer__meta {
    font-size: 11px;
    gap: 6px;
  }

  .record-footer__beian {
    gap: 6px 8px;
  }
}
</style>
