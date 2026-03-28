// config/config.dev.ts
import { defineConfig } from '@umijs/max';
import proxy from './proxy';

console.log('加载 dev 环境配置，代理配置为：', proxy.dev);

export default defineConfig({
  proxy: proxy.dev,
  mock: false,
});
