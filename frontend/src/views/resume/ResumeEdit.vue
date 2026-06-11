<template>
  <div class="resume-edit-page">
    <div v-if="layoutReady" ref="toolbarRef" class="edit-toolbar">
      <button class="btn-back" @click="handleBackNavigation">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M19 12H5"/><polyline points="12 19 5 12 12 5"/>
        </svg>
        返回
      </button>
      <div class="toolbar-center">
        <div class="title-wrap">
          <span ref="titleSizer" class="title-sizer" aria-hidden="true">{{ title || '简历标题' }}​</span>
          <input ref="titleInputRef" v-model="title" class="title-input" placeholder="简历标题" />
          <span ref="titleEditIconRef" class="title-edit-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/><path d="m15 5 4 4"/></svg>
          </span>
        </div>
        <div class="version-wrap">
          <button class="version-trigger" @click="toggleVersionList">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>历史版本</span>
          </button>
          <div class="version-trigger-tip">
            最多保存 30 个历史版本，超过后会自动删除最早的版本；已分享的快照链接仍可浏览，并可在分享管理中关闭。
          </div>
          <button v-if="currentVersionId" class="version-badge latest-btn" @click="backToLatest">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 5v14"/><path d="m19 12-7 7-7-7"/></svg>
            返回最新
          </button>
          <Transition name="version-dropdown">
            <div v-if="showVersionList" class="version-dropdown" @scroll="unhoverVersion">
              <div v-if="versions.length === 0" class="version-empty">暂无版本记录</div>
              <div
                v-for="v in versions"
                :key="v.id"
                class="version-item"
                :class="{ active: v.id === currentVersionId }"
                @click="selectVersion(v)"
                @mouseenter="hoverVersion(v, $event)"
                @mouseleave="unhoverVersion"
              >
                <div class="version-item-left">
                  <span class="version-num">v{{ v.versionNum }}</span>
                  <span class="version-time">{{ formatVersionTime(v.createTime) }}</span>
                </div>
                <span class="version-remark">{{ v.remark || '手动保存' }}</span>
              </div>
              <Transition name="version-hover">
                <Teleport to="body">
                  <div v-if="hoveredSnapshot" class="version-hover-preview">
                    <div class="version-hover-paper">
                      <component
                        :is="getSnapshotTemplate(hoveredSnapshot)"
                        :contents="hoveredSnapshot.contents"
                        :theme-color="getSnapshotStyleConfig(hoveredSnapshot).themeColor"
                        :rich-font-family="getSnapshotStyleConfig(hoveredSnapshot).richFontFamily"
                        :rich-font-size="getSnapshotStyleConfig(hoveredSnapshot).richFontSize"
                        :rich-line-height="getSnapshotStyleConfig(hoveredSnapshot).richLineHeight"
                      />
                    </div>
                  </div>
                </Teleport>
              </Transition>
            </div>
          </Transition>
        </div>
        <button v-if="userStore.user" class="toolbar-notice-btn" @click="showNoticePanel = true" title="消息通知">
          <Bell :size="17" />
          <span v-if="unreadCount > 0" class="toolbar-notice-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </button>
      </div>
      <div class="toolbar-action-group" :style="{ marginRight: `${toolbarActionsRightOffset}px`, visibility: layoutReady ? 'visible' : 'hidden' }">
        <button class="btn-save" :class="{ saved, saving }" @click="handleSave" :disabled="saving">
          <svg v-if="!saving" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <span v-else class="btn-spinner"></span>
          <span>{{ saving ? '保存中...' : saved ? '已保存' : '保存' }}</span>
        </button>
        <button class="btn-export" @click="handleExportPdf" :disabled="exporting">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          <span>{{ exporting ? '导出中...' : '导出 PDF' }}</span>
        </button>
      </div>
    </div>

    <div v-if="loaded" class="edit-body" :class="{ 'edit-body--prerender': !layoutReady }">
      <!-- 左侧：模块选择（紧贴左边窗口） -->
      <ModuleSelector v-model="modules" />

      <!-- 中间：编辑列 -->
      <div class="edit-column">
        <div class="edit-actions-bar">
          <div class="edit-actions-inner">
            <div class="action-with-tip action-with-tip-ai-tools">
              <div class="ai-tools-wrap">
                <button
                  class="action-btn ai-tools-trigger"
                  :class="{ on: showAiToolsMenu || showAiChat || showMatchDialog || showSelfIntroDialog || showScoreDialog || showProofreadDialog }"
                  @click.stop="toggleAiToolsMenu"
                  @mouseenter="showAiToolsRootTip = true"
                  @mouseleave="showAiToolsRootTip = false"
                  >
                    <span class="action-btn-content">
                      <WandSparkles :size="16" />
                      <span class="action-btn-label">AI工具</span>
                      <ChevronDown :size="14" class="ai-tools-trigger-arrow" />
                    </span>
                  </button>
                <div v-show="showAiToolsRootTip" class="ai-tools-root-tip">
                  在一个入口里集中使用 AI 助手、岗位匹配、自我介绍、简历打分和语法纠错能力。
                </div>
                <Transition name="ai-tools-menu">
                  <div v-if="showAiToolsMenu" class="ai-tools-menu" @click.stop>
                    <div class="ai-tool-option-wrap" @mouseenter="hoveredAiToolTip = 'chat'" @mouseleave="hoveredAiToolTip = ''">
                      <button class="ai-tool-option" :class="{ active: showAiChat }" @click="openAiTool('chat')">
                        <Sparkles :size="16" />
                        <span>智能助手</span>
                      </button>
                      <div v-show="hoveredAiToolTip === 'chat'" class="ai-tool-tip">通过对话生成、润色和调整简历内容，修改建议可直接应用到当前简历。</div>
                    </div>
                    <div class="ai-tool-option-wrap" @mouseenter="hoveredAiToolTip = 'match'" @mouseleave="hoveredAiToolTip = ''">
                      <button class="ai-tool-option" :class="{ active: showMatchDialog }" @click="openAiTool('match')">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                        <span>岗位匹配</span>
                      </button>
                      <div v-show="hoveredAiToolTip === 'match'" class="ai-tool-tip">粘贴岗位描述后，系统会分析当前简历的匹配度、亮点和待补足项。</div>
                    </div>
                    <div class="ai-tool-option-wrap" @mouseenter="hoveredAiToolTip = 'self-intro'" @mouseleave="hoveredAiToolTip = ''">
                      <button class="ai-tool-option" :class="{ active: showSelfIntroDialog }" @click="openAiTool('self-intro')">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3Z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><path d="M12 19v2"/><path d="M8 21h8"/></svg>
                        <span>自我介绍</span>
                      </button>
                      <div v-show="hoveredAiToolTip === 'self-intro'" class="ai-tool-tip">根据当前简历内容生成面试自我介绍，支持复制与导出图片。</div>
                    </div>
                    <div class="ai-tool-option-wrap" @mouseenter="hoveredAiToolTip = 'score'" @mouseleave="hoveredAiToolTip = ''">
                      <button class="ai-tool-option" :class="{ active: showScoreDialog }" @click="openAiTool('score')">
                        <FileText class="resume-score-icon" :size="16" />
                        <span>简历打分</span>
                      </button>
                      <div v-show="hoveredAiToolTip === 'score'" class="ai-tool-tip">不依赖岗位描述，直接对当前简历做通用评分，并给出优化建议。</div>
                    </div>
                    <div class="ai-tool-option-wrap" @mouseenter="hoveredAiToolTip = 'proofread'" @mouseleave="hoveredAiToolTip = ''">
                      <button class="ai-tool-option" :class="{ active: showProofreadDialog }" @click="openAiTool('proofread')">
                        <BadgeCheck :size="16" />
                        <span>语法纠错</span>
                      </button>
                      <div v-show="hoveredAiToolTip === 'proofread'" class="ai-tool-tip">检查错别字、病句和不自然表达，给出可手动应用的修改建议。</div>
                    </div>
                  </div>
                </Transition>
              </div>
            </div>
            <div class="action-with-tip">
              <button
                class="action-btn share-action-btn"
                :class="{ copied: shareState === 'copied' || shareState === 'generated', loading: sharing, error: shareState === 'error' }"
                :disabled="sharing"
                @click="handleShare"
                >
                  <span class="action-btn-content">
                    <svg v-if="sharing" class="share-loading-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><path d="M21 12a9 9 0 0 1-9 9"/></svg>
                    <svg v-else-if="shareState === 'copied' || shareState === 'generated'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>
                    <span class="action-btn-label">{{ shareButtonText }}</span>
                  </span>
                </button>
              <div class="action-tip action-tip-wide">
                配置密码和有效期后生成链接，并自动复制到剪贴板。
              </div>
            </div>
              <div class="action-with-tip">
                <button class="action-btn" @click="openShareManager">
                  <span class="action-btn-content">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 6h16"/><path d="M4 12h16"/><path d="M4 18h10"/></svg>
                    <span class="action-btn-label">分享管理</span>
                  </span>
                </button>
              <div class="action-tip action-tip-wide">
                查看已生成的分享快照，复制链接或关闭公开访问。
              </div>
            </div>
              <div class="action-with-tip">
                <button class="action-btn" :class="{ on: noticeEnabled }" @click="toggleNotice">
                  <span class="action-btn-content">
                    <BellRing v-if="noticeEnabled" :size="16" />
                    <BellOff v-else :size="16" />
                    <span class="action-btn-label">邮件通知</span>
                  </span>
                </button>
              <div class="action-tip action-tip-wide">
                简历长时间未更新时，系统会邮件提醒您，并提供优化建议和完整度分析。
              </div>
            </div>
          </div>
        </div>
        <div class="form-panel">
          <div class="form-scroll">
            <div
            v-for="(mod, index) in enabledModules"
            :key="mod.key"
            class="accordion-item"
            :class="{ open: expandedKey === mod.key }"
          >
            <div class="accordion-header">
              <span class="accordion-arrow" @click.stop="toggleExpand(mod.key)">
                <ChevronRight
                  v-if="expandedKey !== mod.key"
                  :size="18"
                />
                <ChevronDown
                  v-else
                  :size="18"
                />
              </span>
              <button class="accordion-toggle" @click="toggleExpand(mod.key)">
                <component :is="getEditorIcon(mod.key)" :size="18" class="accordion-icon" />
                <span class="accordion-title">{{ mod.label }}</span>
              </button>
              <div class="accordion-actions">
                <button
                  :disabled="index <= 1"
                  class="btn-action"
                  @click.stop="moveModule(mod.key, -1)"
                  title="上移"
                >
                  <ChevronUp :size="16" />
                </button>
                <button
                  :disabled="mod.key === 'basic' || index === enabledModules.length - 1"
                  class="btn-action"
                  @click.stop="moveModule(mod.key, 1)"
                  title="下移"
                >
                  <ChevronDown :size="16" />
                </button>
                <button
                  :disabled="mod.key === 'basic'"
                  class="btn-action btn-del"
                  @click.stop="removeModule(mod.key)"
                  title="删除模块"
                >
                  <Trash2 :size="16" />
                </button>
              </div>
            </div>
            <div class="accordion-body" v-show="expandedKey === mod.key">
              <component
                :is="editorMap[mod.key]"
                v-model="moduleData[mod.key]"
                :label="mod.label"
                v-bind="getEditorProps(mod.key)"
              />
            </div>
          </div>
          <div v-if="!enabledModules.length" class="empty-editor">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
              <rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
            <p>请在左侧选择模块开始编辑</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：简历预览 -->
      <div class="preview-panel">
        <div class="theme-bar">
          <div class="theme-bar-inner" :style="{ width: `${A4_W * previewScale}px` }">
            <div class="theme-bar-left">
              <div
                v-for="preset in themePresets" :key="preset.color"
                class="theme-dot"
                :class="{ active: themeColor === preset.color }"
                :style="{ background: preset.color }"
                :title="preset.name"
                @click="themeColor = preset.color"
              />
            </div>
            <div class="theme-bar-divider" aria-hidden="true">|</div>
            <div class="theme-bar-right">
                <div class="theme-select-wrap theme-select-wrap-template" :class="{ open: openThemeSelect === 'template' }">
                  <button class="theme-select-trigger theme-select-trigger-template" type="button" @click.stop="toggleThemeSelect('template')">
                    <span class="theme-select-trigger-content">
                      <span class="theme-select-trigger-label">模板</span>
                      <ChevronDown :size="14" class="theme-select-icon" />
                    </span>
                  </button>
                </div>
                <div class="theme-select-wrap" :class="{ open: openThemeSelect === 'fontFamily' }">
                  <button class="theme-select-trigger" type="button" @click.stop="toggleThemeSelect('fontFamily')">
                    <span class="theme-select-trigger-content">
                      <span class="theme-select-trigger-label">{{ currentFontFamilyLabel }}</span>
                      <ChevronDown :size="14" class="theme-select-icon" />
                    </span>
                  </button>
                <div v-if="openThemeSelect === 'fontFamily'" class="theme-select-menu">
                  <button
                    v-for="option in fontFamilyOptions"
                    :key="option.value"
                    class="theme-select-option"
                    :class="{ active: richFontFamily === option.value }"
                    type="button"
                    @click.stop="selectThemeOption('fontFamily', option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>
                <div class="theme-select-wrap theme-select-wrap-sm" :class="{ open: openThemeSelect === 'fontSize' }">
                  <button class="theme-select-trigger" type="button" @click.stop="toggleThemeSelect('fontSize')">
                    <span class="theme-select-trigger-content">
                      <span class="theme-select-trigger-label">{{ currentFontSizeLabel }}</span>
                      <ChevronDown :size="14" class="theme-select-icon" />
                    </span>
                  </button>
                <div v-if="openThemeSelect === 'fontSize'" class="theme-select-menu">
                  <button
                    v-for="option in fontSizeOptions"
                    :key="option.value"
                    class="theme-select-option"
                    :class="{ active: richFontSize === option.value }"
                    type="button"
                    @click.stop="selectThemeOption('fontSize', option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>
                <div class="theme-select-wrap theme-select-wrap-sm" :class="{ open: openThemeSelect === 'lineHeight' }">
                  <button class="theme-select-trigger" type="button" @click.stop="toggleThemeSelect('lineHeight')">
                    <span class="theme-select-trigger-content">
                      <span class="theme-select-trigger-label">{{ currentLineHeightLabel }}</span>
                      <ChevronDown :size="14" class="theme-select-icon" />
                    </span>
                  </button>
                <div v-if="openThemeSelect === 'lineHeight'" class="theme-select-menu">
                  <button
                    v-for="option in lineHeightOptions"
                    :key="option.value"
                    class="theme-select-option"
                    :class="{ active: richLineHeight === option.value }"
                    type="button"
                    @click.stop="selectThemeOption('lineHeight', option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <TemplateSelectDialog
          :visible="openThemeSelect === 'template'"
          :selected-template="selectedTemplate"
          @close="openThemeSelect = ''"
          @select-template="selectTemplate"
        />
        <div
          ref="scrollRef"
          class="preview-scroll"
        >
          <div
            class="preview-scroll-inner"
            :style="{ width: `${A4_W * previewScale}px`, minHeight: `${pageCount * A4_H * previewScale}px` }"
          >
            <div v-if="pendingSuggestion" class="suggest-bar">
              <span class="suggest-bar-text">AI 建议{{ pendingSuggestion.wasEnabled ? '修改' : '新增' }}【{{ moduleNames[pendingSuggestion.moduleType] || pendingSuggestion.moduleType }}】{{ suggestionQueue.length ? `（剩余 ${suggestionQueue.length} 个）` : '' }}</span>
              <button class="suggest-btn accept" @click="acceptSuggest">接受修改</button>
              <button class="suggest-btn reject" @click="rejectSuggest">撤销</button>
            </div>
            <div
              ref="paperRef"
              class="paper"
              :class="{ 'paper-breakable': showPageBreak }"
              :style="{ minHeight: `${pageCount * A4_H}px`, transform: `scale(${previewScale})` }"
            >
              <div ref="paperContentRef" class="paper-content">
                <component :is="currentTemplate" :contents="previewContents" :theme-color="themeColor" :highlight-module="pendingSuggestion?.moduleType" :proofread-highlights="activeProofreadHighlights" :rich-font-family="richFontFamily" :rich-font-size="richFontSize" :rich-line-height="richLineHeight" />
              </div>
            <div v-if="showPageBreak" v-for="i in pageCount - 1" :key="i" class="page-break-label" :style="{ top: A4_H * i + 'px' }">
              <svg class="page-break-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <circle cx="6" cy="6" r="3"></circle>
                <circle cx="6" cy="18" r="3"></circle>
                <path d="M20 4 8.12 15.88"></path>
                <path d="M14.47 14.48 20 20"></path>
                <path d="M8.12 8.12 12 12"></path>
              </svg>
              <span>分页</span>
            </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!layoutReady" class="edit-loading-state">
      <div class="edit-loading-card">
        <span class="btn-spinner edit-loading-spinner"></span>
        <span>加载简历中...</span>
      </div>
    </div>
    <AiChatDialog
      :visible="showAiChat"
      :resume-id="resumeId"
      :module-data="moduleData"
      @close="showAiChat = false"
      @suggest="handleSuggest"
    />
    <MatchDialog
      :visible="showMatchDialog"
      :resume-id="resumeId"
      :module-data="moduleData"
      @close="showMatchDialog = false"
    />
    <SelfIntroDialog
      :visible="showSelfIntroDialog"
      :resume-id="resumeId"
      :module-data="moduleData"
      @close="showSelfIntroDialog = false"
    />
    <ResumeScoreDialog
      :visible="showScoreDialog"
      :resume-id="resumeId"
      :module-data="moduleData"
      @close="showScoreDialog = false"
    />
    <ProofreadDialog
      :visible="showProofreadDialog"
      :resume-id="resumeId"
      :module-data="moduleData"
      @close="showProofreadDialog = false"
      @apply="applyProofreadSuggestion"
      @apply-all="applyAllProofreadSuggestions"
      @ignore="ignoreProofreadSuggestion"
      @update-highlights="updateProofreadHighlights"
    />
    <Teleport to="body">
      <div
        class="guest-draft-modal-overlay"
        :style="resolvedGuestDraftModalOverlayStyle"
        :aria-hidden="showGuestDraftRestorePrompt ? 'false' : 'true'"
        @click.self="startOverGuestDraft"
      >
        <div class="guest-draft-modal-box" :style="guestDraftModalBoxStyle">
          <div class="guest-draft-modal-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15.2 3a2 2 0 0 1 1.4.6l3.8 3.8a2 2 0 0 1 .6 1.4V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"/>
              <path d="M17 21v-8H7v8"/>
              <path d="M7 3v5h8"/>
            </svg>
          </div>
          <h3 class="guest-draft-modal-title">发现上次草稿</h3>
          <p class="guest-draft-modal-desc">检测到 7 天内保存的游客草稿，继续编辑会恢复上次内容。</p>
          <div class="guest-draft-modal-actions" :style="guestDraftModalActionsStyle">
            <button class="modal-btn modal-btn--cancel" @click="startOverGuestDraft">重新开始</button>
            <button class="modal-btn modal-btn--danger" @click="restoreGuestDraftFromPrompt">继续编辑</button>
          </div>
        </div>
      </div>
    </Teleport>
    <Teleport to="body">
      <div v-if="showEmailPrompt" class="email-prompt-overlay" @click.self="showEmailPrompt = false">
        <div class="email-prompt-box">
          <h3>开启消息通知</h3>
          <p>需要先填写邮箱才能接收通知邮件</p>
          <input v-model="emailInput" placeholder="请输入邮箱地址" @keyup.enter="confirmEmail" />
          <div class="email-prompt-actions">
            <button class="btn-cancel" @click="showEmailPrompt = false">取消</button>
            <button class="btn-confirm" @click="confirmEmail" :disabled="!emailInput.trim()">确定</button>
          </div>
        </div>
      </div>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showNoChangeSavePrompt" class="nochange-save-overlay" @click.self="showNoChangeSavePrompt = false">
          <div class="nochange-save-box">
            <div class="nochange-save-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 8v4"></path>
                <path d="M12 16h.01"></path>
              </svg>
            </div>
            <h3>无需保存</h3>
            <p>当前简历内容与上个已保存版本相比没有发生变化，无需重复保存。</p>
            <div class="nochange-save-actions">
              <button class="btn-confirm" @click="showNoChangeSavePrompt = false">我知道了</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showShareManager" class="share-manager-overlay" @click.self="showShareManager = false">
          <div class="share-manager-panel">
            <div class="share-manager-header">
              <div>
                <h3>分享管理</h3>
                <p>所有分享都是固定快照，后续编辑不会影响已生成链接。</p>
              </div>
              <button class="share-manager-close" @click="showShareManager = false">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="share-manager-body">
              <div v-if="shareListLoading" class="share-manager-empty">加载中...</div>
              <div v-else-if="shareList.length === 0" class="share-manager-empty">暂无分享链接</div>
              <div v-else class="share-record-list">
                <div v-for="item in shareList" :key="item.id" class="share-record">
                  <div class="share-record-main">
                    <div class="share-record-title">
                      {{ shareSourceLabel(item) }}
                      <span class="share-record-status" :class="{ closed: item.status !== 1, expired: item.expired && item.status === 1 }">
                        {{ item.status !== 1 ? '已关闭' : item.expired ? '已过期' : '已开启' }}
                      </span>
                    </div>
                    <div class="share-record-meta">
                      <div>访问 {{ item.viewCount || 0 }} 次</div>
                      <div>{{ formatShareTime(item.createTime) }} · {{ shareExpireRemainingLabel(item) }}</div>
                    </div>
                  </div>
                  <div class="share-record-actions">
                    <button class="share-record-btn" :disabled="item.status !== 1 || item.expired" @click="previewShareRecord(item)">预览</button>
                    <button class="share-record-btn" :disabled="item.status !== 1 || item.expired" @click="openSharePasswordDialog(item)">密码</button>
                    <button class="share-record-btn" :disabled="item.status !== 1 || item.expired" @click="openShareExpireDialog(item)">有效期</button>
                    <div class="share-record-btn-spacer" aria-hidden="true"></div>
                    <button
                      class="share-record-btn"
                      :class="{ copied: shareRecordCopiedId === item.id, loading: shareRecordCopyingId === item.id }"
                      :disabled="item.status !== 1 || item.expired || shareRecordCopyingId === item.id"
                      @click="copyShareRecord(item)"
                    >
                      <span v-if="shareRecordCopyingId === item.id" class="share-record-btn-content">
                        <span class="btn-spinner share-record-btn-spinner"></span>
                        复制中
                      </span>
                      <span v-else-if="shareRecordCopiedId === item.id" class="share-record-btn-content">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                        已复制
                      </span>
                      <span v-else>复制</span>
                    </button>
                    <button class="share-record-btn danger" :disabled="item.status !== 1 || item.expired" @click="handleCloseShare(item)">关闭</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showUnsavedSharePrompt" class="unsaved-share-overlay" @click.self="!savingBeforeShare && (showUnsavedSharePrompt = false)">
          <div class="unsaved-share-box">
            <div class="unsaved-share-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 9v4"/><path d="M12 17h.01"/><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/></svg>
            </div>
            <h3>有未保存修改</h3>
            <p>{{ savingBeforeShare ? '正在保存当前修改，完成后会自动进入分享设置。' : (hasSavedSnapshot ? '当前修改尚未保存，请先保存后再分享。' : '分享前需要先保存当前简历。') }}</p>
            <div class="unsaved-share-actions">
              <button class="btn-cancel" :disabled="savingBeforeShare" @click="cancelPendingShare">取消</button>
              <button class="btn-confirm" :disabled="savingBeforeShare" @click="saveBeforeShare">
                <span v-if="savingBeforeShare" class="unsaved-share-btn-content">
                  <span class="btn-spinner unsaved-share-btn-spinner"></span>
                  保存中...
                </span>
                <span v-else>先保存再分享</span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showUnsavedLeavePrompt" class="share-config-overlay" @click.self="!savingBeforeLeave && closeUnsavedLeavePrompt()">
          <div class="share-config-panel share-config-panel-sm">
            <div class="share-config-header">
              <div>
                <h3>有未保存修改</h3>
                <p class="share-config-subtitle">
                  {{ savingBeforeLeave ? '正在保存当前修改，完成后会自动返回。' : '当前简历尚未保存，是否先保存再离开？' }}
                </p>
              </div>
              <button class="share-config-close" :disabled="savingBeforeLeave" @click="closeUnsavedLeavePrompt">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18"/><path d="M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="share-config-actions">
              <button class="modal-btn modal-btn--cancel" :disabled="savingBeforeLeave" @click="closeUnsavedLeavePrompt">取消</button>
              <button class="modal-btn modal-btn--ghost" :disabled="savingBeforeLeave" @click="leaveWithoutSaving">直接离开</button>
              <button class="modal-btn modal-btn--confirm" :disabled="savingBeforeLeave" @click="saveBeforeLeave">
                <span v-if="savingBeforeLeave" class="share-config-btn-content">
                  <span class="btn-spinner share-config-btn-spinner"></span>
                  保存中...
                </span>
                <span v-else>保存并返回</span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showShareConfigDialog" class="share-config-overlay" @click.self="closeShareConfigDialog">
          <div class="share-config-panel">
            <div class="share-config-header">
              <div>
                <h3>分享设置</h3>
                <p>为这次分享设置访问密码和链接有效期，生成后会自动复制链接。</p>
              </div>
              <button class="share-config-close" @click="closeShareConfigDialog">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="share-config-body">
              <div class="share-config-group">
                <div class="share-config-label">访问密码</div>
                <div class="share-password-mode">
                  <button
                    class="share-segment-btn"
                    :class="{ active: !shareConfig.usePassword }"
                    @click="setSharePasswordMode(false)"
                  >不设置</button>
                  <button
                    class="share-segment-btn"
                    :class="{ active: shareConfig.usePassword }"
                    @click="setSharePasswordMode(true)"
                  >设置密码</button>
                </div>
                <div v-if="shareConfig.usePassword" class="share-password-box">
                  <input
                    v-model="shareConfig.password"
                    class="share-config-input"
                    type="text"
                    inputmode="numeric"
                    maxlength="6"
                    placeholder="请输入 6 位数字密码"
                    @input="sanitizeSharePassword"
                  />
                  <div class="share-config-hint">密码为 6 位纯数字，可在分享管理中查看或修改。</div>
                </div>
              </div>
                <div class="share-config-group">
                  <div class="share-config-label">有效期</div>
                  <div class="share-expire-options">
                    <button
                      v-for="option in presetExpireOptions"
                      :key="option.value"
                      class="share-chip-btn"
                      :class="{ active: shareConfig.expireMode === option.value }"
                      @click="selectShareExpire(option.value)"
                    >{{ option.label }}</button>
                    <button
                      v-if="shareConfig.expireMode !== 'custom'"
                      class="share-chip-btn"
                      :class="{ active: shareConfig.expireMode === 'custom' }"
                      @click="selectShareExpire('custom')"
                    >自定义</button>
                    <div v-else class="share-chip-input-wrap">
                      <input
                        v-model="shareConfig.customDays"
                        class="share-chip-input"
                        type="text"
                        inputmode="numeric"
                        @input="sanitizeCustomDays"
                      />
                      <span class="share-chip-input-unit">天</span>
                    </div>
                  </div>
                  <div class="share-config-hint">默认 30 天；永久有效的链接也可在分享管理中手动关闭。</div>
                </div>
            </div>
            <div class="share-config-actions">
              <button class="modal-btn modal-btn--cancel" @click="closeShareConfigDialog">取消</button>
              <button class="modal-btn modal-btn--confirm" :disabled="sharing || !canSubmitShareConfig" @click="confirmShareConfig">
                <span v-if="sharing" class="share-config-btn-content">
                  <span class="btn-spinner share-config-btn-spinner"></span>
                  {{ shareConfig.usePassword ? '生成中...' : '分享中...' }}
                </span>
                <span v-else>{{ shareConfig.usePassword ? '生成链接' : '直接分享' }}</span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showSharePasswordDialog" class="share-config-overlay" @click.self="closeSharePasswordDialog">
          <div class="share-config-panel share-config-panel-sm">
            <div class="share-config-header">
              <div>
                <h3>管理密码</h3>
                <p>查看当前访问密码，或直接修改为新的 6 位数字密码。</p>
              </div>
              <button class="share-config-close" @click="closeSharePasswordDialog">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="share-config-body">
              <div class="share-config-group">
                <div class="share-config-label">当前密码</div>
                <div class="share-current-password">{{ sharePasswordForm.currentPassword || '未设置密码' }}</div>
              </div>
              <div class="share-config-group">
                <div class="share-config-label">新密码</div>
                <input
                  v-model="sharePasswordForm.nextPassword"
                  class="share-config-input"
                  type="text"
                  inputmode="numeric"
                  maxlength="6"
                  placeholder="输入新的 6 位数字密码"
                  @input="sanitizeManagerPassword"
                />
                <div class="share-config-hint">留空并点击清除密码，可移除当前访问密码。</div>
              </div>
              </div>
              <div class="share-config-actions">
                <div class="modal-btn-hint-wrap">
                  <button class="modal-btn modal-btn--ghost" :disabled="sharePasswordSubmitting || !sharePasswordForm.currentPassword" @click="clearSharePassword">
                    <span v-if="sharePasswordSubmitting && sharePasswordSubmitAction === 'clear'" class="share-config-btn-content">
                      <span class="btn-spinner share-config-btn-spinner"></span>
                      清除中...
                    </span>
                    <span v-else>清除密码</span>
                  </button>
                </div>
                <div class="modal-btn-hint-wrap">
                  <button class="modal-btn modal-btn--cancel" :disabled="sharePasswordSubmitting" @click="closeSharePasswordDialog">取消</button>
                </div>
                <div class="modal-btn-hint-wrap" :data-tooltip="sharePasswordSubmitBlockedReason || null">
                  <button class="modal-btn modal-btn--confirm" :disabled="sharePasswordSubmitting || !canSubmitSharePassword" @click="submitSharePassword">
                    <span v-if="sharePasswordSubmitting && sharePasswordSubmitAction === 'save'" class="share-config-btn-content">
                      <span class="btn-spinner share-config-btn-spinner"></span>
                      保存中...
                    </span>
                    <span v-else>保存</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Transition>
    </Teleport>
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showShareExpireDialog" class="share-config-overlay" @click.self="closeShareExpireDialog">
          <div class="share-config-panel share-config-panel-sm">
            <div class="share-config-header">
              <div>
                <h3>设置有效期</h3>
                <p>更新当前分享链接的过期时间，默认建议保留 30 天。</p>
              </div>
              <button class="share-config-close" @click="closeShareExpireDialog">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
            <div class="share-config-body">
              <div class="share-config-group">
                <div class="share-config-label">当前有效期</div>
                <div class="share-current-password">{{ shareExpireLabel(shareExpireForm.targetItem) }}</div>
              </div>
                <div class="share-config-group">
                  <div class="share-config-label">新的有效期</div>
                  <div class="share-expire-options">
                    <button
                      v-for="option in presetExpireOptions"
                      :key="`manager-${option.value}`"
                      class="share-chip-btn"
                      :class="{ active: shareExpireForm.expireMode === option.value }"
                      @click="selectManagerExpire(option.value)"
                    >{{ option.label }}</button>
                    <button
                      v-if="shareExpireForm.expireMode !== 'custom'"
                      class="share-chip-btn"
                      :class="{ active: shareExpireForm.expireMode === 'custom' }"
                      @click="selectManagerExpire('custom')"
                    >自定义</button>
                    <div v-else class="share-chip-input-wrap">
                      <input
                        v-model="shareExpireForm.customDays"
                        class="share-chip-input"
                        type="text"
                        inputmode="numeric"
                        @input="sanitizeManagerCustomDays"
                      />
                      <span class="share-chip-input-unit">天</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="share-config-actions">
                <div class="modal-btn-hint-wrap">
                  <button class="modal-btn modal-btn--cancel" :disabled="shareExpireSubmitting" @click="closeShareExpireDialog">取消</button>
                </div>
                <div class="modal-btn-hint-wrap" :data-tooltip="shareExpireSubmitBlockedReason || null">
                  <button class="modal-btn modal-btn--confirm" :disabled="shareExpireSubmitting || !canSubmitManagerExpire" @click="submitShareExpire">
                    <span v-if="shareExpireSubmitting" class="share-config-btn-content">
                      <span class="btn-spinner share-config-btn-spinner"></span>
                      保存中...
                    </span>
                    <span v-else>保存</span>
                  </button>
                </div>
              </div>
            </div>
        </div>
      </Transition>
    </Teleport>
    <NoticeCenterDialog v-model="showNoticePanel" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, markRaw, watch, nextTick, onUpdated, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'
