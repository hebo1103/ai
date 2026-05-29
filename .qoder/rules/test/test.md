# 测试规范

## 一、测试原则
- 测试驱动开发（TDD）：先写测试，再写实现
- 测试用例必须覆盖正常流程、异常流程、边界条件
- 测试代码与生产代码同等质量要求

## 二、测试类型

### 2.1 单元测试
- 使用JUnit 5 + Mockito
- 测试单个方法/类的功能
- 隔离外部依赖（数据库、网络等）
- 覆盖率要求：≥ 80%

### 2.2 集成测试
- 使用Spring Boot Test
- 测试多个组件协作
- 使用H2内存数据库或Testcontainers
- 验证API接口正确性

### 2.3 端到端测试（可选）
- 使用Selenium或Cypress
- 模拟真实用户操作
- 覆盖核心业务流程

## 三、测试命名规范

### 3.1 测试类命名
- 格式：`{被测试类名}Test`
- 示例：`ProductServiceTest`

### 3.2 测试方法命名
- 格式：`should{预期行为}When{条件}`
- 示例：`shouldReturnProductListWhenCategoryIsValid`
- 或使用中文：`当分类有效时应返回产品列表`

## 四、测试结构（AAA模式）

```java
@Test
void testMethodName() {
    // Arrange - 准备测试数据和环境
    // Act - 执行被测试的方法
    // Assert - 验证结果是否符合预期
}
```

## 五、Mock规范
- 使用 `@Mock` 创建Mock对象
- 使用 `@InjectMocks` 注入Mock对象
- 使用 `when().thenReturn()` 定义Mock行为
- 使用 `verify()` 验证Mock方法调用

## 六、断言规范
- 使用JUnit 5的 `Assertions` 类
- 常用断言：
  - `assertEquals(expected, actual)`
  - `assertTrue(condition)`
  - `assertNotNull(object)`
  - `assertThrows(Exception.class, () -> {...})`

## 七、测试数据管理
- 使用 `@BeforeEach` 初始化测试数据
- 使用 `@AfterEach` 清理测试数据
- 测试数据应具有代表性（正常值、边界值、异常值）

## 八、测试执行
- 本地执行：`mvn test`
- CI集成：每次提交自动执行测试
- 测试失败必须修复后才能合并代码
