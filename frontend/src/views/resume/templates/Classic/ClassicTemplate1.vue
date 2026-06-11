<template>
  <div class="resume-show-new" :style="{ '--theme-color': themeColor }">
    <!-- ====== Profile ====== -->
    <div v-if="basic" class="profile" :class="{ 'highlight-module': highlightModule === 'basic' }">
      <div class="profile0">
        <div class="profileContent" :style="{ flexDirection: basic.avatarAlign ? 'row' : 'row-reverse' }">
          <div class="profileNoImageArea">
            <div class="profile-0">
              <div v-if="basic.name" class="name">
                <ProofreadInlineText :text="basic.name" module-type="basic" field-path="name" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.phone" class="telephone">
                <svg class="anticon" viewBox="64 64 896 896" fill="currentColor"><path d="M885.6 230.2L779.1 123.8a80.83 80.83 0 00-57.3-23.8c-21.7 0-42.1 8.5-57.4 23.8L549.8 238.4a80.83 80.83 0 00-23.8 57.3c0 21.7 8.5 42.1 23.8 57.4l83.8 83.8A393.82 393.82 0 01553.1 553 395.34 395.34 0 01437 633.8L353.2 550a80.83 80.83 0 00-57.3-23.8c-21.7 0-42.1 8.5-57.4 23.8L123.8 664.5a80.89 80.89 0 00-23.8 57.4c0 21.7 8.5 42.1 23.8 57.4l106.3 106.3c24.4 24.5 58.1 38.4 92.7 38.4 7.3 0 14.3-.6 21.2-1.8 134.8-22.2 268.5-93.9 376.4-201.7C828.2 612.8 899.8 479.2 922.3 344c6.8-41.3-6.9-83.8-36.7-113.8z"/></svg>
                <ProofreadInlineText :text="basic.phone" module-type="basic" field-path="phone" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.email" class="email">
                <svg class="anticon" viewBox="64 64 896 896" fill="currentColor"><path d="M928 160H96c-17.7 0-32 14.3-32 32v640c0 17.7 14.3 32 32 32h832c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zm-80.8 108.9L531.7 514.4c-7.8 6.1-18.7 6.1-26.5 0L189.6 268.9A7.2 7.2 0 01194 256h648.8a7.2 7.2 0 014.4 12.9z"/></svg>
                <ProofreadInlineText :text="basic.email" module-type="basic" field-path="email" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.github" class="github">
                <svg class="anticon" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
                <ProofreadInlineText :text="basic.github" module-type="basic" field-path="github" :proofread-highlights="proofreadHighlights" />
              </div>
            </div>
            <div v-if="basic.status || basic.jobTitle || basic.location || basic.salary || basic.gender" class="profile-1">
              <div v-if="basic.status" class="currentStatus">
                <svg class="anticon" viewBox="64 64 896 896" fill="currentColor"><path d="M938 458.8l-29.6-312.6c-1.5-16.2-14.4-29-30.6-30.6L565.2 86h-.4c-3.2 0-5.7 1-7.6 2.9L88.9 557.2a9.96 9.96 0 000 14.1l363.8 363.8c1.9 1.9 4.4 2.9 7.1 2.9s5.2-1 7.1-2.9l468.3-468.3c2-2.1 3-5 2.8-8zM699 387c-35.3 0-64-28.7-64-64s28.7-64 64-64 64 28.7 64 64-28.7 64-64 64z"/></svg>
                <ProofreadInlineText :text="basic.status" module-type="basic" field-path="status" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.jobTitle" class="positionTitle">
                <svg class="anticon" viewBox="64 64 896 896" fill="currentColor"><path d="M373 411c-28.5 0-51.7 23.3-51.7 52s23.2 52 51.7 52 51.7-23.3 51.7-52-23.2-52-51.7-52zm555-251H96c-17.7 0-32 14.3-32 32v640c0 17.7 14.3 32 32 32h832c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM608 420c0-4.4 1-8 2.3-8h123.4c1.3 0 2.3 3.6 2.3 8v48c0 4.4-1 8-2.3 8H610.3c-1.3 0-2.3-3.6-2.3-8v-48zm-86 253h-43.9c-4.2 0-7.6-3.3-7.9-7.5-3.8-50.5-46-90.5-97.2-90.5s-93.4 40-97.2 90.5c-.3 4.2-3.7 7.5-7.9 7.5H224a8 8 0 01-8-8.4c2.8-53.3 32-99.7 74.6-126.1a111.8 111.8 0 01-29.1-75.5c0-61.9 49.9-112 111.4-112s111.4 50.1 111.4 112c0 29.1-11 55.5-29.1 75.5 42.7 26.5 71.8 72.8 74.6 126.1.4 4.6-3.2 8.4-7.8 8.4zm278.9-53H615.1c-3.9 0-7.1-3.6-7.1-8v-48c0-4.4 3.2-8 7.1-8h185.7c3.9 0 7.1 3.6 7.1 8v48h.1c0 4.4-3.2 8-7.1 8z"/></svg>
                <ProofreadInlineText :text="basic.jobTitle" module-type="basic" field-path="jobTitle" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.location" class="workPlace">
                <svg class="anticon" viewBox="64 64 896 896" fill="currentColor"><path d="M512 327c-29.9 0-58 11.6-79.2 32.8A111.6 111.6 0 00400 439c0 29.9 11.7 58 32.8 79.2A111.6 111.6 0 00512 551c29.9 0 58-11.7 79.2-32.8C612.4 497 624 468.9 624 439c0-29.9-11.6-58-32.8-79.2S541.9 327 512 327zm342.6-37.9a362.49 362.49 0 00-79.9-115.7 370.83 370.83 0 00-118.2-77.8C610.7 76.6 562.1 67 512 67c-50.1 0-98.7 9.6-144.5 28.5-44.3 18.3-84 44.5-118.2 77.8A363.6 363.6 0 00169.4 289c-19.5 45-29.4 92.8-29.4 142 0 70.6 16.9 140.9 50.1 208.7 26.7 54.5 64 107.6 111 158.1 80.3 86.2 164.5 138.9 188.4 153a43.9 43.9 0 0022.4 6.1c7.8 0 15.5-2 22.4-6.1 23.9-14.1 108.1-66.8 188.4-153 47-50.4 84.3-103.6 111-158.1C867.1 572 884 501.8 884 431.1c0-49.2-9.9-97-29.4-142zM512 615c-97.2 0-176-78.8-176-176s78.8-176 176-176 176 78.8 176 176-78.8 176-176 176z"/></svg>
                <ProofreadInlineText :text="basic.location" module-type="basic" field-path="location" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.gender" class="currentStatus">
                <svg class="anticon anticon-stroke" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <template v-if="basic.gender.includes('男')">
                    <path d="M15 5h4v4"/>
                    <path d="M10 14L19 5"/>
                    <circle cx="9" cy="15" r="5"/>
                  </template>
                  <template v-else>
                    <circle cx="12" cy="8" r="5"/>
                    <path d="M12 13v6"/>
                    <path d="M9 17h6"/>
                  </template>
                </svg>
                <ProofreadInlineText :text="basic.gender" module-type="basic" field-path="gender" :proofread-highlights="proofreadHighlights" />
              </div>
              <div v-if="basic.salary" class="currentStatus">
                <svg class="anticon anticon-stroke salary-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/><path d="M12 18V6"/></svg>
                {{ basic.salary.endsWith('元') ? basic.salary : basic.salary + '元' }}
              </div>
            </div>
            <div v-if="basicExtras.length" class="profile-2">
              <div v-for="item in basicExtras" :key="item.label" class="extra-item">
                <svg v-if="item.label === '最高学历'" class="anticon-stroke" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 21v-3a2 2 0 0 0-4 0v3"/><path d="M18 4.933V21"/><path d="m4 6 7.106-3.79a2 2 0 0 1 1.788 0L20 6"/><path d="m6 11-3.52 2.147a1 1 0 0 0-.48.854V19a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-5a1 1 0 0 0-.48-.853L18 11"/><path d="M6 4.933V21"/><circle cx="12" cy="9" r="2"/></svg>
                <svg v-else-if="item.label === '个人网站'" class="anticon-stroke" viewBox="0 0 24 24" fill="none" stroke="var(--theme-color)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
                <svg v-else-if="getExtraIcon(item)" class="anticon" viewBox="64 64 896 896" :fill="getExtraIcon(item).s ? 'none' : 'currentColor'" :stroke="getExtraIcon(item).s ? 'currentColor' : 'none'" stroke-width="60" stroke-linecap="round" stroke-linejoin="round"><path :d="getExtraIcon(item).d"/></svg>
                <span class="extra-text">{{ item.value }}</span>
              </div>
            </div>
          </div>
          <div class="avatar">
            <img v-if="basic.avatar" :src="basic.avatar" alt="avatar" />
            <div v-else class="avatar-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="5"/><path d="M20 21a8 8 0 0 0-16 0"/></svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 动态模块 ====== -->
    <template v-for="type in orderedModules" :key="type">
      <template v-if="type === 'education' && education.length">
        <div class="educationList" :class="{ 'highlight-module': highlightModule === 'education' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.education }}</span></div>
          </div>
          <div class="educationList0">
            <div v-for="(item, i) in education" :key="'e'+i" class="educationListContent">
              <div class="educationListRow">
                <span v-if="item.school" class="school"><ProofreadInlineText :text="item.school" module-type="education" :item-index="i" field-path="school" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.academy" class="major"><ProofreadInlineText :text="item.academy" module-type="education" :item-index="i" field-path="academy" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.major" class="major"><ProofreadInlineText :text="item.major" module-type="education" :item-index="i" field-path="major" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.degree" class="academicDegree"><ProofreadInlineText :text="item.degree" module-type="education" :item-index="i" field-path="degree" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.degreeType" class="academicDegree"><ProofreadInlineText :text="item.degreeType" module-type="education" :item-index="i" field-path="degreeType" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.city" class="metaItem"><ProofreadInlineText :text="item.city" module-type="education" :item-index="i" field-path="city" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.startDate" class="startToEndTime">{{ fmtDate(item.startDate) }} ~ {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
              </div>
              <ProofreadRichHtml
                v-if="item.description"
                class="rich-content"
                :style="richStyle"
                :html="item.description"
                module-type="education"
                :item-index="i"
                field-path="description"
                :proofread-highlights="proofreadHighlights"
              />
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="type === 'experience' && experience.length">
        <div class="experienceList" :class="{ 'highlight-module': highlightModule === 'experience' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.experience }}</span></div>
          </div>
          <div class="experienceList0">
            <div v-for="(item, i) in experience" :key="'x'+i" class="experienceListContent">
              <div class="experienceListRow">
                <span v-if="item.company" class="company"><ProofreadInlineText :text="item.company" module-type="experience" :item-index="i" field-path="company" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.department" class="position"><ProofreadInlineText :text="item.department" module-type="experience" :item-index="i" field-path="department" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.position" class="position"><ProofreadInlineText :text="item.position" module-type="experience" :item-index="i" field-path="position" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.startDate" class="startToEndTime">{{ fmtDate(item.startDate) }} ~ {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="rich-content"
                :style="richStyle"
                :html="item.content"
                module-type="experience"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="type === 'project' && project.length">
        <div class="projectList" :class="{ 'highlight-module': highlightModule === 'project' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.project }}</span></div>
          </div>
          <div class="projectList0">
            <div v-for="(item, i) in project" :key="'p'+i" class="projectListContent">
              <div v-if="item.name || item.link" class="projectList-0">
                <span v-if="item.name" class="projectName"><ProofreadInlineText :text="item.name" module-type="project" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></span>
                <a v-if="item.link" class="projectLink" :href="item.link" target="_blank" rel="noreferrer">{{ item.link }}</a>
              </div>
              <div v-if="item.startDate || item.role || item.city" class="projectList-1">
                <span class="projectMeta">
                  <span v-if="item.startDate">{{ fmtDate(item.startDate) }} ~ {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
                  <span v-if="item.startDate && item.role"> | </span>
                  <span v-if="item.role"><ProofreadInlineText :text="item.role" module-type="project" :item-index="i" field-path="role" :proofread-highlights="proofreadHighlights" /></span>
                  <span v-if="(item.startDate || item.role) && item.city"> | </span>
                  <span v-if="item.city"><ProofreadInlineText :text="item.city" module-type="project" :item-index="i" field-path="city" :proofread-highlights="proofreadHighlights" /></span>
                </span>
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="rich-content"
                :style="richStyle"
                :html="item.content"
                module-type="project"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="type === 'skill' && skill.length">
        <div class="skillList" :class="{ 'highlight-module': highlightModule === 'skill' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.skill }}</span></div>
          </div>
          <div class="skillList0">
            <div v-for="(item, i) in skill" :key="'s'+i" class="skillListContent">
              <ProofreadRichHtml
                v-if="item.content"
                class="rich-content"
                :style="richStyle"
                :html="item.content"
                module-type="skill"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
              <div v-else-if="item.name" class="skillName">{{ item.name }}</div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="type === 'personalStrengths' && personalStrengths">
        <div class="aboutMe" :class="{ 'highlight-module': highlightModule === 'personalStrengths' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.personalStrengths }}</span></div>
          </div>
          <div class="aboutMe0">
            <ProofreadRichHtml
              class="rich-content"
              :style="richStyle"
              :html="personalStrengths"
              module-type="personalStrengths"
              field-path="content"
              :proofread-highlights="proofreadHighlights"
            />
          </div>
        </div>
      </template>

      <template v-else-if="type === 'award' && award.length">
        <div class="awardList" :class="{ 'highlight-module': highlightModule === 'award' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.award }}</span></div>
          </div>
          <div class="awardList0">
            <div v-for="(item, i) in award" :key="'a'+i" class="awardListContent">
              <div class="awardListRow">
                <span v-if="item.name" class="awardName" style="font-weight: bold; font-size: 16px;"><ProofreadInlineText :text="item.name" module-type="award" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.date" class="startToEndTime">{{ fmtDate(item.date) }}</span>
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="rich-content"
                :style="richStyle"
                :html="item.content"
                module-type="award"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="type === 'portfolio' && portfolio.length">
        <div class="portfolioList" :class="{ 'highlight-module': highlightModule === 'portfolio' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.portfolio }}</span></div>
          </div>
            <div class="portfolioList0">
              <div v-for="(item, i) in portfolio" :key="'f'+i" class="portfolioListContent">
                <div class="portfolioCard">
                  <div class="portfolioCardMain">
                    <div v-if="item.name || item.link" class="portfolioHeader">
                      <span v-if="item.name" class="portfolioName" style="font-weight: bold; font-size: 16px;"><ProofreadInlineText :text="item.name" module-type="portfolio" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></span>
                      <a v-if="item.link" class="portfolioLink" :href="item.link" target="_blank" rel="noreferrer">{{ item.link }}</a>
                      </div>
                      <ProofreadRichHtml
                        v-if="item.content"
                        class="rich-content"
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
            </div>
        </div>
      </template>

      <template v-else-if="type === 'other' && other.length">
        <div class="otherExpList" :class="{ 'highlight-module': highlightModule === 'other' }">
          <div class="icon-title">
            <div class="title"><div class="line"></div><span>{{ moduleLabelMap.other }}</span></div>
          </div>
          <div class="otherExpList0">
            <div v-for="(item, i) in other" :key="'o'+i" class="otherExpListContent">
              <div class="otherExpListRow">
                <span v-if="item.name" class="otherExpName"><ProofreadInlineText :text="item.name" module-type="other" :item-index="i" field-path="name" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.startDate" class="startToEndTime">{{ fmtDate(item.startDate) }} ~ {{ item.endDate ? fmtDate(item.endDate) : '至今' }}</span>
              </div>
              <div v-if="item.role || item.department || item.city" class="otherExpMetaRow">
                <span v-if="item.role" class="metaItem"><ProofreadInlineText :text="item.role" module-type="other" :item-index="i" field-path="role" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.department" class="metaItem"><ProofreadInlineText :text="item.department" module-type="other" :item-index="i" field-path="department" :proofread-highlights="proofreadHighlights" /></span>
                <span v-if="item.city" class="metaItem"><ProofreadInlineText :text="item.city" module-type="other" :item-index="i" field-path="city" :proofread-highlights="proofreadHighlights" /></span>
              </div>
              <ProofreadRichHtml
                v-if="item.content"
                class="rich-content"
                :style="richStyle"
                :html="item.content"
                module-type="other"
                :item-index="i"
                field-path="content"
                :proofread-highlights="proofreadHighlights"
              />
            </div>
          </div>
        </div>
      </template>
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
  const s = {}
  if (props.richFontFamily) s.fontFamily = props.richFontFamily
  if (props.richFontSize) s.fontSize = props.richFontSize + 'px'
  if (props.richLineHeight) s.lineHeight = String(props.richLineHeight)
  return s
})

