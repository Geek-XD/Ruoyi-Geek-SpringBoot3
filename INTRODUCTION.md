# 若依极客 (RuoYi-Geek) SpringBoot3 企业级快速开发平台

<p align="center">
    <img alt="RuoYi Logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png" width="200">
    <span style="font-size: 24px; margin: 0 20px;">+</span>
    <img alt="Geek Logo" src="./doc/image/logo.png" width="200">
</p>

<h1 align="center">RuoYi-Geek SpringBoot3 企业级开发平台</h1>
<h4 align="center">基于 SpringBoot3 + Vue3 + Java21 的现代化企业级快速开发框架</h4>

<p align="center">
    <img src="https://img.shields.io/badge/SpringBoot-3.5.3-brightgreen.svg" alt="SpringBoot">
    <img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java">
    <img src="https://img.shields.io/badge/Vue-3.x-green.svg" alt="Vue">
    <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
    <img src="https://img.shields.io/badge/Version-3.9.0--G-red.svg" alt="Version">
</p>

## 📖 项目简介

RuoYi-Geek 是基于若依(RuoYi)框架进行深度优化和扩展的企业级快速开发平台。该项目采用最新的 SpringBoot3 + Vue3 + Java21 技术栈，在保持原有若依框架核心特性的基础上，增加了大量实用的业务模块和插件系统，提供了更加现代化、模块化的开发体验。

### 🎯 项目愿景

打造一个功能完善、架构清晰、易于扩展的现代化企业级开发平台，帮助开发者快速构建高质量的企业应用系统。

### ✨ 核心特性

- **🔥 最新技术栈**：采用 SpringBoot3 + Vue3 + Java21，拥抱新特性
- **🎨 模块化设计**：支持模块的快速插拔，按需组装
- **🔐 权限管理**：完善的 RBAC 权限控制体系
- **💰 支付集成**：内置微信支付、支付宝支付、收钱吧等支付方式
- **🔑 第三方认证**：支持微信、邮箱、手机等多种认证方式
- **📁 文件服务**：支持本地、MinIO、阿里云OSS等存储方式
- **🔧 代码生成**：智能代码生成器，支持复杂关联表生成
- **📊 系统监控**：全面的系统监控和性能分析
- **🌐 多数据源**：支持多数据源和分库分表

## 🏗️ 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Vue3 + Element Plus)              │
├─────────────────────────────────────────────────────────────┤
│                    Web层 (Spring MVC)                       │
├─────────────────────────────────────────────────────────────┤
│                   业务层 (Service Layer)                     │
├─────────────────────────────────────────────────────────────┤
│                   持久层 (MyBatis/MyBatis-Plus)             │
├─────────────────────────────────────────────────────────────┤
│                   数据层 (MySQL/PostgreSQL)                  │
└─────────────────────────────────────────────────────────────┘
```

### 模块架构

```
com.ruoyi
├── ruoyi-common           # 通用工具模块
├── ruoyi-framework        # 框架核心模块
├── ruoyi-system           # 系统管理模块
├── ruoyi-admin            # 后台管理模块
├── ruoyi-auth             # 第三方认证模块
│   ├── ruoyi-auth-common      # 认证通用模块
│   ├── ruoyi-oauth-justauth   # JustAuth第三方登录
│   ├── ruoyi-oauth-wx         # 微信小程序认证
│   ├── ruoyi-tfa-phone        # 手机认证
│   └── ruoyi-tfa-email        # 邮箱认证
├── ruoyi-pay              # 支付服务模块
│   ├── ruoyi-pay-common       # 支付通用模块
│   ├── ruoyi-pay-wx           # 微信支付
│   ├── ruoyi-pay-alipay       # 支付宝支付
│   └── ruoyi-pay-sqb          # 收钱吧支付
├── ruoyi-file             # 文件服务模块
│   ├── ruoyi-file-common      # 文件通用模块
│   ├── ruoyi-file-minio       # MinIO文件存储
│   └── ruoyi-file-oss-alibaba # 阿里云OSS存储
├── ruoyi-models           # 业务场景模块
│   ├── ruoyi-online           # 在线开发
│   ├── ruoyi-quartz           # 定时任务
│   ├── ruoyi-generator        # 代码生成
│   ├── ruoyi-form             # 自定义表单
│   ├── ruoyi-flowable         # 工作流引擎
│   └── ruoyi-message          # 消息管理
└── ruoyi-plugins          # 插件系统
    ├── ruoyi-redis            # Redis缓存
    ├── ruoyi-ehcache          # EhCache缓存
    ├── ruoyi-mybatis-plus     # MyBatis-Plus增强
    ├── ruoyi-mybatis-jpa      # MyBatis-JPA简化
    ├── ruoyi-websocket        # WebSocket支持
    ├── ruoyi-rabbitmq         # RabbitMQ消息队列
    ├── ruoyi-netty            # Netty网络框架
    └── ruoyi-atomikos         # 分布式事务
