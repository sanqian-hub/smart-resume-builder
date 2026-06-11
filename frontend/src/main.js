import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')

requestAnimationFrame(() => {
  const loader = document.getElementById('app-loader')
  if (loader) {
    loader.classList.add('hide')
  }
  document.getElementById('app').classList.add('loaded')
})