function parse(type) {
  const c = props.contents.find(c => c.moduleType === type)
  if (!c || !c.contentJson) return type === 'basic' || type === 'personalStrengths' ? null : []
  try { return JSON.parse(c.contentJson) } catch { return type === 'basic' || type === 'personalStrengths' ? null : [] }
}

const basic = computed(() => parse('basic'))

const optionalLabels = {
  wechat: '微信号', age: '年龄',
  workYears: '工作年限', gender: '性别',
}

const extraIcons = {
  '最高学历': 'M915.4 534.4c-18.4 0-33.2 14.8-33.2 33.2 0 18.4 14.8 33.2 33.2 33.2 18.4 0 33.2-14.8 33.2-33.2 0-18.4-14.8-33.2-33.2-33.2zM460 232c-55.2 0-100 44.8-100 100v124H236c-55.2 0-100 44.8-100 100v124c0 55.2 44.8 100 100 100h124v124c0 55.2 44.8 100 100 100h124c55.2 0 100-44.8 100-100V680h124c55.2 0 100-44.8 100-100V456c0-55.2-44.8-100-100-100H684V332c0-55.2-44.8-100-100-100H460zm-96 224h244v244H364V456zm276-244c0-30.8 25.2-56 56-56h68c30.8 0 56 25.2 56 56v68c0 30.8-25.2 56-56 56h-68c-30.8 0-56-25.2-56-56v-68zm0 276c0-30.8 25.2-56 56-56h68c30.8 0 56 25.2 56 56v68c0 30.8-25.2 56-56 56h-68c-30.8 0-56-25.2-56-56v-68z',
  '微信号': 'M690.1 377.4c5.9 0 11.8.2 17.6.5-24.4-128.7-158.3-227.1-319.9-227.1C209 150.8 64 271.4 64 420.2c0 81.1 43.6 154.2 111.9 203.6a21.5 21.5 0 019.1 17.6c0 2.4-.5 4.6-1.1 6.9-5.5 20.3-14.2 52.8-14.6 54.3-.7 2.6-1.7 5.2-1.7 7.9 0 5.9 4.8 10.8 10.8 10.8 2.3 0 4.2-.9 6.2-2l70.9-40.9c5.3-3.1 11-5 17.2-5 3.2 0 6.4.5 9.5 1.4 33.1 9.5 68.8 14.8 105.7 14.8 6 0 11.9-.1 17.8-.4-7.1-21-10.9-43.1-10.9-66 0-135.8 132.2-245.8 295.3-245.8zm-194.3-86.5c23.8 0 43.2 19.3 43.2 43.1s-19.3 43.1-43.2 43.1c-23.8 0-43.2-19.3-43.2-43.1s19.4-43.1 43.2-43.1zm-215.9 86.2c-23.8 0-43.2-19.3-43.2-43.1s19.3-43.1 43.2-43.1 43.2 19.3 43.2 43.1-19.4 43.1-43.2 43.1zm586.8 415.6c56.9-41.2 93.2-102 93.2-169.7 0-124-120.8-224.5-269.9-224.5-149 0-269.9 100.5-269.9 224.5S540.9 847.5 690 847.5c30.8 0 60.6-4.4 88.1-12.3 2.6-.8 5.2-1.2 7.9-1.2 5.2 0 9.9 1.6 14.3 4.1l59.1 34c1.7 1 3.3 1.7 5.2 1.7a9 9 0 006.4-2.6 9 9 0 002.6-6.4c0-2.2-.9-4.4-1.4-6.6-.3-1.2-7.6-28.3-12.2-45.3-.5-1.9-.9-3.8-.9-5.7.1-5.9 3.1-11.2 7.6-14.5zM600.2 587.2c-19.9 0-36-16.1-36-35.9 0-19.8 16.1-35.9 36-35.9s36 16.1 36 35.9c0 19.8-16.2 35.9-36 35.9zm179.9 0c-19.9 0-36-16.1-36-35.9 0-19.8 16.1-35.9 36-35.9s36 16.1 36 35.9a36.08 36.08 0 01-36 35.9z',
  '年龄': 'M880 184H712v-64c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v64H384v-64c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v64H144c-17.7 0-32 14.3-32 32v664c0 17.7 14.3 32 32 32h736c17.7 0 32-14.3 32-32V216c0-17.7-14.3-32-32-32zm-40 656H184V460h656v380zM184 392V256h128v48c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8v-48h256v48c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8v-48h128v136H184zm256 216c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8v-56zm192 0c0-4.4-3.6-8-8-8h-56c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h56c4.4 0 8-3.6 8-8v-56z',
  '工作年限': 'M742 318V184h86c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H196c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h86v134c0 81.5 42.4 153.2 106.4 194-64 40.8-106.4 112.5-106.4 194v134h-86c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h632c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8h-86V706c0-81.5-42.4-153.2-106.4-194 64-40.8 106.4-112.5 106.4-194z',
}

