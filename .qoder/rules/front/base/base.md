# 前端开发规范

## 一、代码规范（强制执行）

### 1. 统一代码风格

- 必须使用 ESLint + Prettier，配置基于 @antfu/eslint-config 或 airbnb/standard 风格，禁止 console.log 在生产环境保留（调试用 debug 模块或条件编译）。
- 必须配置 Git Hooks（husky + lint-staged），在 commit 前自动格式化并修复 lint 错误。
- 必须强制换行符为 LF（.editorconfig 锁定）。

### 2. 命名约定

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 文件/文件夹 | kebab-case | user-profile.vue / use-request.ts |
| 变量/函数 | camelCase | userName, fetchData |
| 常量 | UPPER_SNAKE_CASE | API_BASE_URL |
| 组件名 | PascalCase | UserProfile |
| 类型/接口 | PascalCase | 接口不加 I 前缀，类型别名用 type |

> **注意**：React 中组件名即标签名，模板中使用 kebab-case 或保持相同大小写风格。

### 3. 注释与文档

- 所有公共 API（导出的函数、组件、hooks）必须有 JSDoc/TSDoc 注释（描述、参数、返回值、示例）。
- 复杂业务逻辑必须附解释注释，禁止无意义的注释（// 加1）。
- 待办事项使用 `TODO(username): 说明`。

## 二、组件化与工程化（架构基准）

### 4. 组件设计原则

- **单一职责**：每个组件只做一件事，代码超过 300 行（不含样式）必须拆分。
- **受控/非受控明确**：Vue 使用 v-model 约定，React 明确区分 value + onChange。
- **避免深度嵌套**：超过 3 层传递数据必须使用 context / provide/inject 或状态管理。
- **样式隔离**：必须使用 CSS Modules / Scoped CSS / Tailwind（但禁止全局污染），禁止随意覆盖第三方组件内部样式。

### 5. 状态管理

- 全局状态仅在**真正跨组件共享**时使用（用户信息、主题、路由状态等），局部状态保持在组件内。
- 必须遵循不可变数据原则（使用 immer 或扩展运算符）。
- 异步操作必须在 action / effect 中处理，不混入组件逻辑。

### 6. 项目结构规范

```
src/
├── assets/          # 静态资源（会被构建处理）
├── components/      # 通用组件（无业务耦合）
├── features/        # 业务功能模块（高内聚）
├── hooks/           # 自定义 hooks / composables
├── layouts/         # 布局组件
├── pages/           # 页面级组件（路由对应）
├── services/        # API 调用层
├── stores/          # 状态管理
├── types/           # 全局类型定义
├── utils/           # 纯函数工具
└── config/          # 环境配置
```

> **说明**：每个业务模块（features/xxx）内部自包含：components/, hooks/, types/。

### 7. 依赖管理

- 禁止使用未经评审的三方库，新增依赖需经过技术负责人确认。
- 定期（每月）执行 npm audit 或 pnpm outdated，及时升级安全补丁。
- 必须锁定包管理器版本（packageManager 字段），团队统一使用 pnpm 或 yarn。

## 三、性能（硬性指标）

### 8. 打包体积与加载性能

- **路由懒加载**：所有页面组件必须动态导入（`() => import('./Page.vue')`）。
- **图片优化**：所有大于 20KB 的图片必须使用 WebP/AVIF + 懒加载，图标优先使用 SVG 或图标字体。
- **代码分割**：第三方库必须单独 chunk（splitChunks 配置），避免单个文件超过 500KB（未压缩）。
- **首屏指标**：FCP < 1.5s，LCP < 2.5s（以 3G/4G 低端手机测试为准）。

### 9. 运行时性能

- 列表渲染必须带 key（唯一且稳定）。
- 避免在 render/模板中创建函数或对象（如 `@click=() => foo()`），应使用 useCallback/useMemo 或事件处理器绑定。
- 大列表（>1000 项）必须使用虚拟滚动（vue-virtual-scroller / react-window）。
- 高频事件（scroll, resize）必须节流/防抖。

### 10. 资源加载策略