import { inlineImageSourcesForExport } from './exportInlineAssets'
import { ChevronDown, ChevronRight, ChevronUp, Sparkles, WandSparkles, FileText, Bell, BellRing, BellOff, Trash2, BadgeCheck } from 'lucide-vue-next'
import {
  User, GraduationCap, Briefcase, Wrench, FolderKanban,
  Star, Trophy, Image, MoreHorizontal, LayoutGrid,
} from 'lucide-vue-next'
import { getResume, addResume, updateResume } from '../../api/resume'
import { saveVersion, listVersions } from '../../api/version'
import { analyzeAndNotify } from '../../api/notice'
import { createShare, listShares, closeShare, updateSharePassword, updateShareExpire } from '../../api/share'
import { updateMyInfo } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { useResumeListCache } from '../../composables/useResumeListCache'
import { clearGuestResumeDraft, loadGuestResumeDraft, readGuestResumeDraftRecord, saveGuestResumeDraft } from '../../composables/useGuestResumeDraft'
import NoticeCenterDialog from '../../components/NoticeCenterDialog.vue'
import { useNotice } from '../../composables/useNotice'
const { unreadCount, fetchUnread } = useNotice()
import ModuleSelector from './editors/ModuleSelector.vue'
import BasicEditor from './editors/BasicEditor.vue'
import EducationEditor from './editors/EducationEditor.vue'
import ExperienceEditor from './editors/ExperienceEditor.vue'
import ProjectEditor from './editors/ProjectEditor.vue'
import SkillEditor from './editors/SkillEditor.vue'
import SelfIntroEditor from './editors/SelfIntroEditor.vue'
import AwardEditor from './editors/AwardEditor.vue'
import PortfolioEditor from './editors/PortfolioEditor.vue'
import OtherEditor from './editors/OtherEditor.vue'
import AiChatDialog from './editors/AiChatDialog.vue'
import MatchDialog from './editors/MatchDialog.vue'
import ResumeScoreDialog from './editors/ResumeScoreDialog.vue'
import ProofreadDialog from './editors/ProofreadDialog.vue'
import SelfIntroDialog from './components/SelfIntroDialog.vue'
import {
  DEFAULT_RICH_FONT_FAMILY,
  DEFAULT_RICH_FONT_SIZE,
  DEFAULT_RICH_LINE_HEIGHT,
  DEFAULT_THEME_COLOR,
  hasStoredStyleConfig,
  readStyleConfig,
  serializeStyleConfig,
  stripLegacyStyleFieldsFromBasic,
} from './styleConfig'
import { COMPACT_PLACEHOLDER_DATA, DEFAULT_MODULES, PLACEHOLDER_DATA } from './resumeDefaults'
import {
  DEFAULT_TEMPLATE_ID,
  normalizeTemplateId,
  TEMPLATE_COMPONENTS,
} from './templateRegistry'
import TemplateSelectDialog from './components/TemplateSelectDialog.vue'
import { applySuggestionDraft, rejectSuggestionDraft } from './utils/suggestionFlow'
import { replaceProofreadContent } from './proofreadApply'