const genderIcons = {
  '男': 'M661 587 A224 224 0 1 0 213 587 A224 224 0 1 0 661 587 M660 176 L847 176 L847 363 M847 176 L596 428',
  '女': 'M736 400 A224 224 0 1 0 288 400 A224 224 0 1 0 736 400 M512 624 L512 885 M400 771 L624 771',
}

function getExtraIcon(item) {
  if (item.label === '性别') {
    const v = item.value || ''
    if (v.includes('男')) return { d: genderIcons['男'], s: true }
    if (v.includes('女')) return { d: genderIcons['女'], s: true }
  }
  const d = extraIcons[item.label]
  return d ? { d } : null
}

const suffixMap = { age: '岁', workYears: '年' }

const basicExtras = computed(() => {
  if (!basic.value) return []
  const items = []
  if (basic.value.education) items.push({ label: '最高学历', value: basic.value.education })
  if (basic.value.website) items.push({ label: '个人网站', value: basic.value.website })
  for (const [key, label] of Object.entries(optionalLabels)) {
    if (key === 'gender') continue
    const raw = basic.value[key]
    if (!raw) continue
    const suffix = suffixMap[key] || ''
    const value = suffix && !raw.endsWith(suffix) ? raw + suffix : raw
    items.push({ label, value })
  }
  return items
})
const education = computed(() => parse('education') || [])
const experience = computed(() => parse('experience') || [])
const project = computed(() => parse('project') || [])
const skill = computed(() => parse('skill') || [])
const personalStrengths = computed(() => parse('personalStrengths')?.content || '')
const award = computed(() => parse('award') || [])
const portfolio = computed(() => parse('portfolio') || [])
const other = computed(() => parse('other') || [])

