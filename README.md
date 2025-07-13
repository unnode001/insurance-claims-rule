# 旅游延误险自动理赔系统

核心开发框架基于Spring Boot + 规则引擎的旅游延误险自动理赔判断模块，实现智能化理赔决策。

## 项目概述

该系统实现"延误超4小时赔付300元，超8小时赔付600元"等保险条款的自动化理赔判断，支持REST API调用和规则引擎决策。

## 技术栈

- **框架**: Spring Boot 3.1.5
- **规则引擎**: SimpleRuleEngineService (纯Java)
- **数据库**: H2 (内存数据库)
- **持久层**: Spring Data JPA + Hibernate
- **API文档**: Swagger/OpenAPI 3.0
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

## 本地运行

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