const route = useRoute()
const router = useRouter()
const { refresh: refreshList } = useResumeListCache()
const sharing = ref(false)
const shareState = ref('')
let shareStateTimer = null
const showShareManager = ref(false)
const shareList = ref([])
const shareListLoading = ref(false)
const shareListLoaded = ref(false)
const shareRecordCopyingId = ref(null)
const shareRecordCopiedId = ref(null)
let shareRecordCopyTimer = null
const showUnsavedSharePrompt = ref(false)
const savingBeforeShare = ref(false)
const showUnsavedLeavePrompt = ref(false)
const savingBeforeLeave = ref(false)
const showShareConfigDialog = ref(false)
const showNoticePanel = ref(false)
const showSharePasswordDialog = ref(false)
const showShareExpireDialog = ref(false)
const sharePasswordSubmitting = ref(false)
const sharePasswordSubmitAction = ref(null)
const shareExpireSubmitting = ref(false)
const expireOptions = [
  { label: '7天', value: '7' },
  { label: '30天', value: '30' },
  { label: '永久有效', value: 'forever' },
  { label: '自定义', value: 'custom' },
]
const presetExpireOptions = expireOptions.filter(option => option.value !== 'custom')
const shareConfig = reactive({
  usePassword: false,
  password: '',
  expireMode: '30',
  customDays: '',
})
const pendingShareAfterSave = ref(false)
const sharePasswordForm = reactive({
  targetItem: null,
  currentPassword: '',
  nextPassword: '',
})
const shareExpireForm = reactive({
  targetItem: null,
  expireMode: '30',
  customDays: '',
})

const shareButtonText = computed(() => {
  if (sharing.value) return '复制中'
  if (shareState.value === 'copied') return '已复制'
  if (shareState.value === 'generated') return '已生成'
  if (shareState.value === 'error') return '分享失败'
  return '分享简历'
})

const canSubmitShareConfig = computed(() => {
  if (shareConfig.usePassword && shareConfig.password.length !== 6) return false
  if (shareConfig.expireMode === 'custom') {
    const days = Number(shareConfig.customDays)
    return Number.isInteger(days) && days > 0
  }
  return true
})

  const canSubmitManagerExpire = computed(() => {
    if (!hasManagerExpireChanged.value) return false
    if (shareExpireForm.expireMode === 'custom') {
      const days = Number(shareExpireForm.customDays)
      return Number.isInteger(days) && days > 0
    }
    return true
  })

  const normalizedCurrentSharePassword = computed(() => {
    const password = sharePasswordForm.currentPassword?.trim()
    return password ? password : ''
  })

  const normalizedNextSharePassword = computed(() => {
    const password = sharePasswordForm.nextPassword?.trim()
    return password ? password : ''
  })

  const canSubmitSharePassword = computed(() =>
    normalizedNextSharePassword.value.length === 6 &&
    normalizedNextSharePassword.value !== normalizedCurrentSharePassword.value
  )

  const sharePasswordSubmitBlockedReason = computed(() => {
    if (sharePasswordSubmitting.value) return ''
    if (normalizedNextSharePassword.value.length === 6 &&
      normalizedNextSharePassword.value === normalizedCurrentSharePassword.value) {
      return '新密码与当前密码一致，未发生变化'
    }
    return ''
  })

  const currentManagerExpireDays = computed(() => {
    const expireDays = shareExpireForm.targetItem?.expireDays
    if (Number.isInteger(expireDays)) return expireDays
    const inferred = inferExpireMode(shareExpireForm.targetItem)
    if (inferred.mode === 'forever') return 0
    if (inferred.mode === 'custom') return Number(inferred.customDays || 0)
    return Number(inferred.mode)
  })

  const hasManagerExpireChanged = computed(() => {
    if (!shareExpireForm.targetItem) return false
    return currentManagerExpireDays.value !== managerExpireDays()
  })

  const shareExpireSubmitBlockedReason = computed(() => {
    if (shareExpireSubmitting.value || !shareExpireForm.targetItem) return ''
    if (!hasManagerExpireChanged.value) {
      return '新的有效期与当前一致，未发生变化'
    }
    return ''
  })

const resume = ref(null)
const title = ref('')
const titleSizer = ref(null)
const titleInputRef = ref(null)
const titleEditIconRef = ref(null)

function updateTitleWidth() {
  const input = titleInputRef.value
  const sizer = titleSizer.value
  if (input && sizer) {
    const icon = titleEditIconRef.value
    const iconW = icon ? icon.offsetWidth : 0
    input.style.width = Math.max(80, sizer.offsetWidth + iconW + 8) + 'px'
  }
}

watch(title, () => nextTick(updateTitleWidth))
const expandedKey = ref('')
const toolbarRef = ref(null)
const paperRef = ref(null)
const paperContentRef = ref(null)
const scrollRef = ref(null)
const pageCount = ref(1)
const showPageBreak = ref(false)
const previewScale = ref(1)
const toolbarActionsRightOffset = ref(0)
const layoutReady = ref(false)
watch(layoutReady, async (ready) => {
  if (!ready) return
  await nextTick()
  requestAnimationFrame(() => updateTitleWidth())
})
const A4_W = 794
const A4_H = 1123
let resizeObserver = null
let toolbarAlignRaf = 0
let guestDraftPromptTimer = null

const themePresets = [
  { name: '经典蓝', color: 'rgb(70, 114, 242)' },
  { name: '墨蓝', color: '#1d4ed8' },
  { name: '活力橙', color: '#f59e0b' },
  { name: '自然绿', color: '#10b981' },
  { name: '优雅紫', color: '#8b5cf6' },
  { name: '热情红', color: '#ef4444' },
  { name: '沉稳灰', color: '#64748b' },
  { name: '玫瑰粉', color: '#ec4899' },
  { name: '深邃青', color: '#0d9488' },
  { name: '酒红', color: '#9f1239' },
  { name: '琥珀金', color: '#d97706' },
]

const fontFamilyOptions = [
  { label: '思源黑体', shortLabel: '思源', value: "'Noto Sans SC', 'Source Han Sans SC', sans-serif" },
  { label: '微软雅黑', shortLabel: '雅黑', value: "'Microsoft YaHei', sans-serif" },
  { label: '宋体', shortLabel: '宋体', value: "'SimSun', serif" },
  { label: '楷体', shortLabel: '楷体', value: "'KaiTi', serif" },
]
const fontSizeOptions = [12, 13, 14, 16].map(value => ({ label: `${value}px`, value }))
const lineHeightOptions = [1, 1.2, 1.5, 1.7, 2].map(value => ({ label: `${value}x`, value }))
const openThemeSelect = ref('')
const richFontFamilyTouched = ref(false)
const richFontSizeTouched = ref(false)
const richLineHeightTouched = ref(false)
const richFontFamily = ref(DEFAULT_RICH_FONT_FAMILY)
const richFontSize = ref(DEFAULT_RICH_FONT_SIZE)
const richLineHeight = ref(DEFAULT_RICH_LINE_HEIGHT)
const themeColor = ref(DEFAULT_THEME_COLOR)
const selectedTemplate = ref(DEFAULT_TEMPLATE_ID)
const currentFontFamilyLabel = computed(() => richFontFamilyTouched.value
  ? (fontFamilyOptions.find(option => option.value === richFontFamily.value)?.shortLabel || '字体')
  : '字体')
const currentFontSizeLabel = computed(() => richFontSizeTouched.value
  ? (fontSizeOptions.find(option => option.value === richFontSize.value)?.label || `${richFontSize.value}px`)
  : '字号')
const currentLineHeightLabel = computed(() => richLineHeightTouched.value
  ? (lineHeightOptions.find(option => option.value === richLineHeight.value)?.label || `${richLineHeight.value}x`)
  : '行距')
provide('richFontFamily', richFontFamily)
provide('richFontSize', richFontSize)
provide('richLineHeight', richLineHeight)

function toggleThemeSelect(key) {
  openThemeSelect.value = openThemeSelect.value === key ? '' : key
}

function setShareState(state) {
  shareState.value = state
  clearTimeout(shareStateTimer)
  shareStateTimer = setTimeout(() => {
    shareState.value = ''
  }, 1800)
}

async function loadShareList(silent = false) {
  if (!resumeId.value) return
  if (!silent) shareListLoading.value = true
  try {
    shareList.value = await listShares(resumeId.value)
    shareListLoaded.value = true
  } catch {
    shareListLoaded.value = false
    if (!silent) shareList.value = []
  } finally {
    if (!silent) shareListLoading.value = false
  }
}

