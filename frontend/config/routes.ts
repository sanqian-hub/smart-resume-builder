export default [
  {
    // 父路由的路径前缀,所有嵌套在 routes 里的子路由，路径都会以 /user 开头
    path: '/user',
    layout: false,
    routes: [
      // path 就是用户在浏览器输入的网址，component就是你实际的页面
      // Umi 约定路由的 component 路径是相对于项目的 src/pages 目录来解析的。
      // 会最终找到 src/pages/user/register/index.tsx
      {name: '登录', path: '/user/login', component: './user/login'},
      {name: '注册', path: '/user/register', component: './user/register'},
    ],
    // component 的路径
    // 1. 路径是相对于项目 src/pages 目录的；
    // 2. ./user/login 对应实际文件：src/pages/user/login.tsx（或 .jsx）；
    // Umi 框架的 “约定式路由” 规则 帮你做了默认配置,不用写超长路径，让代码更简洁
  },

  {path: '/user/center', component: './user/center'},
  {path: '/user/settings', component: './user/settings'},

  // 当浏览器地址栏输入 域名/user/login 时，加载 ./user/login 这个页面组件
  // 并且不显示项目默认的布局（比如侧边菜单、顶部导航栏）（因为登录页不需要这些）。
  // 简单来说，路由就是定义了前端访问某个路径（url）时，读取哪个页面进行展示，还有一些其他规则
  {path: '/welcome', name: '欢迎', icon: 'smile', component: './Welcome'},
  {
    path: '/admin',
    name: '管理页',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      // {path: '/admin', redirect: '/admin/sub-page'},
      // {path: '/admin/sub-page', name: '二级管理页', component: './Admin'},
      {path: '/admin/user-manage', name: '用户管理页', component: './admin/user-manage'},
    ],
  },
  {path: '/', redirect: '/welcome'},
  {path: '*', layout: false, component: './404'},
];
