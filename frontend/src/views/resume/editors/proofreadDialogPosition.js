export function clampProofreadDialogPosition(nextX, nextY, viewportWidth, viewportHeight, dialogWidth, dialogHeight) {
  return {
    x: Math.max(0, Math.min(viewportWidth - dialogWidth, nextX)),
    y: Math.max(0, Math.min(viewportHeight - dialogHeight, nextY)),
  }
}

export function getCenteredProofreadDialogPosition(viewportWidth, viewportHeight, dialogWidth, dialogHeight) {
  return {
    x: Math.max(0, (viewportWidth - dialogWidth) / 2),
    y: Math.max(48, (viewportHeight - dialogHeight) / 2),
  }
}
