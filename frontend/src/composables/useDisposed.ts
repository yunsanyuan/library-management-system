import { ref, onUnmounted } from 'vue'

export function useDisposed() {
  const disposed = ref(false)
  onUnmounted(() => { disposed.value = true })
  return disposed
}
