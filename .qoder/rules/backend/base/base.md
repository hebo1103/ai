# Java开发准则（强制性规范）

本准则适用于所有基于Java语言的后端项目，由技术委员会强制执行。未满足以下任何一条的代码不得合并、不得上线。

---

## 1. 命名与代码风格

### 1.1 命名规则

- **包名**：全小写，单词连续不使用下划线，格式为 `com.公司名.项目名.模块名`。
- **类名**：UpperCamelCase，如 `UserService`。测试类以 `Test` 结尾，抽象类以 `Abstract` 或 `Base` 开头。
- **接口名**：UpperCamelCase，建议不加 `I` 前缀，实现类以 `Impl` 结尾。
- **方法名、变量名**：lowerCamelCase，如 `getUserById`。
- **常量名**：全大写，下划线分隔，如 `MAX_RETRY_COUNT`（static final 字段）。
- **枚举类**：类名 UpperCamelCase，枚举值全大写。

### 1.2 格式规则

- **缩进**：4个空格，禁止使用Tab。
- **单行最大长度**：120字符，超出必须换行，换行缩进8空格。
- **大括号**：左大括号不换行，右大括号独立一行（if/for/while 即使只有一条语句也必须使用大括号）。
- **空行**：方法之间、逻辑段落之间、import 组之间（按 java、javax、org、com、静态导入顺序分组）。

---

## 2. 注释与文档

### 2.1 必须包含Javadoc的元素

- 所有 `public` 类、接口、枚举。
- 所有 `public` 或 `protected` 方法（含构造方法）。
- 所有常量（static final）如有业务含义。
- Javadoc 必须包含：
  - `@param`（每个参数）
  - `@return`（若非void）
  - `@throws`（每个声明的异常并说明触发条件）

### 2.2 注释规范

- 复杂业务逻辑、算法、设计决策必须添加行内注释或块注释。
- 禁止使用 `// TODO` 而不关联任务管理系统（必须附带Jira编号或Git Issue ID）。`// FIXME` 必须明确责任人。
- 禁止注释掉的代码，一旦发现立即删除。

---

## 3. 异常处理

### 3.1 强制规则

- **不得吞没异常**：catch 块内必须记录日志或重新抛出更具体的异常，禁止空 catch。
- **不得使用 `e.printStackTrace()`**：必须使用日志框架（如SLF4J）记录，格式 `log.error("操作失败, param={}", param, e)`。
- **finally 块中不得有 return**：会导致异常被抑制。
- **异常链不可断裂**：重新抛出时需传入原始异常（`new BusinessException("msg", e)`）。
- **业务异常**：必须自定义受检或非受检异常，继承 `RuntimeException` 并区分错误码。

### 3.2 资源释放

- 所有实现了 `AutoCloseable` 的资源（InputStream、Connection、Stream 等）必须使用 `try-with-resources` 语句块，禁止手动 finally 调用 `close()`（除非确有必要）。

---

## 4. 日志规范

### 4.1 强制要求

- 所有线上项目必须使用 SLF4J + Logback/Log4j2，禁止 `System.out/System.err`。
- **日志级别**：
  - **ERROR**：系统错误、无法自动恢复的异常。
  - **WARN**：可自动降级或重试的异常、配置错误。
  - **INFO**：关键业务流程节点（如订单创建、支付回调）、外部接口调用（含请求/响应摘要）。
  - **DEBUG**：调试信息，生产环境默认关闭。
- **占位符**：必须使用 `{}`，禁止字符串拼接（`log.info("user:"+id)` → 禁止）。
- **异常日志**：必须包含完整堆栈，使用 `log.error("msg", e)`，不要只打印 `e.getMessage()`。

### 4.2 敏感信息

- 日志中禁止打印密码、Token、身份证号、手机号（如需打印需脱敏：138****1234）。
- 禁止打印超长JSON（超过500字符）或二进制数据。

---

## 5. 集合与数组

### 5.1 初始化与遍历

- 禁止使用 `Arrays.asList()` 返回的集合执行 add/remove 操作（应使用 `new ArrayList<>()` 包装）。
- 禁止在 foreach 循环中对集合进行 remove/add 操作，必须使用 `Iterator` 或 `removeIf`。
- 预估算集合大小，避免频繁扩容：`new ArrayList<>(expectedSize)`，`new HashMap<>(initialCapacity)`。

