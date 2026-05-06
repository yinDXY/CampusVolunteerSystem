import { reactive } from 'vue'

let toastSeq = 0
export const toastBus = reactive({ list: [] })

export function showToast(msg, type = 'info', duration = 3000) {
  const id = ++toastSeq
  toastBus.list.push({ id, msg, type })
  setTimeout(() => {
    const i = toastBus.list.findIndex((t) => t.id === id)
    if (i >= 0) toastBus.list.splice(i, 1)
  }, duration)
}

let confirmResolver = null
export const confirmState = reactive({
  visible: false,
  title: '',
  message: '',
})

export function showConfirm(title, message) {
  return new Promise((resolve) => {
    confirmState.title = title
    confirmState.message = message
    confirmState.visible = true
    confirmResolver = resolve
  })
}

export function confirmDialogOk() {
  confirmState.visible = false
  if (confirmResolver) {
    confirmResolver(true)
    confirmResolver = null
  }
}

export function confirmDialogCancel() {
  confirmState.visible = false
  if (confirmResolver) {
    confirmResolver(false)
    confirmResolver = null
  }
}
