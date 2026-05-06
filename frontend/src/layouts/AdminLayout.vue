<script setup>
import { useRoute } from 'vue-router'
import AppNavbar from '../components/AppNavbar.vue'
import AdminSidebar from '../components/AdminSidebar.vue'

const route = useRoute()
</script>

<template>
  <AppNavbar active-page="admin" />
  <AdminSidebar :active-page="route.meta.sidebar || ''" />
  <div class="admin-body">
    <router-view />
  </div>
</template>
