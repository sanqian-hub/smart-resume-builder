function normalizeOccurrenceIndex(value) {
  if (Number.isInteger(value) && value >= 0) return value
  const parsed = Number.parseInt(value, 10)
  return Number.isInteger(parsed) && parsed >= 0 ? parsed : 0
}

export function normalizeFieldPath(value) {
  if (typeof value !== 'string') return value ?? null
  const trimmed = value.trim()
  if (!trimmed) return null
  const parts = trimmed.split('.').filter(Boolean)
  return parts.length ? parts[parts.length - 1] : trimmed
}

function normalizeHighlightItemIndex(moduleType, itemIndex) {
  if (moduleType === 'basic' || moduleType === 'personalStrengths') {
    return null
  }
  return itemIndex ?? null
}

function findOccurrenceIndex(text, fragment, occurrenceIndex) {
  if (!text || !fragment) return -1
  let start = -1
  let searchFrom = 0
  for (let i = 0; i <= occurrenceIndex; i += 1) {
    start = text.indexOf(fragment, searchFrom)
    if (start < 0) return -1
    searchFrom = start + fragment.length
  }
  return start
}

function findAllOccurrenceIndexes(text, fragment) {
  if (!text || !fragment) return []
  const indexes = []
  let searchFrom = 0
  while (searchFrom <= text.length) {
    const start = text.indexOf(fragment, searchFrom)
    if (start < 0) break
    indexes.push(start)
    searchFrom = start + fragment.length
  }
  return indexes
}

function normalizeComparableText(text) {
  return typeof text === 'string' ? text.replace(/\s+/g, '') : ''
}

function buildNormalizedOffsetMap(text) {
  if (!text) {
    return {
      normalizedText: '',
      rawIndexes: [],
    }
  }

  const rawIndexes = []
  let normalizedText = ''
  for (let index = 0; index < text.length; index += 1) {
    const char = text[index]
    if (/\s/.test(char)) continue
    normalizedText += char
    rawIndexes.push(index)
  }

  return { normalizedText, rawIndexes }
}

function findOccurrenceRange(text, fragment, occurrenceIndex) {
  const exactMatch = findOccurrenceIndex(text, fragment, occurrenceIndex)
  if (exactMatch >= 0) {
    return {
      start: exactMatch,
      end: exactMatch + fragment.length,
    }
  }

  const allMatches = findAllOccurrenceIndexes(text, fragment)
  if (allMatches.length === 1) {
    return {
      start: allMatches[0],
      end: allMatches[0] + fragment.length,
    }
  }

  return null
}

function findNormalizedOccurrenceRange(text, fragment, occurrenceIndex) {
  const normalizedFragment = normalizeComparableText(fragment)
  if (!text || !normalizedFragment) return null

  const { normalizedText, rawIndexes } = buildNormalizedOffsetMap(text)
  if (!normalizedText) return null

  let start = -1
  let searchFrom = 0
  for (let index = 0; index <= occurrenceIndex; index += 1) {
    start = normalizedText.indexOf(normalizedFragment, searchFrom)
    if (start < 0) return null
    searchFrom = start + normalizedFragment.length
  }

  const rawStart = rawIndexes[start]
  const rawEnd = rawIndexes[start + normalizedFragment.length - 1]
  if (rawStart == null || rawEnd == null) return null

  return {
    start: rawStart,
    end: rawEnd + 1,
  }
}

export function findBestOccurrenceRange(text, fragment, occurrenceIndex) {
  const exactRange = findOccurrenceRange(text, fragment, occurrenceIndex)
  if (exactRange) return exactRange
  return findNormalizedOccurrenceRange(text, fragment, occurrenceIndex)
}

function hasHighlightedSegment(segments) {
  return segments.some((segment) => segment.highlighted)
}

const proofreadHighlightInlineStyle = [
  'background: rgba(255, 196, 61, 0.5)',
  'border-radius: 3px',
  'box-shadow: inset 0 -1px 0 rgba(230, 145, 0, 0.78), 0 0 0 1px rgba(255, 196, 61, 0.18)',
].join('; ')

export function matchProofreadHighlights(highlights = [], moduleType, itemIndex, fieldPath) {
  return highlights.filter((item) => {
    if (!item?.original) return false
    if (item.moduleType !== moduleType) return false
    if (normalizeHighlightItemIndex(item.moduleType, item.itemIndex) !== normalizeHighlightItemIndex(moduleType, itemIndex)) return false
    if (normalizeFieldPath(item.fieldPath) !== normalizeFieldPath(fieldPath)) return false
    return true
  })
}

export function buildProofreadSegments(text, highlights = []) {
  if (typeof text !== 'string' || !text) {
    return [{ text: text || '', highlighted: false, highlightId: null }]
  }

  const matches = highlights
    .map((item) => {
      const range = findBestOccurrenceRange(text, item.original, normalizeOccurrenceIndex(item.occurrenceIndex))
      if (!range) return null
      return {
        start: range.start,
        end: range.end,
        id: item.id || null,
      }
    })
    .filter(Boolean)
    .sort((left, right) => left.start - right.start || left.end - right.end)

  if (!matches.length) {
    return [{ text, highlighted: false, highlightId: null }]
  }

  const segments = []
  let cursor = 0
  matches.forEach((match) => {
    if (match.start < cursor) return
    if (match.start > cursor) {
      segments.push({
        text: text.slice(cursor, match.start),
        highlighted: false,
        highlightId: null,
      })
    }
    segments.push({
      text: text.slice(match.start, match.end),
      highlighted: true,
      highlightId: match.id,
    })
    cursor = match.end
  })
  if (cursor < text.length) {
    segments.push({
      text: text.slice(cursor),
      highlighted: false,
      highlightId: null,
    })
  }
  return segments
}