```

## 🚀 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| SpringBoot | 3.5.3 | 基础框架 |
| Spring Security | 6.x | 安全框架 |
| Spring Data JPA | 3.x | 数据访问层 |
| MyBatis | 3.5.16 | 持久层框架 |
| MyBatis-Plus | 3.x | MyBatis增强工具 |
| Druid | 1.2.24 | 数据库连接池 |
| Redis | 7.x | 缓存数据库 |
| JWT | 0.12.6 | 认证令牌 |
| Knife4j | 4.5.0 | API文档 |
| Quartz | 2.x | 定时任务 |
| Flowable | 7.x | 工作流引擎 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Element Plus | 2.x | UI组件库 |
| TypeScript | 5.x | 类型检查 |
| Vite | 5.x | 构建工具 |
| Axios | 1.x | HTTP客户端 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |

### 数据库支持

- MySQL 8.x
- PostgreSQL 15.x
- Oracle 19c
- SQL Server 2019

## 🎯 核心功能

### 基础功能

1. **用户管理**：用户增删改查、角色分配、权限控制
2. **角色管理**：角色权限分配、数据权限控制
3. **菜单管理**：动态菜单配置、按钮权限控制
4. **部门管理**：组织架构管理、数据权限范围
5. **字典管理**：系统字典配置、前端动态渲染
6. **参数管理**：系统参数配置、运行时动态获取
7. **通知公告**：系统消息通知、公告发布
8. **操作日志**：系统操作记录、异常日志追踪

### 高级功能

1. **代码生成**：智能代码生成、支持复杂关联表
2. **定时任务**：在线任务调度、执行日志查看
3. **系统监控**：服务器监控、数据库监控、缓存监控
4. **在线用户**：在线用户监控、强制踢出
5. **文件管理**：多种存储方式、分片上传、权限控制
6. **工作流**：基于Flowable的工作流引擎
7. **表单设计**：在线表单设计、动态表单渲染
8. **消息推送**：站内消息、邮件推送、短信通知

### 业务扩展

1. **支付集成**：微信支付、支付宝支付、收钱吧支付
2. **第三方登录**：微信登录、QQ登录、支付宝登录
3. **手机认证**：短信验证码、手机号登录
4. **邮箱认证**：邮箱验证、邮箱登录
5. **分布式锁**：基于Redis的分布式锁
6. **消息队列**：RabbitMQ消息队列集成
7. **分布式事务**：Atomikos分布式事务支持
8. **网络通信**：Netty网络框架集成

## 🛠️ 快速开始

### 环境要求

- **JDK**：21+
- **Maven**：3.8+
- **MySQL**：8.0+
- **Redis**：7.0+
- **Node.js**：18+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/Geek-XD/Ruoyi-Geek-SpringBoot3.git
cd Ruoyi-Geek-SpringBoot3
```

2. **数据库初始化**
```bash
# 导入数据库脚本
mysql -u root -p < sql/mysql/ry_20250603.sql
```

3. **配置文件修改**
```yaml
# ruoyi-admin/src/main/resources/application-druid.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: password
```

4. **启动后端服务**
```bash
mvn clean install
cd ruoyi-admin
mvn spring-boot:run
```

5. **启动前端服务**
```bash
cd ruoyi-ui
npm install
npm run dev
```

6. **访问系统**
   - 后台管理：http://localhost:8080
   - 接口文档：http://localhost:8080/doc.html
   - 前端页面：http://localhost:80

### 默认账户

- **管理员**：admin / admin123
- **普通用户**：test / admin123

## 📦 模块说明

### 可选模块

以下模块可以根据需要选择性启用或禁用：

