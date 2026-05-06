<script setup>
import { confirmState, confirmDialogOk, confirmDialogCancel } from '../lib/ui.js'

function onOverlayClick(e) {
  if (e.target === e.currentTarget) confirmDialogCancel()
}
</script>

<template>
  <div
    v-if="confirmState.visible"
    class="modal-overlay show"
    @click="onOverlayClick"
  >
    <div class="modal" @click.stop>
      <h3>{{ confirmState.title }}</h3>
      <p>{{ confirmState.message }}</p>
      <div class="modal-actions">
        <button type="button" class="btn btn-outline" @click="confirmDialogCancel">
          取消
        </button>
        <button type="button" class="btn btn-primary" @click="confirmDialogOk">
          确认
        </button>
      </div>
    </div>
  </div>
</template>
