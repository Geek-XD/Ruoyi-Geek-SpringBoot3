# 快速开始指南

## 📋 环境准备

### 必需软件

| 软件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 21+ | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| Maven | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| MySQL | 8.0+ | [MySQL Community](https://dev.mysql.com/downloads/mysql/) |
| Redis | 7.0+ | [Redis](https://redis.io/download) |
| Node.js | 18+ | [Node.js](https://nodejs.org/) |

### 环境验证

```bash
# 验证Java版本
java -version

# 验证Maven版本
mvn -version

# 验证MySQL服务
mysql --version

# 验证Redis服务
redis-server --version

# 验证Node.js版本
node --version
npm --version
```

## 🚀 快速安装

### 1. 获取源码

```bash
# 克隆项目
git clone https://github.com/Geek-XD/Ruoyi-Geek-SpringBoot3.git

# 进入项目目录
cd Ruoyi-Geek-SpringBoot3
```

### 2. 数据库初始化

#### 2.1 创建数据库

```sql
-- 创建数据库
CREATE DATABASE `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建用户（可选）
CREATE USER 'ruoyi'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON `ry-vue`.* TO 'ruoyi'@'%';
FLUSH PRIVILEGES;
```

#### 2.2 导入基础数据

```bash
# 导入核心数据
mysql -u root -p ry-vue < sql/mysql/ry_20250603.sql

# 导入扩展模块数据（根据需要选择）
mysql -u root -p ry-vue < sql/mysql/auth.sql      # 认证模块
mysql -u root -p ry-vue < sql/mysql/pay.sql       # 支付模块
mysql -u root -p ry-vue < sql/mysql/file.sql      # 文件模块
mysql -u root -p ry-vue < sql/mysql/gen.sql       # 代码生成
mysql -u root -p ry-vue < sql/mysql/quartz.sql    # 定时任务
mysql -u root -p ry-vue < sql/mysql/flowable.sql  # 工作流
mysql -u root -p ry-vue < sql/mysql/form.sql      # 表单模块
mysql -u root -p ry-vue < sql/mysql/message.sql   # 消息模块
mysql -u root -p ry-vue < sql/mysql/online.sql    # 在线开发
```

### 3. 后端配置

#### 3.1 修改数据库配置

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
spring:
  datasource:
    druid:
      # 主库数据源
      master:
        url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: password
        driver-class-name: com.mysql.cj.jdbc.Driver
      # 从库数据源（可选）
      slave:
        enabled: false
        url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: password
        driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 3.2 修改Redis配置

编辑 `ruoyi-admin/src/main/resources/application-plugins.yml`：

```yaml
redis:
  enable: true
  host: localhost
  port: 6379
  password: 
  timeout: 10s
  lettuce:
    pool:
      min-idle: 0
      max-idle: 8
      max-active: 8
      max-wait: -1ms
```

#### 3.3 配置文件服务

编辑 `ruoyi-admin/src/main/resources/application-file.yml`：

```yaml
# 本地文件存储
local:
  enable: true
  path: D:/ruoyi/uploadPath  # Windows路径
  # path: /home/ruoyi/uploadPath  # Linux路径
  domain: http://localhost:8080
  prefix: /profile

# MinIO配置（可选）
minio:
  enable: false
  client:
    default:
      endpoint: http://localhost:9000
      accessKey: minioadmin
      secretKey: minioadmin
      bucketName: ruoyi
```

### 4. 启动后端服务

```bash
# 编译项目
mvn clean compile

# 启动服务（开发模式）
cd ruoyi-admin
mvn spring-boot:run

# 或者打包启动
mvn clean package -DskipTests
java -jar target/ruoyi-admin.jar
```

### 5. 前端配置与启动

#### 5.1 安装依赖

```bash
# 进入前端目录
cd ruoyi-ui

# 安装依赖
npm install

# 或使用yarn
yarn install
```

#### 5.2 修改API地址

编辑 `ruoyi-ui/src/utils/request.js`：

```javascript
// 后端API地址
const baseURL = process.env.NODE_ENV === 'production' 
  ? 'https://your-domain.com/prod-api' 
  : 'http://localhost:8080/dev-api'
```

#### 5.3 启动前端服务

```bash
# 开发模式启动
npm run dev

# 或使用yarn
yarn dev
```

## 🎯 访问系统

### 系统地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端管理系统 | http://localhost:80 | 管理后台 |
| 后端API服务 | http://localhost:8080 | 后端API |
| API文档 | http://localhost:8080/doc.html | Knife4j文档 |
| Swagger文档 | http://localhost:8080/swagger-ui/index.html | Swagger UI |

### 默认账户

| 用户类型 | 用户名 | 密码 | 说明 |
|----------|--------|------|------|
| 超级管理员 | admin | admin123 | 拥有所有权限 |
| 普通用户 | test | admin123 | 基础权限 |

## 🔧 开发模式

### 热部署配置

在 `ruoyi-admin/src/main/resources/application.yml` 中：

```yaml
spring:
  devtools:
    restart:
      enabled: true  # 开启热部署
      exclude: static/**,public/**,templates/**
```

### 开发工具推荐

#### IDE推荐
- **IntelliJ IDEA** - Java开发推荐
- **Visual Studio Code** - 前端开发推荐
- **DataGrip** - 数据库管理工具

#### 浏览器插件
- **Vue.js devtools** - Vue开发调试
- **Redux DevTools** - 状态管理调试
- **Postman** - API测试工具

## 📦 生产部署

### 1. 后端打包

```bash
# 打包项目
mvn clean package -DskipTests

# 生成的jar文件位于
# ruoyi-admin/target/ruoyi-admin.jar
```

### 2. 前端打包

```bash
cd ruoyi-ui

# 构建生产环境
npm run build:prod

# 生成的文件位于 dist 目录
```

### 3. 部署配置

#### 3.1 应用配置

创建 `application-prod.yml`：

```yaml
# 生产环境配置
server:
  port: 8080

spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://your-db-host:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: ${DB_USERNAME:root}
        password: ${DB_PASSWORD:password}

# Redis配置
redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:}
```

#### 3.2 启动脚本

创建 `start.sh`：

```bash
#!/bin/bash
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
export SPRING_PROFILES_ACTIVE=prod

nohup java $JAVA_OPTS -jar ruoyi-admin.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE > application.log 2>&1 &
echo "应用启动成功，PID: $!"
```

#### 3.3 Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /usr/share/nginx/html/dist;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /prod-api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 文件上传目录
    location /profile/ {
        root /home/ruoyi/uploadPath;
    }
}
```

## 🛠️ 常见问题

### 1. 数据库连接失败

**问题**：启动时报数据库连接错误

**解决方案**：
```bash
# 检查数据库服务状态
systemctl status mysql

# 检查数据库用户权限
mysql -u root -p
SHOW GRANTS FOR 'ruoyi'@'%';
```

### 2. Redis连接失败

**问题**：Redis连接超时

**解决方案**：
```bash
# 检查Redis服务状态
redis-cli ping

# 检查Redis配置
cat /etc/redis/redis.conf | grep bind
```

### 3. 端口被占用

**问题**：端口8080被占用

**解决方案**：
```bash
# 查看端口占用
netstat -tlnp | grep :8080

# 修改端口配置
# 在application.yml中修改server.port配置
```

### 4. 前端资源加载失败

**问题**：前端页面白屏或资源404

**解决方案**：
```bash
# 检查nginx配置
nginx -t

# 检查前端构建产物
ls -la ruoyi-ui/dist/

# 检查API代理配置
curl -I http://localhost/prod-api/captchaImage
```

### 5. 权限异常

**问题**：登录后无法访问菜单

**解决方案**：
```sql
-- 检查用户角色
SELECT * FROM sys_user_role WHERE user_id = 1;

-- 检查角色权限
SELECT * FROM sys_role_menu WHERE role_id = 1;

-- 重置admin用户权限
UPDATE sys_user SET admin = 1 WHERE user_id = 1;
```

## 🔍 调试指南

### 1. 开启DEBUG日志

```yaml
# application.yml
logging:
  level:
    com.ruoyi: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```

### 2. 数据库SQL日志

```yaml
# application.yml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 3. 监控端点

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

访问监控端点：
- 健康检查：http://localhost:8080/actuator/health
- 应用信息：http://localhost:8080/actuator/info
- 指标统计：http://localhost:8080/actuator/metrics

## 📚 进阶配置

### 1. 多环境配置

创建不同环境的配置文件：
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

### 2. 配置外部化

使用环境变量或外部配置文件：

```bash
# 使用环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ry-vue
export DB_USERNAME=root
export DB_PASSWORD=password

# 使用外部配置文件
java -jar ruoyi-admin.jar --spring.config.location=classpath:/application.yml,/path/to/external/config.yml
```

### 3. 集群部署

```bash
# 启动多个实例
java -jar ruoyi-admin.jar --server.port=8081 --spring.profiles.active=prod &
java -jar ruoyi-admin.jar --server.port=8082 --spring.profiles.active=prod &
java -jar ruoyi-admin.jar --server.port=8083 --spring.profiles.active=prod &
```

### 4. 健康检查

```bash
#!/bin/bash
# health_check.sh
curl -f http://localhost:8080/actuator/health || exit 1
```

通过以上配置，您就可以成功启动和运行 RuoYi-Geek SpringBoot3 系统了。如果在部署过程中遇到任何问题，请参考常见问题部分或联系我们的技术支持。