#### 业务模块 (ruoyi-models)
- ✅ **在线开发**：在线数据库表设计、代码生成
- ✅ **定时任务**：Quartz定时任务调度
- ✅ **代码生成**：智能代码生成器
- ✅ **自定义表单**：在线表单设计器
- ✅ **工作流**：Flowable工作流引擎
- ✅ **消息管理**：站内消息、推送通知

#### 插件模块 (ruoyi-plugins)
- ✅ **Redis缓存**：Redis分布式缓存
- ✅ **EhCache缓存**：本地缓存支持
- ✅ **MyBatis-Plus**：MyBatis增强工具
- ✅ **MyBatis-JPA**：JPA风格的MyBatis扩展
- ✅ **WebSocket**：实时通信支持
- ✅ **RabbitMQ**：消息队列支持
- ✅ **Netty**：高性能网络框架
- ✅ **Atomikos**：分布式事务支持

#### 认证模块 (ruoyi-auth)
- ✅ **第三方登录**：微信、QQ、支付宝等第三方登录
- ✅ **微信小程序**：小程序授权登录
- ✅ **手机认证**：短信验证码登录
- ✅ **邮箱认证**：邮箱验证登录

#### 支付模块 (ruoyi-pay)
- ✅ **微信支付**：微信支付集成
- ✅ **支付宝支付**：支付宝支付集成
- ✅ **收钱吧支付**：收钱吧支付集成

#### 文件模块 (ruoyi-file)
- ✅ **本地存储**：本地文件系统存储
- ✅ **MinIO存储**：MinIO分布式文件存储
- ✅ **阿里云OSS**：阿里云对象存储

## 🔧 配置说明

### 主要配置文件

1. **application.yml**：主配置文件
2. **application-druid.yml**：数据库配置
3. **application-auth.yml**：认证配置
4. **application-pay.yml**：支付配置
5. **application-file.yml**：文件存储配置
6. **application-plugins.yml**：插件配置
7. **application-model.yml**：业务模块配置

### 关键配置项

```yaml
# 项目配置
ruoyi:
  name: RuoYi-Geek
  version: 3.9.0-G
  fileServer: local  # 文件服务类型：local/minio/oss
  
# 缓存配置
spring:
  cache:
    type: redis  # 缓存类型：redis/jcache
    
# MyBatis配置
createSqlSessionFactory:
  use: mybatis-plus  # mybatis/mybatis-plus
```

## 🎨 界面展示

### 系统管理
- 用户管理界面
- 角色权限分配
- 菜单管理界面
- 部门组织架构

### 功能模块
- 代码生成器
- 定时任务管理
- 系统监控界面
- 在线表单设计

### 业务功能
- 支付管理界面
- 文件管理界面
- 工作流设计
- 消息中心

## 🤝 参与贡献

### 贡献指南

1. **Fork** 本项目
2. **创建** 特性分支 (`git checkout -b feature/AmazingFeature`)
3. **提交** 更改 (`git commit -m 'Add some AmazingFeature'`)
4. **推送** 到分支 (`git push origin feature/AmazingFeature`)
5. **提交** Pull Request

### 代码规范

- 遵循阿里巴巴Java开发规范
- 使用统一的代码格式化配置
- 编写单元测试和集成测试
- 添加必要的注释和文档

### 问题反馈

- 通过 [GitHub Issues](https://github.com/Geek-XD/Ruoyi-Geek-SpringBoot3/issues) 提交问题
- 加入QQ交流群：744785891

## 📄 许可证

本项目基于 [MIT 许可证](LICENSE) 开源发布。

## 🙏 致谢

感谢以下开源项目的贡献：

- [RuoYi](https://github.com/yangzongzhuan/RuoYi) - 原始框架基础
- [Vue](https://github.com/vuejs/vue) - 前端框架
- [Element Plus](https://github.com/element-plus/element-plus) - UI组件库
- [Spring Boot](https://github.com/spring-projects/spring-boot) - 后端框架
- [MyBatis](https://github.com/mybatis/mybatis-3) - 持久层框架

## 📞 联系我们

- **QQ交流群**：744785891
- **邮箱**：geek-xd@example.com
- **GitHub**：https://github.com/Geek-XD/Ruoyi-Geek-SpringBoot3

---

<p align="center">
    <b>⭐ 如果这个项目对您有帮助，请给我们一个星标支持！</b>
</p>