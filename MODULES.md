# 模块功能详细说明

## 核心模块

### ruoyi-common (通用工具模块)

**功能概述**：提供整个系统的通用工具类和基础功能。

**主要包结构**：
```
├── annotation/      # 自定义注解
├── config/         # 全局配置
├── constant/       # 通用常量
├── core/           # 核心控制
├── enums/          # 通用枚举
├── exception/      # 通用异常
├── filter/         # 过滤器处理
└── utils/          # 通用工具类
```

**核心功能**：
- 🔧 **工具类集合**：字符串、日期、文件、HTTP等常用工具
- 🎯 **自定义注解**：权限验证、数据脱敏、日志记录等注解
- 🚀 **异常处理**：统一异常处理机制
- 📊 **响应封装**：标准化API响应格式
- 🔐 **安全工具**：加密解密、签名验证等

**使用示例**：
```java
// 字符串工具
String result = StringUtils.isEmpty(str) ? "default" : str;

// 日期工具
String dateStr = DateUtils.parseDateToStr("yyyy-MM-dd", new Date());

// 响应封装
return AjaxResult.success("操作成功", data);
```

### ruoyi-framework (框架核心模块)

**功能概述**：系统的核心框架，提供基础功能支撑。

**主要包结构**：
```
├── aspectj/        # AOP切面实现
├── config/         # 系统配置
├── datasource/     # 数据源配置
├── interceptor/    # 拦截器
├── manager/        # 异步任务管理
├── security/       # 安全框架
└── web/           # Web相关配置
```

**核心功能**：
- 🔐 **Spring Security集成**：基于JWT的认证授权
- 🔄 **多数据源支持**：动态数据源切换
- 📝 **操作日志记录**：AOP自动记录操作日志
- 🚦 **接口限流**：基于Redis的接口访问限流
- 🎯 **数据权限**：自动注入数据权限条件
- 📊 **接口监控**：API调用统计和性能监控

**配置示例**：
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

### ruoyi-system (系统管理模块)

**功能概述**：提供系统基础管理功能。

**核心功能**：
- 👥 **用户管理**：用户增删改查、状态管理、密码重置
- 🏢 **部门管理**：组织架构管理、树形结构展示
- 🎭 **角色管理**：角色权限分配、数据权限控制
- 📋 **菜单管理**：动态菜单配置、权限控制
- 📚 **字典管理**：系统字典配置、动态加载
- ⚙️ **参数管理**：系统参数配置、实时更新
- 📢 **通知公告**：系统通知发布、状态管理
- 📊 **日志管理**：登录日志、操作日志查询

**API示例**：
```java
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }
}
```

## 认证模块 (ruoyi-auth)

### ruoyi-auth-common (认证通用模块)

**功能概述**：提供第三方认证的通用基础功能。

**核心功能**：
- 🔑 **认证抽象接口**：统一的认证接口定义
- 🎯 **认证策略模式**：支持多种认证方式
- 🔄 **认证结果标准化**：统一的认证结果格式
- 🛡️ **安全防护**：防止认证攻击和异常处理

### ruoyi-oauth-justauth (第三方登录模块)

**功能概述**：基于JustAuth的第三方登录集成。

**支持平台**：
- 🎨 **微信**：微信开放平台登录
- 🔍 **QQ**：QQ互联登录
- 💰 **支付宝**：支付宝登录
- 🐙 **GitHub**：GitHub OAuth登录
- 🎯 **钉钉**：钉钉扫码登录

**配置示例**：
```yaml
justauth:
  enabled: true
  configs:
    WECHAT_OPEN:
      client-id: your-client-id
      client-secret: your-client-secret
      redirect-uri: http://localhost:8080/oauth/callback/wechat
```

### ruoyi-oauth-wx (微信小程序认证)

**功能概述**：微信小程序登录授权。

**核心功能**：
- 📱 **小程序登录**：code2session获取用户信息
- 🔐 **授权验证**：微信授权验证
- 👤 **用户信息获取**：获取微信用户基本信息
- 📞 **手机号获取**：获取用户手机号码

### ruoyi-tfa-phone (手机认证模块)

**功能概述**：基于手机号的短信验证码认证。

**核心功能**：
- 📞 **短信发送**：集成多家短信服务商
- 🔢 **验证码生成**：随机验证码生成
- ⏰ **验证码验证**：验证码有效期控制
- 🛡️ **防刷机制**：防止验证码恶意刷取