### 5.2 空值处理

- 方法返回集合或数组时，禁止返回 `null`，必须返回空集合（`Collections.emptyList()` / `new ArrayList<>(0)`）。
- 从集合中获取元素后必须判空，避免 `NullPointerException`。

---

## 6. 并发与线程安全

### 6.1 线程安全类

- 多线程环境共享变量时，优先使用 `java.util.concurrent` 包下的原子类、ConcurrentHashMap、CopyOnWriteArrayList 等。
- 禁止使用 `HashMap`、`ArrayList`、`SimpleDateFormat`（改用 `ThreadLocal<SimpleDateFormat>` 或 `DateTimeFormatter`）。
- 双重检查锁（DCL）中 `instance` 必须声明为 `volatile`。

### 6.2 线程池

- 禁止使用 `Executors.newFixedThreadPool` / `newCachedThreadPool` 等无界队列或最大线程数无限的方式，必须使用 `ThreadPoolExecutor` 并明确参数（coreSize、maxSize、queueSize、rejectionPolicy）。
- 所有线程池必须指定有意义的 `ThreadFactory` 和 `UncaughtExceptionHandler`。
- 提交的任务必须考虑 `RejectedExecutionException`，并进行重试或补偿处理。

### 6.3 锁

- 尽量不要使用 `synchronized` 在方法上，优先使用 `ReentrantLock` + try-finally 确保释放。
- 避免在持有锁时调用外部方法（可能死锁或性能低下）。

---

## 7. 面向对象与设计

### 7.1 类设计

- **继承限制**：继承层次不超过3层，优先使用组合而非继承。
- **方法长度**：单一方法不超过50行（不含注释与空行），若超过必须拆分。
- **参数数量**：方法参数不超过5个，超过时必须封装为对象。
- **工具类**：必须有私有构造器，且声明为 `final` 防止继承。

### 7.2 依赖注入

- 使用Spring框架时，字段注入（`@Autowired` 在字段上）禁止使用，必须使用构造器注入（或至少Setter注入并标记 `@Autowired`），以便于单元测试和避免循环依赖。
- 循环依赖必须通过重构解决，禁止使用 `@Lazy` 临时绕过。

### 7.3 常量与魔法值

- 禁止任何魔法数字/字符串（除数组索引、循环中的 0/1 及显式注释的平凡数值）。所有业务含义数值必须定义为 `static final` 常量或枚举。
- 同一类中的常量使用类内部常量，跨类使用常量接口（不推荐）或常量类（如 `Constants` 按模块拆分）。

---

## 8. 数据库与SQL

### 8.1 SQL编写

- 所有SQL必须使用参数化查询（`PreparedStatement` 或 MyBatis `#{}`），禁止拼接字符串（防止SQL注入）。
- 查询语句中禁止 `select *`，必须明确列出字段名。
- 批量操作（如批量插入、更新）必须使用JDBC batch（`addBatch` / `executeBatch`）。

### 8.2 ORM框架（MyBatis/JPA）

- MyBatis Mapper XML 中 `resultMap` 和 `parameterType` 必须使用实体类全限定名，禁止使用别名（易造成混乱）。
- 对于 `update` 操作，必须判断返回值并处理异常情况。
- 禁止在循环中执行SQL（n+1问题），应使用 `in` 查询或批量操作。

### 8.3 事务管理

- 事务边界应在Service层（`@Transactional`），不要打在Controller或Repository层。
- 必须明确指定 `rollbackFor = Exception.class`（默认只对 `RuntimeException` 回滚）。
- 避免在事务内进行远程调用、大量内存计算，及时缩小事务范围。

---

## 9. 性能与内存

### 9.1 对象创建与GC

- 禁止在循环内创建 `StringBuilder` / `StringBuffer` 而不复用（应提前在循环外创建）。
- 避免频繁装箱拆箱（如 `Integer.parseInt` 与 `Integer.valueOf` 的选择）。
- 大对象（如byte[] > 1MB）尽量复用，避免直接分配。

### 9.2 缓存