async function openShareManager() {
  if (await requireGuestLogin('share-manage')) return
  showShareManager.value = true
  if (!resumeId.value) {
    shareList.value = []
    shareListLoaded.value = true
    shareListLoading.value = false
    return
  }
  if (!shareListLoaded.value) {
    await loadShareList()
  }
}

  function formatShareTime(str) {
    if (!str) return ''
    const normalized = str.replace('T', ' ')
    const hasTimezone = /(?:Z|[+-]\d{2}:\d{2})$/.test(str)
    if (!hasTimezone) {
      return normalized.substring(0, 16)
    }
    const date = new Date(str)
    if (Number.isNaN(date.getTime())) return normalized.substring(0, 16)
    return new Intl.DateTimeFormat('zh-CN', {
      timeZone: 'Asia/Shanghai',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).format(date).replace(/\//g, '-')
  }

async function copyShareRecord(item) {
  if (shareRecordCopyingId.value === item.id) return
  clearTimeout(shareRecordCopyTimer)
  shareRecordCopiedId.value = null
  shareRecordCopyingId.value = item.id
  const shareUrl = `${window.location.origin}/share/${item.shareKey}`
  const minDelay = new Promise(r => setTimeout(r, 500))
  const copied = await copyText(shareUrl)
  await minDelay
  shareRecordCopyingId.value = null
  if (copied) {
    shareRecordCopiedId.value = item.id
    shareRecordCopyTimer = setTimeout(() => {
      shareRecordCopiedId.value = null
    }, 1800)
  }
}

function previewShareRecord(item) {
  if (item.status !== 1 || item.expired) return
  const shareUrl = `${window.location.origin}/share/${item.shareKey}`
  window.open(shareUrl, '_blank', 'noopener')
}

function shareExpireLabel(item) {
  if (item?.expired) return '已过期'
  if (!item?.expireTime) return '永久有效'
  return `至 ${formatShareTime(item.expireTime)}`
}

function shareSourceLabel(item) {
  return `简历版本 v${item?.sourceVersionNum || '-'}`
}

function shareExpireRemainingLabel(item) {
  if (item?.expired) return '已过期'
  if (!item?.expireTime) return '永久有效'
  const diffMs = new Date(item.expireTime).getTime() - Date.now()
  const days = Math.max(1, Math.ceil(diffMs / 86400000))
  return `剩余 ${days} 天`
}

function resetShareConfig() {
  shareConfig.usePassword = false
  shareConfig.password = ''
  shareConfig.expireMode = '30'
  shareConfig.customDays = ''
}

function setSharePasswordMode(enabled) {
  shareConfig.usePassword = enabled
  if (!enabled) shareConfig.password = ''
}

function sanitizeSharePassword() {
  shareConfig.password = shareConfig.password.replace(/\D/g, '').slice(0, 6)
}

function sanitizeCustomDays() {
  shareConfig.customDays = shareConfig.customDays.replace(/\D/g, '').slice(0, 4)
}

function selectShareExpire(value) {
  shareConfig.expireMode = value
  if (value !== 'custom') shareConfig.customDays = ''
}

function currentShareExpireDays() {
  if (shareConfig.expireMode === 'forever') return 0
  if (shareConfig.expireMode === 'custom') return Number(shareConfig.customDays)
  return Number(shareConfig.expireMode)
}

function openShareConfigDialog() {
  resetShareConfig()
  showShareConfigDialog.value = true
}

function closeShareConfigDialog() {
  if (sharing.value) return
  showShareConfigDialog.value = false
}

async function confirmShareConfig() {
  if (!canSubmitShareConfig.value || sharing.value) return
  sharing.value = true
  const minDelay = new Promise(r => setTimeout(r, 800))
  let nextState = ''
  try {
    const expireDays = currentShareExpireDays()
    const shareKey = await createShare(resumeId.value, currentVersionId.value, {
      password: shareConfig.usePassword ? shareConfig.password : '',
      expireDays,
    })
    const shareUrl = `${window.location.origin}/share/${shareKey}`
    const copied = await copyText(shareUrl)
    await loadShareList()
    nextState = copied ? 'copied' : 'generated'
    showShareConfigDialog.value = false
    resetShareConfig()
  } catch (e) {
    nextState = 'error'
  } finally {
    await minDelay
    sharing.value = false
    setShareState(nextState)
  }
}

async function openSharePasswordDialog(item) {
  sharePasswordForm.targetItem = item
  sharePasswordForm.currentPassword = item?.password || ''
  sharePasswordForm.nextPassword = ''
  showSharePasswordDialog.value = true
}

function closeSharePasswordDialog() {
  if (sharePasswordSubmitting.value) return
  showSharePasswordDialog.value = false
  sharePasswordForm.targetItem = null
  sharePasswordForm.currentPassword = ''
  sharePasswordForm.nextPassword = ''
}

function sanitizeManagerPassword() {
  sharePasswordForm.nextPassword = sharePasswordForm.nextPassword.replace(/\D/g, '').slice(0, 6)
}

function patchShareListItem(updatedItem) {
  if (!updatedItem?.id) return
  const index = shareList.value.findIndex(item => item.id === updatedItem.id)
  if (index >= 0) {
    shareList.value[index] = { ...shareList.value[index], ...updatedItem }
  }
}

async function clearSharePassword() {
  if (!sharePasswordForm.targetItem || sharePasswordSubmitting.value) return
  sharePasswordSubmitting.value = true
  sharePasswordSubmitAction.value = 'clear'
  const minDelay = new Promise(r => setTimeout(r, 800))
  try {
    const updatedItem = await updateSharePassword(sharePasswordForm.targetItem.id, '')
    await minDelay
    patchShareListItem(updatedItem)
    sharePasswordSubmitting.value = false
    sharePasswordSubmitAction.value = null
    closeSharePasswordDialog()
  } finally {
    sharePasswordSubmitting.value = false
    sharePasswordSubmitAction.value = null
  }
}

async function submitSharePassword() {
  if (!sharePasswordForm.targetItem || !canSubmitSharePassword.value || sharePasswordSubmitting.value) return
  sharePasswordSubmitting.value = true
  sharePasswordSubmitAction.value = 'save'
  const minDelay = new Promise(r => setTimeout(r, 800))
  try {
    const updatedItem = await updateSharePassword(sharePasswordForm.targetItem.id, sharePasswordForm.nextPassword)
    await minDelay
    patchShareListItem(updatedItem)
    sharePasswordSubmitting.value = false
    sharePasswordSubmitAction.value = null
    closeSharePasswordDialog()
  } finally {
    sharePasswordSubmitting.value = false
    sharePasswordSubmitAction.value = null
  }
}

function inferExpireMode(item) {
  if (!item?.expireTime) return { mode: 'forever', customDays: '' }
  const diffMs = new Date(item.expireTime).getTime() - Date.now()
  const days = Math.max(1, Math.round(diffMs / 86400000))
  if (days === 7) return { mode: '7', customDays: '' }
  if (days === 30) return { mode: '30', customDays: '' }
  return { mode: 'custom', customDays: String(days) }
}

function openShareExpireDialog(item) {
  shareExpireForm.targetItem = item
  const inferred = inferExpireMode(item)
  shareExpireForm.expireMode = inferred.mode
  shareExpireForm.customDays = inferred.customDays
  showShareExpireDialog.value = true
}

function closeShareExpireDialog() {
  if (shareExpireSubmitting.value) return
  showShareExpireDialog.value = false
  shareExpireForm.targetItem = null
  shareExpireForm.expireMode = '30'
  shareExpireForm.customDays = ''
}

function selectManagerExpire(value) {
  shareExpireForm.expireMode = value
  if (value !== 'custom') shareExpireForm.customDays = ''
}

function sanitizeManagerCustomDays() {
  shareExpireForm.customDays = shareExpireForm.customDays.replace(/\D/g, '').slice(0, 4)
}

function managerExpireDays() {
  if (shareExpireForm.expireMode === 'forever') return 0
  if (shareExpireForm.expireMode === 'custom') return Number(shareExpireForm.customDays)
  return Number(shareExpireForm.expireMode)
}

async function submitShareExpire() {
  if (!shareExpireForm.targetItem || !canSubmitManagerExpire.value || shareExpireSubmitting.value) return
  shareExpireSubmitting.value = true
  const minDelay = new Promise(r => setTimeout(r, 800))
  try {
    const updatedItem = await updateShareExpire(shareExpireForm.targetItem.id, managerExpireDays())
    await minDelay
    patchShareListItem(updatedItem)
    shareExpireSubmitting.value = false
    closeShareExpireDialog()
  } finally {
    shareExpireSubmitting.value = false
  }
}

async function handleCloseShare(item) {
  await closeShare(item.id)
  shareList.value = shareList.value.map(share =>
    share.id === item.id
      ? { ...share, status: 0 }
      : share
  )
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    window.prompt('请复制以下链接', text)
    return false
  }
}

async function handleShare() {
  if (await requireGuestLogin('share')) return
  if (!resumeId.value || dirty.value) {
    pendingShareAfterSave.value = true
    showUnsavedSharePrompt.value = true
    return
  }
  openShareConfigDialog()
}

async function saveBeforeShare() {
  if (savingBeforeShare.value) return
  savingBeforeShare.value = true
  await handleSave()
  if (!dirty.value && pendingShareAfterSave.value) {
    pendingShareAfterSave.value = false
    showUnsavedSharePrompt.value = false
    await nextTick()
    openShareConfigDialog()
  }
  savingBeforeShare.value = false
}

function cancelPendingShare() {
  if (savingBeforeShare.value) return
  pendingShareAfterSave.value = false
  showUnsavedSharePrompt.value = false
}

function navigateBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/')
}

function closeUnsavedLeavePrompt() {
  if (savingBeforeLeave.value) return
  showUnsavedLeavePrompt.value = false
}

function handleBackNavigation() {
  if (!dirty.value) {
    navigateBack()
    return
  }
  showUnsavedLeavePrompt.value = true
}

function leaveWithoutSaving() {
  if (savingBeforeLeave.value) return
  showUnsavedLeavePrompt.value = false
  navigateBack()
}

async function saveBeforeLeave() {
  if (savingBeforeLeave.value) return
  savingBeforeLeave.value = true
  await handleSave()
  if (!dirty.value) {
    showUnsavedLeavePrompt.value = false
    await nextTick()
    navigateBack()
  }
  savingBeforeLeave.value = false
}

function selectThemeOption(key, value) {
  if (key === 'fontFamily') {
    richFontFamily.value = value
    richFontFamilyTouched.value = true
  }
  if (key === 'fontSize') {
    richFontSize.value = value
    richFontSizeTouched.value = true
  }
  if (key === 'lineHeight') {
    richLineHeight.value = value
    richLineHeightTouched.value = true
  }
  openThemeSelect.value = ''
}

function updateScale() {
  if (!scrollRef.value || !paperRef.value || !paperContentRef.value) return
  const scrollStyle = window.getComputedStyle(scrollRef.value)
  const horizontalPadding = parseFloat(scrollStyle.paddingLeft || '0') + parseFloat(scrollStyle.paddingRight || '0')
  const availableWidth = Math.max(0, scrollRef.value.clientWidth - horizontalPadding)
  previewScale.value = Math.min(1, availableWidth / A4_W)
  const templateRoot = paperRef.value.querySelector('.resume-show-new')
  const contentHeight = Math.ceil(Math.max(
    templateRoot?.scrollHeight || 0,
    templateRoot?.offsetHeight || 0,
    templateRoot?.getBoundingClientRect().height || 0,
    paperContentRef.value.scrollHeight || 0,
    paperContentRef.value.offsetHeight || 0,
    paperContentRef.value.getBoundingClientRect().height || 0
  ))
  showPageBreak.value = contentHeight > A4_H + 1
  pageCount.value = Math.max(1, Math.ceil(contentHeight / A4_H))
  nextTick(updateToolbarActionOffset)
}

function updateToolbarActionOffset() {
  cancelAnimationFrame(toolbarAlignRaf)
  toolbarAlignRaf = requestAnimationFrame(() => {
      if (!toolbarRef.value || !paperRef.value) return
      const toolbarRect = toolbarRef.value.getBoundingClientRect()
      const paperRect = paperRef.value.getBoundingClientRect()
      toolbarActionsRightOffset.value = Math.round(toolbarRect.right - paperRect.right) - 21
    })
  }

function setupScaleObserver() {
  resizeObserver?.disconnect()
  resizeObserver = null
  if (!scrollRef.value || !paperRef.value || !paperContentRef.value) return
  updateScale()
  resizeObserver = new ResizeObserver(() => updateScale())
  resizeObserver.observe(scrollRef.value)
  resizeObserver.observe(paperContentRef.value)
  resizeObserver.observe(paperRef.value)
}

onMounted(() => {
  nextTick(updateTitleWidth)
  window.addEventListener('resize', updateScale)
})

onBeforeUnmount(() => {
  clearTimeout(guestDraftSaveTimer)
  clearTimeout(guestDraftPromptTimer)
  persistGuestDraftNow()
  resizeObserver?.disconnect()
  cancelAnimationFrame(toolbarAlignRaf)
  window.removeEventListener('resize', updateScale)
  window.removeEventListener('beforeunload', onBeforeUnload)
  window.removeEventListener('pagehide', persistGuestDraftNow)
  document.removeEventListener('visibilitychange', onVisibilityChange)
  document.removeEventListener('click', handleDocumentClick)
})

const resumeId = computed(() => route.params.id)
const modules = ref(resumeId.value
  ? DEFAULT_MODULES.map(m => ({ ...m, enabled: false }))
  : DEFAULT_MODULES.map(m => ({ ...m }))
)

const moduleData = reactive({
  basic: '',
  education: '',
  experience: '',
  project: '',
  skill: '',
  personalStrengths: '',
  award: '',
  portfolio: '',
  other: '',
})

const placeholderData = computed(() => (resumeId.value ? PLACEHOLDER_DATA : COMPACT_PLACEHOLDER_DATA))

const editorMap = {
  basic: markRaw(BasicEditor),
  education: markRaw(EducationEditor),
  experience: markRaw(ExperienceEditor),
  skill: markRaw(SkillEditor),
  project: markRaw(ProjectEditor),
  personalStrengths: markRaw(SelfIntroEditor),
  award: markRaw(AwardEditor),
  portfolio: markRaw(PortfolioEditor),
  other: markRaw(OtherEditor),
}

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

function getEditorProps(key) {
  if (key === 'basic') {
    return { currentTemplate: selectedTemplate.value }
  }
  return {}
}

function getEditorIcon(key) {
  return iconMap[key] || LayoutGrid
}

function moveModule(key, dir) {
  const list = modules.value
  const enabledKeys = list.filter(m => m.enabled).map(m => m.key)
  const enabledIdx = enabledKeys.indexOf(key)
  const enabledTarget = enabledIdx + dir
  if (enabledIdx < 0 || enabledTarget < 0 || enabledTarget >= enabledKeys.length) return

  const targetKey = enabledKeys[enabledTarget]
  const idx = list.findIndex(m => m.key === key)
  const target = list.findIndex(m => m.key === targetKey)
  if (idx < 0 || target < 0) return
  if (target === 0 && list[target].key === 'basic') return

  const newList = [...list]
  ;[newList[idx], newList[target]] = [newList[target], newList[idx]]
  modules.value = newList
}

function removeModule(key) {
  if (expandedKey.value === key) expandedKey.value = ''
  modules.value = modules.value.map(m =>
    m.key === key ? { ...m, enabled: false } : m
  )
}

const currentTemplate = computed(() => TEMPLATE_COMPONENTS[selectedTemplate.value] || TEMPLATE_COMPONENTS[DEFAULT_TEMPLATE_ID])

const enabledModules = computed(() => modules.value.filter(m => m.enabled))

watch(modules, (list) => {
  list.forEach(m => {
    if (!(m.key in moduleData)) {
      moduleData[m.key] = ''
    }
    if (m.enabled && !moduleData[m.key] && placeholderData.value[m.key]) {
      moduleData[m.key] = placeholderData.value[m.key]
    }
  })
}, { immediate: true })

function toggleExpand(key) {
  expandedKey.value = expandedKey.value === key ? '' : key
}

function selectTemplate(template) {
  selectedTemplate.value = template
  openThemeSelect.value = ''
}

const previewContents = computed(() => {
  return enabledModules.value.map((mod, i) => ({
    moduleType: mod.key,
    contentJson: moduleData[mod.key] || null,
    sortOrder: i,
  }))
})

const loaded = ref(false)
const EXISTING_RESUME_MIN_LAYOUT_DELAY_MS = 400
const NEW_RESUME_MIN_LAYOUT_DELAY_MS = 800

watch(loaded, async (val) => {
  if (!val) {
    layoutReady.value = false
    return
  }
  const minLayoutDelayMs = resumeId.value
    ? EXISTING_RESUME_MIN_LAYOUT_DELAY_MS
    : NEW_RESUME_MIN_LAYOUT_DELAY_MS
  const minLayoutDelay = new Promise(resolve => setTimeout(resolve, minLayoutDelayMs))
  await nextTick()
  setupScaleObserver()
  updateScale()
  await nextTick()
  await new Promise(resolve => requestAnimationFrame(() => resolve()))
  await new Promise(resolve => requestAnimationFrame(() => resolve()))
  await minLayoutDelay
  layoutReady.value = true
  await nextTick()
  updateScale()
})

watch(previewContents, async () => {
  if (!loaded.value) return
  await nextTick()
  updateScale()
}, { deep: true })

function applyStyleConfig(styleConfigInput, basicContentJson = '') {
  const styleConfig = readStyleConfig(styleConfigInput, basicContentJson)
  const hasStoredConfig = hasStoredStyleConfig(styleConfigInput, basicContentJson)
  themeColor.value = styleConfig.themeColor
  richFontSize.value = styleConfig.richFontSize
  richFontFamily.value = styleConfig.richFontFamily
  richLineHeight.value = styleConfig.richLineHeight
  richFontSizeTouched.value = hasStoredConfig
  richFontFamilyTouched.value = hasStoredConfig
  richLineHeightTouched.value = hasStoredConfig
}