**支持服务商**：
- 阿里云短信
- 腾讯云短信
- 华为云短信

### ruoyi-tfa-email (邮箱认证模块)

**功能概述**：基于邮箱的邮件验证码认证。

**核心功能**：
- 📧 **邮件发送**：HTML邮件发送
- 🎨 **邮件模板**：支持邮件模板
- 🔐 **验证码验证**：邮箱验证码验证
- 📊 **发送记录**：邮件发送记录追踪

## 支付模块 (ruoyi-pay)

### ruoyi-pay-common (支付通用模块)

**功能概述**：提供支付相关的通用功能。

**核心功能**：
- 💳 **支付抽象接口**：统一的支付接口定义
- 🔄 **支付状态管理**：支付订单状态流转
- 📊 **支付记录**：支付流水记录
- 🔔 **支付通知**：支付结果通知处理

### ruoyi-pay-wx (微信支付模块)

**功能概述**：微信支付集成。

**支持功能**：
- 💰 **JSAPI支付**：微信内H5支付
- 📱 **扫码支付**：二维码支付
- 🏪 **APP支付**：移动APP支付
- 💻 **H5支付**：移动网页支付
- 🔄 **退款**：支付退款功能

**使用示例**：
```java
@Autowired
private WxPayService wxPayService;

// 创建支付订单
PayOrder order = PayOrder.builder()
    .orderNo("ORDER123456")
    .amount(BigDecimal.valueOf(100))
    .subject("商品购买")
    .build();

// 发起支付
PayResult result = wxPayService.pay(order);
```

### ruoyi-pay-alipay (支付宝支付模块)

**功能概述**：支付宝支付集成。

**支持功能**：
- 💳 **当面付**：扫码支付、刷卡支付
- 📱 **APP支付**：移动APP支付
- 💻 **网页支付**：PC网页支付
- 📞 **手机网站支付**：移动网页支付
- 🔄 **退款**：支付退款功能

### ruoyi-pay-sqb (收钱吧支付模块)

**功能概述**：收钱吧支付集成。

**支持功能**：
- 🏪 **聚合支付**：支持多种支付方式
- 📱 **扫码支付**：二维码收款
- 💳 **刷卡支付**：银行卡支付
- 📊 **对账功能**：交易对账
- 🔄 **退款**：支付退款功能

## 文件模块 (ruoyi-file)

### ruoyi-file-common (文件通用模块)

**功能概述**：提供文件存储的通用功能。

**核心功能**：
- 📁 **文件上传**：多文件上传支持
- 🗂️ **文件管理**：文件增删改查
- 🔐 **权限控制**：文件访问权限
- 📊 **文件统计**：文件大小、类型统计
- 🔄 **分片上传**：大文件分片上传

**存储策略**：
```java
public interface StorageService {
    String upload(MultipartFile file) throws Exception;
    InputStream download(String filePath) throws Exception;
    boolean delete(String filePath) throws Exception;
    String generateUrl(String filePath) throws Exception;
}
```

### ruoyi-file-minio (MinIO文件存储)

**功能概述**：基于MinIO的分布式文件存储。

**核心功能**：
- 🗄️ **对象存储**：分布式对象存储
- 🔄 **分片上传**：大文件分片上传
- 🔐 **预签名URL**：临时访问URL生成
- 📊 **存储桶管理**：存储桶创建和管理
- 🛡️ **访问控制**：文件访问权限控制

**配置示例**：
```yaml
minio:
  enable: true
  client:
    default:
      endpoint: http://localhost:9000
      accessKey: minioadmin
      secretKey: minioadmin
      bucketName: ruoyi
```

### ruoyi-file-oss-alibaba (阿里云OSS存储)

**功能概述**：阿里云对象存储服务集成。

**核心功能**：
- ☁️ **云存储**：阿里云OSS存储
- 🔄 **分片上传**：大文件分片上传
- 🔐 **STS临时凭证**：安全访问凭证
- 🌐 **CDN加速**：内容分发网络
- 📊 **存储统计**：存储用量统计

## 业务模块 (ruoyi-models)

### ruoyi-online (在线开发模块)

**功能概述**：在线数据库表设计和代码生成。

**核心功能**：
- 🗃️ **在线建表**：可视化数据库表设计
- 🔗 **表关系管理**：表间关联关系设计
- 🏗️ **代码生成**：自动生成CRUD代码
- 📋 **表单生成**：自动生成表单页面
- 📊 **列表生成**：自动生成列表页面

