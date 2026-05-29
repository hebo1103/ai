# 后端技术规范

## 一、技术栈
- JDK版本：1.8
- 框架：Spring Boot 2.x
- 构建工具：Maven
- 数据库：MySQL 8.0+
- ORM框架：MyBatis / MyBatis-Plus
- 缓存：Redis（可选）

## 二、项目结构
```
src/main/java
├── controller      # 控制器层
├── service         # 业务逻辑层
├── mapper          # 数据访问层
├── entity          # 实体类
├── dto             # 数据传输对象
├── vo              # 视图对象
├── config          # 配置类
└── common          # 公共组件（工具类、常量、异常等）
```

## 三、编码规范

### 3.1 RESTful API设计
- 使用HTTP动词：GET（查询）、POST（创建）、PUT（更新）、DELETE（删除）
- URL使用名词复数形式，如 `/api/products`
- 统一响应格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 3.2 异常处理
- 使用全局异常处理器 `@ControllerAdvice`
- 自定义业务异常类
- 统一错误码规范

### 3.3 日志规范
- 使用SLF4J + Logback
- 关键操作记录INFO级别日志
- 异常记录ERROR级别日志

### 3.4 数据库规范
- 表名、字段名使用小写下划线命名
- 必须包含：id（主键）、create_time、update_time
- 逻辑删除使用 is_deleted 字段

## 四、安全规范
- 参数校验：使用 `@Valid` / `@Validated`
- SQL注入防护：使用预编译语句
- XSS防护：输入输出过滤
- 敏感数据加密存储

## 五、性能规范
- 合理使用索引
- 避免N+1查询
- 分页查询必须使用
- 缓存热点数据
