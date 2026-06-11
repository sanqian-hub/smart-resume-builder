<template>
  <div class="dual-two" :style="themeVars">
    <div class="dual-two__ornament dual-two__ornament--right-half" aria-hidden="true">
      <svg viewBox="0 0 544 544" class="dual-two__ornament-svg" preserveAspectRatio="none">
        <path
          class="dual-two__ornament-line dual-two__ornament-line--thin"
          d="M 192 12
             Q 222 36, 252 17"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--thick"
          d="M 122 46
             Q 197 92, 252 52"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--medium"
          d="M 72 87
             Q 167 162, 252 122"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--wide"
          d="M 22 165
             Q 245 229, 17 367"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--fifth-main"
          d="M 252 222
             C 232 224, 176 246, 214 322
             C 224 346, 228 366, 218 382
             C 212 406, 194 432, 160 454
             C 142 444, 122 416, 102 394
             S 58 392, 42 417"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--sixth"
          d="M 252 472
             C 220 474, 194 506, 207 540"
        />
      </svg>
    </div>
    <div class="dual-two__ornament dual-two__ornament--left-half" aria-hidden="true">
      <svg viewBox="0 0 544 544" class="dual-two__ornament-svg" preserveAspectRatio="none">
        <g transform="translate(544 544) scale(-1 -1)">
        <path
          class="dual-two__ornament-line dual-two__ornament-line--thin"
          d="M 192 12
             Q 222 36, 252 17"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--thick"
          d="M 122 46
             Q 197 92, 252 52"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--medium"
          d="M 72 87
             Q 167 162, 252 122"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--wide"
          d="M 22 165
             Q 245 229, 17 367"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--fifth-main"
          d="M 252 222
             C 232 224, 176 246, 214 322
             C 224 346, 228 366, 218 382
             C 212 406, 194 432, 160 454
             C 142 444, 122 416, 102 394
             S 58 392, 42 417"
        />
        <path
          class="dual-two__ornament-line dual-two__ornament-line--sixth"
          d="M 252 472
             C 220 474, 194 506, 207 540"
        />
        </g>
      </svg>
    </div>
    <header
      class="dual-two__hero"
      :class="{
        'highlight-module': highlightModule === 'basic',
        'dual-two__hero--avatar-left': basic?.avatarAlign,
      }"
    >
      <div class="dual-two__hero-main" :class="{ 'dual-two__hero-main--right': basic?.avatarAlign }">
        <div class="dual-two__name"><ProofreadInlineText :text="basic?.name || '您的姓名'" module-type="basic" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
        <div class="dual-two__job"><ProofreadInlineText :text="basic?.jobTitle || '求职方向'" module-type="basic" field-path="jobTitle" :proofread-highlights="proofreadHighlights" /></div>
        <div v-if="heroContacts.length" class="dual-two__contact-row">
          <div v-for="item in heroContacts" :key="item.label" class="dual-two__contact-chip">
            <component :is="item.icon" :size="11.5" :stroke-width="2.2" />
            <span>
              <ProofreadInlineText
                :text="item.displayValue || item.value"
                module-type="basic"
                :field-path="item.fieldPath"
                :proofread-highlights="proofreadHighlights"
              />
            </span>
          </div>
        </div>
        <div v-if="heroPrimaryMeta.length" class="dual-two__meta-block">
          <div class="dual-two__meta-row">
            <div v-for="item in heroPrimaryMeta" :key="item.label" class="dual-two__meta-item">
              <component :is="item.icon" :size="11.5" :stroke-width="2.2" class="dual-two__meta-icon" />
              <span class="dual-two__meta-value">
                <ProofreadInlineText
                  :text="item.displayValue || item.value"
                  module-type="basic"
                  :field-path="item.fieldPath"
                  :proofread-highlights="proofreadHighlights"
                />
              </span>
            </div>
          </div>
        </div>
        <div v-if="heroSecondaryMeta.length" class="dual-two__meta-block">
          <div class="dual-two__meta-row">
            <div v-for="item in heroSecondaryMeta" :key="item.label" class="dual-two__meta-item">
              <component :is="item.icon" :size="11.5" :stroke-width="2.2" class="dual-two__meta-icon" />
              <span class="dual-two__meta-value">
                <ProofreadInlineText
                  :text="item.displayValue || item.value"
                  module-type="basic"
                  :field-path="item.fieldPath"
                  :proofread-highlights="proofreadHighlights"
                />
              </span>
            </div>
          </div>
        </div>
        <div v-if="heroTertiaryMeta.length" class="dual-two__meta-block">
          <div class="dual-two__meta-row">
            <div v-for="item in heroTertiaryMeta" :key="item.label" class="dual-two__meta-item">
              <component :is="item.icon" :size="11.5" :stroke-width="2.2" class="dual-two__meta-icon" />
              <span class="dual-two__meta-value">
                <ProofreadInlineText
                  :text="item.displayValue || item.value"
                  module-type="basic"
                  :field-path="item.fieldPath"
                  :proofread-highlights="proofreadHighlights"
                />
              </span>
            </div>
          </div>
        </div>
      </div>
      <div v-if="basic?.avatar" class="dual-two__avatar-wrap" :class="{ 'dual-two__avatar-wrap--left': basic?.avatarAlign }">
        <img :src="basic.avatar" alt="avatar" class="dual-two__avatar" />
      </div>
    </header>

    <div class="dual-two__body">
      <main class="dual-two__primary">
        <section v-if="experience.length" class="dual-two__section" :class="{ 'highlight-module': highlightModule === 'experience' }">
          <h2 class="dual-two__section-title">工作经历</h2>
          <article v-for="(item, index) in experience" :key="'exp' + index" class="dual-two__entry">
            <div class="dual-two__entry-head">
              <div>
                <div class="dual-two__entry-name"><ProofreadInlineText :text="item.company" module-type="experience" :item-index="index" field-path="company" :proofread-highlights="proofreadHighlights" /></div>
                <div class="dual-two__entry-role">{{ [item.position, item.department].filter(Boolean).join(' · ') }}</div>
              </div>
              <div v-if="item.startDate || item.city" class="dual-two__entry-side">
                <div v-if="item.startDate" class="dual-two__entry-date">
                  {{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}
                </div>
                <div v-if="item.city" class="dual-two__entry-city">{{ item.city }}</div>
              </div>
            </div>
            <ProofreadRichHtml
              v-if="item.content"
              class="dual-two__rich"
              :style="richStyle"
              :html="item.content"
              module-type="experience"
              :item-index="index"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </article>
        </section>

        <section v-if="project.length" class="dual-two__section" :class="{ 'highlight-module': highlightModule === 'project' }">
          <h2 class="dual-two__section-title">项目经历</h2>
          <article v-for="(item, index) in project" :key="'project' + index" class="dual-two__entry">
            <div class="dual-two__entry-head">
              <div>
                <div class="dual-two__entry-name"><ProofreadInlineText :text="item.name" module-type="project" :item-index="index" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
                <div class="dual-two__entry-role"><ProofreadInlineText :text="item.role" module-type="project" :item-index="index" field-path="role" :proofread-highlights="proofreadHighlights" /></div>
              </div>
              <div v-if="item.startDate || item.city" class="dual-two__entry-side">
                <div v-if="item.startDate" class="dual-two__entry-date">
                  {{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}
                </div>
                <div v-if="item.city" class="dual-two__entry-city">{{ item.city }}</div>
              </div>
            </div>
            <a v-if="item.link" :href="item.link" target="_blank" rel="noreferrer" class="dual-two__inline-link">
              {{ item.link }}
            </a>
            <ProofreadRichHtml
              v-if="item.content"
              class="dual-two__rich"
              :style="richStyle"
              :html="item.content"
              module-type="project"
              :item-index="index"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </article>
        </section>

        <section v-if="portfolio.length" class="dual-two__section" :class="{ 'highlight-module': highlightModule === 'portfolio' }">
          <h2 class="dual-two__section-title">个人作品</h2>
          <article v-for="(item, index) in portfolio" :key="'portfolio' + index" class="dual-two__entry">
            <div class="dual-two__portfolio-card">
              <div class="dual-two__portfolio-main">
                <div v-if="item.name || item.link" class="dual-two__portfolio-top">
                  <div v-if="item.name" class="dual-two__entry-name"><ProofreadInlineText :text="item.name" module-type="portfolio" :item-index="index" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
                  <a v-if="item.link" :href="item.link" target="_blank" rel="noreferrer" class="dual-two__inline-link">
                    {{ item.link }}
                  </a>
                </div>
                <ProofreadRichHtml
                  v-if="item.content"
                  class="dual-two__rich"
                  :style="richStyle"
                  :html="item.content"
                  module-type="portfolio"
                  :item-index="index"
                  field-path="content"
                  :proofread-highlights="proofreadHighlights"
                />
              </div>
            </div>
          </article>
        </section>

        <section v-if="other.length" class="dual-two__section" :class="{ 'highlight-module': highlightModule === 'other' }">
          <h2 class="dual-two__section-title">其他经历</h2>
          <article v-for="(item, index) in other" :key="'other' + index" class="dual-two__entry">
            <div class="dual-two__entry-head">
              <div>
                <div class="dual-two__entry-name"><ProofreadInlineText :text="item.name" module-type="other" :item-index="index" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
                <div class="dual-two__entry-role">{{ [item.role, item.department].filter(Boolean).join(' · ') }}</div>
              </div>
              <div v-if="item.startDate || item.city" class="dual-two__entry-side">
                <div v-if="item.startDate" class="dual-two__entry-date">
                  {{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}
                </div>
                <div v-if="item.city" class="dual-two__entry-city">{{ item.city }}</div>
              </div>
            </div>
            <ProofreadRichHtml
              v-if="item.content"
              class="dual-two__rich dual-two__rich--other"
              :style="richStyle"
              :html="item.content"
              module-type="other"
              :item-index="index"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </article>
        </section>
      </main>

      <aside class="dual-two__secondary">
        <section v-if="education.length" class="dual-two__section dual-two__section--side" :class="{ 'highlight-module': highlightModule === 'education' }">
          <h2 class="dual-two__section-title">教育经历</h2>
          <article v-for="(item, index) in education" :key="'edu' + index" class="dual-two__side-entry">
            <div class="dual-two__side-name">{{ [item.degree, item.major].filter(Boolean).join(' · ') || item.major || item.degree || item.school }}</div>
            <div class="dual-two__side-school"><ProofreadInlineText :text="item.school" module-type="education" :item-index="index" field-path="school" :proofread-highlights="proofreadHighlights" /></div>
            <div class="dual-two__side-meta">
              <span v-if="item.startDate">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
              <span v-if="item.city">{{ item.startDate ? ' · ' : '' }}{{ item.city }}</span>
            </div>
            <ProofreadRichHtml
              v-if="item.content"
              class="dual-two__rich dual-two__rich--side"
              :style="richStyle"
              :html="item.content"
              module-type="education"
              :item-index="index"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
            <ProofreadRichHtml
              v-else-if="item.description"
              class="dual-two__rich dual-two__rich--side"
              :style="richStyle"
              :html="item.description"
              module-type="education"
              :item-index="index"
              field-path="description"
              :proofread-highlights="proofreadHighlights"
            />
          </article>
        </section>

        <section v-if="skill.length" class="dual-two__section dual-two__section--side" :class="{ 'highlight-module': highlightModule === 'skill' }">
          <h2 class="dual-two__section-title">专业技能</h2>
          <div class="dual-two__skill-tags">
            <div v-for="(item, index) in skill" :key="'skill' + index" class="dual-two__skill-tag">
              <ProofreadRichHtml
                v-if="item.content"
                class="dual-two__rich dual-two__rich--skill"
                :style="richStyle"
                :html="item.content"
                module-type="skill"
                :item-index="index"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </div>
          </div>
        </section>

        <section v-if="personalStrengths" class="dual-two__section dual-two__section--side" :class="{ 'highlight-module': highlightModule === 'personalStrengths' }">
          <h2 class="dual-two__section-title">个人优势</h2>
          <div class="dual-two__strength-card">
            <div class="dual-two__strength-icon">
              <Sparkles :size="12" :stroke-width="2.4" />
            </div>
            <ProofreadRichHtml
              class="dual-two__rich dual-two__rich--side"
              :style="richStyle"
              :html="personalStrengths"
              module-type="personalStrengths"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </div>
        </section>

        <section v-if="award.length" class="dual-two__section dual-two__section--side" :class="{ 'highlight-module': highlightModule === 'award' }">
          <h2 class="dual-two__section-title">荣誉奖项</h2>
          <article v-for="(item, index) in award" :key="'award' + index" class="dual-two__side-entry dual-two__side-entry--compact">
            <div class="dual-two__side-name"><ProofreadInlineText :text="item.name" module-type="award" :item-index="index" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
            <div v-if="item.date" class="dual-two__side-meta">{{ fmtDate(item.date) }}</div>
            <ProofreadRichHtml
              v-if="item.content"
              class="dual-two__rich dual-two__rich--side"
              :style="richStyle"
              :html="item.content"
              module-type="award"
              :item-index="index"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </article>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { AtSign, Banknote, BriefcaseBusiness, CalendarDays, Clock3, Github, Globe, GraduationCap, MapPin, MessageCircleMore, Phone, Sparkles, UserRound } from 'lucide-vue-next'
import ProofreadInlineText from '../shared/ProofreadInlineText.vue'
import ProofreadRichHtml from '../shared/ProofreadRichHtml.vue'

const props = defineProps({
  contents: { type: Array, default: () => [] },
  themeColor: { type: String, default: '#ff7439' },
  highlightModule: { type: String, default: '' },
  proofreadHighlights: { type: Array, default: () => [] },
  richFontFamily: { type: String, default: '' },
  richFontSize: { type: Number, default: 0 },
  richLineHeight: { type: Number, default: 0 },
})

const richStyle = computed(() => {
  const style = {}
  if (props.richFontFamily) style.fontFamily = props.richFontFamily
  if (props.richFontSize) style.fontSize = `${props.richFontSize}px`
  if (props.richLineHeight) style.lineHeight = String(props.richLineHeight)
  return style
})

function toThemeRgba(color, alpha) {
  if (!color) return `rgba(255, 116, 57, ${alpha})`

  const normalized = color.trim()
  const hexMatch = normalized.match(/^#([0-9a-f]{3}|[0-9a-f]{6})$/i)
  if (hexMatch) {
    const hex = hexMatch[1]
    const fullHex = hex.length === 3 ? hex.split('').map(char => char + char).join('') : hex
    const r = Number.parseInt(fullHex.slice(0, 2), 16)
    const g = Number.parseInt(fullHex.slice(2, 4), 16)
    const b = Number.parseInt(fullHex.slice(4, 6), 16)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }

  const rgbMatch = normalized.match(/^rgba?\(\s*(\d+)[,\s]+(\d+)[,\s]+(\d+)/i)
  if (rgbMatch) {
    const [, r, g, b] = rgbMatch
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }

  return `rgba(255, 116, 57, ${alpha})`
}

const themeVars = computed(() => ({
  '--theme-color': props.themeColor,
  '--theme-soft-1': toThemeRgba(props.themeColor, 0.07),
  '--theme-soft-2': toThemeRgba(props.themeColor, 0.1),
  '--theme-soft-3': toThemeRgba(props.themeColor, 0.14),
  '--theme-soft-4': toThemeRgba(props.themeColor, 0.18),
  '--theme-soft-5': toThemeRgba(props.themeColor, 0.22),
  '--theme-soft-6': toThemeRgba(props.themeColor, 0.28),
}))

function parse(type) {
  const current = props.contents.find(item => item.moduleType === type)
  if (!current || !current.contentJson) return type === 'basic' || type === 'personalStrengths' ? null : []
  try {
    return JSON.parse(current.contentJson)
  } catch {
    return type === 'basic' || type === 'personalStrengths' ? null : []
  }
}

const basic = computed(() => parse('basic'))
const experience = computed(() => parse('experience') || [])
const project = computed(() => parse('project') || [])
const portfolio = computed(() => parse('portfolio') || [])
const education = computed(() => parse('education') || [])
const skill = computed(() => parse('skill') || [])
const personalStrengths = computed(() => parse('personalStrengths')?.content || '')
const award = computed(() => parse('award') || [])
const other = computed(() => parse('other') || [])

const heroContacts = computed(() => {
  const items = [
    basic.value?.phone && { label: '电话', value: basic.value.phone, fieldPath: 'phone', icon: Phone },
    basic.value?.location && { label: '地点', value: basic.value.location, fieldPath: 'location', icon: MapPin },
    basic.value?.email && { label: '邮箱', value: basic.value.email, fieldPath: 'email', icon: AtSign },
    basic.value?.status && { label: '状态', value: basic.value.status, fieldPath: 'status', icon: BriefcaseBusiness },
  ]
  return items.filter(Boolean)
})

const heroPrimaryMeta = computed(() => [
  basic.value?.github && { label: 'GitHub', value: basic.value.github, fieldPath: 'github', icon: Github },
  basic.value?.education && { label: '学历', value: basic.value.education, fieldPath: 'education', icon: GraduationCap },
  basic.value?.workYears && { label: '年限', value: basic.value.workYears, fieldPath: 'workYears', icon: Clock3 },
  basic.value?.salary && { label: '薪资', value: basic.value.salary, fieldPath: 'salary', icon: Banknote },
].filter(Boolean))

const heroSecondaryMeta = computed(() => [])

const heroTertiaryMeta = computed(() => [
  basic.value?.wechat && { label: '微信', value: basic.value.wechat, fieldPath: 'wechat', icon: MessageCircleMore },
  basic.value?.website && { label: '网站', value: basic.value.website, fieldPath: 'website', icon: Globe },
  basic.value?.age && { label: '年龄', value: basic.value.age, fieldPath: 'age', icon: CalendarDays },
  basic.value?.gender && { label: '性别', value: basic.value.gender, fieldPath: 'gender', icon: UserRound },
].filter(Boolean))

function fmtDate(value) {
  if (!value) return ''
  return value.replace(/\//g, '-')
}

</script>

<style scoped>
.dual-two {
  position: relative;
  min-height: 1123px;
  padding: 24px 26px 22px;
  box-sizing: border-box;
  overflow: hidden;
  color: #2d2d2d;
  font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
}

.dual-two__ornament {
  position: absolute;
  pointer-events: none;
  z-index: 0;
}

.dual-two__ornament--right-half {
  right: -292px;
  top: 266px;
  width: 544px;
  height: 544px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--theme-soft-2);
  opacity: 1;
}

.dual-two__ornament--left-half {
  left: -292px;
  top: 509px;
  width: 544px;
  height: 544px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--theme-soft-2);
  opacity: 1;
}

.dual-two__ornament-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.dual-two__ornament-line {
  fill: none;
  stroke: rgba(255, 255, 255, 0.62);
  stroke-linecap: round;
}

.dual-two__ornament-line--thin {
  stroke-width: 3.2;
}

.dual-two__ornament-line--thick {
  stroke-width: 18.2;
}

.dual-two__ornament-line--medium {
  stroke-width: 16.8;
}

.dual-two__ornament-line--wide {
  stroke-width: 25.2;
}

.dual-two__ornament-line--fifth-main {
  stroke-width: 23px;
  stroke-linecap: butt;
  stroke-linejoin: miter;
}

.dual-two__ornament-line--sixth {
  stroke-width: 18px;
}

.dual-two__hero {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: start;
  padding: 28px 36px 26px;
  background: var(--theme-color);
  color: #fff;
}

.dual-two__hero--avatar-left {
  grid-template-columns: auto minmax(0, 1fr);
}

.dual-two__hero-main {
  min-width: 0;
}

.dual-two__hero-main--right {
  order: 2;
  text-align: right;
}

.dual-two__hero-main--right .dual-two__contact-row,
.dual-two__hero-main--right .dual-two__meta-row {
  justify-content: flex-end;
}

.dual-two__name {
  font-size: 21px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0.4px;
  text-transform: uppercase;
}

.dual-two__job {
  margin-top: 4px;
  font-size: 15px;
  font-weight: 600;
  opacity: 0.96;
}

.dual-two__contact-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
}

.dual-two__contact-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  opacity: 0.96;
  white-space: nowrap;
}

.dual-two__meta-block {
  margin-top: 10px;
  display: grid;
  gap: 5px;
}

.dual-two__meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.dual-two__meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  font-size: 12.5px;
  line-height: 1.2;
}

.dual-two__meta-icon {
  flex-shrink: 0;
  color: rgba(255, 255, 255, 0.76);
}

.dual-two__meta-item:has(.lucide-message-circle-more) .dual-two__meta-icon {
  transform: translateY(1px);
}

.dual-two__meta-item:has(.lucide-globe) .dual-two__meta-icon {
  transform: translateY(1px);
}

.dual-two__meta-item:has(.lucide-calendar-days) .dual-two__meta-icon,
.dual-two__meta-item:has(.lucide-user-round) .dual-two__meta-icon {
  transform: translateY(1px);
}

.dual-two__meta-value {
  color: rgba(255, 255, 255, 0.96);
  font-weight: 600;
  white-space: nowrap;
}

.dual-two__avatar-wrap {
  width: 80px;
  height: 94px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
  align-self: center;
  background: rgba(255, 255, 255, 0.2);
}

.dual-two__avatar-wrap--left {
  order: 1;
}

.dual-two__avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.dual-two__body {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(220px, 0.94fr);
  gap: 28px;
  padding: 22px 4px 0;
}

.dual-two__section {
  margin-bottom: 18px;
}

.dual-two__section:last-child {
  margin-bottom: 0;
}

.dual-two__section--side {
  margin-bottom: 20px;
}

.dual-two__section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 800;
  color: #1e1e1e;
  text-transform: uppercase;
}