onMounted(async () => {
  if (resumeId.value) {
    resume.value = await getResume(resumeId.value)
    const initialPayload = {
      title: resume.value.title,
      currentTemplate: normalizeTemplateId(resume.value.currentTemplate),
      styleConfig: resume.value.styleConfig || '',
      contents: (resume.value.contents || []).map((item, index) => ({
        moduleType: item.moduleType,
        contentJson: item.contentJson || null,
        sortOrder: item.sortOrder ?? index,
      })),
    }
    applyEditorPayload(initialPayload)
    await loadShareList(true)
  } else {
    let authenticated = false
    if (userStore.user) {
      authenticated = await userStore.fetchUser()
    }
    authenticatedSession.value = authenticated
    const resumePendingIntent = sessionStorage.getItem(RESUME_PENDING_LOGIN_INTENT_KEY)
    const resumePendingGuestPayload = sessionStorage.getItem(RESUME_PENDING_GUEST_PAYLOAD_KEY)
    const shouldResumeGuestDraft = authenticated && !!resumePendingIntent
    restoredGuestDraftNeedsSave.value = shouldResumeGuestDraft
    guestDraftMode.value = !authenticated || shouldResumeGuestDraft
    const guestDraftRecord = !authenticated && !resumePendingIntent
      ? readGuestResumeDraftRecord()
      : null
    const guestDraftPayload = guestDraftMode.value
      ? (resumePendingGuestPayload
        ? JSON.parse(resumePendingGuestPayload)
        : shouldResumeGuestDraft
          ? loadGuestResumeDraft()
          : null)
      : null
    if (shouldResumeGuestDraft) {
      sessionStorage.removeItem(RESUME_PENDING_LOGIN_INTENT_KEY)
      sessionStorage.removeItem(RESUME_PENDING_GUEST_PAYLOAD_KEY)
    }
    if (guestDraftRecord) {
      guestDraftRestorePayload.value = guestDraftRecord.payload
      guestDraftRestorePending.value = true
    }
    const initialPayload = guestDraftPayload || {
      title: '未命名简历',
      currentTemplate: DEFAULT_TEMPLATE_ID,
      styleConfig: '',
      contents: DEFAULT_MODULES
        .filter(mod => mod.enabled && placeholderData.value[mod.key])
        .map((mod, index) => ({
          moduleType: mod.key,
          contentJson: placeholderData.value[mod.key],
          sortOrder: index,
        })),
    }
    applyEditorPayload(initialPayload)
  }
  loaded.value = true
  if (guestDraftRestorePending.value && guestDraftRestorePayload.value && !showGuestDraftRestorePrompt.value) {
    await nextTick()
    queueGuestDraftRestorePrompt()
  }
  loadVersions()
  nextTick(() => {
    hasSavedSnapshot.value = !!resumeId.value
    lastSavedPayload.value = serializeSavePayload()
    currentBaselinePayload.value = restoredGuestDraftNeedsSave.value ? '' : lastSavedPayload.value
    initialized = true
    dirty.value = restoredGuestDraftNeedsSave.value
  })
})

const exporting = ref(false)
const saving = ref(false)
const saved = ref(false)
const noticeEnabled = computed(() => userStore.user?.noticeEnabled === 1)
const showAiChat = ref(false)
const showMatchDialog = ref(false)
const showSelfIntroDialog = ref(false)
const showScoreDialog = ref(false)
const showProofreadDialog = ref(false)
const activeProofreadHighlights = ref([])
const showAiToolsMenu = ref(false)
const showAiToolsRootTip = ref(false)
const hoveredAiToolTip = ref('')
const showEmailPrompt = ref(false)
const showNoChangeSavePrompt = ref(false)
const emailInput = ref('')

function toggleAiToolsMenu() {
  showAiToolsMenu.value = !showAiToolsMenu.value
}

async function openAiTool(tool) {
  showAiToolsMenu.value = false
  if (await requireGuestLogin(`ai-${tool}`)) return
  if (tool === 'chat') {
    showAiChat.value = true
    return
  }
  if (tool === 'match') {
    showMatchDialog.value = true
    return
  }
  if (tool === 'self-intro') {
    showSelfIntroDialog.value = true
    return
  }
  if (tool === 'score') {
    showScoreDialog.value = true
    return
  }
  if (tool === 'proofread') {
    showProofreadDialog.value = true
    return
  }
}

const userStore = useUserStore()
const pendingSuggestion = ref(null)
const suggestionQueue = ref([])
const versions = ref([])
const currentVersionId = ref(null)
const showVersionList = ref(false)
const hoveredVersion = ref(null)
const hoveredSnapshot = ref(null)
const hoverPos = reactive({ x: 0, y: 0 })
const HOVER_PREVIEW_WIDTH = 670
const HOVER_PREVIEW_HEIGHT = 965
const HOVER_PREVIEW_GAP = 20
const HOVER_PREVIEW_MARGIN = 12
const HOVER_PREVIEW_OFFSET_Y = 64

async function loadVersions() {
  if (!resumeId.value) return
  try {
    versions.value = await listVersions(resumeId.value)
  } catch {}
}

async function toggleVersionList() {
  if (!showVersionList.value && (await requireGuestLogin('version'))) return
  showVersionList.value = !showVersionList.value
}

function selectVersion(version) {
  if (!version.snapshotJson) return
  try {
    suppressDirty = true
    const snapshot = JSON.parse(version.snapshotJson)
    applyEditorPayload(buildEditorPayloadFromSnapshot(snapshot))
    currentVersionId.value = version.id
    currentBaselinePayload.value = serializeSavePayload()
    dirty.value = false
    showVersionList.value = false
    nextTick(() => {
      suppressDirty = false
      syncDirtyState()
    })
  } catch {
    suppressDirty = false
  }
}

function backToLatest() {
  suppressDirty = true
  applyEditorPayload(JSON.parse(lastSavedPayload.value || '{}'))
  currentVersionId.value = null
  currentBaselinePayload.value = lastSavedPayload.value
  dirty.value = false
  showVersionList.value = false
  nextTick(() => {
    suppressDirty = false
    syncDirtyState()
  })
}

function formatVersionTime(str) {
  if (!str) return ''
  return str.replace('T', ' ').substring(0, 16)
}

function hoverVersion(v, e) {
  if (!v.snapshotJson) return
  try {
    hoveredSnapshot.value = JSON.parse(v.snapshotJson)
    hoveredVersion.value = v.id
    const rect = e.currentTarget.getBoundingClientRect()
    const dropdownRect = e.currentTarget.closest('.version-dropdown')?.getBoundingClientRect()
    const viewportWidth = window.innerWidth
    const viewportHeight = window.innerHeight
    const preferredX = rect.right + HOVER_PREVIEW_GAP
    const fallbackX = rect.left - HOVER_PREVIEW_WIDTH - HOVER_PREVIEW_GAP
    const maxX = Math.max(HOVER_PREVIEW_MARGIN, viewportWidth - HOVER_PREVIEW_WIDTH - HOVER_PREVIEW_MARGIN)
    const maxY = Math.max(HOVER_PREVIEW_MARGIN, viewportHeight - HOVER_PREVIEW_HEIGHT - HOVER_PREVIEW_MARGIN)
    const anchorTop = dropdownRect?.top ?? rect.top

    hoverPos.x = preferredX + HOVER_PREVIEW_WIDTH <= viewportWidth - HOVER_PREVIEW_MARGIN
      ? preferredX
      : Math.max(HOVER_PREVIEW_MARGIN, Math.min(fallbackX, maxX))
    hoverPos.y = Math.max(HOVER_PREVIEW_MARGIN, Math.min(anchorTop + HOVER_PREVIEW_OFFSET_Y, maxY))
  } catch {}
}

function unhoverVersion() {
  hoveredVersion.value = null
  hoveredSnapshot.value = null
  hoverPos.y = 0
}

function getSnapshotStyleConfig(snapshot) {
  const basic = snapshot?.contents?.find(c => c.moduleType === 'basic')
  return readStyleConfig(snapshot?.styleConfig, basic?.contentJson)
}

function getSnapshotTemplate(snapshot) {
  return TEMPLATE_COMPONENTS[normalizeTemplateId(snapshot?.template)] || TEMPLATE_COMPONENTS[DEFAULT_TEMPLATE_ID]
}

function handleDocumentClick(e) {
  if (!e.target.closest('.version-wrap')) {
    showVersionList.value = false
  }
  if (!e.target.closest('.theme-select-wrap')) {
    openThemeSelect.value = ''
  }
  if (!e.target.closest('.ai-tools-wrap')) {
    showAiToolsMenu.value = false
  }
}

const moduleNames = {
  basic: '基本信息', education: '教育经历', experience: '工作经历',
  project: '项目经历', skill: '专业技能', personalStrengths: '个人优势',
  award: '荣誉奖项', portfolio: '作品集', other: '其他',
}

const proofreadModuleLabelMap = {
  基本信息: 'basic',
  教育经历: 'education',
  工作经历: 'experience',
  项目经历: 'project',
  专业技能: 'skill',
  个人优势: 'personalStrengths',
  荣誉奖项: 'award',
  个人作品: 'portfolio',
  作品集: 'portfolio',
  其他经历: 'other',
  其他: 'other',
}

function handleSuggest(moduleType, content, itemIndex = null) {
  suggestionQueue.value.push({ moduleType, content, itemIndex })
  if (!pendingSuggestion.value) showNextSuggestion()
}

function collectProofreadTargets(item) {
  const explicitModuleKey = item?.moduleType
  if (explicitModuleKey && explicitModuleKey in moduleData) {
    return [explicitModuleKey]
  }
  const mappedModuleKey = proofreadModuleLabelMap[item?.fieldLabel]
  if (mappedModuleKey && mappedModuleKey in moduleData) {
    return [mappedModuleKey]
  }
  return Object.keys(moduleData)
}

function applyProofreadSuggestion(item) {
  if (!item?.original || !item?.suggestion) return
  const targets = collectProofreadTargets(item)
  let updated = false
  for (const key of targets) {
    const nextValue = replaceProofreadContent(moduleData[key], item.original, item.suggestion)
    if (nextValue !== moduleData[key]) {
      moduleData[key] = nextValue
      updated = true
      break
    }
  }
  if (updated) {
    nextTick(() => {
      syncDirtyState()
      persistGuestDraftNow()
    })
  }
}

function applyAllProofreadSuggestions(items) {
  if (!Array.isArray(items) || !items.length) return
  items.forEach(item => {
    applyProofreadSuggestion(item)
  })
}

function ignoreProofreadSuggestion() {}

function updateProofreadHighlights(items) {
  activeProofreadHighlights.value = Array.isArray(items) ? items : []
}

