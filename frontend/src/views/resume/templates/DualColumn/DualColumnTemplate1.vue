<template>
  <div class="dual-one" :style="{ '--theme-color': themeColor }">
    <aside class="dual-one__sidebar" :class="{ 'highlight-module': highlightModule === 'basic' }">
      <div class="dual-one__sidebar-head">
        <div class="dual-one__name"><ProofreadInlineText :text="basic?.name || '您的姓名'" module-type="basic" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
        <div class="dual-one__job"><ProofreadInlineText :text="basic?.jobTitle || '求职方向'" module-type="basic" field-path="jobTitle" :proofread-highlights="proofreadHighlights" /></div>
      </div>

      <img
        v-if="basic?.avatar"
        :src="basic.avatar"
        alt="avatar"
        class="dual-one__avatar"
        :class="{ 'dual-one__avatar--left': basic?.avatarAlign }"
      />

      <section v-if="contactItems.length" class="dual-one__side-section">
        <div class="dual-one__side-title">基本信息</div>
        <div class="dual-one__divider"></div>
        <div class="dual-one__contact-list">
          <div v-for="item in contactItems" :key="item.label" class="dual-one__contact-item">
            <span
              class="dual-one__contact-icon"
              :class="{
                'dual-one__contact-icon--wechat': item.label === '微信',
                'dual-one__contact-icon--link': item.label === '网站' || item.label === 'GitHub',
              }"
            >
              <component :is="item.icon" :size="12" :stroke-width="2.1" />
            </span>
            <span class="dual-one__contact-text">
              <ProofreadInlineText
                :text="item.displayValue || item.value"
                module-type="basic"
                :field-path="item.fieldPath"
                :proofread-highlights="proofreadHighlights"
              />
            </span>
          </div>
        </div>
      </section>

      <section v-if="personalStrengths" class="dual-one__side-section" :class="{ 'highlight-module': highlightModule === 'personalStrengths' }">
        <div class="dual-one__side-title">个人优势</div>
        <div class="dual-one__divider"></div>
        <ProofreadRichHtml
          class="dual-one__rich dual-one__rich--side"
          :style="richStyle"
          :html="personalStrengths"
          module-type="personalStrengths"
          field-path="content"
          :proofread-highlights="proofreadHighlights"
        />
      </section>

      <section v-if="award.length" class="dual-one__side-section" :class="{ 'highlight-module': highlightModule === 'award' }">
        <div class="dual-one__side-title">荣誉奖项</div>
        <div class="dual-one__divider"></div>
          <div v-for="(item, i) in award" :key="'a' + i" class="dual-one__mini-entry">
          <div class="dual-one__mini-name"><ProofreadInlineText :text="item.name" module-type="award" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
          <div v-if="item.date" class="dual-one__mini-date">{{ fmtDate(item.date) }}</div>
          <ProofreadRichHtml
            v-if="item.content"
            class="dual-one__rich dual-one__rich--side"
            :style="richStyle"
            :html="item.content"
            module-type="award"
            :item-index="i"
            field-path="content"
            :proofread-highlights="proofreadHighlights"
          />
        </div>
      </section>
    </aside>

    <main class="dual-one__main">
      <div class="dual-one__columns">
        <div class="dual-one__primary">
          <section v-if="experience.length" class="dual-one__section" :class="{ 'highlight-module': highlightModule === 'experience' }">
            <h2 class="dual-one__section-title">工作经历</h2>
            <article v-for="(item, i) in experience" :key="'x' + i" class="dual-one__entry">
              <div class="dual-one__entry-name"><ProofreadInlineText :text="item.company" module-type="experience" :item-index="i" field-path="company" :proofread-highlights="proofreadHighlights" /></div>
              <div class="dual-one__entry-role">{{ [item.department, item.position].filter(Boolean).join(' · ') }}</div>
              <div class="dual-one__entry-meta">
                <span v-if="item.startDate">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
                <span v-if="item.city">{{ item.startDate ? ' · ' : '' }}{{ item.city }}</span>
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="dual-one__rich"
                :style="richStyle"
                :html="item.content"
                module-type="experience"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </article>
          </section>

          <section v-if="project.length" class="dual-one__section" :class="{ 'highlight-module': highlightModule === 'project' }">
            <h2 class="dual-one__section-title">项目经历</h2>
            <article v-for="(item, i) in project" :key="'p' + i" class="dual-one__entry">
              <div class="dual-one__project-top">
                <div class="dual-one__entry-name"><ProofreadInlineText :text="item.name" module-type="project" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
                <a v-if="item.link" :href="item.link" target="_blank" rel="noreferrer" class="dual-one__project-link">{{ item.link }}</a>
              </div>
              <div class="dual-one__entry-meta">
                <span v-if="item.startDate">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
                <span v-if="item.role">{{ item.startDate ? ' · ' : '' }}{{ item.role }}</span>
                <span v-if="item.city">{{ (item.startDate || item.role) ? ' · ' : '' }}{{ item.city }}</span>
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="dual-one__rich"
                :style="richStyle"
                :html="item.content"
                module-type="project"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </article>
          </section>

          <section v-if="portfolio.length" class="dual-one__section" :class="{ 'highlight-module': highlightModule === 'portfolio' }">
            <h2 class="dual-one__section-title">个人作品</h2>
            <article v-for="(item, i) in portfolio" :key="'f' + i" class="dual-one__portfolio">
              <div class="dual-one__portfolio-card">
                <div class="dual-one__portfolio-main">
                  <div v-if="item.name || item.link" class="dual-one__portfolio-top">
                    <div v-if="item.name" class="dual-one__entry-name"><ProofreadInlineText :text="item.name" module-type="portfolio" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
                    <a v-if="item.link" :href="item.link" target="_blank" rel="noreferrer" class="dual-one__project-link">{{ item.link }}</a>
                  </div>
                  <ProofreadRichHtml
                    v-if="item.content"
                    class="dual-one__rich"
                    :style="richStyle"
                    :html="item.content"
                    module-type="portfolio"
                    :item-index="i"
                    field-path="content"
                    :proofread-highlights="proofreadHighlights"
                  />
                </div>
              </div>
            </article>
          </section>
        </div>

        <div class="dual-one__secondary">
          <section v-if="education.length" class="dual-one__section" :class="{ 'highlight-module': highlightModule === 'education' }">
            <h2 class="dual-one__section-title">教育经历</h2>
            <article v-for="(item, i) in education" :key="'e' + i" class="dual-one__entry dual-one__entry--compact">
              <div class="dual-one__entry-name"><ProofreadInlineText :text="item.school" module-type="education" :item-index="i" field-path="school" :proofread-highlights="proofreadHighlights" /></div>
              <div class="dual-one__entry-role">{{ [item.major, item.degree, item.degreeType].filter(Boolean).join(' · ') }}</div>
              <div class="dual-one__entry-meta">
                <span v-if="item.startDate">{{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
                <span v-if="item.city">{{ item.startDate ? ' · ' : '' }}{{ item.city }}</span>
              </div>
              <ProofreadRichHtml
                v-if="item.description"
                class="dual-one__rich"
                :style="richStyle"
                :html="item.description"
                module-type="education"
                :item-index="i"
                field-path="description"
                :proofread-highlights="proofreadHighlights"
              />
            </article>
          </section>

          <section v-if="skill.length" class="dual-one__section" :class="{ 'highlight-module': highlightModule === 'skill' }">
            <h2 class="dual-one__section-title">专业技能</h2>
            <div class="dual-one__skill-grid">
              <div v-for="(item, i) in skill" :key="'s' + i" class="dual-one__skill-item dual-one__skill-item--indented">
                <ProofreadRichHtml
                  v-if="item.content"
                  class="dual-one__rich dual-one__rich--skill"
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

          <section v-if="other.length" class="dual-one__section" :class="{ 'highlight-module': highlightModule === 'other' }">
            <h2 class="dual-one__section-title">其他经历</h2>
            <article v-for="(item, i) in other" :key="'o' + i" class="dual-one__entry dual-one__entry--compact dual-one__entry--other">
              <div class="dual-one__entry-name"><ProofreadInlineText :text="item.name" module-type="other" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></div>
              <div class="dual-one__entry-role">{{ [item.role, item.department, item.city].filter(Boolean).join(' · ') }}</div>
              <div class="dual-one__entry-meta" v-if="item.startDate">
                {{ fmtDate(item.startDate) }} - {{ item.endDate ? fmtDate(item.endDate) : '至今' }}
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="dual-one__rich"
                :style="richStyle"
                :html="item.content"
                module-type="other"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </article>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Banknote, BriefcaseBusiness, CalendarDays, Clock3, Github, Globe, GraduationCap, Mail, MapPin, MessageCircleMore, Phone, UserRound } from 'lucide-vue-next'
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

const contactItems = computed(() => {
  const items = [
    basic.value?.phone && { label: '电话', value: basic.value.phone, fieldPath: 'phone', icon: Phone },
    basic.value?.email && { label: '邮箱', value: basic.value.email, fieldPath: 'email', icon: Mail },
    basic.value?.location && { label: '地点', value: basic.value.location, fieldPath: 'location', icon: MapPin },
    basic.value?.website && { label: '网站', value: basic.value.website, displayValue: formatLinkText(basic.value.website), fieldPath: 'website', icon: Globe },
    basic.value?.github && { label: 'GitHub', value: basic.value.github, displayValue: formatLinkText(basic.value.github), fieldPath: 'github', icon: Github },
    basic.value?.wechat && { label: '微信', value: basic.value.wechat, fieldPath: 'wechat', icon: MessageCircleMore },
    basic.value?.status && { label: '状态', value: basic.value.status, fieldPath: 'status', icon: BriefcaseBusiness },
    basic.value?.salary && { label: '薪资', value: basic.value.salary, fieldPath: 'salary', icon: Banknote },
    basic.value?.education && { label: '学历', value: basic.value.education, fieldPath: 'education', icon: GraduationCap },
    basic.value?.workYears && { label: '年限', value: basic.value.workYears, fieldPath: 'workYears', icon: Clock3 },
    basic.value?.gender && { label: '性别', value: basic.value.gender, fieldPath: 'gender', icon: UserRound },
    basic.value?.age && { label: '年龄', value: basic.value.age, fieldPath: 'age', icon: CalendarDays },
  ]
  return items.filter(Boolean)
})

function fmtDate(value) {
  if (!value) return ''
  return value.replace(/\//g, '-')
}

function formatLinkText(value) {
  if (!value) return ''
  return value.replace(/^https?:\/\//i, '')
}
</script>

<style scoped>
.dual-one {
  display: grid;
  grid-template-columns: 232px minmax(0, 1fr);
  gap: 18px;
  width: 100%;
  min-height: 100%;
  padding: 18px;
  box-sizing: border-box;
  color: #2f2f2f;
  font-size: 13px;
  line-height: 1.58;
  font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
}

.dual-one__sidebar {
  min-width: 0;
  background: var(--theme-color);
  color: rgba(255, 255, 255, 0.94);
  padding: 22px 22px 26px;
}

.dual-one__sidebar-head {
  margin-bottom: 20px;
}

.dual-one__name {
  font-size: 26px;
  line-height: 1.2;
  font-weight: 300;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  white-space: pre-line;
}

.dual-one__job {
  margin-top: 9px;
  font-size: 11.5px;
  letter-spacing: 1.3px;
  text-transform: uppercase;
  opacity: 0.82;
}

.dual-one__avatar {
  width: 80px;
  height: 94px;
  object-fit: cover;
  display: block;
  margin: 0 0 16px auto;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.12);
}

.dual-one__avatar--left {
  margin-left: 0;
  margin-right: auto;
}

.dual-one__side-section {
  margin-top: 20px;
}

.dual-one__side-title {
  font-size: 14.5px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  font-weight: 700;
}

.dual-one__divider {
  height: 1px;
  margin: 10px 0 12px;
  background: rgba(255, 255, 255, 0.62);
}

.dual-one__contact-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dual-one__contact-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
}

.dual-one__contact-icon {
  width: 16px;
  height: 16px;
  margin-top: 3px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.98);
}

