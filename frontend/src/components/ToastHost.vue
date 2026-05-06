<script setup>
import { toastBus } from '../lib/ui.js'
</script>

<template>
  <div class="toast-container">
    <div
      v-for="t in toastBus.list"
      :key="t.id"
      :class="['toast', 'toast-' + t.type]"
    >
      {{ t.msg }}
    </div>
  </div>
</template>