const moduleLabelMap = {
  education: '教育经历', experience: '工作经历', project: '项目经历',
  skill: '专业技能', personalStrengths: '个人优势', award: '荣誉奖项',
  portfolio: '个人作品', other: '其他经历',
}

const orderedModules = computed(() => {
  const order = props.contents.map(c => c.moduleType)
  return order.filter(type => type !== 'basic' && hasContent(type))
})

function hasContent(type) {
  const data = parse(type)
  if (!data) return false
  if (typeof data === 'string') return !!data
  if (!Array.isArray(data)) return Object.values(data).some(v => v && typeof v === 'string' && v.trim())
  if (!data.length) return false
  return data.some(item => item && typeof item === 'object' && Object.values(item).some(v => v && typeof v === 'string' && v.trim()))
}

function fmtDate(d) {
  if (!d) return ''
  return d.replace(/\//g, '-')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&display=swap');

/* === Root === */
.resume-show-new {
  min-height: auto;
  width: 100%;
  max-width: 100%;
  font-size: 15px;
  font-family: 'Noto Sans SC', 'Source Han Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  font-weight: 400;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  line-height: 24px;
  color: rgba(0, 0, 0, 0.85);
  box-sizing: border-box;
}

/* === Profile === */
.profile {
  padding: 20px 35px 0;
}
.profile0 {
  padding-bottom: 8px;
}
.profileContent {
  display: flex;
  justify-content: space-between;
}
.profileNoImageArea {
  display: block;
  flex: 1 1 0%;
  flex-wrap: nowrap;
  padding-left: 16px;
  margin: 0;
}
.profile-0, .profile-1, .profile-2 {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 0 12px;
}
.extra-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-right: 8px;
  color: rgba(0, 0, 0, 0.85);
}
.extra-text {
  color: rgba(0, 0, 0, 0.85);
  font-size: 15px;
}
.name {
  word-break: break-word;
  overflow-wrap: break-word;
  margin: 0 0 8px;
  display: block;
  width: 100%;
  font-weight: bold;
  font-size: 24px;
  text-align: left;
}
.telephone, .email, .github, .currentStatus, .positionTitle, .workPlace, .selfWeb {
  word-break: break-word;
  overflow-wrap: break-word;
  margin-right: 8px;
  display: inline;
  color: rgba(0, 0, 0, 0.85);
}
.anticon {
  width: 1em;
  height: 1em;
  margin-right: 4px;
  color: var(--theme-color);
  vertical-align: -0.15em;
}
.anticon-stroke {
  width: 1em;
  height: 1em;
  margin-right: 4px;
  color: var(--theme-color);
  vertical-align: -0.15em;
}
.salary-icon {
  margin-right: 2px;
}
.avatar {
  width: 80px;
  height: 96px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
}
.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page, #f5f5f5);
  color: var(--text-3, #bbb);
}
.avatar-placeholder svg {
  width: 32px;
  height: 32px;
}

/* === Section Title === */
.icon-title {
  /* wrapper */
}
.title {
  margin-top: 0;
  margin-bottom: 12px;
  position: relative;
  padding-left: 12px;
  font-weight: bold;
  font-size: 20px;
  background: transparent;
  width: 100%;
  box-sizing: border-box;
  border-bottom: none;
}
.line {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 6px;
  height: 100%;
  background: var(--theme-color);
}
.title span {
  color: var(--theme-color);
  background: transparent;
}

/* === Section Content Areas === */
.educationList, .experienceList, .projectList,
.skillList, .aboutMe, .awardList, .portfolioList, .otherExpList {
  padding: 0;
  margin-right: 35px;
  margin-left: 35px;
}
.educationList0, .experienceList0, .projectList0,
.skillList0, .aboutMe0, .awardList0, .portfolioList0, .otherExpList0 {
  padding-bottom: 8px;
}

/* === Row Layout === */
.educationListRow, .experienceListRow, .projectListRow,
.awardListRow, .otherExpListRow {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  flex-wrap: wrap;
}
.school {
  word-break: break-word;
  overflow-wrap: break-word;
  margin: 0 8px;
  font-weight: bold;
  font-size: 17px;
}
.major, .academicDegree, .position, .company {
  word-break: break-word;
  overflow-wrap: break-word;
  margin: 0 8px;
  color: rgba(0, 0, 0, 0.45);
}
.startToEndTime {
  word-break: break-word;
  overflow-wrap: break-word;
  margin: 0 8px 0 auto;
  color: rgba(0, 0, 0, 0.45);
}

/* === Other meta row === */
.otherExpMetaRow {
  display: flex;
  justify-content: flex-start;
}
.metaItem {
  margin: 0 8px;
  color: rgba(0, 0, 0, 0.45);
}

/* === Rich Content (v-html) === */
.rich-content {
  word-break: break-word;
  overflow-wrap: break-word;
  white-space: break-spaces;
  margin-left: 8px;
}
.projectDetail {
  overflow-wrap: anywhere;
  white-space: break-spaces;
}

/* === Project === */
.projectList-0 {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.projectList-1 {
  display: flex;
  justify-content: flex-end;
}
.projectList-2 {
  display: block;
}
.projectName {
  font-weight: bold;
  font-size: 16px;
  word-break: break-word;
  overflow-wrap: break-word;
}
.projectMeta {
  color: rgba(0, 0, 0, 0.45);
  font-size: 13px;
}
.projectLink {
  color: var(--theme-color);
  font-weight: bold;
  margin-left: auto;
  word-break: break-word;
  overflow-wrap: break-word;
}
.projectDetail {
  overflow-wrap: anywhere;
  white-space: break-spaces;
}

/* === Portfolio === */
.portfolioName {
  word-break: break-word;
  overflow-wrap: break-word;
}
.portfolioListContent {
  display: block;
}
.portfolioCard {
  display: block;
}
.portfolioCardMain {
  min-width: 0;
}
.portfolioHeader {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}
.portfolioLink {
  color: var(--theme-color);
  font-weight: bold;
  word-break: break-word;
  overflow-wrap: anywhere;
  flex-shrink: 1;
  margin-left: 4px;
}

/* === Skill name fallback === */
.skillName {
  word-break: break-word;
  overflow-wrap: break-word;
}

/* === Highlight Module (AI suggest preview) === */
.highlight-module {
  background: rgba(255, 235, 59, 0.25);
  border-radius: 4px;
  transition: background 0.3s ease;
}

/* === Print === */
@media print {
  .resume-show-new {
    font-size: 14px;
  }
  .title {
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
}
</style>

<style>
.resume-show-new .rich-content ul,
.resume-show-new .projectDetail ul {
  list-style: none;
  padding-left: 0;
  margin: 4px 0;
}
.resume-show-new .rich-content ol,
.resume-show-new .projectDetail ol {
  padding-left: 20px;
  margin: 4px 0;
}
.resume-show-new .rich-content li,
.resume-show-new .projectDetail li {
  padding-left: 0;
  margin-left: -3px;
}
.resume-show-new .rich-content ul > li::before,
.resume-show-new .projectDetail ul > li::before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
  vertical-align: middle;
  margin-right: 12px;
}
.resume-show-new .rich-content p {
  margin: 2px 0;
}
.resume-show-new [class$="List"],
.resume-show-new .aboutMe {
  page-break-inside: avoid;
}
</style>