.dual-one__contact-icon--wechat {
  margin-top: 3px;
}

.dual-one__contact-icon--link {
  margin-top: 4px;
}

.dual-one__contact-text {
  min-width: 0;
  font-size: 14.5px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.dual-one__main {
  min-width: 0;
  padding: 2px 4px 0 0;
}

.dual-one__columns {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(0, 0.9fr);
  gap: 22px;
}

.dual-one__section {
  margin-bottom: 18px;
}

.dual-one__section:last-child {
  margin-bottom: 0;
}

.dual-one__section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 10px;
  font-size: 14px;
  color: #4b4b4b;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  font-weight: 700;
}

.dual-one__section-title::after {
  content: '';
  flex: 1;
  min-width: 24px;
  height: 1px;
  background: rgba(47, 47, 47, 0.2);
}

.dual-one__entry {
  margin-bottom: 14px;
}

.dual-one__entry:last-child {
  margin-bottom: 0;
}

.dual-one__entry--compact {
  margin-bottom: 12px;
}

.dual-one__entry-name {
  font-size: 15px;
  font-weight: 700;
  color: #2f2f2f;
}

.dual-one__entry-role {
  margin-top: 2px;
  color: #555;
  font-size: 13px;
}

.dual-one__entry-meta {
  margin-top: 2px;
  color: #7a7a7a;
  font-size: 12px;
}

