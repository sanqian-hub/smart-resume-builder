<template>
  <div class="creative-template" :style="{ '--theme-color': themeColor }">
    <section v-if="basic" class="c-hero" :class="{ 'highlight-module': highlightModule === 'basic' }">
      <div class="c-hero-title">
        <div class="c-name">
          <ProofreadInlineText :text="basic.name || '您的姓名'" module-type="basic" field-path="name" :proofread-highlights="proofreadHighlights" />
        </div>
        <div class="c-job">
          <ProofreadInlineText :text="basic.jobTitle || '求职方向'" module-type="basic" field-path="jobTitle" :proofread-highlights="proofreadHighlights" />
        </div>
      </div>
          <div v-if="heroTopMetaItems.length" class="c-hero-top-meta-wrap">
            <div class="c-hero-top-meta">
          <div v-if="heroTopMetaFirstRow.length" class="c-hero-top-meta-row">
            <span
                  v-for="item in heroTopMetaFirstRow"
                  :key="item.label"
                  class="c-meta-item c-meta-item--compact"
                  :class="{ 'c-meta-item--narrow': item.compact }"
            >
              <span class="c-meta-label">{{ item.label }}</span>
              <span class="c-meta-value">
                <ProofreadInlineText
                  :text="item.value"
                  module-type="basic"
                  :field-path="item.fieldPath"
                  :proofread-highlights="proofreadHighlights"
                />
              </span>
            </span>
          </div>
          <div v-if="heroTopMetaSecondRow.length" class="c-hero-top-meta-row c-hero-top-meta-row--secondary">
            <span
              v-for="item in heroTopMetaSecondRow"
              :key="item.label"
              class="c-meta-item c-meta-item--compact"
              :class="{ 'c-meta-item--narrow': item.compact }"
            >
              <span class="c-meta-label">{{ item.label }}</span>
              <span class="c-meta-value">
                <ProofreadInlineText
                  :text="item.value"
                  module-type="basic"
                  :field-path="item.fieldPath"
                  :proofread-highlights="proofreadHighlights"
                />
              </span>
            </span>
          </div>
        </div>
      </div>
      <div class="c-hero-main">
        <div class="c-contact">
          <span v-for="item in heroContactItems" :key="item.label" class="c-contact-item">
            <span class="c-contact-label">{{ item.label }}</span>
            <a v-if="item.href" :href="item.href" target="_blank" rel="noreferrer" class="c-contact-value c-contact-link">
              <ProofreadInlineText
                :text="item.value"
                module-type="basic"
                :field-path="item.fieldPath"
                :proofread-highlights="proofreadHighlights"
              />
            </a>
            <span v-else class="c-contact-value">
              <ProofreadInlineText
                :text="item.value"
                module-type="basic"
                :field-path="item.fieldPath"
                :proofread-highlights="proofreadHighlights"
              />
            </span>
          </span>
        </div>
      </div>
      <div class="c-avatar-shell">
        <img v-if="basic.avatar" :src="basic.avatar" alt="avatar" class="c-avatar" />
        <div v-else class="c-avatar c-avatar--placeholder">PHOTO</div>
      </div>
    </section>

    <template v-for="type in orderedModules" :key="type">
      <section
        v-if="type === 'education' && education.length"
        class="c-section"
        :class="{ 'highlight-module': highlightModule === 'education' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('education') }}</span>
          <h2>教育经历</h2>
        </header>
        <div v-for="(item, i) in education" :key="'e' + i" class="c-entry">
          <div class="c-entry-top">
            <div class="c-entry-title"><ProofreadInlineText :text="item.school" module-type="education" :item-index="i" field-path="school" :proofread-highlights="proofreadHighlights" /></div>
            <div v-if="item.startDate" class="c-entry-date">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</div>
          </div>
          <div class="c-entry-meta">{{ [item.academy, item.major, item.degree, item.degreeType, item.city].filter(Boolean).join(' · ') }}</div>
          <ProofreadRichHtml
            v-if="item.description"
            class="c-rich"
            :style="richStyle"
            :html="item.description"
            module-type="education"
            :item-index="i"
            field-path="description"
            :proofread-highlights="proofreadHighlights"
          />
        </div>
      </section>

      <section
        v-else-if="type === 'experience' && experience.length"
        class="c-section"
        :class="{ 'highlight-module': highlightModule === 'experience' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('experience') }}</span>
          <h2>工作经历</h2>
        </header>
        <div v-for="(item, i) in experience" :key="'x' + i" class="c-entry">
          <div class="c-entry-top">
            <div class="c-entry-title"><ProofreadInlineText :text="item.company" module-type="experience" :item-index="i" field-path="company" :proofread-highlights="proofreadHighlights" /></div>
            <div v-if="item.startDate" class="c-entry-date">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</div>
          </div>
          <div class="c-entry-meta">{{ [item.department, item.position].filter(Boolean).join(' · ') }}</div>
          <ProofreadRichHtml
            v-if="item.content"
            class="c-rich"
            :style="richStyle"
            :html="item.content"
            module-type="experience"
            :item-index="i"
            field-path="content"
            :proofread-highlights="proofreadHighlights"
          />
        </div>
      </section>

      <section
        v-else-if="type === 'project' && project.length"
        class="c-section"
        :class="{ 'highlight-module': highlightModule === 'project' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('project') }}</span>
          <h2>项目经历</h2>
        </header>
        <div v-for="(item, i) in project" :key="'p' + i" class="c-entry">
          <div class="c-entry-top">
            <div class="c-entry-title"><ProofreadInlineText :text="item.name" module-type="project" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
            <a v-if="item.link" :href="item.link" target="_blank" rel="noreferrer" class="c-project-link">{{ item.link }}</a>
          </div>
          <div class="c-entry-meta">
            <div class="c-entry-meta-left">
              <span v-if="item.role">{{ item.role }}</span>
              <span v-if="item.role && item.city"> · </span>
              <span v-if="item.city">{{ item.city }}</span>
            </div>
            <div v-if="item.startDate" class="c-entry-meta-right">
              {{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}
            </div>
          </div>
          <ProofreadRichHtml
            v-if="item.content"
            class="c-rich"
            :style="richStyle"
            :html="item.content"
            module-type="project"
            :item-index="i"
            field-path="content"
            :proofread-highlights="proofreadHighlights"
          />
        </div>
      </section>

      <section
        v-else-if="type === 'skill' && skill.length"
        class="c-section"
        :class="{ 'highlight-module': highlightModule === 'skill' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('skill') }}</span>
          <h2>专业技能</h2>
        </header>
        <div class="c-skill-list">
          <div v-for="(item, i) in skill" :key="'s' + i" class="c-skill-item" :style="skillItemStyle">
            <ProofreadRichHtml
              v-if="item.content"
              class="c-rich"
              :style="richStyle"
              :html="item.content"
              module-type="skill"
              :item-index="i"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </div>
        </div>
      </section>

      <section
        v-else-if="type === 'personalStrengths' && personalStrengths"
        class="c-section c-section--self-intro"
        :class="{ 'highlight-module': highlightModule === 'personalStrengths' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('personalStrengths') }}</span>
          <h2>个人优势</h2>
        </header>
        <ProofreadRichHtml
          class="c-rich"
          :style="richStyle"
          :html="personalStrengths"
          module-type="personalStrengths"
          field-path="content"
          :proofread-highlights="proofreadHighlights"
        />
      </section>

      <section
        v-else-if="type === 'award' && award.length"
        class="c-section"
        :class="{ 'highlight-module': highlightModule === 'award' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('award') }}</span>
          <h2>荣誉奖项</h2>
        </header>
        <div v-for="(item, i) in award" :key="'a' + i" class="c-entry">
          <div class="c-entry-top">
            <div class="c-entry-title"><ProofreadInlineText :text="item.name" module-type="award" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
            <div v-if="item.date" class="c-entry-date">{{ fmtDate(item.date) }}</div>
          </div>
          <ProofreadRichHtml
            v-if="item.content"
            class="c-rich"
            :style="richStyle"
            :html="item.content"
            module-type="award"
            :item-index="i"
            field-path="content"
            :proofread-highlights="proofreadHighlights"
          />
        </div>
      </section>

      <section
        v-else-if="type === 'portfolio' && portfolio.length"
        class="c-section"
        :class="{ 'highlight-module': highlightModule === 'portfolio' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('portfolio') }}</span>
          <h2>个人作品</h2>
        </header>
        <div v-for="(item, i) in portfolio" :key="'f' + i" class="c-entry c-entry--portfolio">
          <div class="c-portfolio-card">
            <div class="c-portfolio-main">
              <div v-if="item.name || item.link" class="c-entry-top c-entry-top--portfolio">
                <div class="c-entry-title"><ProofreadInlineText :text="item.name" module-type="portfolio" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
                <a v-if="item.link" :href="item.link" target="_blank" rel="noreferrer" class="c-project-link c-project-link--portfolio">{{ item.link }}</a>
                </div>
                <ProofreadRichHtml
                  v-if="item.content"
                  class="c-rich"
                  :style="richStyle"
                  :html="item.content"
                  module-type="portfolio"
                  :item-index="i"
                  field-path="content"
                  :proofread-highlights="proofreadHighlights"
                />
              </div>
            </div>
          </div>
      </section>

      <section
        v-else-if="type === 'other' && other.length"
        class="c-section c-section--other"
        :class="{ 'highlight-module': highlightModule === 'other' }"
      >
        <header class="c-section-head">
          <span class="c-index">{{ sectionNumber('other') }}</span>
          <h2>其他经历</h2>
        </header>
        <div v-for="(item, i) in other" :key="'o' + i" class="c-entry">
          <div class="c-entry-top">
            <div class="c-entry-title"><ProofreadInlineText :text="item.name" module-type="other" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
            <div v-if="item.startDate" class="c-entry-date">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</div>
          </div>
          <div class="c-entry-meta">{{ [item.role, item.department, item.city].filter(Boolean).join(' · ') }}</div>
          <ProofreadRichHtml
            v-if="item.content"
            class="c-rich"
            :style="richStyle"
            :html="item.content"
            module-type="other"
            :item-index="i"
            field-path="content"
            :proofread-highlights="proofreadHighlights"
          />
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ProofreadInlineText from '../shared/ProofreadInlineText.vue'
import ProofreadRichHtml from '../shared/ProofreadRichHtml.vue'

const props = defineProps({
  contents: { type: Array, default: () => [] },
  themeColor: { type: String, default: 'rgb(70, 114, 242)' },
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
  if (!color) return `rgba(70, 114, 242, ${alpha})`

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

  return `rgba(70, 114, 242, ${alpha})`
}

const skillItemStyle = computed(() => ({
  background: toThemeRgba(props.themeColor, 0.14),
  borderColor: toThemeRgba(props.themeColor, 0.05),
}))

function parse(type) {
  const c = props.contents.find(item => item.moduleType === type)
  if (!c || !c.contentJson) return type === 'basic' || type === 'personalStrengths' ? null : []
  try {
    return JSON.parse(c.contentJson)
  } catch {
    return type === 'basic' || type === 'personalStrengths' ? null : []
  }
}

const basic = computed(() => parse('basic'))
const education = computed(() => parse('education') || [])
const experience = computed(() => parse('experience') || [])
const project = computed(() => parse('project') || [])
const skill = computed(() => parse('skill') || [])
const personalStrengths = computed(() => parse('personalStrengths')?.content || '')
const award = computed(() => parse('award') || [])
const portfolio = computed(() => parse('portfolio') || [])
const other = computed(() => parse('other') || [])

const heroContactItems = computed(() => {
  const current = basic.value || {}
  const items = [
    current.phone ? { label: '电话', value: current.phone, fieldPath: 'phone' } : null,
    current.email ? { label: '邮箱', value: current.email, href: `mailto:${current.email}`, fieldPath: 'email' } : null,
    current.location ? { label: '地点', value: current.location, fieldPath: 'location' } : null,
    current.website ? { label: '网站', value: simplifyUrl(current.website), href: normalizeUrl(current.website), fieldPath: 'website' } : null,
    current.github ? { label: 'GitHub', value: simplifyUrl(current.github), href: normalizeUrl(current.github), fieldPath: 'github' } : null,
  ].filter(Boolean)

  if (items.length) return items

  return [
    { label: '电话', value: '138-0000-0000', fieldPath: 'phone' },
    { label: '邮箱', value: 'example@email.com', fieldPath: 'email' },
    { label: '地点', value: '所在城市', fieldPath: 'location' },
  ]
})

const heroMetaItems = computed(() => {
  const current = basic.value || {}
  return [
    createMetaItem('状态', current.status, 'status'),
    createMetaItem('年龄', current.age, 'age'),
    createMetaItem('学历', current.education, 'education'),
    createMetaItem('工作年限', current.workYears, 'workYears'),
    createMetaItem('微信', current.wechat, 'wechat'),
    createMetaItem('薪资', current.salary, 'salary'),
    createMetaItem('性别', current.gender, 'gender'),
  ].filter(Boolean)
})

const heroTopMetaFirstRow = computed(() => {
  const current = basic.value || {}
  return [
    createMetaItem('状态', current.status, 'status'),
    createMetaItem('年龄', current.age, 'age'),
    createMetaItem('学历', current.education, 'education'),
    createMetaItem('工作年限', current.workYears, 'workYears'),
  ].filter(Boolean)
})

const heroTopMetaSecondRow = computed(() => {
  const current = basic.value || {}
  return [
    createMetaItem('微信', current.wechat, 'wechat'),
    createMetaItem('薪资', current.salary, 'salary'),
    createMetaItem('性别', current.gender, 'gender'),
  ].filter(Boolean)
})

const heroTopMetaItems = computed(() => [...heroTopMetaFirstRow.value, ...heroTopMetaSecondRow.value])

const orderedModules = computed(() =>
  props.contents
    .map(item => item.moduleType)
    .filter(type => type !== 'basic' && hasContent(type))
)

const hasBasicIdentity = computed(() => !!(basic.value?.phone || basic.value?.email || basic.value?.location))

function hasContent(type) {
  const data = parse(type)
  if (!data) return false
  if (typeof data === 'string') return !!data.trim()
  if (!Array.isArray(data)) return Object.values(data).some(value => typeof value === 'string' && value.trim())
  if (!data.length) return false
  return data.some(item => item && Object.values(item).some(value => typeof value === 'string' && value.trim()))
}

function sectionNumber(type) {
  const index = orderedModules.value.indexOf(type)
  return String(index + 1).padStart(2, '0')
}

function fmtDate(value) {
  if (!value) return ''
  return value.replace(/\//g, '-')
}

function normalizeUrl(value) {
  if (!value) return ''
  return /^https?:\/\//i.test(value) ? value : `https://${value}`
}

function simplifyUrl(value) {
  if (!value) return ''
  return value.replace(/^https?:\/\//i, '').replace(/\/$/, '')
}

function createMetaItem(label, value, fieldPath) {
  if (!value) return null
  return { label, value, fieldPath, compact: label === '年龄' }
}
</script>

<style scoped>
.creative-template {
  --section-gap: 20px;
  width: 100%;
  box-sizing: border-box;
  padding: 20px 35px 0;
  color: #1f2937;
  font-size: 14px;
  line-height: 1.75;
  font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
}

.c-hero {
  display: grid;
  grid-template-columns: minmax(160px, max-content) minmax(0, 1fr) 88px;
  column-gap: 22px;
  row-gap: 10px;
  align-items: start;
  padding: 0 0 20px;
  margin-bottom: var(--section-gap);
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.c-name {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: 1px;
}

.c-hero-title {
  min-width: 0;
  grid-column: 1;
  grid-row: 1;
}

.c-job {
  margin-top: 6px;
  color: var(--theme-color);
  font-size: 15px;
  font-weight: 600;
}

.c-hero-main {
  grid-column: 1 / 3;
  grid-row: 2;
}

.c-hero-top-meta-wrap {
  grid-column: 2;
  grid-row: 1;
  width: 100%;
  min-width: 0;
}

.c-hero-top-meta {
  display: flex;
  flex-direction: column;
  width: 372px;
  max-width: 100%;
  gap: 8px;
  padding-top: 4px;
}

.c-hero-top-meta-row {
  display: flex;
  justify-content: flex-start;
  gap: 8px 24px;
}

.c-contact {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  margin-top: 12px;
}

.c-contact-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  color: #4b5563;
  font-size: 12.5px;
}

.c-contact-label {
  color: #94a3b8;
  flex-shrink: 0;
}

.c-contact-value {
  min-width: 0;
  color: #334155;
  overflow-wrap: anywhere;
}

.c-contact-link {
  color: inherit;
  text-decoration: none;
}

.c-contact-link:hover {
  color: var(--theme-color);
  text-decoration: underline;
}

.c-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  white-space: nowrap;
  color: #475569;
  font-size: 12.5px;
  line-height: 1.2;
}

.c-meta-item--compact {
  gap: 5px;
}

.c-meta-label {
  color: #94a3b8;
}

.c-meta-value {
  color: #334155;
}

.c-avatar-shell {
  grid-column: 3;
  grid-row: 1 / span 2;
  display: flex;
  justify-content: flex-end;
}

.c-avatar {
  width: 88px;
  height: 108px;
  object-fit: cover;
  border-radius: 22px 8px 22px 8px;
  background: #e5e7eb;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.c-avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  letter-spacing: 1px;
  color: rgba(15, 23, 42, 0.32);
}

.c-section {
  margin-bottom: var(--section-gap);
}

.c-section:last-child {
  margin-bottom: 0;
}

.c-section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.c-section-head h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.c-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(15, 23, 42, 0.08);
  color: var(--theme-color);
  font-size: 11px;
  font-weight: 700;
}

.c-entry {
  margin-bottom: 12px;
}

.c-entry:last-child {
  margin-bottom: 0;
}

.c-entry-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
}

.c-entry-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.c-entry-date {
  flex-shrink: 0;
  color: #6b7280;
  font-size: 12.5px;
}

.c-entry-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-top: 2px;
  color: #6b7280;
  font-size: 12.5px;
}