export function buildProofreadRichHtml(html, highlights = []) {
  if (typeof html !== 'string' || !html || !highlights.length || typeof DOMParser === 'undefined') {
    return html || ''
  }

  const parser = new DOMParser()
  const doc = parser.parseFromString(`<div data-proofread-root="true">${html}</div>`, 'text/html')
  const root = doc.body.querySelector('[data-proofread-root="true"]')

  if (!root) return html

  const blocks = Array.from(root.querySelectorAll('p, li'))
  const targets = blocks.length ? blocks : [root]
  const blockMetas = targets.map((block) => {
    const textNodes = collectRichTextNodes(doc, block)
    const plainText = textNodes.map(node => node.textContent || '').join('')
    return {
      block,
      textNodes,
      plainText,
      matches: [],
    }
  })

  highlights.forEach((item) => {
    let matchedSingleBlock = false
    for (const meta of blockMetas) {
      if (!meta.plainText.trim()) continue
      const range = findBestOccurrenceRange(meta.plainText, item.original, normalizeOccurrenceIndex(item.occurrenceIndex))
      if (range) {
        meta.matches.push({
          start: range.start,
          end: range.end,
          id: item.id || null,
        })
        matchedSingleBlock = true
        break
      }

      if (normalizeComparableText(meta.plainText) === normalizeComparableText(item.original)) {
        meta.matches.push({
          start: 0,
          end: meta.plainText.length,
          id: item.id || null,
        })
        matchedSingleBlock = true
        break
      }

    }

    if (matchedSingleBlock) return

    const range = findConsecutiveRichBlockRange(blockMetas.map(meta => meta.plainText), item.original)
    if (!range) return
    for (let index = range.startIndex; index <= range.endIndex; index += 1) {
      const meta = blockMetas[index]
      if (!meta?.plainText) continue
      meta.matches.push({
        start: 0,
        end: meta.plainText.length,
        id: item.id || null,
      })
    }
  })

  let replaced = false

  blockMetas.forEach((meta) => {
    if (!meta.textNodes.length || !meta.matches.length) return

    const matches = meta.matches
      .sort((left, right) => right.start - left.start || right.end - left.end)

    matches.forEach((match) => {
      const liveTextNodes = collectRichTextNodes(doc, meta.block)
      const nodeRanges = []
      let cursor = 0
      liveTextNodes.forEach((node) => {
        const value = node.textContent || ''
        nodeRanges.push({
          node,
          start: cursor,
          end: cursor + value.length,
        })
        cursor += value.length
      })

      const startBoundary = resolveRichTextBoundary(nodeRanges, match.start)
      const endBoundary = resolveRichTextBoundary(nodeRanges, match.end)
      if (!startBoundary || !endBoundary) return
      if (!startBoundary.node || !endBoundary.node) return
      if (
        startBoundary.node === endBoundary.node &&
        endBoundary.offset <= startBoundary.offset
      ) {
        return
      }

      const range = doc.createRange()
      range.setStart(startBoundary.node, startBoundary.offset)
      range.setEnd(endBoundary.node, endBoundary.offset)

      const span = doc.createElement('span')
      span.className = 'proofread-text-highlight'
      if (match.id) {
        span.setAttribute('data-highlight-id', match.id)
      }
      span.setAttribute('style', proofreadHighlightInlineStyle)
      span.appendChild(range.extractContents())
      range.insertNode(span)
      replaced = true
    })
  })

  return replaced ? root.innerHTML : html
}

function collectRichTextNodes(doc, block) {
  const walker = doc.createTreeWalker(block, NodeFilter.SHOW_TEXT)
  const textNodes = []
  let currentNode = walker.nextNode()
  while (currentNode) {
    if (currentNode.textContent) {
      textNodes.push(currentNode)
    }
    currentNode = walker.nextNode()
  }
  return textNodes
}

export function findConsecutiveRichBlockRange(blockTexts = [], original = '') {
  const normalizedOriginal = normalizeComparableText(original)
  if (!normalizedOriginal) return null

  for (let startIndex = 0; startIndex < blockTexts.length; startIndex += 1) {
    const first = normalizeComparableText(blockTexts[startIndex])
    if (!first) continue
    let combined = ''
    for (let endIndex = startIndex; endIndex < blockTexts.length; endIndex += 1) {
      const current = normalizeComparableText(blockTexts[endIndex])
      if (!current) break
      combined += current
      if (combined === normalizedOriginal && endIndex > startIndex) {
        return { startIndex, endIndex }
      }
      if (!normalizedOriginal.startsWith(combined)) {
        break
      }
    }
  }

  return null
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