.dual-one__rich {
  margin-top: 6px;
  color: #434343;
  overflow-wrap: anywhere;
  white-space: break-spaces;
}

.dual-one__rich--side {
  color: rgba(255, 255, 255, 0.92);
  font-size: 13.8px;
}

.dual-one__rich--skill {
  margin-top: 0;
}

.dual-one__project-top,
.dual-one__portfolio-top {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.dual-one__project-link {
  color: var(--theme-color);
  text-decoration: none;
  font-size: 12px;
  font-weight: 600;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.dual-one__project-link:hover {
  text-decoration: underline;
}

.dual-one__skill-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 18px;
  row-gap: 6px;
}

.dual-one__skill-item {
  min-width: 0;
  grid-column: 1 / -1;
}

.dual-one__skill-item--indented {
  padding-left: 8px;
}

.dual-one__entry--other .dual-one__rich {
  padding-left: 8px;
}

.dual-one__portfolio-card {
  display: block;
}

.dual-one__portfolio-main {
  min-width: 0;
}

.dual-one__mini-entry {
  margin-bottom: 10px;
}

.dual-one__mini-entry:last-child {
  margin-bottom: 0;
}

.dual-one__mini-name {
  font-size: 14.5px;
  font-weight: 700;
}

.dual-one__mini-date {
  margin-top: 2px;
  font-size: 12.5px;
  opacity: 0.82;
}

.highlight-module {
  background: rgba(255, 235, 59, 0.2);
  border-radius: 8px;
  transition: background 0.3s ease;
}

@media print {
  .dual-one__sidebar {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
</style>

<style>
@media screen {
  .paper.paper-breakable .dual-one__sidebar {
    background-color: var(--theme-color);
    background-image: repeating-linear-gradient(
      to bottom,
      transparent 0,
      transparent 1122px,
      rgba(192, 192, 192, 0.92) 1122px,
      rgba(192, 192, 192, 0.92) 1123px
    );
    background-size: 100% 1123px;
    background-repeat: repeat;
    background-position: 0 -18px;
  }
}

.dual-one .dual-one__rich ul {
  list-style: none;
  padding-left: 0;
  margin: 4px 0;
}

.dual-one .dual-one__rich ol {
  margin: 4px 0;
  padding-left: 20px;
}

.dual-one .dual-one__rich li {
  padding-left: 0;
  margin-left: -3px;
}

.dual-one .dual-one__rich ul > li::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  vertical-align: middle;
  margin-right: 12px;
}

.dual-one .dual-one__rich p {
  margin: 2px 0;
}
</style>
