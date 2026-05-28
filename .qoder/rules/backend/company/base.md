# 薯片平台 Java 开发规范（公司版）

> 本规范为薯片平台专属强制规范，所有基于 Java 的后端项目必须严格遵守，与行业规范冲突时以公司规范为准。

## 1. 总体原则

- **唯一技术栈**：后端统一采用 Spring Boot + Spring Cloud 全家桶，不得引入其他非标技术体系（如 Dubbo、Thrift 等）
- **微服务架构**：按业务边界拆分，服务间通过 Feign 调用，**严禁直接使用 HTTP/HTTPS 调用**
- **高可用与扩展性**：所有服务须支持多实例部署，基于 Kubernetes 实现自动伸缩
- **可监控性**：所有应用必须暴露健康检查、指标（Micrometer）和链路追踪（SkyWalking）
- **灰度发布**：代码设计须支持按用户、地域、设备等维度的灰度路由（通过网关或配置中心实现）
- **低耦合高内聚**：服务之间禁止循环依赖，禁止跨领域直接访问数据库
- **编码范式**：按照 DDD（领域驱动设计）模式进行编码，能用设计模式的场景优先使用设计模式

## 2. 技术栈选型

### 核心框架与运行时

| 组件 | 技术选型 | 版本要求 |
|------|----------|----------|
| 应用框架 | Spring Boot | 3.2.x |
| 微服务生态 | Spring Cloud Alibaba | 2022.0.0.0+ |
| API 网关 | Spring Cloud Gateway | 4.1.x |
| JDK | OpenJDK 17 | 17+ |
### 服务治理与配置

| 组件 | 技术选型 | 部署模式 |
|------|----------|----------|
| 配置中心 + 服务注册 | Nacos | 2.2.3+，集群（3节点） |
| 负载均衡 | Spring Cloud LoadBalancer | 内置 |
| 远程调用 | OpenFeign | 集成 SCA |
### 持久层与数据访问

| 组件 | 技术选型 | 版本 |
|------|----------|------|
| ORM | MyBatis Plus | 3.5.5+ |
| 连接池 | Druid | 1.2.20+ |
| 多数据源 | dynamic-datasource | 4.1.0+ |
| 数据库（主） | MySQL 8.0+ / TiDB | 开启 binlog（行格式） |
| 数据库（从） | MySQL 8.0+ | 至少1只读节点 |
### 消息中间件

| 场景 | 首选技术 | 可靠性要求 |
|------|----------|------------|
| 异步通知/日志/审计 | Kafka | acks=all，幂等生产者 |
| 延迟消息/重试队列 | Kafka（定时消息插件） | 死信队列 + 消息审计表 |
| 极低延迟任务分发 | RabbitMQ（保留少数） | confirm + return callback |

> **目标**：业务消息 80% 以上使用 Kafka。

### 缓存与 NoSQL

| 组件 | 技术选型 | 部署形态 |
|------|----------|----------|
| 缓存 | Redis | Cluster 模式（3主3从）+ Caffeine 本地二级缓存 |
| 文档存储 | MongoDB | 副本集 → 分片集群 |
| 搜索引擎 | Elasticsearch | 7.17+ 集群（Canal 实时同步 MySQL） |
| 对象存储 | 兼容 S3 的 OSS | MinIO 或云厂商 |
### 可观测性

| 领域 | 技术选型 | 集成方式 |
|------|----------|----------|
| 日志收集 | GELF + Graylog | Logback 直接输出 GELF |
| 链路追踪 | Apache SkyWalking | 9.7+，Java Agent 无侵入，集成 traceId 到 MDC |
| 指标监控 | Micrometer + Prometheus | /actuator/prometheus 暴露 |
| 可视化 | Grafana | 10.x，预置 JVM、业务、MQ 大盘 |
| 告警 | AlertManager | 对接钉钉/企微/邮件 |
| 健康监控平台 | EagleEye（自研） | 接入 CD、CI 报告及任务，应用监控预警 |
### 分布式基础设施

| 组件 | 技术选型 |
|------|----------|
| 分布式 ID | Leaf（美团）或雪花算法（workerId 从 Nacos 获取） |
| 分布式事务 | Seata AT（仅跨库强一致场景），多数场景用本地消息表 + 定时补偿 |
| 限流/熔断 | Sentinel（网关层 + 服务层） |
| 调度任务 | XXL-JOB（支持分片、失败重试） |
### 构建与部署

| 工具 | 技术选型 |
|------|----------|
| 构建工具 | Maven 3.9+（多模块聚合） |
| 代码质量 | Spotless + JaCoCo（覆盖率≥70%） |
| 镜像构建 | Jib / Buildpacks |
| 容器编排 | Kubernetes 1.24+ |
| CI/CD | GitLab CI / Jenkins（门禁：编译、单测、集成测试、镜像扫描） |
### 文档与协作

