export const DEFAULT_MODULES = [
  { key: 'basic', label: '基本信息', enabled: true },
  { key: 'education', label: '教育经历', enabled: true },
  { key: 'skill', label: '专业技能', enabled: true },
  { key: 'experience', label: '工作经历', enabled: true },
  { key: 'project', label: '项目经历', enabled: true },
  { key: 'personalStrengths', label: '个人优势', enabled: true },
  { key: 'award', label: '荣誉奖项', enabled: false },
  { key: 'portfolio', label: '个人作品', enabled: false },
  { key: 'other', label: '其他经历', enabled: false },
]

const DEFAULT_BASIC_PROFILE = {
  name: 'Elovara',
  phone: '13800000000',
  email: 'your.email@example.com',
  avatar: '/avatar.png',
  status: '在校/应届生',
  jobTitle: '前端开发工程师',
  location: '北京',
  salary: '5000-10000元',
  education: '本科',
  website: 'https://portfolio.example.com',
  github: 'https://github.com/your-github-id',
  wechat: 'your_wechat_id',
  age: '24',
  workYears: '3年',
  gender: '男',
}

const DEFAULT_EDUCATION_ITEMS = [
  {
    school: '东应大学',
    major: '信息科学',
    degree: '本科',
    startDate: '2023/09',
    endDate: '2027/06',
    description: '<p>主修课程：信息系统、数据结构、概率统计、社会心理学等。</p><p>成绩长期位列前列，具备扎实的逻辑分析能力与较强的问题解决意识。</p>',
  },
]

export const PLACEHOLDER_DATA = {
  basic: JSON.stringify(DEFAULT_BASIC_PROFILE),
  education: JSON.stringify(DEFAULT_EDUCATION_ITEMS),
    experience: JSON.stringify([
      {
        company: '某某科技有限公司',
        department: '技术部',
        position: '前端开发实习生',
        city: '北京',
        startDate: '2026/03',
        endDate: '2026/08',
        content: '<ol><li>负责公司内部<strong>运营后台、数据看板和配置中心</strong>的前端开发与日常维护，独立完成 10+ 业务页面交付。</li><li>使用 <strong>Vue 3 + TypeScript</strong> 重构原有 jQuery 页面，统一路由、状态与表单交互逻辑，首屏加载速度提升约 40%。</li><li>参与组件库建设，封装<strong>表格、筛选器、弹窗表单、上传器</strong>等 20+ 通用组件，显著减少重复开发成本。</li><li>与产品、测试协作推进迭代，跟进需求评审、联调验收和线上回归，保证版本按期稳定发布。</li><li>定位并修复多项线上样式错位、权限边界和异常状态问题，提升<strong>复杂表单场景</strong>的可用性。</li></ol>',
      },
    ]),
    project: JSON.stringify([
      {
        name: '智能简历生成系统',
        role: '前端负责人',
        city: '北京',
        link: 'https://github.com/example/resume',
        startDate: '2026/06',
        endDate: '2026/09',
        content: '<p>基于 <strong>Vue 3 + Spring Boot</strong> 开发的在线简历编辑与预览平台，支持多模板切换、实时预览、分享与 PDF 导出。</p><ol><li>设计并实现<strong>模块化简历编辑器</strong>，支持内容分区编辑、实时预览、历史版本切换和样式配置。</li><li>完成模板体系抽象，统一 <strong>template props、主题色、字体、字号与行距配置</strong>，降低后续扩模板成本。</li><li>实现<strong>分享管理、密码控制、有效期设置与岗位匹配</strong>等功能，提升产品完整度和可用性。</li><li>优化复杂弹窗、加载反馈和边界交互，增强多步骤操作过程中的状态可感知性与一致性。</li></ol>',
      },
      {
        name: '用户中心管理后台',
        role: '前端开发',
        city: '北京',
        link: 'https://github.com/example/user-center',
        startDate: '2026/09',
        endDate: '2026/01',
        content: '<p>面向内部运营与管理场景的一站式用户管理后台，覆盖<strong>用户查询、权限配置、内容审核</strong>等核心模块。</p><ol><li>基于 <strong>Vue 3、Pinia 与 Vite</strong> 搭建后台项目骨架，完成登录鉴权、菜单权限与通用页面结构设计。</li><li>封装通用查询表单与表格页面模板，统一<strong>列表筛选、批量操作、详情弹窗</strong>等交互模式。</li><li>参与接口联调与异常处理规范建设，提升前后端协作效率并减少重复排查成本。</li></ol>',
      },
    ]),
    skill: JSON.stringify([
      {
        name: '前端开发',
        content: '<ul><li>熟练掌握 <strong>HTML5、CSS3、JavaScript（ES6+）与 TypeScript</strong>，能够独立完成中后台与内容型页面开发。</li><li>熟练使用 <strong>Vue 3、Pinia、Vue Router</strong> 构建复杂单页应用，熟悉 React 基础生态与常见组件化模式。</li><li>熟悉 Vite、Webpack、ESLint、Prettier 等工程化工具，具备<strong>项目脚手架配置与构建优化</strong>经验。</li><li>掌握组件抽象、表单状态管理、列表筛选、弹窗交互、权限控制等常见业务前端方案。</li><li>了解 <strong>Node.js、Spring Boot、MySQL、Redis</strong> 等全栈协作基础，能够独立完成接口联调与问题排查。</li></ul>',
      },
    ]),
    personalStrengths: JSON.stringify({
      content: '<ul><li>热爱前端开发，关注<strong>产品体验与工程质量</strong>，具备较强的自驱力和学习能力。</li><li>能够从需求理解、交互拆解到组件实现独立推进完整功能开发，并持续打磨细节体验。</li><li>熟悉中后台常见业务场景，重视<strong>代码可维护性、状态一致性与边界交互设计</strong>。</li></ul>',
    }),
  award: JSON.stringify([
    { name: '全国大学生数学建模竞赛', date: '2025/11', content: '获得国家二等奖' },
    { name: '校级优秀毕业设计', date: '2026/06', content: '' },
  ]),
    portfolio: JSON.stringify([
      {
        name: '个人技术博客',
        link: 'https://blog.example.com',
        image: '',
        content: '<p>记录前端技术学习笔记与项目实战经验，累计发布 50+ 篇原创文章。</p><ol><li>围绕 Vue 3、TypeScript 与工程化体系整理系列教程，沉淀可复用的项目模板与实践清单。</li><li>持续复盘后台系统、低代码表单和组件库项目中的性能优化、交互细节与问题排查过程。</li></ol>',
      },
    ]),
  other: JSON.stringify([
    {
      name: '开源社区贡献者',
      role: '核心贡献者',
      department: 'Vue.js 生态',
      city: '远程',
      startDate: '2023/09',
      endDate: '2027/06',
      content: '<ul><li>参与 Vue 3 生态多个开源项目的维护与文档翻译</li><li>提交 PR 30+，累计获得 2000+ GitHub Stars</li></ul>',
    },
  ]),
}

