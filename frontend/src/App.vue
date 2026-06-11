<template>
  <router-view />
  <RuntimeLoader />
</template>

<script setup>
import RuntimeLoader from './components/RuntimeLoader.vue'
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