.c-entry-meta-left {
  min-width: 0;
}

.c-entry-meta-right {
  flex-shrink: 0;
  margin-left: auto;
}

.c-project-link {
  margin-left: 12px;
  color: var(--theme-color);
  text-decoration: none;
  font-weight: 600;
}

.c-project-link:hover {
  text-decoration: underline;
}

.c-project-link--portfolio {
  margin-left: 4px;
  overflow-wrap: anywhere;
}

.c-skill-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.c-skill-item {
  padding: 8px 11px;
  border: 1px solid rgba(70, 114, 242, 0.14);
  border-radius: 10px;
}

.c-rich {
  margin-top: 6px;
  color: #374151;
  white-space: break-spaces;
  overflow-wrap: anywhere;
}

.c-entry--portfolio {
  margin-bottom: 14px;
}

.c-portfolio-card {
  display: block;
}

.c-portfolio-main {
  min-width: 0;
}

.c-entry-top--portfolio {
  justify-content: flex-start;
  flex-wrap: wrap;
}


.highlight-module {
  background: rgba(255, 235, 59, 0.22);
  border-radius: 10px;
  transition: background 0.3s ease;
}

@media print {
  .c-index {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
</style>

<style>
.creative-template .c-rich ul {
  list-style: none;
  padding-left: 0;
  margin: 4px 0;
}

.creative-template .c-rich ol {
  margin: 4px 0;
  padding-left: 20px;
}

.creative-template .c-rich li {
  padding-left: 0;
  margin-left: -3px;
}

.creative-template .c-rich ul > li::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  vertical-align: middle;
  margin-right: 12px;
}

.creative-template .c-rich p {
  margin: 2px 0;
}

.creative-template .c-section--self-intro .c-rich ul,
.creative-template .c-section--other .c-rich ul {
  padding-left: 10px;
}

.creative-template .c-section--self-intro .c-rich li,
.creative-template .c-section--other .c-rich li {
  margin-left: 0;
}
</style>