function showNextSuggestion() {
  if (!suggestionQueue.value.length) return
  const next = suggestionQueue.value.shift()
  const draft = applySuggestionDraft(modules.value, moduleData, next)
  modules.value = draft.modules
  Object.entries(draft.moduleData).forEach(([key, value]) => {
    moduleData[key] = value
  })
  pendingSuggestion.value = draft.pendingSuggestion
  nextTick(() => {
    const el = paperRef.value?.querySelector('.highlight-module')
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
}

function acceptSuggest() {
  pendingSuggestion.value = null
  showNextSuggestion()
}

function rejectSuggest() {
  const reverted = rejectSuggestionDraft(modules.value, moduleData, pendingSuggestion.value)
  modules.value = reverted.modules
  Object.entries(reverted.moduleData).forEach(([key, value]) => {
    moduleData[key] = value
  })
  pendingSuggestion.value = null
  showNextSuggestion()
}
let savedTimer = null
let guestDraftSaveTimer = null
const dirty = ref(false)
let initialized = false
let suppressDirty = false
const hasSavedSnapshot = ref(false)
const lastSavedPayload = ref('')
const currentBaselinePayload = ref('')
const guestDraftMode = ref(false)
const restoredGuestDraftNeedsSave = ref(false)
const showGuestDraftRestorePrompt = ref(false)
const guestDraftRestorePending = ref(false)
const guestDraftRestorePayload = ref(null)
const authenticatedSession = ref(false)
const guestDraftModalOverlayStyle = {
  position: 'fixed',
  inset: '0',
  zIndex: '10010',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: 'rgba(0, 0, 0, 0.4)',
}
const guestDraftModalOverlayHiddenStyle = {
  display: 'none',
}
const resolvedGuestDraftModalOverlayStyle = computed(() => (
  showGuestDraftRestorePrompt.value
    ? guestDraftModalOverlayStyle
    : { ...guestDraftModalOverlayStyle, ...guestDraftModalOverlayHiddenStyle }
))
const guestDraftModalBoxStyle = {
  width: '360px',
  maxWidth: 'calc(100vw - 32px)',
  padding: '28px 24px 20px',
  background: '#fff',
  borderRadius: '14px',
  boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
  textAlign: 'center',
}
const guestDraftModalActionsStyle = {
  display: 'flex',
  gap: '10px',
}
const GUEST_DRAFT_PROMPT_DELAY_MS = 180
const GUEST_DRAFT_PROMPT_RETRY_MS = 80
const RESUME_PENDING_LOGIN_INTENT_KEY = 'resume-pending-login-intent'
const RESUME_PENDING_GUEST_PAYLOAD_KEY = 'resume-pending-guest-payload'

function isAppBootOverlaySettled() {
  const bootLoader = document.getElementById('app-loader')
  if (bootLoader) return false
  const appRoot = document.getElementById('app')
  if (appRoot && !appRoot.classList.contains('loaded')) return false
  return true
}

function queueGuestDraftRestorePrompt(delay = GUEST_DRAFT_PROMPT_DELAY_MS) {
  clearTimeout(guestDraftPromptTimer)
  guestDraftPromptTimer = setTimeout(() => {
    if (!guestDraftRestorePending.value || !guestDraftRestorePayload.value || showGuestDraftRestorePrompt.value) return
    if (!isAppBootOverlaySettled()) {
      queueGuestDraftRestorePrompt(GUEST_DRAFT_PROMPT_RETRY_MS)
      return
    }
    showGuestDraftRestorePrompt.value = true
  }, delay)
}

function applyEditorPayload(payload) {
  const contentMap = new Map((payload?.contents || []).map(item => [item.moduleType, item.contentJson || '']))
  Object.keys(moduleData).forEach(key => {
    moduleData[key] = contentMap.get(key) || ''
  })
  modules.value = DEFAULT_MODULES.map(mod => ({
    ...mod,
    enabled: contentMap.has(mod.key),
  }))
  title.value = payload?.title || ''
  selectedTemplate.value = normalizeTemplateId(payload?.currentTemplate || DEFAULT_TEMPLATE_ID)
  applyStyleConfig(payload?.styleConfig, moduleData.basic)
}

function buildEditorPayloadFromSnapshot(snapshot) {
  return {
    title: snapshot?.title || '',
    currentTemplate: normalizeTemplateId(snapshot?.template || DEFAULT_TEMPLATE_ID),
    styleConfig: snapshot?.styleConfig || '',
    contents: (snapshot?.contents || []).map((item, index) => ({
      moduleType: item.moduleType,
      contentJson: item.contentJson || null,
      sortOrder: item.sortOrder ?? index,
    })),
  }
}

function buildSavePayload() {
  const contents = enabledModules.value
    .map((mod, i) => ({
      moduleType: mod.key,
      contentJson: mod.key === 'basic'
        ? stripLegacyStyleFieldsFromBasic(moduleData.basic || '')
        : (moduleData[mod.key] || null),
      sortOrder: i,
    }))
    .filter(c => c.contentJson)
    .map((c, i) => ({ ...c, sortOrder: i }))

  return {
    title: title.value,
    currentTemplate: selectedTemplate.value,
    styleConfig: serializeStyleConfig({
      themeColor: themeColor.value,
      richFontSize: richFontSize.value,
      richFontFamily: richFontFamily.value,
      richLineHeight: richLineHeight.value,
    }),
    contents,
  }
}

function buildDefaultGuestPayload() {
  const contents = DEFAULT_MODULES
    .filter(mod => mod.enabled && placeholderData.value[mod.key])
    .map((mod, index) => ({
      moduleType: mod.key,
      contentJson: mod.key === 'basic'
        ? stripLegacyStyleFieldsFromBasic(placeholderData.value[mod.key] || '')
        : placeholderData.value[mod.key],
      sortOrder: index,
    }))

  return {
    title: '未命名简历',
    currentTemplate: DEFAULT_TEMPLATE_ID,
    styleConfig: serializeStyleConfig({
      themeColor: themeColor.value,
      richFontSize: richFontSize.value,
      richFontFamily: richFontFamily.value,
      richLineHeight: richLineHeight.value,
    }),
    contents,
  }
}

function serializeSavePayload() {
  return JSON.stringify(buildSavePayload())
}

function syncDirtyState() {
  if (!initialized || suppressDirty) return
  dirty.value = serializeSavePayload() !== currentBaselinePayload.value
}

function persistGuestDraftNow() {
  if (!guestDraftMode.value || resumeId.value || !loaded.value || showGuestDraftRestorePrompt.value || guestDraftRestorePending.value) return
  syncTitleFromInput()
  const payload = buildSavePayload()
  if (JSON.stringify(payload) === JSON.stringify(buildDefaultGuestPayload())) {
    clearGuestResumeDraft()
    return
  }
  saveGuestResumeDraft(payload)
}

function scheduleGuestDraftSave() {
  if (!guestDraftMode.value || resumeId.value || !initialized || suppressDirty || !loaded.value || showGuestDraftRestorePrompt.value || guestDraftRestorePending.value) return
  clearTimeout(guestDraftSaveTimer)
  persistGuestDraftNow()
}

watch(moduleData, () => {
  syncDirtyState()
}, { deep: true })
watch(title, () => {
  syncDirtyState()
})
watch(modules, () => {
  syncDirtyState()
}, { deep: true })
watch([richFontSize, richFontFamily, richLineHeight, richFontSizeTouched, richFontFamilyTouched, richLineHeightTouched], () => {
  syncDirtyState()
})
watch(themeColor, () => {
  syncDirtyState()
})
watch(selectedTemplate, () => {
  syncDirtyState()
})
watch(() => serializeSavePayload(), () => {
  scheduleGuestDraftSave()
})

function onBeforeUnload(e) {
  persistGuestDraftNow()
  if (dirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

function onVisibilityChange() {
  if (document.visibilityState === 'hidden') {
    persistGuestDraftNow()
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', onBeforeUnload)
  window.addEventListener('pagehide', persistGuestDraftNow)
  document.addEventListener('visibilitychange', onVisibilityChange)
  document.addEventListener('click', handleDocumentClick)
})

async function doSave() {
  const data = buildSavePayload()
  const serializedPayload = JSON.stringify(data)
  if (resumeId.value) {
    data.id = resumeId.value
    await updateResume(data)
    return { resumeId: resumeId.value, serializedPayload }
  } else {
    const id = await addResume(data)
    data.id = id
    await updateResume(data)
    router.replace(`/edit/${id}`)
    return { resumeId: id, serializedPayload }
  }
}

async function toggleNotice() {
  if (await requireGuestLogin('notice')) return
  if (noticeEnabled.value) {
    await updateMyInfo({ noticeEnabled: 0 })
    userStore.setUser({ ...userStore.user, noticeEnabled: 0 })
    return
  }
  if (!userStore.user?.email) {
    emailInput.value = ''
    showEmailPrompt.value = true
    return
  }
  await updateMyInfo({ noticeEnabled: 1 })
  userStore.setUser({ ...userStore.user, noticeEnabled: 1 })
}

async function confirmEmail() {
  const email = emailInput.value.trim()
  if (!email) return
  try {
    await updateMyInfo({ email, noticeEnabled: 1 })
    userStore.setUser({ ...userStore.user, email, noticeEnabled: 1 })
    showEmailPrompt.value = false
  } catch {}
}

async function handleSave() {
  if (saving.value) return
  if (!resumeId.value && !authenticatedSession.value) {
    syncTitleFromInput()
    persistGuestDraftNow()
    sessionStorage.setItem(RESUME_PENDING_GUEST_PAYLOAD_KEY, serializeSavePayload())
    sessionStorage.setItem(RESUME_PENDING_LOGIN_INTENT_KEY, 'save')
    router.push({
      path: '/login',
      query: {
        redirect: route.fullPath,
        intent: 'save',
      },
    })
    return
  }
  if (hasSavedSnapshot.value && !dirty.value && !currentVersionId.value) {
    showNoChangeSavePrompt.value = true
    return
  }
  saving.value = true
  const minDelay = new Promise(r => setTimeout(r, 800))
  try {
    const result = await doSave()
    if (result) {
      if (guestDraftMode.value && !resumeId.value) {
        clearGuestResumeDraft()
        guestDraftMode.value = false
      }
      hasSavedSnapshot.value = true
      lastSavedPayload.value = result.serializedPayload
      currentBaselinePayload.value = result.serializedPayload
      dirty.value = false
      const version = await saveVersion(result.resumeId, '手动保存')
      currentVersionId.value = null
      await loadVersions()
      refreshList()
      saved.value = true
      clearTimeout(savedTimer)
      savedTimer = setTimeout(() => { saved.value = false }, 3000)

      analyzeAndNotify(result.resumeId, version?.versionNum).then(() => fetchUnread()).catch(() => {})
    }
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    await minDelay
    saving.value = false
  }
}

async function handleExportPdf() {
  if (!paperRef.value) return
  exporting.value = true
  const minDelay = new Promise(r => setTimeout(r, 800))
  let restoreInlineImages = () => {}
  try {
    await nextTick()
    await new Promise(resolve => requestAnimationFrame(() => resolve()))
    await new Promise(resolve => requestAnimationFrame(() => resolve()))
    await new Promise(resolve => setTimeout(resolve, 100))
    restoreInlineImages = await inlineImageSourcesForExport(paperRef.value)
    const canvas = await html2canvas(paperRef.value, {
      scale: 2, useCORS: true, logging: false,
      backgroundColor: '#ffffff',
      onclone: (doc) => {
        const paper = doc.querySelector('.paper')
        if (!paper) return
        // 移除缩放和分页线背景
        paper.style.transform = 'none'
        paper.style.transformOrigin = 'top left'
        paper.style.backgroundImage = 'none'
        paper.classList.remove('paper-breakable')
        // 解除所有祖先 overflow 限制，防止裁剪
        const ancestors = paper.parentElement?.closest('[style*="overflow"]')
        const themeBar = doc.querySelector('.theme-bar')
        if (themeBar) themeBar.style.display = 'none'
        doc.querySelectorAll('.page-break-label').forEach(el => el.style.display = 'none')
        doc.querySelectorAll('.preview-scroll, .preview-panel, .preview-scroll-inner, .edit-body').forEach(el => {
          el.style.overflow = 'visible'
          el.style.height = 'auto'
        })
      },
    })
    const pageH = A4_H * 2
    const pdf = new jsPDF('p', 'mm', 'a4')
    const pdfW = pdf.internal.pageSize.getWidth()
    for (let y = 0; y < canvas.height; y += pageH) {
      if (y > 0) pdf.addPage()
      const h = Math.min(pageH, canvas.height - y)
      const slice = document.createElement('canvas')
      slice.width = canvas.width
      slice.height = h
      const ctx = slice.getContext('2d')
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, h)
      ctx.drawImage(canvas, 0, y, canvas.width, h, 0, 0, canvas.width, h)
      pdf.addImage(slice.toDataURL('image/jpeg', 0.98), 'JPEG', 0, 0, pdfW, (h / canvas.width) * pdfW)
    }
    pdf.save(title.value ? `${title.value}.pdf` : '简历.pdf')
  } catch (e) {
    alert('导出失败：' + e.message)
  } finally {
    restoreInlineImages()
    await minDelay
    exporting.value = false
  }
}

function syncTitleFromInput() {
  const input = titleInputRef.value
  if (input instanceof HTMLInputElement) {
    title.value = input.value
  }
}

function restoreGuestDraftFromPrompt() {
  const payload = guestDraftRestorePayload.value
  if (!payload) return
  showGuestDraftRestorePrompt.value = false
  guestDraftRestorePending.value = false
  guestDraftRestorePayload.value = null
  suppressDirty = true
  applyEditorPayload(payload)
  nextTick(() => {
    suppressDirty = false
    syncDirtyState()
    persistGuestDraftNow()
  })
}

function startOverGuestDraft() {
  clearGuestResumeDraft()
  showGuestDraftRestorePrompt.value = false
  guestDraftRestorePending.value = false
  guestDraftRestorePayload.value = null
}

async function requireGuestLogin(intent) {
  if (resumeId.value || authenticatedSession.value) return false
  await nextTick()
  syncTitleFromInput()
  persistGuestDraftNow()
  sessionStorage.setItem(RESUME_PENDING_GUEST_PAYLOAD_KEY, serializeSavePayload())
  sessionStorage.setItem(RESUME_PENDING_LOGIN_INTENT_KEY, intent)
  router.push({
    path: '/login',
    query: {
      redirect: route.fullPath,
      intent,
    },
  })
  return true
}
</script>

<style scoped>
.resume-edit-page {
  max-width: 100%;
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
  overflow-x: auto;
  overflow-y: hidden;
}

.edit-loading-state {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(180deg, #f8fafc 0%, #f3f6fb 100%);
  z-index: 2;
}

.edit-loading-card {
  min-width: 160px;
  height: 44px;
  padding: 0 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid rgba(79, 70, 229, 0.12);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  color: var(--primary);
  font-size: 13px;
  font-weight: 600;
}

.edit-body--prerender {
  visibility: hidden;
  pointer-events: none;
}

.edit-loading-spinner {
  width: 15px;
  height: 15px;
  border-width: 2px;
}

.edit-toolbar {
  display: flex;
  align-items: center;
  gap: clamp(10px, 0.8vw, 12px);
  padding: clamp(6px, 0.5vw, 8px) clamp(18px, 1.3vw, 28px);
  padding-left: clamp(36px, 2.8vw, 52px);
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.btn-back {
  height: clamp(26px, 1.8vw, 28px);
  min-width: clamp(84px, 5.8vw, 92px);
  display: inline-flex;
  align-items: center;
  gap: clamp(4px, 0.4vw, 5px);
  padding: 0 clamp(8px, 0.65vw, 10px);
  font-size: clamp(12px, 0.85vw, 13px);
  font-weight: 600;
  color: var(--text-1);
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s ease;
  flex-shrink: 0;
  justify-content: center;
}
.btn-back svg { flex-shrink: 0; display: block; }
.btn-back:hover { border-color: var(--text-3); }

.toolbar-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: clamp(12px, 1vw, 16px);
}

.title-wrap {
  display: flex;
  align-items: center;
  position: relative;
}

.title-sizer {
  position: absolute;
  visibility: hidden;
  white-space: pre;
  font-size: 14px;
  font-weight: 600;
  padding: 6px 8px;
  pointer-events: none;
}

.title-input {
  padding: 6px 28px 6px 8px;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid transparent;
  border-radius: 6px;
  color: var(--text-1);
  background: transparent;
  width: 80px;
  min-width: 80px;
  max-width: 300px;
  transition: border-color 0.15s, background-color 0.15s;
}
.title-input:focus {
  outline: none;
  border-color: var(--border);
  background: var(--bg-page);
}

.title-edit-icon {
  position: absolute;
  right: 6px;
  display: flex;
  align-items: center;
  color: var(--text-3);
  pointer-events: none;
}

.version-wrap {
  position: relative;
  margin-left: clamp(4px, 0.5vw, 8px);
  display: flex;
  align-items: center;
  gap: clamp(8px, 0.8vw, 10px);
  flex-shrink: 0;
}

.toolbar-notice-btn {
  width: clamp(30px, 2.1vw, 34px);
  height: clamp(30px, 2.1vw, 34px);
  margin-left: 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-2);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  flex-shrink: 0;
  transition: color 0.15s ease, background 0.15s ease;
}

.toolbar-notice-btn:hover {
  color: var(--primary);
  background: var(--primary-light, #eef2ff);
}

.toolbar-notice-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--danger, #ef4444);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.version-trigger {
  height: clamp(26px, 1.8vw, 28px);
  min-width: clamp(84px, 6vw, 92px);
  display: inline-flex;
  align-items: center;
  gap: clamp(4px, 0.4vw, 5px);
  padding: 0 clamp(8px, 0.65vw, 10px);
  font-size: clamp(12px, 0.85vw, 13px);
  font-weight: 600;
  color: var(--text-1);
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s ease;
  justify-content: center;
}
.version-trigger svg { flex-shrink: 0; display: block; }
.version-trigger span { white-space: nowrap; }
.version-trigger:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.version-trigger-tip {
  position: absolute;
  left: 0;
  top: calc(100% + 10px);
  z-index: 31;
  width: 300px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
  color: var(--text-2);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.55;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12), 0 1px 4px rgba(15, 23, 42, 0.04);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-4px);
  pointer-events: none;
  transition: opacity 0.16s ease, transform 0.16s ease, visibility 0.16s ease;
}

.version-trigger:hover + .version-trigger-tip {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.version-badge.latest-btn {
  height: clamp(26px, 1.8vw, 28px);
  display: inline-flex;
  align-items: center;
  gap: clamp(4px, 0.4vw, 5px);
  padding: 0 clamp(8px, 0.65vw, 10px);
  font-size: clamp(12px, 0.85vw, 13px);
  font-weight: 600;
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 8px;
  background: var(--primary-light);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s ease;
}
.version-badge.latest-btn svg { flex-shrink: 0; display: block; }
.version-badge.latest-btn:hover {
  opacity: 0.8;
}

.version-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  width: 280px;
  max-height: 320px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: var(--shadow-lg);
  z-index: 100;
  padding: 6px;
}

.version-empty {
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-3);
}

.version-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.12s;
}
.version-item:hover {
  background: var(--bg-page);
}
.version-item.active {
  background: var(--primary-light);
}

.version-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.version-num {
  font-size: 12px;
  font-weight: 700;
  color: var(--primary);
  flex-shrink: 0;
}

.version-time {
  font-size: 11px;
  color: var(--text-3);
}