- 对于重复计算或数据库查询结果（如配置表、字典），必须使用本地缓存（Caffeine/Guava）或分布式缓存（Redis），并设置合理的过期时间。
- 所有缓存必须有失效策略，禁止无限增长（可能OOM）。

### 9.3 接口超时与熔断

- 所有RPC/HTTP调用必须设置连接超时（≤1s）和读超时（根据业务场景，通常≤3s），并配置重试（幂等接口）。
- 关键外部依赖必须配置熔断降级（使用Sentinel/Hystrix/Resilience4j）。

---

## 10. 测试与质量

### 10.1 单元测试

- 每个新功能或修改的核心逻辑必须编写单元测试（JUnit 5 + Mockito）。
- 单元测试覆盖率要求：核心模块 ≥ 80%，整体项目 ≥ 70%（由Jacoco检测）。
- 单测方法名采用 `should_expectedBehavior_when_condition` 格式，每个测试只验证一个行为。
- 禁止在单测中依赖数据库、网络、文件系统（应使用Mock或内存数据库如H2）。

### 10.2 集成测试

- 涉及数据库、消息队列、外部接口的功能必须编写集成测试，使用 `@SpringBootTest` 并清理上下文。
- 测试数据库使用独立库或事务回滚（`@Transactional`）。

### 10.3 静态代码检查

- 项目必须集成SpotBugs（或FindSecBugs）、Checkstyle、PMD，在CI流程中设置门禁（任何违规导致构建失败）。
- 所有 `SuppressWarnings` 必须注释原因，且范围尽可能缩小。

---

## 11. 安全规范

### 11.1 输入验证

- 所有来自外部的输入（HTTP参数、请求体、消息队列、文件上传）必须进行验证，使用 `@Valid` + Hibernate Validator或自定义校验。
- 白名单校验：用户ID、角色、操作类型等必须枚举或从会话获取，禁止信任前端传参。

### 11.2 密码与敏感数据

- 密码必须加密存储（BCrypt/SCrypt/Argon2，禁止MD5、SHA1）。
- 敏感数据（身份证、银行卡）在数据库中必须加密（AES），密钥托管在KMS或配置中心。
- 禁止在任何代码仓库中存储明文密码、密钥、Token（使用配置中心或环境变量）。

### 11.3 Web安全

- 防止XSS：输出到页面时对HTML/JS特殊字符转义（使用CSP或框架过滤器）。
- 防止CSRF：POST/PUT/DELETE请求必须携带CSRF Token。
- SQL注入防护已由8.1覆盖。

---

## 12. 依赖与构建

### 12.1 依赖管理

- 所有依赖必须明确版本号（Maven/Gradle），禁止使用范围依赖（如 `1.+`）。
- 禁止引入重复依赖、无用依赖（执行 `mvn dependency:analyze` 检查）。
- 关键依赖（Spring、Netty、Jackson等）必须升级到无已知CVE的版本（每月扫描一次）。

### 12.2 构建

- 构建工具使用Maven 3.6+或Gradle 7+，CI必须执行 `clean install` 并运行全部测试。
- 制品版本号采用 `主版本.次版本.修订号-里程碑`（如 `1.0.0-SNAPSHOT`），正式版不带 `-SNAPSHOT`。

---

## 13. 文档与交付

### 13.1 必需文档

- 每个服务必须有 `README.md`：包含项目简介、构建运行方式、主要配置项、API入口、部署依赖。
- 对外API必须有OpenAPI（Swagger）文档，且通过注解自动生成，与实际代码保持一致。

### 13.2 代码审查

- 任何代码变更（包括修复）必须经过至少1位同级或高级工程师的Code Review，并由作者回复所有评论。
- Review的检查清单包含本准则的所有条目。

---

## 附：违规处理与自动化

以上所有规则配置到CI流水线中，使用：

- **Checkstyle**：检查命名、格式、Javadoc。
- **SpotBugs**：检查潜在Bug、空指针、资源泄漏。
- **SonarQube**：综合质量门禁（覆盖率、复杂度、安全热点）。
- **OWASP Dependency Check**：检查依赖漏洞。

**任何一条规则被检出，CI构建失败，不允许合并至主分支。**