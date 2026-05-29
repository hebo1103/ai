# 前端技术规范

## 一、技术栈
- 模板引擎：Thymeleaf（与Spring Boot集成）
- 前端框架：Bootstrap 5 / Tailwind CSS
- JavaScript：ES6+
- 构建工具：Maven（集成前端资源）

## 二、项目结构
```
src/main/resources
├── templates         # Thymeleaf模板
│   ├── layout        # 布局模板
│   ├── pages         # 页面模板
│   └── fragments     # 片段模板
├── static
│   ├── css           # 样式文件
│   ├── js            # JavaScript文件
│   └── images        # 图片资源
└── application.yml   # 配置文件
```

## 三、编码规范

### 3.1 HTML规范
- 使用语义化标签（header、nav、main、footer等）
- 保持缩进一致（2个空格）
- 图片必须添加alt属性
- 表单元素必须有label关联

### 3.2 CSS规范
- 使用BEM命名规范或CSS Modules
- 避免使用内联样式
- 响应式设计使用媒体查询
- 颜色、字体等使用CSS变量统一管理

### 3.3 JavaScript规范
- 使用ES6+语法
- 变量使用let/const，避免使用var
- 函数命名使用camelCase
- 模块化开发，避免全局变量污染

### 3.4 Thymeleaf规范
- 模板继承使用 `layout:decorate`
- 片段引入使用 `th:replace` 或 `th:include`
- 数据绑定使用 `th:text`、`th:each` 等
- URL生成使用 `@{/path}`

## 四、响应式设计
- 移动优先设计策略
- 断点设置：
  - 手机：< 768px
  - 平板：768px - 992px
  - 桌面：> 992px
- 图片自适应：max-width: 100%

## 五、性能优化
- 压缩CSS/JS文件
- 图片懒加载
- 合理使用浏览器缓存
- 减少HTTP请求次数
- 使用CDN加速静态资源

## 六、兼容性
- 支持主流浏览器（Chrome、Firefox、Safari、Edge）
- IE11+（如需要）
- 移动端主流浏览器