.version-remark {
  font-size: 12px;
  color: var(--text-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.version-dropdown-enter-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.version-dropdown-leave-active { transition: opacity 0.1s ease, transform 0.1s ease; }
.version-dropdown-enter-from, .version-dropdown-leave-to { opacity: 0; transform: translateX(-50%) translateY(-4px); }

.version-hover-preview {
  position: fixed;
  left: v-bind('hoverPos.x + "px"');
  top: v-bind('hoverPos.y + "px"');
  width: 670px;
  height: 965px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: var(--shadow-lg);
  z-index: 10001;
  padding: 16px;
}

.version-hover-paper {
  width: 794px;
  transform-origin: top left;
  transform: scale(0.8);
  pointer-events: none;
}

.version-hover-enter-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.version-hover-leave-active { transition: opacity 0.1s ease, transform 0.1s ease; }
.version-hover-enter-from, .version-hover-leave-to { opacity: 0; transform: scale(0.95); }

.btn-save {
    height: clamp(26px, 1.8vw, 28px);
    width: clamp(90px, 6.2vw, 98px);
    min-width: clamp(90px, 6.2vw, 98px);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: clamp(4px, 0.4vw, 5px);
    padding: 0 clamp(8px, 0.65vw, 10px);
    font-size: clamp(12px, 0.85vw, 13px);
    font-weight: 600;
    line-height: 1;
    color: var(--text-1);
    background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s ease;
  flex-shrink: 0;
  justify-content: center;
}
.btn-save svg { flex-shrink: 0; display: block; }
.btn-save span { white-space: nowrap; }
.btn-save:hover { border-color: var(--text-3); }
.btn-save.saved,
.btn-save.saving {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light, #eef1ff);
}
.btn-save:disabled { cursor: default; opacity: 0.78; }
.btn-spinner {
  width: 14px; height: 14px;
  border: 2px solid rgba(79, 70, 229, 0.22);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.5s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.toolbar-action-group {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: clamp(8px, 0.6vw, 10px);
  flex-shrink: 0;
  min-width: 0;
}
.btn-export {
    height: clamp(26px, 1.8vw, 28px);
    width: clamp(90px, 6.2vw, 98px);
    min-width: clamp(90px, 6.2vw, 98px);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: clamp(4px, 0.4vw, 5px);
    padding: 0 clamp(8px, 0.65vw, 10px);
    font-size: clamp(12px, 0.85vw, 13px);
    font-weight: 600;
    line-height: 1;
    color: var(--text-1);
    background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s ease;
  flex-shrink: 0;
  justify-content: center;
}
.btn-export svg { flex-shrink: 0; display: block; }
.btn-export span { white-space: nowrap; }
.btn-export:hover { border-color: var(--text-3); }
.btn-export:disabled { cursor: default; opacity: 0.78; }

.edit-body {
  flex: 1;
  display: flex;
  align-items: stretch;
  min-height: 0;
  min-width: 1560px;
  gap: 24px;
  padding: 0 24px 0 0;
  margin: 2.5vh 0;
}

/* 中间编辑面板 */
.edit-column {
  flex: 9;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  min-height: 0;
}

.form-panel {
  flex: 1;
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
  overflow: visible;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.edit-actions-bar {
  flex-shrink: 0;
  position: relative;
}
.edit-actions-inner {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  width: 794px;
  max-width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
  align-items: stretch;
  gap: 14px 24px;
  padding: 8px 20px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}
.action-with-tip {
  position: relative;
  display: flex;
  align-items: stretch;
  flex: 0 0 clamp(90px, 6.2vw, 98px);
  width: clamp(90px, 6.2vw, 98px);
  min-width: clamp(90px, 6.2vw, 98px);
}
.action-with-tip-ai-tools {
  flex-basis: clamp(90px, 6.2vw, 98px);
  width: clamp(90px, 6.2vw, 98px);
  min-width: clamp(90px, 6.2vw, 98px);
}
.action-btn {
    min-height: 27px;
  height: 100%;
  width: 100%;
  min-width: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  color: var(--text-1);
  font-size: 13px;
    font-family: inherit;
    transition: all 0.15s ease;
  }
.action-btn-content {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    min-width: 0;
  }
.action-btn svg { flex-shrink: 0; display: block; }
  .action-btn-label {
      display: inline-flex;
      align-items: center;
      width: auto;
    flex: 0 0 auto;
    min-width: 0;
    white-space: nowrap;
    word-break: keep-all;
      text-align: center;
      line-height: 1;
    }
.action-btn:hover { border-color: var(--text-3); }
.action-btn.on { color: var(--primary); border-color: var(--primary); background: var(--primary-light, #eef1ff); }
.action-btn.copied,
.action-btn.loading { color: var(--primary); border-color: var(--primary); background: var(--primary-light, #eef1ff); }
.action-btn.error { color: var(--danger); border-color: rgba(239, 68, 68, 0.28); background: #fef2f2; }
.action-btn:disabled { cursor: default; opacity: 0.78; }
.share-action-btn { min-width: 0; }
.share-loading-icon { animation: share-spin 0.8s linear infinite; }
@keyframes share-spin {
  to { transform: rotate(360deg); }
}
.action-tip {
  position: absolute;
  left: 50%;
  top: calc(100% + 10px);
  transform: translateX(-50%) translateY(-4px);
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  color: var(--primary);
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid var(--border);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
  pointer-events: none;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.16s ease, transform 0.16s ease, visibility 0.16s ease;
  z-index: 30;
}
.action-tip-wide {
  width: 260px;
}
.action-with-tip:hover .action-tip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}
.action-with-tip-ai-tools {
  z-index: 2;
}
.ai-tools-wrap {
  position: relative;
  width: 100%;
}
.ai-tools-trigger {
    gap: 4px;
    padding: 0 8px;
  }
.ai-tools-trigger .action-btn-label {
    white-space: nowrap;
  }
.ai-tools-trigger-arrow {
  opacity: 0.72;
  transition: transform 0.16s ease;
}
.ai-tools-trigger.on .ai-tools-trigger-arrow {
  transform: rotate(180deg);
}
.ai-tools-wrap > .action-tip {
  left: 50%;
  top: calc(100% + 10px);
}
.ai-tools-root-tip {
  position: absolute;
  right: calc(100% + 10px);
  top: 50%;
  width: 260px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  color: var(--primary);
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid var(--border);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
  pointer-events: none;
  transform: translateY(-50%);
  z-index: 50;
}
.ai-tools-menu {
  position: absolute;
  top: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
  width: max-content;
  min-width: 100%;
  max-width: min(160px, calc(100vw - 24px));
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: visible;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.14);
  z-index: 40;
}
.ai-tool-option {
  width: clamp(90px, 6.2vw, 98px);
  min-width: clamp(90px, 6.2vw, 98px);
  height: clamp(26px, 1.8vw, 28px);
  min-height: clamp(26px, 1.8vw, 28px);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  color: var(--text-1);
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
  z-index: 2;
}
.ai-tool-option span {
  white-space: nowrap;
}
.ai-tool-option-wrap {
  position: relative;
  width: max-content;
}
.ai-tool-tip {
  position: absolute;
  left: calc(100% + 10px);
  top: 50%;
  width: 240px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  color: var(--primary);
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid var(--border);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
  pointer-events: none;
  transform: translateY(-50%);
  z-index: 50;
}
.ai-tool-option:hover {
  border-color: var(--text-3);
}
.ai-tool-option.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light, #eef1ff);
}
.ai-tool-option-disabled {
  color: var(--text-3);
  background: #f8fafc;
  cursor: not-allowed;
}
.ai-tools-menu-enter-active,
.ai-tools-menu-leave-active {
  transition: opacity 0.16s ease, transform 0.16s ease;
}
.ai-tools-menu-enter-from,
.ai-tools-menu-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-4px);
}

.form-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  scrollbar-width: none;
  border-radius: var(--radius) var(--radius) 0 0;
}
.form-scroll::-webkit-scrollbar {
  display: none;
}

/* 手风琴折叠 */
.accordion-item {
  border-bottom: 1px solid var(--border);
  padding: 0 16px;
}

.accordion-header {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 8px 0 12px;
  border: none;
  background: var(--bg-card);
  position: relative;
}
.accordion-arrow {
  flex-shrink: 0;
  color: var(--text-1);
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 4px;
  transition: all 0.15s ease;
}
.accordion-arrow:hover { background: none; }
.accordion-arrow.expanded { color: var(--primary); }

.accordion-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex: 1;
  padding: 18px 0;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-1);
  transition: background 0.15s ease;
}
.accordion-toggle:hover { opacity: 0.8; user-select: none; }

.accordion-icon {
  flex-shrink: 0;
  color: var(--text-3);
}

.accordion-title {
  font-size: 15px;
  font-weight: 700;
}

/* 操作按钮 — 悬浮显示，绝对定位不影响居中 */
.accordion-actions {
  display: flex;
  gap: 4px;
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease;
}

.accordion-header:hover .accordion-actions,
.accordion-header:focus-within .accordion-actions {
  opacity: 1;
  pointer-events: auto;
}

.btn-action {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-1);
  border-radius: 4px;
  flex-shrink: 0;
}
.btn-action:disabled {
  opacity: 0.2;
  cursor: default;
}
.btn-del:disabled {
  opacity: 0.2;
  cursor: default;
  color: var(--text-3);
}
.btn-del:not(:disabled):hover {
  color: var(--danger);
  background: #fef2f2;
}

.accordion-body {
  padding: 0 20px 16px;
}

.empty-editor {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 20px;
  color: var(--text-3);
}
.empty-editor p {
  font-size: 13px;
  margin: 0;
}

/* 主题色选择条 */
.theme-bar {
  position: relative;
  z-index: 6;
  padding: 0 24px 12px;
  background: var(--bg-page);
  flex-shrink: 0;
}
.theme-bar-inner {
  position: relative;
  z-index: 6;
  width: 794px;
  max-width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: none;
  overflow: visible;
}
.theme-bar-left {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0;
}
.theme-bar-divider {
  flex: 0 0 auto;
  color: var(--text-3);
  font-size: 16px;
  line-height: 1;
  user-select: none;
}
.theme-bar-right {
  flex: 1 1 0;
  min-width: 0;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  align-items: center;
  gap: 10px;
}
.theme-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.15s ease;
  border: 2px solid transparent;
  box-sizing: border-box;
  flex-shrink: 0;
}
.theme-dot:hover { transform: scale(1.2); }
.theme-dot.active {
  border-color: var(--text-1);
}
.theme-select-wrap {
  position: relative;
  z-index: 8;
  min-width: 0;
}
.theme-select-wrap-sm {
  min-width: 0;
}
.theme-select-trigger {
  height: 28px;
  width: 100%;
  min-width: 0;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 0 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-1);
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
    font-family: inherit;
    transition: all 0.15s ease;
  }
.theme-select-trigger-content {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      min-width: 0;
    }
  .theme-select-trigger-label {
      display: inline-flex;
      align-items: center;
      min-width: 0;
    white-space: nowrap;
    line-height: 1;
      writing-mode: horizontal-tb;
    }
.theme-select-trigger:hover {
  border-color: var(--text-3);
}
.theme-select-wrap.open .theme-select-trigger {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(70, 114, 242, 0.1);
}
.theme-select-icon {
  color: var(--text-3);
  flex-shrink: 0;
  transition: transform 0.15s ease;
}
.theme-select-wrap.open .theme-select-icon {
  transform: rotate(180deg);
}
.theme-select-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 20;
  width: max-content;
  min-width: 100%;
  padding: 6px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
  display: flex;
  flex-direction: column;
  gap: 4px;
}
  .template-select-backdrop {
    position: fixed;
    inset: 0;
    z-index: 40;
    display: flex;
    align-items: center;
    justify-content: center;
      padding: 14px;
    background: rgba(15, 23, 42, 0.24);
    backdrop-filter: blur(6px);
  }
  .template-select-menu {
    position: relative;
    top: auto;
    left: auto;
    transform: none;
    width: min(1040px, calc(100vw - 64px));
    min-width: 0;
      max-height: min(870px, calc(100vh - 64px));
    padding: 22px;
    border-radius: 22px;
    display: flex;
    flex-direction: column;
    gap: 18px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.96);
    border: 1px solid rgba(148, 163, 184, 0.2);
    box-shadow: 0 28px 72px rgba(15, 23, 42, 0.16);
  }
  .template-select-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
    padding-bottom: 14px;
    border-bottom: 1px solid rgba(148, 163, 184, 0.16);
  }
.template-select-title-block {
  display: grid;
  gap: 4px;
}
  .template-select-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-1);
  }
  .template-select-subtitle {
    font-size: 13px;
    line-height: 1.5;
  color: var(--text-3);
}
.template-select-close {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: none;
  color: var(--text-3);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}
.template-select-close:hover {
  color: var(--text-1);
  background: rgba(0, 0, 0, 0.06);
}
  .template-select-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
    overflow-y: auto;
    padding-right: 4px;
  }
  .template-option-card {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 0;
    border: 1px solid rgba(148, 163, 184, 0.22);
    border-radius: 8px;
    background: #fff;
    cursor: pointer;
    text-align: left;
    transition: box-shadow 0.18s ease, border-color 0.18s ease, background 0.18s ease;
  }
  .template-option-card:hover {
    border-color: rgba(70, 114, 242, 0.28);
  }
  .template-option-card.active {
    border-color: var(--primary);
    background: #fff;
  }
  .template-option-preview {
    width: 100%;
    aspect-ratio: 794 / 1123;
    height: auto;
    position: relative;
    overflow: hidden;
    border-radius: 8px;
    border: 1px solid rgba(148, 163, 184, 0.18);
    background: #fff;
    margin: 0;
  }
  .template-option-scale {
    width: 794px;
    height: 1123px;
    transform-origin: top left;
    transform: scale(0.592);
    pointer-events: none;
  }
  .template-option-meta {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    flex-wrap: wrap;
    text-align: center;
    padding: 0 10px 10px;
  }
  .template-option-name {
    font-size: 14px;
    font-weight: 700;
    color: var(--text-1);
  }
  .template-option-sep {
    font-size: 13px;
    color: var(--text-3);
    line-height: 1;
  }
  .template-option-desc {
    font-size: 12px;
    line-height: 1.55;
    color: var(--text-3);
  }
