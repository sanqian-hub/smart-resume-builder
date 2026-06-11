<template>
  <div class="module-selector" :class="{ collapsed: isCollapsed }">
    <!-- 顶部固定区域：标题 + 折叠按钮 -->
    <div class="selector-top">
      <span v-if="!isCollapsed" class="selector-header-text">模块选择</span>
      <button
        class="collapse-btn"
        @click="handleCollapse"
        :aria-label="isCollapsed ? '展开模块选择' : '收起模块选择'"
      >
        <ChevronLeft v-if="!isCollapsed" :size="14" />
        <ChevronRight v-else :size="14" />
        <span class="collapse-btn-text">{{ isCollapsed ? '展开' : '收起' }}</span>
      </button>
    </div>

    <div class="module-list">
      <div
        v-for="(mod, index) in modelValue"
        :key="mod.key"
        class="module-item"
        :class="{ enabled: mod.enabled, locked: mod.key === 'basic' }"
      >
        <div
          class="module-item-main"
          :class="{ disabled: mod.key === 'basic' }"
          :title="isCollapsed ? mod.label : ''"
          @click="mod.key !== 'basic' && toggle(mod.key)"
        >
          <component :is="getIcon(mod.key)" :size="20" class="module-icon" />
          <span v-show="!isCollapsed" class="module-label">{{ mod.label }}</span>
        </div>
        <button
          class="module-toggle"
          :class="{ on: mod.enabled }"
          @click.stop="mod.key !== 'basic' && toggle(mod.key)"
          :disabled="mod.key === 'basic'"
          :aria-label="(mod.enabled ? '禁用' : '启用') + ' ' + mod.label"
        >
          <span class="toggle-thumb" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import {
  User, GraduationCap, Briefcase, Wrench, FolderKanban,
  Star, Trophy, Image, MoreHorizontal,
  ChevronLeft, ChevronRight, LayoutGrid,
} from 'lucide-vue-next'

const props = defineProps({ modelValue: { type: Array, default: () => [] } })
const emit = defineEmits(['update:modelValue'])

const isCollapsed = ref(false)
const userCollapsed = ref(false)
let resizeHandler = null

const COLLAPSE_BREAKPOINT = 1100

function onResize() {
  if (userCollapsed.value) return
  isCollapsed.value = window.innerWidth < COLLAPSE_BREAKPOINT
}

function handleCollapse() {
  userCollapsed.value = !isCollapsed.value
  isCollapsed.value = !isCollapsed.value
}

onMounted(() => {
  onResize()
  resizeHandler = onResize
  window.addEventListener('resize', resizeHandler)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
})

const iconMap = {
  basic: User,
  education: GraduationCap,
  experience: Briefcase,
  skill: Wrench,
  project: FolderKanban,
    personalStrengths: Star,
  award: Trophy,
  portfolio: Image,
  other: MoreHorizontal,
}

function getIcon(key) {
  return iconMap[key] || LayoutGrid
}

function toggle(key) {
  emit('update:modelValue', props.modelValue.map(m => m.key === key ? { ...m, enabled: !m.enabled } : { ...m }))
}

function remove(key) {
  emit('update:modelValue', props.modelValue.filter(m => m.key !== key))
}
</script>

<style scoped>
.module-selector {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  width: 220px;
}
.module-selector.collapsed {
  width: 92px;
}

/* 顶部固定区域 */
.selector-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 10px 6px;
  flex-shrink: 0;
}
.selector-header-text {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-2);
  letter-spacing: 0.5px;
}
.collapse-btn {
  height: 28px;
  min-width: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  color: var(--text-1);
  padding: 0 9px;
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  transition: all 0.15s ease;
  flex-shrink: 0;
}
.collapse-btn svg {
  flex-shrink: 0;
  display: block;
}
.collapse-btn:hover {
  border-color: var(--text-3);
}

.collapse-btn-text {
  line-height: 1;
  white-space: nowrap;
}

.module-list {
  flex: 1;
  overflow-y: auto;
  padding: 2px 6px;
}

/* 每一行：固定布局，三列始终存在 */
.module-item {
  display: flex;
  align-items: center;
  padding: 0 2px;
  border-radius: 6px;
  transition: background 0.15s ease;
}
.module-item:hover { background: var(--bg-page); }
.module-item.locked:hover { background: transparent; }
.module-item.enabled .module-icon { color: var(--primary); }
.module-item + .module-item {
  margin-top: 1px;
  padding-top: 1px;
  border-top: 1px solid var(--border);
  border-radius: 0 0 6px 6px;
}

.module-item-main {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 8px;
  cursor: pointer;
  flex: 1;
  min-width: 0;
}
.module-icon {
  flex-shrink: 0;
  width: 18px;
  text-align: center;
  color: var(--text-3);
  transition: color 0.15s ease;
}
.module-item-main:hover .module-icon { color: var(--primary); }
.module-item-main.disabled { cursor: not-allowed; }

.module-label {
  font-size: 15px;
  color: var(--text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Toggle 开关 — 始终在最右侧对齐 */
.module-toggle {
  flex-shrink: 0;
  width: 36px;
  height: 20px;
  border-radius: 10px;
  border: none;
  background: var(--border);
  cursor: pointer;
  position: relative;
  transition: background 0.2s ease;
  padding: 0;
  align-self: center;
}
.module-toggle.on {
  background: var(--primary);
}
.module-toggle:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,0.15);
  transition: transform 0.2s ease;
}
.module-toggle.on .toggle-thumb {
  transform: translateX(16px);
}

/* 操作按钮 — 用 width 收起，不用 opacity */
.module-item-actions {
  display: flex;
  gap: 1px;
  flex-shrink: 0;
  width: 0;
  overflow: hidden;
  transition: width 0.15s ease;
  align-items: center;
}
.module-item:hover .module-item-actions {
  width: auto;
}

.btn-action {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-3);
  border-radius: 4px;
  transition: all 0.15s ease;
  flex-shrink: 0;
}
.btn-action:hover { color: var(--text-1); background: rgba(0,0,0,0.04); }
.btn-del:hover { color: var(--danger); background: #fef2f2; }

/* 折叠态 */
.collapsed .module-item-main {
  justify-content: center;
  padding: 9px 4px;
}
.collapsed .module-item-actions {
  display: none;
}
.collapsed .selector-top {
  justify-content: center;
  padding: 10px 4px 6px;
}
.collapsed .add-custom-wrap {
  padding: 4px;
}

@media (max-width: 1200px) {
  .collapse-btn {
    min-width: 64px;
    padding: 0 8px;
  }
}
</style>