.dual-two__section-title::after {
  content: '';
  flex: 1;
  height: 2px;
  background: rgba(30, 30, 30, 0.92);
}

.dual-two__entry {
  margin-bottom: 16px;
}

.dual-two__entry:last-child {
  margin-bottom: 0;
}

.dual-two__entry-head {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: start;
}

.dual-two__entry-name,
.dual-two__side-name {
  font-size: 14px;
  font-weight: 800;
  color: #353535;
}

.dual-two__entry-role,
.dual-two__side-school {
  margin-top: 2px;
  font-size: 12.5px;
  color: var(--theme-color);
  font-weight: 600;
}

.dual-two__entry-side,
.dual-two__side-meta {
  font-size: 12.5px;
  color: #7a7a7a;
}

.dual-two__entry-side {
  text-align: right;
}

.dual-two__entry-city {
  margin-top: 2px;
}

.dual-two__inline-link {
  display: inline-block;
  margin-top: 4px;
  color: var(--theme-color);
  text-decoration: none;
  font-size: 12px;
  font-weight: 700;
}

.dual-two__inline-link:hover {
  text-decoration: underline;
}

.dual-two__portfolio-top {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.dual-two__portfolio-card {
  display: block;
}

.dual-two__portfolio-main {
  min-width: 0;
}

.dual-two__rich {
  margin-top: 6px;
  font-size: 11.4px;
  line-height: 1.6;
  color: #555;
  overflow-wrap: anywhere;
  white-space: break-spaces;
}

.dual-two__rich--side {
  color: #636363;
}

.dual-two__side-entry {
  margin-bottom: 12px;
}

.dual-two__side-entry:last-child {
  margin-bottom: 0;
}

.dual-two__side-entry--compact .dual-two__rich {
  margin-top: 4px;
}

.dual-two__skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dual-two__skill-tag {
  padding: 0;
  border: none;
  background: transparent;
  box-shadow: none;
}

.dual-two__rich--skill {
  margin-top: 0;
}

.dual-two__strength-card {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
  padding-top: 2px;
}

.dual-two__strength-icon {
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-color);
}

.highlight-module {
  background: rgba(255, 235, 59, 0.18);
  border-radius: 8px;
  transition: background 0.3s ease;
}

@media print {
  .dual-two__hero {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
</style>

<style>
.dual-two .dual-two__rich ul {
  list-style: none;
  padding-left: 0;
  margin: 4px 0;
}

.dual-two .dual-two__rich ol {
  margin: 4px 0;
  padding-left: 18px;
}

.dual-two .dual-two__rich li {
  padding-left: 0;
  margin-left: -2px;
}

.dual-two .dual-two__rich ul > li::before {
  content: '';
  display: inline-block;
  width: 5px;
  height: 5px;
  margin-right: 10px;
  border-radius: 50%;
  vertical-align: middle;
  background: currentColor;
}

.dual-two .dual-two__rich p {
  margin: 2px 0;
}

.dual-two .dual-two__rich--skill ul,
.dual-two .dual-two__rich--other ul {
  padding-left: 10px;
}

.dual-two .dual-two__rich--skill li,
.dual-two .dual-two__rich--other li {
  margin-left: 0;
}
</style>