| 工具 | 技术选型 |
|------|----------|
| API 文档 | OpenAPI 3 + Knife4j，导出 JSON 自动同步 YApi |
| 接口管理 | YApi（唯一 Mock & 测试平台） |
| 数据库文档 | Schema Spy / DB Doc |
## 3. 项目结构规范

### 3.1 模块划分（Maven/Gradle 多模块）

```
shuping-<服务名>/
├── shuping-<服务名>-api        # Feign 接口、DTO、枚举定义
├── shuping-<服务名>-service    # 业务实现、领域逻辑
├── shuping-<服务名>-web        # Controller、网关内外接口实现
└── shuping-<服务名>-starter    # 启动类、配置文件
```
### 3.2 包结构（按业务领域）

```
com.shuping.<服务名>
├── controller        # REST 控制器（区分内网/外网）
├── service           # 业务逻辑接口及实现
├── dao/mapper        # 数据访问层（MyBatis-Plus）
├── entity/domain     # 实体/领域对象
├── dto               # 数据传输对象（入参、出参）
├── feign             # Feign 客户端定义
├── config            # 配置类
├── constant          # 常量、枚举
├── util              # 工具类
└── aspect            # 切面（埋点、日志）
```
## 4. 微服务开发规范

### 4.1 服务定义与注册

- 所有服务必须注册到 Nacos（服务注册与配置中心）
- 服务名采用小写，格式 `shuping-<业务域>`，例如 `shuping-order`
- 开发、测试、生产环境使用不同 Nacos 命名空间

### 4.2 Feign 调用规范

- 内部调用使用 Feign，接口定义在 `*-api` 模块
- **超时配置**：连接超时 ≤1000ms，读取超时 ≤3000ms。熔断降级使用 Sentinel
- Feign 接口必须实现 `fallback` 或 `fallbackFactory`，并记录日志
- 通过 Feign 拦截器自动透传 TraceId、用户信息

### 4.3 网关隔离

- **对外接口**（面向 APP/PC/WAP）：统一走企业网关，Controller 类标注 `@ExternalApi`
- **对内接口**（服务间调用）：不暴露给网关，Controller 类标注 `@InternalApi`，路径以 `/internal/` 开头
- 两种接口必须分属不同包或不同 Controller 类，**严禁混合**

### 4.4 基础服务访问

- 基础服务（用户中心、消息中心、数据字典等）**不能直接暴露给终端**，必须由上层业务应用服务封装后调用
- 业务应用服务通过 Feign 调用基础服务，基础服务本身不应包含业务逻辑

## 5. 数据访问规范

### 5.1 数据库设计与约定

- **SQL**：MySQL 8.0+ 或 TiDB（分库分表场景）。表名 `t_` 前缀，字段名 `f_` 前缀（须统一）
- **NoSQL**：Redis（缓存）、MongoDB（文档）、Elasticsearch（搜索）
- **禁止跨库查询**：一个服务只能访问自己的数据库（或同库内对应表）。跨域数据通过 API 获取
- 表必须包含 `f_create_time`、`f_update_time`、`f_is_deleted`（逻辑删除）

### 5.2 查询规范

- **默认优先使用 Redis + ES**：列表、搜索、聚合查询走 ES；单对象缓存走 Redis
- **直查数据库仅限**：通过主键/唯一键的简单查询，且必须添加缓存
- **分页查询**：使用 PageHelper（MySQL）或 ES 分页，限制最大偏移量 10000
- **禁止 `SELECT *`**，必须明确列出所需字段

### 5.3 事务规范

- 使用 `@Transactional(rollbackFor = Exception.class)`
- 事务边界尽量短，**避免远程调用、缓存操作在事务内**
- 分布式事务（如订单→库存）：采用最终一致性 + 本地消息表，或 Seata AT（需评估性能）

### 5.4 缓存规范

- **Key 命名**：`shuping:业务域:实体:ID[:子场景]`，例 `shuping:user:user:1001`
- **过期时间**：一般 5~30 分钟，热数据可延长至 1 小时
- **更新策略**：先更新数据库，再删除/更新缓存（Cache-Aside 模式）
- **防穿透/雪崩**：空值缓存（短时）、布隆过滤器、随机过期时间

## 6. 日志与链路追踪规范

- **日志框架**：SLF4J + Logback，禁止自行引入 Log4j2
- 日志必须包含 TraceId（MDC 自动注入），格式 `[traceId=%X{traceId}]`
- **禁止输出用户敏感信息**（密码、身份证、手机号需脱敏）
- 异常日志必须记录堆栈：`log.error("描述", e);`
- 所有服务必须接入 SkyWalking（Java agent），Feign、MySQL、Redis、MQ 自动埋点。自定义埋点使用 `@Trace` 或 `TraceContext`

