# TDD（测试驱动开发）自动化流程指南

> **目标**：当产品经理提供 PRD（产品需求文档）和原型设计后，本指南将帮助开发者（或 AI）严格按照 TDD 流程生成高质量、可测试、可维护的代码。  
> **适用范围**：后端/全栈项目（Java/Spring Boot），可适配其他技术栈。  
> **核心理念**：先写测试，后写实现 → 红（失败）→ 绿（通过）→ 重构。

---

## 一、输入物

在启动 TDD 流程前，你需要拿到以下材料：

1. **PRD 文档**（Markdown 或文本）  
   - 功能列表、用户故事、验收标准、业务规则、异常场景。
2. **原型设计**（图片 / Figma / HTML 草稿）  
   - 页面结构、组件、交互逻辑、数据字段定义。

---

## 二、输出物（按流程顺序）

| 阶段 | 产物 | 说明 |
|------|------|------|
| 分析 | 测试用例清单（Excel / Markdown 表格） | 覆盖正常路径、边界条件、异常场景 |
| 红 | 测试代码（JUnit / Mockito / 集成测试） | 所有测试必须编译通过但运行失败（红） |
| 绿 | 最小实现代码 | 仅使测试通过，不做多余优化 |
| 重构 | 重构后的代码 + 通过测试 | 消除重复、提升可读性/性能 |
| 循环 | 下一组测试 → 实现 → 重构 | 按用户故事或模块迭代 |
| 最终 | 完整项目 + 测试报告 | 所有测试通过，代码覆盖率 ≥ 80% |

---

## 三、详细步骤（AI 执行指令）

### Step 0：环境准备
- 确认项目技术栈（Spring Boot + JUnit 5 + Mockito + Testcontainers 等）。
- 构建工具：Maven / Gradle。
- 配置测试目录：`src/test/java`。

### Step 1：需求分析 → 生成测试用例清单

从 PRD 和原型中提取以下内容：

#### 1.1 用户故事拆解
- 例：`作为一个用户，我希望注册账号，以便使用系统。`

#### 1.2 验收标准转测试点
- **正常路径**：成功注册 → 返回用户信息 + 201 Created。
- **边界条件**：用户名长度最小值/最大值、邮箱格式。
- **异常场景**：用户名已存在、缺少必填字段、密码不符合规则。

#### 1.3 生成测试用例表格（示例）

| 用例ID | 模块 | 场景 | 输入 | 期望输出 | 测试类型 |
|--------|------|------|------|----------|----------|
| TC001 | 注册 | 成功注册 | 合法用户名、邮箱、密码 | 201，用户对象 | 单元/集成 |
| TC002 | 注册 | 用户名重复 | 已存在用户名 | 409 Conflict | 单元/集成 |
| TC003 | 注册 | 密码过短 | 长度<6 | 400 Bad Request | 单元测试 |
| ... | ... | ... | ... | ... | ... |

> 将此表格保存为 `test-cases.md`，作为后续编写测试的依据。

---

### Step 2：编写测试（红阶段）

**规则**：
- 每个测试用例对应一个测试方法。
- 测试方法命名遵循：`should_ExpectedBehavior_When_StateUnderTest`。
- 使用断言库（AssertJ / Hamcrest）。
- 对外部依赖（数据库、第三方 API）使用 Mock 或 Testcontainers。

#### 2.1 单元测试示例（Service 层）

```java
@Test
void shouldReturnUser_WhenRegisterWithValidData() {
    // given
    RegisterRequest request = new RegisterRequest("john", "john@example.com", "pass123");
    when(userRepository.existsByUsername("john")).thenReturn(false);
    when(userRepository.save(any())).thenReturn(new User(1L, "john", "john@example.com"));

    // when
    UserDto result = userService.register(request);

    // then
    assertThat(result.getUsername()).isEqualTo("john");
    verify(userRepository).save(any());
}

@Test
void shouldThrowConflict_WhenUsernameAlreadyExists() {
    // given
    RegisterRequest request = new RegisterRequest("john", "john@example.com", "pass123");
    when(userRepository.existsByUsername("john")).thenReturn(true);

    // when / then
    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(DuplicateUsernameException.class);
}
2.2 集成测试示例（Controller + 真实数据库）
java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb");

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturn201_WhenRegister() throws Exception {
        String requestJson = """
                {
                    "username": "alice",
                    "email": "alice@example.com",
                    "password": "secret123"
                }
                """;
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"));
    }
}
2.3 执行测试（红）
运行 mvn test 或 gradle test —— 所有测试均失败（因为实现代码尚未编写）。

Step 3：编写最小实现（绿阶段）
规则：

只编写让当前失败测试通过的最少代码。

不预先设计，不添加额外功能。

允许硬编码或简单返回，只要通过测试即可（后续重构）。

示例（Service 实现）
java
public UserDto register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new DuplicateUsernameException();
    }
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    User saved = userRepository.save(user);
    return new UserDto(saved.getId(), saved.getUsername(), saved.getEmail());
}
再次执行测试
所有测试应变为 绿色（通过）。

Step 4：重构（消除异味）
在测试全部通过的前提下，优化代码结构：

消除重复逻辑（提取私有方法）。

改善命名、变量作用域。

调整包结构或引入设计模式（如果必要）。

确保重构后所有测试 依然通过（自动验证）。

重构示例
java
// 重构前
if (userRepository.existsByUsername(request.getUsername())) {
    throw new DuplicateUsernameException();
}
if (userRepository.existsByEmail(request.getEmail())) {
    throw new DuplicateEmailException();
}
// 重构后：提取方法 validateUniqueness(request)
Step 5：迭代循环
对 PRD 中的每个用户故事或每个模块，重复 Step 1 → Step 4，直到所有功能实现完毕。

典型迭代顺序：

实体层（Repository 测试）

业务逻辑层（Service 单元测试）

API 层（Controller 集成测试）

端到端场景（@SpringBootTest 全栈测试）

Step 6：最终交付物
完成全部迭代后，输出：

源代码（src/main/java）

测试代码（src/test/java）

测试报告（target/surefire-reports 或 build/reports/tests）

覆盖率报告（JaCoCo / Cobertura），要求行覆盖率 ≥ 80%

四、给 AI 的额外指令（嵌入 Prompt）
当你（AI）收到 PRD 和原型后，请按以下模板输出：

markdown
## 1. 分析报告
- 用户故事列表
- 测试用例清单（表格）

## 2. 测试代码（先）
- 单元测试（JUnit + Mockito）
- 集成测试（@SpringBootTest + Testcontainers）

## 3. 实现代码（后）
- 最小功能实现
- 重构版本（可选，注明改动）

## 4. 验证结果
- 测试通过截图 / 日志
- 覆盖率统计
约束：

不允许在测试通过前编写任何业务实现。

每次只生成一个迭代的内容，等待用户确认后继续下一步。

若存在模糊需求（如缺少异常处理规则），主动请求明确。