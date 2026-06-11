import { findBestOccurrenceRange, findConsecutiveRichBlockRange } from './proofreadHighlight'

function normalizeComparableText(text) {
  return typeof text === 'string' ? text.replace(/\s+/g, '') : ''
}

function replaceProofreadText(value, original, suggestion) {
  if (typeof value === 'string') {
    if (value.includes(original)) {
      return value.split(original).join(suggestion)
    }
    const richHtmlReplaced = replaceProofreadRichHtmlByBlocks(value, original, suggestion)
    return richHtmlReplaced ?? value
  }
  if (Array.isArray(value)) {
    return value.map(item => replaceProofreadText(item, original, suggestion))
  }
  if (value && typeof value === 'object') {
    let changed = false
    const next = {}
    Object.entries(value).forEach(([key, item]) => {
      const nextValue = replaceProofreadText(item, original, suggestion)
      if (nextValue !== item) changed = true
      next[key] = nextValue
    })
    return changed ? next : value
  }
  return value
}

function replaceProofreadRichHtmlByBlocks(html, original, suggestion) {
  if (typeof html !== 'string' || !html.includes('<') || !html.includes('>') || typeof DOMParser === 'undefined') {
    return null
  }

  const parser = new DOMParser()
  const doc = parser.parseFromString(`<div data-proofread-root="true">${html}</div>`, 'text/html')
  const root = doc.body.querySelector('[data-proofread-root="true"]')
  if (!root) return null

  const blocks = Array.from(root.querySelectorAll('p, li'))
  const targets = blocks.length ? blocks : [root]
  const singleBlockIndex = targets.findIndex((block) => {
    const text = block.textContent || ''
    if (normalizeComparableText(text) === normalizeComparableText(original)) {
      return true
    }
    return Boolean(findBestOccurrenceRange(text, original, 0))
  })

  if (singleBlockIndex >= 0) {
    const target = targets[singleBlockIndex]
    if (!target?.parentNode) return null
    const targetText = target.textContent || ''
    const wholeBlockMatch = normalizeComparableText(targetText) === normalizeComparableText(original)

    if (wholeBlockMatch) {
      const replacement = target.cloneNode(false)
      replacement.textContent = suggestion
      target.parentNode.insertBefore(replacement, target)
      target.remove()
      return root.innerHTML
    }

    const walker = doc.createTreeWalker(target, NodeFilter.SHOW_TEXT)
    const textNodes = []
    let currentNode = walker.nextNode()
    while (currentNode) {
      if (currentNode.textContent) {
        textNodes.push(currentNode)
      }
      currentNode = walker.nextNode()
    }

    const rangeMatch = findBestOccurrenceRange(targetText, original, 0)
    if (!textNodes.length || !rangeMatch) return null

    const nodeRanges = []
    let cursor = 0
    textNodes.forEach((node) => {
      const value = node.textContent || ''
      nodeRanges.push({
        node,
        start: cursor,
        end: cursor + value.length,
      })
      cursor += value.length
    })

    const startBoundary = resolveRichTextBoundary(nodeRanges, rangeMatch.start)
    const endBoundary = resolveRichTextBoundary(nodeRanges, rangeMatch.end)
    if (!startBoundary || !endBoundary) return null
    if (!startBoundary.node || !endBoundary.node) return null
    if (
      startBoundary.node === endBoundary.node &&
      endBoundary.offset <= startBoundary.offset
    ) {
      return null
    }

    const range = doc.createRange()
    range.setStart(startBoundary.node, startBoundary.offset)
    range.setEnd(endBoundary.node, endBoundary.offset)
    range.deleteContents()
    range.insertNode(doc.createTextNode(suggestion))
    return root.innerHTML
  }

  const range = findConsecutiveRichBlockRange(
    targets.map(block => block.textContent || ''),
    original,
  )

  if (!range) return null

  const startBlock = targets[range.startIndex]
  if (!startBlock?.parentNode) return null

  const replacement = startBlock.cloneNode(false)
  replacement.textContent = suggestion
  startBlock.parentNode.insertBefore(replacement, startBlock)

  for (let index = range.startIndex; index <= range.endIndex; index += 1) {
    targets[index]?.remove()
  }

  return root.innerHTML
}

function resolveRichTextBoundary(nodeRanges, absoluteOffset) {
  for (const entry of nodeRanges) {
    const nodeLength = entry.node?.textContent?.length ?? 0
    if (absoluteOffset < entry.end) {
      return {
        node: entry.node,
        offset: Math.min(nodeLength, Math.max(0, absoluteOffset - entry.start)),
      }
    }
    if (absoluteOffset === entry.end) {
      return {
        node: entry.node,
        offset: Math.min(nodeLength, entry.end - entry.start),
      }
    }
  }

  const last = nodeRanges[nodeRanges.length - 1]
  if (!last) return null
  const lastNodeLength = last.node?.textContent?.length ?? 0
  return {
    node: last.node,
    offset: Math.min(lastNodeLength, last.end - last.start),
  }
}

export function replaceProofreadContent(raw, original, suggestion) {
  if (!raw || !original || !suggestion || original === suggestion) return raw
  try {
    const parsed = JSON.parse(raw)
    const replaced = replaceProofreadText(parsed, original, suggestion)
    return JSON.stringify(replaced)
  } catch {
    if (raw.includes(original)) {
      return raw.split(original).join(suggestion)
    }
    return replaceProofreadRichHtmlByBlocks(raw, original, suggestion) ?? raw
  }
}