## 7. 异常处理规范

### 7.1 异常分类

| 类型 | 说明 |
|------|------|
| BizException | 业务异常（参数错误、状态不允许、数据不存在） |
| SysException | 系统异常（远程调用失败、数据库连接失败） |
| UnauthorizedException | 认证/授权失败 |
### 7.2 全局异常处理

- 使用 `@RestControllerAdvice` 统一处理，返回标准错误结构：

```json
{
  "code": "BUS_1001",
  "message": "用户不存在",
  "traceId": "a1b2c3d4",
  "timestamp": 1700000000000
}
```

- 所有 Controller 方法必须显式 throws 或内部捕获，**不允许吞异常**
- 前端调用的接口必须对每个可能的异常做兜底处理（返回空数据或默认 UI）

## 8. 接口设计规范

### 8.1 RESTful 风格

- URL 使用名词复数，如 `/users`、`/orders/{id}`
- **方法**：GET（查）、POST（增/复杂查询）、PUT（全改）、PATCH（部分改）、DELETE
- **分页参数**：page、size，排序使用 sort

### 8.2 参数校验

- 使用 `javax.validation` 注解：`@NotNull`、`@NotBlank`、`@Min`、`@Max`、`@Pattern`
- 自定义业务校验放在 Service 层，不放在 Controller

### 8.3 DTO 规范

- **请求 DTO**：`XxxRequest`，必须包含 `@ApiModelProperty`
- **响应 DTO**：`XxxResponse` 或 `XxxVO`，**禁止直接暴露 Entity**
- **分页响应**：统一使用 `PageResponse<T>`，包含 total、page、size、records
- **接口数量限制**：一个页面功能调用的接口数不超过 3 个，最多 5 个

### 8.4 版本管理

- URL 中体现版本：`/v1/users`
- **兼容性**：只增加字段，不删除或修改已有字段类型

## 9. 并发与异步规范

- **禁止使用** `Executors.newFixedThreadPool` 等无界队列方式，必须使用 `ThreadPoolExecutor` 自定义参数
- 使用 `@Async` 时，线程池必须显式配置
- 高并发场景下对共享资源操作需使用分布式锁（Redis Redisson 或 ZooKeeper）
- **谨慎使用** `synchronized` 或 `ReentrantLock`，仅适用于单机场景

## 10. 安全规范

### 10.1 鉴权与免密

- 对外接口默认需要鉴权（通过企业网关校验 Token）
- 支持免密访问的接口必须在网关配置白名单，并做频次限制

### 10.2 数据安全

- 用户密码、支付密钥等使用 BCrypt 或 AES 加密存储
- 日志、接口返回、MQ 消息中**禁止输出敏感信息明文**
- SQL 防注入：使用预编译（MyBatis `#{}`），禁止拼接 SQL 字符串

### 10.3 接口防刷

- 对外写操作（登录、注册、下单）必须增加验证码或限流（令牌桶/漏桶）
- 网关层使用 Sentinel 做 QPS 限流，业务层可做用户级限流

## 11. 测试规范

- **单元测试覆盖率**：核心模块 ≥80%，整体 ≥70%（JaCoCo 检测）
- 使用 JUnit 5 + Mockito，测试类命名 `XxxTest`
- 每个 Feign 接口必须提供 Mock 实现（fallback）
- 关键链路（如订单创建→支付回调）必须有集成测试
- 服务间 Feign 接口建议使用 Spring Cloud Contract 做契约测试

## 12. 部署与配置规范

### 12.1 环境配置

- 配置文件：`application-{profile}.yml`（profile: dev, test, pre, prod）
- 所有**敏感配置**（数据库密码、Redis 密码、AK/SK）必须使用 Nacos 配置中心或 K8s Secret，**禁止写在代码或 properties 中**

### 12.2 灰度发布

- 代码中预留灰度逻辑点，例如通过 `@GrayConditional` 注解或配置开关
- 灰度规则（用户 ID、IP、设备号）由网关或配置中心下发，业务服务只负责执行

### 12.3 健康与监控

- 暴露 `/actuator/health`、`/actuator/prometheus`
- 所有服务必须注册到 EagleEye 健康监控平台，并提供关键业务指标（如订单量、错误率）

---

## 附则

- 本规范自发布之日起生效，所有薯片平台 Java 项目必须遵守
- **违反规范的代码，Code Review 不予通过，CI 构建失败**
- 规范每半年修订一次，由架构中心负责解释和更新