- 字体使用 `font-display: swap`，并预加载关键字体。
- 第三方脚本（埋点、广告）使用 defer 或 async，避免阻塞渲染。

## 四、安全（底线）

### 11. XSS 防护

- 禁止使用 innerHTML / dangerouslySetInnerHTML 插入用户生成内容（UGC）。若必须，需使用 DOMPurify 净化。
- 所有动态 URL（链接、src）必须校验协议白名单（仅允许 http: https: mailto:），禁止 javascript: 伪协议。

### 12. CSRF / 认证

- 所有状态变更请求（POST/PUT/DELETE）必须携带 CSRF token（或使用 SameSite=Lax cookie + 双重提交 cookie 模式）。
- 敏感数据（token, 用户ID）禁止存储在 localStorage（易受 XSS 窃取），应存放在 httpOnly cookie 中或内存存储（配合 refresh token）。

### 13. 第三方依赖安全

- 定期使用 npm audit / snyk 扫描漏洞，高危漏洞必须在 24 小时内修复。
- 禁止引用未锁定版本（如 ^5.0.0），必须使用 pnpm-lock.yaml 或 package-lock.json 锁定。

## 五、可访问性（a11y，必要基础）

### 14. 语义化 HTML

- 必须使用正确语义标签（header/nav/main/article/button 而非 div 模拟）。
- 所有表单控件必须关联 `<label>`（显式 for 或隐式包裹）。

### 15. 键盘可操作性

- 所有交互功能（模态框、下拉菜单）必须可通过键盘（Tab、Enter、Esc）完整操作。
- 焦点可见性不可被 `outline: none` 覆盖（除非提供替代高亮）。

### 16. ARIA 与辅助技术

- 动态内容变化（弹出提示、加载状态）必须通过 aria-live 告知屏幕阅读器。
- 图片必须提供 alt（装饰性图片用 `alt=""`）。
- 若项目用户群体包含残障人士（或公司政策要求），则需通过 WCAG 2.1 AA 级别合规。

## 六、测试与质量门禁（不可跳过）

### 17. 测试覆盖率（强制门槛）

| 测试类型 | 要求 | 工具 |
|----------|------|------|
| 单元测试 | 核心业务逻辑、工具函数、hooks/composables 覆盖率 ≥ 80% | Vitest/Jest |
| 组件测试 | 通用 UI 组件必须测试渲染、事件、属性变化 | Testing Library / Vue Test Utils |
| E2E 测试 | 关键用户流程（登录、下单、提交表单）至少有 3 条端到端测试 | Playwright/Cypress |

### 18. CI 门禁

每次 PR 必须通过：

- ✅ 所有 lint 检查
- ✅ 单元测试 + 组件测试
- ✅ 构建成功（生产模式）
- ✅ 包体积比较报告（超过阈值 +5% 需人工批准）

> **分支保护**：main/master 禁止直接推送，必须 PR + 至少 1 人 Approve。

## 七、文档与协作（保障可持续）

### 19. 生存文档

项目根目录必须包含：

| 文档 | 内容要求 |
|------|----------|
| README.md | 启动、构建、测试命令，环境变量说明，目录结构 |
| CONTRIBUTING.md | 提交规范、代码评审要点、设计评审流程 |
| STYLE_GUIDE.md | 本项目特有的编码示例（如如何处理 API 错误） |

> **注意**：所有环境变量必须在 `.env.example` 中列出，附说明。

### 20. 提交规范

- 必须遵循 Conventional Commits（feat:, fix:, docs:, refactor:, test:, chore:），便于自动生成 changelog 和版本管理。
- 提交粒度：一个逻辑变更一个 commit，禁止巨型 commit。

## 八、例外与仲裁

上述准则为**强制性最低标准**。若出现确实无法满足的情况（例如老项目迁移、特殊浏览器兼容性需求），必须：

1. 在 PR 描述中明确标注豁免项及理由。
2. 至少两位 senior 工程师审核签字。
3. 在项目根目录 EXCEPTIONS.md 记录，并设定解决期限。