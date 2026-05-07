<script setup>
import { useRoute } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'

const route = useRoute()
</script>

<template>
  <AppNavbar :active-page="route.meta.nav || ''" />
  <div class="volunteer-shell">
    <router-view />
  </div>
</template>

<style scoped>
.volunteer-shell {
  padding-top: calc(var(--navbar-h) + 24px);
  padding-left: 24px;
  padding-right: 24px;
  padding-bottom: 40px;
}
</style>
