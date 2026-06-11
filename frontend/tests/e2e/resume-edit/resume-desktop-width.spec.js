import { expect, test } from '@playwright/test'
import { mockResumeEditorPersistence } from '../helpers/resumeEditorMocks'

test.describe('resume desktop width adaptation', () => {
  test('shows a loading placeholder until toolbar and preview layout are settled on entry', async ({ page }) => {
    await page.setViewportSize({ width: 1700, height: 900 })

    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()

    await expect(page).toHaveURL(/\/$/)

    await page.evaluate(() => {
      window.__editEntryCapture = {
        loadingShown: false,
        toolbarFullyHidden: false,
        firstSeenAt: null,
        lastSeenAt: null,
        minTop: null,
        maxTop: null,
      }

      const mark = () => {
        const loading = document.querySelector('.edit-loading-state')
        if (
          loading &&
          loading.textContent?.includes('加载简历中...') &&
          getComputedStyle(loading).display !== 'none'
        ) {
          const rect = loading.getBoundingClientRect()
          const now = performance.now()
          window.__editEntryCapture.loadingShown = true
          window.__editEntryCapture.firstSeenAt ??= now
          window.__editEntryCapture.lastSeenAt = now
          window.__editEntryCapture.minTop =
            window.__editEntryCapture.minTop == null
              ? rect.top
              : Math.min(window.__editEntryCapture.minTop, rect.top)
          window.__editEntryCapture.maxTop =
            window.__editEntryCapture.maxTop == null
              ? rect.top
              : Math.max(window.__editEntryCapture.maxTop, rect.top)
        }

        const toolbarRoot = document.querySelector('.edit-toolbar')
        if (
          !toolbarRoot ||
          getComputedStyle(toolbarRoot).display === 'none' ||
          getComputedStyle(toolbarRoot).visibility === 'hidden'
        ) {
          window.__editEntryCapture.toolbarFullyHidden = true
        }
      }

      mark()
      let rafId = 0
      const sample = () => {
        mark()
        rafId = requestAnimationFrame(sample)
      }
      rafId = requestAnimationFrame(sample)
      const observer = new MutationObserver(mark)
      observer.observe(document.body, {
        subtree: true,
        childList: true,
        attributes: true,
        attributeFilter: ['class', 'style'],
      })
      window.__editEntryCaptureObserver = observer
      window.__editEntryCaptureRaf = rafId
    })

    await page.getByRole('button', { name: '新建简历' }).click()
    await expect(page).toHaveURL(/\/edit$/)
    await expect(page.locator('.toolbar-action-group')).toBeVisible()
    await expect(page.locator('.paper')).toBeVisible()

    const entryCapture = await page.evaluate(() => {
      window.__editEntryCaptureObserver?.disconnect()
      cancelAnimationFrame(window.__editEntryCaptureRaf)
      return window.__editEntryCapture
    })

    expect(entryCapture.loadingShown).toBe(true)
    expect(entryCapture.toolbarFullyHidden).toBe(true)
    expect(entryCapture.lastSeenAt - entryCapture.firstSeenAt).toBeGreaterThanOrEqual(150)
    expect(entryCapture.maxTop - entryCapture.minTop).toBeLessThanOrEqual(6)
  })

  test('keeps the preview paper fully visible within a 1280px laptop viewport', async ({ page }) => {
    await page.setViewportSize({ width: 1700, height: 900 })

    await mockResumeEditorPersistence(page, {
      user: { id: 1, username: '测试用户' },
      initialResumeId: 101,
    })

    await page.goto('/login')
    await page.getByLabel('账号').fill('tester')
    await page.getByLabel('密码').fill('password123')
    await page.getByRole('button', { name: '登录' }).click()

    await expect(page).toHaveURL(/\/$/)
    await page.getByRole('button', { name: '新建简历' }).click()
    await expect(page).toHaveURL(/\/edit$/)
    await expect(page.locator('.edit-toolbar')).toBeVisible()
    await expect(page.locator('.paper')).toBeVisible()

    const wideMetrics = await page.evaluate(() => {
      const actionButtons = Array.from(document.querySelectorAll('.edit-actions-inner .action-btn'))
      const toolbarButtons = Array.from(document.querySelectorAll('.edit-toolbar .btn-save, .edit-toolbar .btn-export'))
      const root = document.documentElement
      const previewScroll = document.querySelector('.preview-scroll')
      const paper = document.querySelector('.paper')
      const firstActionIcon = document.querySelector('.edit-actions-inner .action-btn svg')
      const firstActionText = document.querySelector('.edit-actions-inner .action-btn .action-btn-label')
      const firstThemeIcon = document.querySelector('.theme-select-trigger svg')
      const firstThemeText = document.querySelector('.theme-select-trigger .theme-select-trigger-label')
      const saveIcon = document.querySelector('.btn-save svg')
      const saveText = document.querySelector('.btn-save > span:last-child')
      const exportIcon = document.querySelector('.btn-export svg')
      const exportText = document.querySelector('.btn-export > span:last-child')
      if (!previewScroll || !paper) {
        return null
      }

      const previewRect = previewScroll.getBoundingClientRect()
      const paperRect = paper.getBoundingClientRect()
      const actionIconRect = firstActionIcon?.getBoundingClientRect()
      const actionTextRect = firstActionText?.getBoundingClientRect()
      const themeIconRect = firstThemeIcon?.getBoundingClientRect()
      const themeTextRect = firstThemeText?.getBoundingClientRect()
      const saveIconRect = saveIcon?.getBoundingClientRect()
      const saveTextRect = saveText?.getBoundingClientRect()
      const exportIconRect = exportIcon?.getBoundingClientRect()
      const exportTextRect = exportText?.getBoundingClientRect()

      const collectButtonCenterDeltas = selector =>
        Array.from(document.querySelectorAll(selector)).map(button => {
          const icon = button.querySelector('svg')
          const text = button.querySelector('.action-btn-label, .theme-select-trigger-label')
          if (!icon || !text) {
            return null
          }
          const ir = icon.getBoundingClientRect()
          const tr = text.getBoundingClientRect()
          return {
            text: text.textContent?.trim() ?? '',
            delta: Math.abs(
              ir.top + ir.height / 2 -
                (tr.top + tr.height / 2),
            ),
            fontSize: getComputedStyle(text).fontSize,
            lineHeight: getComputedStyle(text).lineHeight,
            display: getComputedStyle(text).display,
          }
        }).filter(Boolean)

      return {
        clientWidth: root.clientWidth,
        scrollWidth: root.scrollWidth,
        backLeft: Math.round(document.querySelector('.btn-back')?.getBoundingClientRect().left ?? 0),
        actionButtonFontSize: window.getComputedStyle(document.querySelector('.action-btn')).fontSize,
        themeTriggerFontSize: window.getComputedStyle(document.querySelector('.theme-select-trigger')).fontSize,
        actionButtonTextMetrics: Array.from(document.querySelectorAll('.edit-actions-inner .action-btn .action-btn-label')).map(el => ({
          text: el.textContent?.trim() ?? '',
          fontSize: getComputedStyle(el).fontSize,
          lineHeight: getComputedStyle(el).lineHeight,
          display: getComputedStyle(el).display,
          transform: getComputedStyle(el).transform,
        })),
        themeTriggerTextMetrics: Array.from(document.querySelectorAll('.theme-select-trigger .theme-select-trigger-label')).map(el => ({
          text: el.textContent?.trim() ?? '',
          fontSize: getComputedStyle(el).fontSize,
          lineHeight: getComputedStyle(el).lineHeight,
          display: getComputedStyle(el).display,
          transform: getComputedStyle(el).transform,
        })),
        previewRight: Math.round(previewRect.right),
        paperRight: Math.round(paperRect.right),
        actionBarRight: Math.round(document.querySelector('.edit-actions-inner')?.getBoundingClientRect().right ?? 0),
        actionButtonMaxRight: Math.round(Math.max(
          0,
          ...actionButtons.map(el => el.getBoundingClientRect().right),
        )),
        toolbarButtonMaxRight: Math.round(Math.max(
          0,
          ...toolbarButtons.map(el => el.getBoundingClientRect().right),
        )),
        toolbarButtonViewportRightGap: Math.round(root.clientWidth - Math.max(
          0,
          ...toolbarButtons.map(el => el.getBoundingClientRect().right),
        )),
        actionButtonWidths: actionButtons.map(el => Math.round(el.getBoundingClientRect().width)),
        themeBarRight: Math.round(document.querySelector('.theme-bar-inner')?.getBoundingClientRect().right ?? 0),
        themeControlMaxRight: Math.round(Math.max(
          0,
          ...Array.from(document.querySelectorAll('.theme-bar-inner .theme-dot, .theme-bar-inner .theme-select-wrap')).map(el => el.getBoundingClientRect().right),
        )),
        actionIconTextCenterDelta: actionIconRect && actionTextRect
          ? Math.abs(
              actionIconRect.top + actionIconRect.height / 2 -
                (actionTextRect.top + actionTextRect.height / 2),
            )
          : null,
        themeIconTextCenterDelta: themeIconRect && themeTextRect
          ? Math.abs(
              themeIconRect.top + themeIconRect.height / 2 -
                (themeTextRect.top + themeTextRect.height / 2),
            )
          : null,
        saveIconTextCenterDelta: saveIconRect && saveTextRect
          ? Math.abs(
              saveIconRect.top + saveIconRect.height / 2 -
                (saveTextRect.top + saveTextRect.height / 2),
            )
          : null,
        exportIconTextCenterDelta: exportIconRect && exportTextRect
          ? Math.abs(
              exportIconRect.top + exportIconRect.height / 2 -
                (exportTextRect.top + exportTextRect.height / 2),
            )
          : null,
        actionButtonCenterDeltas: collectButtonCenterDeltas('.edit-actions-inner .action-btn'),
        themeTriggerCenterDeltas: collectButtonCenterDeltas('.theme-select-trigger'),
      }
    })

    await page.setViewportSize({ width: 1538, height: 720 })
    await page.waitForTimeout(100)

    const thresholdMetrics = await page.evaluate(() => ({
      actionButtonFontSize: window.getComputedStyle(document.querySelector('.action-btn')).fontSize,
      themeTriggerFontSize: window.getComputedStyle(document.querySelector('.theme-select-trigger')).fontSize,
      actionButtonTextMetrics: Array.from(document.querySelectorAll('.edit-actions-inner .action-btn .action-btn-label')).map(el => ({
        text: el.textContent?.trim() ?? '',
        fontSize: getComputedStyle(el).fontSize,
        lineHeight: getComputedStyle(el).lineHeight,
        display: getComputedStyle(el).display,
        transform: getComputedStyle(el).transform,
      })),
      themeTriggerTextMetrics: Array.from(document.querySelectorAll('.theme-select-trigger .theme-select-trigger-label')).map(el => ({
        text: el.textContent?.trim() ?? '',
        fontSize: getComputedStyle(el).fontSize,
        lineHeight: getComputedStyle(el).lineHeight,
        display: getComputedStyle(el).display,
        transform: getComputedStyle(el).transform,
      })),
    }))

    await page.setViewportSize({ width: 1280, height: 720 })
    await page.waitForTimeout(100)

    const narrowMetrics = await page.evaluate(() => {
      const actionButtons = Array.from(document.querySelectorAll('.edit-actions-inner .action-btn'))
      const toolbarButtons = Array.from(document.querySelectorAll('.edit-toolbar .btn-save, .edit-toolbar .btn-export'))
      const root = document.documentElement
      const previewScroll = document.querySelector('.preview-scroll')
      const paper = document.querySelector('.paper')
      if (!previewScroll || !paper) {
        return null
      }

      const previewRect = previewScroll.getBoundingClientRect()
      const paperRect = paper.getBoundingClientRect()

      return {
        clientWidth: root.clientWidth,
        scrollWidth: root.scrollWidth,
        backLeft: Math.round(document.querySelector('.btn-back')?.getBoundingClientRect().left ?? 0),
        previewRight: Math.round(previewRect.right),
        paperRight: Math.round(paperRect.right),
        actionBarRight: Math.round(document.querySelector('.edit-actions-inner')?.getBoundingClientRect().right ?? 0),
        actionButtonMaxRight: Math.round(Math.max(
          0,
          ...actionButtons.map(el => el.getBoundingClientRect().right),
        )),
        toolbarButtonMaxRight: Math.round(Math.max(
          0,
          ...toolbarButtons.map(el => el.getBoundingClientRect().right),
        )),
        toolbarButtonViewportRightGap: Math.round(root.clientWidth - Math.max(
          0,
          ...toolbarButtons.map(el => el.getBoundingClientRect().right),
        )),
        actionButtonWidths: actionButtons.map(el => Math.round(el.getBoundingClientRect().width)),
        visibleActionButtons: actionButtons
          .map(el => {
            return {
              text: el.textContent?.trim() ?? '',
              isFullyVisible:
                el.scrollWidth <= el.clientWidth + 1 &&
                el.scrollHeight <= el.clientHeight + 1,
            }
          })
          .filter(Boolean),
        themeBarRight: Math.round(document.querySelector('.theme-bar-inner')?.getBoundingClientRect().right ?? 0),
        themeControlMaxRight: Math.round(Math.max(
          0,
          ...Array.from(document.querySelectorAll('.theme-bar-inner .theme-dot, .theme-bar-inner .theme-select-wrap')).map(el => el.getBoundingClientRect().right),
        )),
      }
    })

    expect(wideMetrics).not.toBeNull()
    expect(narrowMetrics).not.toBeNull()
    expect(
      wideMetrics.actionButtonTextMetrics.every(
        item =>
          item.display === 'flex' &&
          Math.abs(Number.parseFloat(item.lineHeight) - Number.parseFloat(item.fontSize)) <= 0.2 &&
          item.transform === 'none',
      ),
    ).toBe(true)
    expect(
      wideMetrics.themeTriggerTextMetrics.every(
        item =>
          item.display === 'flex' &&
          Math.abs(Number.parseFloat(item.lineHeight) - Number.parseFloat(item.fontSize)) <= 0.2 &&
          item.transform === 'none',
      ),
    ).toBe(true)
    expect(thresholdMetrics.actionButtonFontSize).toBe(wideMetrics.actionButtonFontSize)
    expect(thresholdMetrics.themeTriggerFontSize).toBe(wideMetrics.themeTriggerFontSize)
    expect(thresholdMetrics.actionButtonTextMetrics.length).toBeGreaterThan(0)
    expect(
      thresholdMetrics.actionButtonTextMetrics.every(
        item =>
          item.display === 'flex' &&
          Math.abs(Number.parseFloat(item.lineHeight) - Number.parseFloat(item.fontSize)) <= 0.2 &&
          item.transform === 'none',
      ),
    ).toBe(true)
    expect(thresholdMetrics.themeTriggerTextMetrics.length).toBeGreaterThan(0)
    expect(
      thresholdMetrics.themeTriggerTextMetrics.every(
        item =>
          item.display === 'flex' &&
          Math.abs(Number.parseFloat(item.lineHeight) - Number.parseFloat(item.fontSize)) <= 0.2 &&
          item.transform === 'none',
      ),
    ).toBe(true)
    expect(narrowMetrics.scrollWidth).toBeLessThanOrEqual(narrowMetrics.clientWidth)
    expect(narrowMetrics.paperRight).toBeLessThanOrEqual(narrowMetrics.previewRight)
    expect(Math.abs(narrowMetrics.toolbarButtonMaxRight - narrowMetrics.paperRight)).toBeLessThanOrEqual(12)
    expect(narrowMetrics.actionButtonMaxRight).toBeLessThanOrEqual(narrowMetrics.actionBarRight)
    expect(narrowMetrics.themeControlMaxRight).toBeLessThanOrEqual(narrowMetrics.themeBarRight)
    expect(Math.abs(wideMetrics.toolbarButtonMaxRight - wideMetrics.paperRight)).toBeLessThanOrEqual(12)
    expect(Math.abs(wideMetrics.backLeft - wideMetrics.toolbarButtonViewportRightGap)).toBeLessThanOrEqual(8)
    expect(wideMetrics.actionIconTextCenterDelta).not.toBeNull()
    expect(wideMetrics.themeIconTextCenterDelta).not.toBeNull()
    expect(wideMetrics.saveIconTextCenterDelta).not.toBeNull()
    expect(wideMetrics.exportIconTextCenterDelta).not.toBeNull()
    expect(wideMetrics.actionIconTextCenterDelta).toBeLessThanOrEqual(0.5)
    expect(wideMetrics.themeIconTextCenterDelta).toBeLessThanOrEqual(0.5)
    expect(wideMetrics.saveIconTextCenterDelta).toBeLessThanOrEqual(1)
    expect(wideMetrics.exportIconTextCenterDelta).toBeLessThanOrEqual(1)
    expect(wideMetrics.actionButtonCenterDeltas.every(item => item.delta <= 0.5)).toBe(true)
    expect(wideMetrics.themeTriggerCenterDeltas.every(item => item.delta <= 0.5)).toBe(true)
    expect(Math.max(...wideMetrics.actionButtonWidths) - Math.min(...wideMetrics.actionButtonWidths)).toBeLessThanOrEqual(2)
    expect(Math.max(...narrowMetrics.actionButtonWidths) - Math.min(...narrowMetrics.actionButtonWidths)).toBeLessThanOrEqual(2)
    expect(
      narrowMetrics.visibleActionButtons.every(button => button.isFullyVisible),
    ).toBe(true)
  })
})
