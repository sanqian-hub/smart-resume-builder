function blobToDataUrl(blob, FileReaderCtor = FileReader) {
  return new Promise((resolve, reject) => {
    const reader = new FileReaderCtor()
    reader.onload = () => resolve(reader.result)
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

export async function inlineImageSourcesForExport(root, options = {}) {
  if (!root) {
    return () => {}
  }

  const fetchImpl = options.fetchImpl || fetch
  const FileReaderCtor = options.FileReaderCtor || FileReader
  const images = Array.from(root.querySelectorAll('img[src]'))
  const originals = new Map()

  await Promise.all(images.map(async (img) => {
    const src = img.getAttribute('src') || ''
    if (!src || src.startsWith('data:')) {
      return
    }

    try {
      const response = await fetchImpl(src)
      if (!response?.ok) {
        return
      }

      const blob = await response.blob()
      const dataUrl = await blobToDataUrl(blob, FileReaderCtor)
      originals.set(img, src)
      img.setAttribute('src', dataUrl)
      if (typeof img.decode === 'function') {
        await img.decode().catch(() => {})
      }
    } catch {
      // Keep the original src on fetch or decode failure.
    }
  }))

  return () => {
    originals.forEach((src, img) => {
      img.setAttribute('src', src)
    })
  }
}