### ruoyi-quartz (定时任务模块)

**功能概述**：基于Quartz的定时任务调度。

**核心功能**：
- ⏰ **任务调度**：Cron表达式定时任务
- 📊 **任务监控**：任务执行状态监控
- 📝 **执行日志**：任务执行日志记录
- 🔄 **动态管理**：在线启动、停止、修改任务
- 🎯 **任务分组**：任务分组管理

### ruoyi-generator (代码生成模块)

**功能概述**：智能代码生成器。

**核心功能**：
- 🔍 **数据库逆向**：从数据库表生成代码
- 🎨 **模板引擎**：基于Velocity的模板引擎
- 🏗️ **代码生成**：生成Controller、Service、Mapper等
- 📋 **页面生成**：生成Vue页面代码
- 🔗 **关联表支持**：支持主子表关联生成

### ruoyi-form (自定义表单模块)

**功能概述**：可视化表单设计器。

**核心功能**：
- 🎨 **表单设计**：拖拽式表单设计
- 📝 **组件库**：丰富的表单组件
- 🎯 **表单验证**：表单字段验证规则
- 💾 **表单保存**：表单配置保存
- 📊 **表单渲染**：动态表单渲染

### ruoyi-flowable (工作流模块)

**功能概述**：基于Flowable的工作流引擎。

**核心功能**：
- 🔄 **流程设计**：可视化流程设计器
- 📋 **表单集成**：与表单模块集成
- 👥 **任务分配**：任务分配和审批
- 📊 **流程监控**：流程执行状态监控
- 📝 **流程历史**：流程执行历史记录

### ruoyi-message (消息模块)

**功能概述**：系统消息和通知管理。

**核心功能**：
- 📨 **站内消息**：用户站内消息
- 📧 **邮件通知**：邮件消息推送
- 📞 **短信通知**：短信消息推送
- 🔔 **实时通知**：WebSocket实时通知
- 📊 **消息统计**：消息发送统计

## 插件模块 (ruoyi-plugins)

### ruoyi-redis (Redis缓存插件)

**功能概述**：Redis分布式缓存集成。

**核心功能**：
- 🗄️ **缓存管理**：Redis缓存操作
- 🔒 **分布式锁**：基于Redis的分布式锁
- 🔢 **计数器**：Redis计数器
- 📊 **缓存监控**：缓存命中率监控
- 🔄 **缓存更新**：缓存更新策略

### ruoyi-mybatis-plus (MyBatis-Plus插件)

**功能概述**：MyBatis-Plus增强工具。

**核心功能**：
- 🚀 **CRUD增强**：自动CRUD操作
- 📄 **分页插件**：物理分页支持
- 🔍 **条件构造器**：Lambda条件构造
- 🔄 **代码生成**：MyBatis-Plus代码生成
- 📊 **性能分析**：SQL性能分析

### ruoyi-websocket (WebSocket插件)

**功能概述**：WebSocket实时通信支持。

**核心功能**：
- 🔌 **连接管理**：WebSocket连接管理
- 📨 **消息推送**：实时消息推送
- 👥 **群组管理**：消息群组管理
- 📊 **连接监控**：连接状态监控
- 🔐 **权限控制**：WebSocket访问权限

### ruoyi-rabbitmq (RabbitMQ插件)

**功能概述**：RabbitMQ消息队列集成。

**核心功能**：
- 📮 **消息发送**：消息队列发送
- 📥 **消息消费**：消息队列消费
- 🔄 **消息路由**：消息路由配置
- 📊 **队列监控**：队列状态监控
- 🛡️ **可靠性保证**：消息可靠性保证

### ruoyi-netty (Netty插件)

**功能概述**：Netty高性能网络框架。

**核心功能**：
- 🌐 **TCP服务器**：高性能TCP服务器
- 📡 **协议支持**：多种协议支持
- 🔄 **连接管理**：连接池管理
- 📊 **性能监控**：网络性能监控
- 🛡️ **安全传输**：数据加密传输

### ruoyi-atomikos (分布式事务插件)

**功能概述**：Atomikos分布式事务管理。

**核心功能**：
- 🔄 **事务管理**：分布式事务管理
- 🗄️ **多数据源**：多数据源事务支持
- 📊 **事务监控**：事务状态监控
- 🔧 **事务恢复**：事务故障恢复
- ⚡ **性能优化**：事务性能优化

每个模块都支持独立启用和禁用，可以根据实际需求选择性集成，实现了真正的模块化架构设计。