.theme-select-option {
  position: relative;
  min-height: 32px;
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 10px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-1);
  font-size: 13px;
  font-family: inherit;
  text-align: left;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.15s ease;
}
.theme-select-option + .theme-select-option {
  border-top: none;
}
.theme-select-option:hover {
  background: var(--bg-page);
}
.theme-select-option.active {
  background: var(--primary-light, #eef1ff);
  color: var(--primary);
  font-weight: 600;
}
/* AI 建议修改确认条 */
.suggest-bar {
  position: sticky;
  top: 0;
  z-index: 10001;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fffbe6;
  border-bottom: 1px solid #f0d060;
  flex-shrink: 0;
}
.suggest-bar-text {
  font-size: 13px;
  color: #333;
}
.suggest-btn {
  padding: 4px 14px;
  font-size: 12px;
  border-radius: 6px;
  cursor: pointer;
  font-family: inherit;
  border: 1px solid var(--border);
  transition: all 0.15s;
}
.suggest-btn.accept {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}
.suggest-btn.accept:hover { opacity: 0.85; }
.suggest-btn.reject {
  background: #fff;
  color: var(--text-1);
}
.suggest-btn.reject:hover { background: var(--bg-page); }

/* 右侧预览面板 */
.preview-panel {
  flex: 10;
  background: var(--bg-page);
  border: none;
  box-shadow: none;
  border-radius: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.preview-scroll {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: scroll;
  scrollbar-width: none;
  padding: 0 24px;
}
.preview-scroll::-webkit-scrollbar {
  display: none;
}

.preview-scroll-inner {
  position: relative;
  margin: auto auto 0;
}

.paper {
  position: relative;
  width: 794px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  border-radius: 2px;
  min-height: 1123px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  transform-origin: top left;
}
.paper.paper-breakable {
  background-image: repeating-linear-gradient(
    to bottom,
    transparent 0,
    transparent 1122px,
    #c0c0c0 1122px,
    #c0c0c0 1123px
  );
  background-size: 100% 1123px;
}
.paper-content {
  width: 100%;
}
.page-break-label {
  position: absolute;
  right: 4px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 700;
  color: var(--primary);
  background: #fff;
  border: 1px solid rgba(79, 70, 229, 0.18);
  border-radius: 999px;
  padding: 2px 8px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
  transform: translateY(-50%);
  pointer-events: none;
  user-select: none;
}

.page-break-icon {
  flex-shrink: 0;
}

@media (max-width: 1200px) {
  .edit-body {
    gap: 16px;
    padding: 0 16px 0 0;
    margin: 1.5vh 0;
  }
  .edit-toolbar {
    padding: 8px 16px;
    gap: 8px;
  }
}

  @media (max-width: 1500px) {
  .edit-body {
    gap: 16px;
    padding: 0 16px 0 0;
    margin: 1.5vh 0;
  }
  .module-selector {
    width: 200px;
  }
  .module-selector.collapsed {
    width: 84px;
  }
  .edit-toolbar {
    gap: 10px;
    padding: 8px 16px;
  }
  .toolbar-center {
    gap: 12px;
    min-width: 0;
  }
  .version-wrap {
    gap: 8px;
    margin-left: 0;
  }
  .edit-actions-inner {
    gap: 10px 18px;
    padding: 8px 14px;
  }
  .action-with-tip {
    flex-basis: clamp(90px, 6.2vw, 98px);
    width: clamp(90px, 6.2vw, 98px);
    min-width: clamp(90px, 6.2vw, 98px);
  }
  .action-with-tip-ai-tools {
    flex-basis: clamp(90px, 6.2vw, 98px);
    width: clamp(90px, 6.2vw, 98px);
    min-width: clamp(90px, 6.2vw, 98px);
  }
  .action-btn {
      min-height: 27px;
      padding: 0 6px;
      font-size: 11px;
      border-radius: 7px;
    }
    .action-btn-content {
      gap: 3px;
    }
    .action-btn svg {
      width: 14px;
      height: 14px;
    }
    .share-action-btn {
      min-width: 0;
    }
  .ai-tools-menu {
    max-width: min(160px, calc(100vw - 20px));
  }
  .theme-bar {
    padding: 0 16px 12px;
  }
  .theme-bar-inner {
    gap: 14px;
    padding: 8px 16px;
  }
  .theme-bar-right {
    gap: 10px;
  }
  .theme-bar-left {
    gap: 0;
    justify-content: space-between;
    flex-wrap: nowrap;
  }
  .theme-select-trigger {
      padding: 0 8px;
      font-size: 13px;
    }
    .theme-select-trigger-content {
      gap: 6px;
    }
  }

  @media (min-width: 1450px) and (max-width: 1500px) {
  .edit-actions-inner {
    gap: 10px 6px;
    padding: 8px 12px;
  }
    .action-btn {
      display: grid;
      grid-auto-flow: column;
      grid-auto-columns: max-content;
      justify-content: center;
      align-items: center;
      padding: 0 5px;
      font-size: 11px;
    }
    .action-btn-content {
      gap: 2px;
    }
    .action-btn svg {
      width: 13px;
      height: 13px;
    }
    .theme-bar-right {
      gap: 6px;
      grid-template-columns: repeat(4, minmax(58px, 1fr));
    }
  .theme-select-trigger {
      padding: 0 5px;
      font-size: 11px;
    }
    .theme-select-trigger-content {
      gap: 4px;
    }
  }

@media (max-width: 900px) {
  .template-select-menu {
      width: min(520px, calc(100vw - 24px));
      max-height: min(82vh, calc(100vh - 24px));
      padding: 14px;
      gap: 12px;
  }
  .template-select-backdrop {
    padding: 12px;
  }
  .template-select-head {
    gap: 10px;
  }
  .template-select-title {
    font-size: 16px;
  }
  .template-select-subtitle {
    font-size: 12px;
  }
  .template-select-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }
    .template-option-preview {
      width: 100%;
    }
    .preview-scroll {
      padding: 0 12px;
    }
  }
</style>

<style>
@media (max-width: 600px) {
  .form-panel .form-grid[class*="cols-"] {
    grid-template-columns: 1fr !important;
    max-width: none !important;
  }
  .form-panel .opt-active {
    flex: 0 0 calc(50% - 5px) !important;
    max-width: calc(50% - 5px) !important;
  }
  .form-panel .salary-row {
    max-width: 50% !important;
  }
}

.guest-draft-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 10010;
  display: flex;
  align-items: center;
  justify-content: center;
}

.guest-draft-modal-box {
  width: 360px;
  max-width: calc(100vw - 32px);
  padding: 28px 24px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  text-align: center;
}

.guest-draft-modal-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #eef2ff;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.guest-draft-modal-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.guest-draft-modal-desc {
  margin: 0 0 24px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-3);
}

.guest-draft-modal-actions {
  display: flex;
  gap: 10px;
}

@page {
  size: A4 portrait;
  margin: 0;
}

/* 邮箱提示弹窗 */
.email-prompt-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10002;
}
.email-prompt-box {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  width: 380px;
  max-width: 90vw;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
}
.email-prompt-box h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}
.email-prompt-box p {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--text-2);
}
.email-prompt-box input {
  width: 100%;
  padding: 8px 12px;
  font-size: 14px;
  border: 1px solid var(--border);
  border-radius: 6px;
  box-sizing: border-box;
  color: var(--text-1);
  margin-bottom: 16px;
}
.email-prompt-box input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
}
.email-prompt-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.btn-cancel {
  padding: 6px 16px;
  font-size: 13px;
  color: var(--text-2);
  background: var(--bg-page);
  border: 1px solid var(--border);
  border-radius: 6px;
  cursor: pointer;
  font-family: inherit;
}
.btn-confirm {
  padding: 6px 16px;
  font-size: 13px;
  color: #fff;
  background: var(--primary);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-family: inherit;
}
.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-confirm:not(:disabled):hover {
  opacity: 0.85;
}

.unsaved-share-overlay {
  position: fixed;
  inset: 0;
  z-index: 10004;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.36);
  backdrop-filter: blur(2px);
}

.unsaved-share-box {
  width: 380px;
  max-width: calc(100vw - 32px);
  padding: 26px 24px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.18);
  text-align: center;
}

.unsaved-share-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fffbeb;
  color: #d97706;
}

.unsaved-share-box h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.unsaved-share-box p {
  margin: 0 0 22px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-2);
}

.unsaved-share-actions {
  display: flex;
  gap: 10px;
}

.unsaved-share-actions .btn-cancel,
.unsaved-share-actions .btn-confirm {
  flex: 1;
  justify-content: center;
}

.unsaved-share-btn-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.unsaved-share-btn-spinner {
  width: 12px;
  height: 12px;
  border-width: 1.8px;
}

.nochange-save-overlay {
  position: fixed;
  inset: 0;
  z-index: 10004;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.36);
  backdrop-filter: blur(2px);
}

.nochange-save-box {
  width: 380px;
  max-width: calc(100vw - 32px);
  padding: 26px 24px 20px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.18);
  text-align: center;
}

.nochange-save-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #eff6ff;
  color: var(--primary);
}

.nochange-save-box h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.nochange-save-box p {
  margin: 0 0 22px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-2);
}

.nochange-save-actions {
  display: flex;
  justify-content: center;
}

.nochange-save-actions .btn-confirm {
  min-width: 108px;
}

.share-manager-overlay {
  position: fixed;
  inset: 0;
  z-index: 10003;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.36);
  backdrop-filter: blur(2px);
}

.share-manager-panel {
  width: 560px;
  max-width: calc(100vw - 32px);
  max-height: min(680px, calc(100vh - 48px));
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.share-manager-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border);
}

.share-manager-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.share-manager-header p {
  margin: 5px 0 0;
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.5;
}

.share-manager-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  transition: all 0.15s ease;
}

.share-manager-close:hover {
  color: var(--text-1);
  background: var(--bg-page);
}

.share-manager-body {
  overflow-y: auto;
  padding: 10px;
}

.share-manager-empty {
  padding: 44px 0;
  text-align: center;
  font-size: 13px;
  color: var(--text-3);
}

.share-record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.share-record {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
}

.share-record-main {
  flex: 1;
  min-width: 0;
}

.share-record-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}

.share-record-status {
  padding: 2px 7px;
  border-radius: 999px;
  background: var(--primary-light);
  color: var(--primary);
  font-size: 11px;
  font-weight: 600;
}

.share-record-status.closed {
  background: var(--bg-page);
  color: var(--text-3);
}

.share-record-status.expired {
  background: #fff7ed;
  color: #c2410c;
}

.share-record-meta {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: var(--text-3);
}

.share-record-actions {
  display: grid;
  grid-template-columns: repeat(3, 90px);
  column-gap: 8px;
  row-gap: 12px;
  flex-shrink: 0;
  width: 286px;
}

.share-record-btn {
  box-sizing: border-box;
  height: 28px;
  width: 100%;
  min-width: 90px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: #fff;
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}

.share-record-btn-content {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
}

.share-record-btn.loading,
.share-record-btn.copied {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.share-record-btn-spinner {
  width: 12px;
  height: 12px;
  border-width: 1.8px;
}

.share-record-btn:hover:not(:disabled) {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.share-record-btn.danger:hover:not(:disabled) {
  color: var(--danger);
  border-color: rgba(239, 68, 68, 0.3);
  background: #fef2f2;
}

.share-record-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.share-config-overlay {
  position: fixed;
  inset: 0;
  z-index: 10004;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.38);
  backdrop-filter: blur(4px);
}

.share-config-panel {
  width: 520px;
  max-width: calc(100vw - 32px);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 26px 70px rgba(15, 23, 42, 0.22);
  overflow: visible;
}

.share-config-panel-sm {
  width: 460px;
}

.share-config-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px 16px;
  border-bottom: 1px solid var(--border);
}

.share-config-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-1);
}

.share-config-header p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.55;
  color: var(--text-3);
}

.share-config-close {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-3);
  cursor: pointer;
  transition: all 0.15s ease;
}

.share-config-close:hover {
  color: var(--text-1);
  background: var(--bg-page);
}

.share-config-body {
  padding: 18px 22px 8px;
}

.share-config-group {
  margin-bottom: 18px;
}

.share-config-label {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}

.share-password-mode,
.share-expire-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.share-segment-btn,
.share-chip-btn,
.share-ghost-btn {
  height: 32px;
  min-width: 92px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: 9px;
  background: #fff;
  color: var(--text-2);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}

.share-segment-btn:hover,
.share-chip-btn:hover,
.share-ghost-btn:hover {
  border-color: var(--text-3);
  color: var(--text-1);
}

.share-segment-btn.active,
.share-chip-btn.active {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-light);
}

.share-password-box,
.share-custom-expire {
  margin-top: 12px;
}

.share-chip-input-wrap {
  height: 32px;
  width: 92px;
  min-width: 92px;
  max-width: 92px;
  flex: 0 0 92px;
  padding: 0 12px;
  display: inline-grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  column-gap: 6px;
  border: 1px solid var(--primary);
  border-radius: 9px;
  background: var(--primary-light);
  color: var(--primary);
  box-sizing: border-box;
}

.share-chip-input {
  width: 100%;
  min-width: 0;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--primary);
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  outline: none;
  text-align: left;
}

.share-chip-input::placeholder {
  color: var(--primary);
  opacity: 0.62;
}

.share-chip-input-unit {
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  color: inherit;
  white-space: nowrap;
  justify-self: end;
}

.share-config-input {
  width: 100%;
  height: 38px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fff;
  color: var(--text-1);
  font-size: 13px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.share-config-input:focus {
  border-color: rgba(79, 70, 229, 0.42);
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.08);
}

.share-config-input-sm {
  width: 92px;
  height: 32px;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 9px;
  font-size: 12px;
  font-weight: 600;
}

.share-config-hint {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-3);
}

.share-custom-expire {
  display: flex;
  align-items: center;
  gap: 10px;
}

.share-custom-unit {
  font-size: 12px;
  color: var(--text-3);
}

.share-current-password {
  min-height: 38px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--bg-page);
  color: var(--text-1);
  font-size: 13px;
  font-weight: 600;
}

.share-config-actions {
  display: flex;
  justify-content: flex-end;
  align-items: stretch;
  gap: 10px;
  padding: 14px 22px 20px;
  border-top: 1px solid var(--border);
}

.share-config-actions > .modal-btn,
.share-config-actions > .share-ghost-btn,
.share-config-actions > .modal-btn-hint-wrap {
  flex: 1 1 0;
  min-width: 0;
}

.modal-btn {
  flex: 1;
  height: 38px;
  min-height: 38px;
  padding: 0 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  box-sizing: border-box;
  transition: all 0.15s;
}

.modal-btn--cancel {
  color: var(--text-2);
  background: var(--bg-page);
}

.modal-btn--cancel:hover {
  background: #e5e7eb;
  color: var(--text-1);
}

.modal-btn--ghost {
  color: var(--text-2);
  background: #fff;
  border: 1px solid var(--border);
}

.modal-btn--ghost:hover:not(:disabled) {
  border-color: var(--text-3);
  color: var(--text-1);
  background: #fff;
}

.modal-btn--ghost:disabled {
  color: #9ca3af;
  background: #f3f4f6;
  border-color: #e5e7eb;
  opacity: 1;
  cursor: not-allowed;
  box-shadow: none;
}

.modal-btn--confirm {
  color: #fff;
  background: var(--primary);
}

.modal-btn--confirm:hover:not(:disabled) {
  background: var(--primary-hover);
}

.modal-btn--confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.share-config-actions .modal-btn,
.share-config-actions .share-ghost-btn {
  min-width: 0;
  width: 100%;
}

.modal-btn-hint-wrap {
  position: relative;
  display: flex;
  min-width: 0;
}

.modal-btn-hint-wrap::after {
  content: attr(data-tooltip);
  position: absolute;
  left: 50%;
  bottom: calc(100% + 8px);
  z-index: 20;
  padding: 7px 10px;
  color: var(--text-2);
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transform: translateX(-50%) translateY(4px);
  transition: opacity 0.16s ease, transform 0.16s ease, visibility 0.16s ease;
}

.modal-btn-hint-wrap::before {
  content: '';
  position: absolute;
  left: 50%;
  bottom: calc(100% + 4px);
  z-index: 20;
  width: 8px;
  height: 8px;
  background: #fff;
  border-left: 1px solid rgba(148, 163, 184, 0.28);
  border-top: 1px solid rgba(148, 163, 184, 0.28);
  transform: translateX(-50%) rotate(45deg);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.16s ease, visibility 0.16s ease;
}

.modal-btn-hint-wrap[data-tooltip]:hover::after,
.modal-btn-hint-wrap[data-tooltip]:hover::before {
  opacity: 1;
  visibility: visible;
}

.modal-btn-hint-wrap[data-tooltip]:hover::after {
  transform: translateX(-50%) translateY(0);
}

.share-config-actions .share-ghost-btn {
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.share-config-btn-content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.share-config-btn-spinner {
  width: 12px;
  height: 12px;
  border-width: 1.8px;
}

.share-ghost-btn {
  background: #fff;
}

.share-ghost-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

@media print {
  body {
    margin: 0;
    padding: 0;
  }

  .edit-toolbar,
  .module-selector,
  .form-panel,
  .theme-bar,
  .preview-scroll {
    display: none !important;
  }

  .preview-panel {
    overflow: visible !important;
    flex: none !important;
    background: none !important;
    padding: 0 !important;
  }

  .preview-scroll-inner {
    max-width: none !important;
    margin: 0 !important;
  }

  .paper {
    box-shadow: none !important;
    border-radius: 0 !important;
    transform: none !important;
    width: 100% !important;
  }

  .edit-body {
    display: block !important;
    margin: 0 !important;
    padding: 0 !important;
    gap: 0 !important;
  }

  .accordion-item {
    page-break-inside: avoid;
  }
}
</style>