const compactSkill = JSON.parse(PLACEHOLDER_DATA.skill)
compactSkill[0].content = '<ul><li>熟练掌握 <strong>HTML5、CSS3、JavaScript（ES6+）与 TypeScript</strong>，能够独立完成中后台与内容型页面开发。</li><li>熟练使用 <strong>Vue 3、Pinia、Vue Router</strong> 构建复杂单页应用，熟悉 React 基础生态与常见组件化模式。</li><li>熟悉 Vite、Webpack、ESLint、Prettier 等工程化工具，具备<strong>项目脚手架配置与构建优化</strong>经验。</li></ul>'

const compactProject = JSON.parse(PLACEHOLDER_DATA.project).slice(0, 1)

const compactPersonalStrengths = JSON.parse(PLACEHOLDER_DATA.personalStrengths)
compactPersonalStrengths.content = '<ul><li>热爱前端开发，关注<strong>产品体验与工程质量</strong>，具备较强的自驱力和学习能力。</li><li>能够从需求理解、交互拆解到组件实现独立推进完整功能开发，并持续打磨细节体验。</li></ul>'

export const COMPACT_PLACEHOLDER_DATA = {
  ...PLACEHOLDER_DATA,
  basic: JSON.stringify({ ...DEFAULT_BASIC_PROFILE }),
  education: JSON.stringify([...DEFAULT_EDUCATION_ITEMS]),
  skill: JSON.stringify(compactSkill),
  project: JSON.stringify(compactProject),
  personalStrengths: JSON.stringify(compactPersonalStrengths),
}
