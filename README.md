# 旅游延误险自动理赔系统

基于Spring Boot + 规则引擎的旅游延误险自动理赔判断模块，实现智能化理赔决策。

## 项目概述

该系统实现"延误超4小时赔付300元，超8小时赔付600元"等保险条款的自动化理赔判断，支持REST API调用和规则引擎决策。

## 技术栈

- **框架**: Spring Boot 3.1.5
- **规则引擎**: SimpleRuleEngineService (纯Java实现)
- **数据库**: H2 (内存数据库)
- **持久层**: Spring Data JPA + Hibernate
- **API文档**: Swagger/OpenAPI 3.0
- **构建工具**: Maven
- **Java版本**: 17

## 核心功能

### 理赔规则

| 延误时长 | 赔付金额 | 说明 |
|---------|---------|------|
| < 4小时 | 0元 | 不符合理赔条件 |
| 4-8小时 | 300元 | 标准延误赔付 |
| > 8小时 | 600元 | 严重延误赔付 |

### 主要功能

1. **理赔申请提交** - 自动计算延误时长并执行规则判断
2. **规则引擎决策** - 基于SimpleRuleEngineService规则自动判断理赔结果
3. **风险评估** - 自动评估理赔风险级别
4. **申请状态管理** - 支持APPROVED/REJECTED状态管理
5. **实时监控** - 提供系统健康检查和API文档

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

### 启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

### 访问地址

- **应用地址**: <http://localhost:8080>
- **API文档**: <http://localhost:8080/api/swagger-ui.html>
- **健康检查**: <http://localhost:8080/api/actuator/health>
- **H2控制台**: <http://localhost:8080/api/h2-console>

## 项目结构

```
src/
├── main/
│   ├── java/com/insurance/claims/
│   │   ├── InsuranceClaimsApplication.java     # 主启动类
│   │   ├── config/
│   │   │   └── DroolsConfig.java              # Drools配置
│   │   ├── controller/
│   │   │   ├── TravelDelayClaimController.java # REST API控制器
│   │   │   └── GlobalExceptionHandler.java    # 全局异常处理
│   │   ├── dto/
│   │   │   ├── TravelDelayClaimRequest.java   # 请求DTO
│   │   │   └── ClaimResponse.java             # 响应DTO
│   │   ├── model/
│   │   │   ├── TravelDelayClaim.java          # 理赔申请实体
│   │   │   └── ClaimDecision.java             # 决策结果模型
│   │   ├── repository/
│   │   │   └── TravelDelayClaimRepository.java # 数据访问层
│   │   └── service/
│   │       ├── RuleEngineService.java         # 规则引擎服务
│   │       └── TravelDelayClaimService.java   # 业务服务
│   └── resources/
│       ├── rules/
│       │   └── travel-delay-claims.drl        # Drools规则文件
│       └── application.yml                    # 应用配置
└── test/
    └── java/com/insurance/claims/
        └── service/
            └── RuleEngineServiceTest.java     # 单元测试
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+

### 2. 构建项目

```bash
mvn clean compile
```

### 3. 运行测试

```bash
mvn test
```

### 4. 启动应用

```bash
mvn spring-boot:run
```

### 5. 访问应用

- 应用地址: <http://localhost:8080/api>
- API文档: <http://localhost:8080/swagger-ui.html>
- H2控制台: <http://localhost:8080/api/h2-console>

## API接口

### 提交理赔申请

```http
POST /api/claims/submit
Content-Type: application/json

{
  "policyholderName": "张三",
  "policyNumber": "POL123456789",
  "flightNumber": "CZ3251",
  "scheduledDeparture": "2025-06-26 08:30:00",
  "actualDeparture": "2025-06-26 12:30:00",
  "delayReason": "天气原因",
  "claimedAmount": 300.00
}
```

### 查询申请详情

```http
GET /api/claims/{claimNumber}
```

### 人工审核

```http
POST /api/claims/{claimNumber}/review
Content-Type: application/json

{
  "approved": true,
  "notes": "经核实，延误确实由天气原因造成，批准理赔"
}
```

## 规则引擎说明

### 规则文件位置

`src/main/resources/rules/travel-delay-claims.drl`

### 主要规则

1. **延误4-8小时理赔300元** (优先级: 100)
2. **延误8小时以上理赔600元** (优先级: 110)
3. **延误不足4小时拒赔** (优先级: 90)
4. **异常延误需人工审核** (优先级: 120)
5. **申请金额异常检查** (优先级: 80)
6. **关键信息完整性检查** (优先级: 130)
7. **负延误时长检查** (优先级: 125)

### 规则执行顺序

规则按照salience（优先级）从高到低执行，确保关键检查优先进行。

## 数据库设计

### 主要表结构

- `travel_delay_claim`: 理赔申请主表
  - 基本信息：申请单号、投保人、保单号、航班号
  - 时间信息：计划起飞时间、实际起飞时间、延误时长
  - 理赔信息：申请金额、计算金额、理赔状态
  - 审核信息：审核结果、审核备注、处理时间

## 扩展说明

### 添加新规则

1. 在 `travel-delay-claims.drl` 文件中添加新规则
2. 重启应用，规则引擎会自动加载新规则

### 自定义业务逻辑

1. 在 `service` 包中添加新的服务类
2. 在 `controller` 包中添加对应的API接口

### 数据库扩展

1. 修改实体类添加新字段
2. 更新Repository接口添加新查询方法

## 监控和维护

### 系统健康检查

```http
GET /api/claims/health
```

### 日志监控

- 应用日志级别: DEBUG
- 规则引擎日志级别: INFO
- 数据库SQL日志: 开启（开发环境）

### 性能优化建议

1. 生产环境使用外部数据库（MySQL/PostgreSQL）
2. 启用数据库连接池优化
3. 添加缓存层减少规则引擎重复计算
4. 异步处理大批量理赔申请

## 版本信息

- **当前版本**: 1.0.0
- **创建日期**: 2025-06-26
- **最后更新**: 2025-06-26

## 联系方式

如有问题或建议，请联系开发团队。
