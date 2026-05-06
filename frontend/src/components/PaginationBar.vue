<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
})

const emit = defineEmits(['update:pageNum'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)

const pages = computed(() => {
  const tp = totalPages.value
  const p = props.pageNum
  if (tp <= 1) return []
  if (tp <= 7) {
    return Array.from({ length: tp }, (_, i) => i + 1)
  }
  const arr = [1]
  if (p > 3) arr.push('...')
  for (let i = Math.max(2, p - 1); i <= Math.min(tp - 1, p + 1); i++) {
    arr.push(i)
  }
  if (p < tp - 2) arr.push('...')
  arr.push(tp)
  return arr
})

function go(p) {
  emit('update:pageNum', p)
}
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button type="button" :disabled="pageNum <= 1" @click="go(pageNum - 1)">
      ‹
    </button>
    <button
      v-for="(p, idx) in pages"
      :key="idx"
      type="button"
      :disabled="p === '...'"
      :class="{ active: p === pageNum }"
      @click="typeof p === 'number' && go(p)"
    >
      {{ p }}
    </button>
    <button type="button" :disabled="pageNum >= totalPages" @click="go(pageNum + 1)">
      ›
    </button>
  </div>
</template>
