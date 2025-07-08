# 开发指南

## 📋 开发环境搭建

### IDE配置

#### IntelliJ IDEA配置

1. **项目导入**
```bash
# 使用IDEA打开项目
File -> Open -> 选择项目根目录
```

2. **JDK配置**
```
File -> Project Structure -> Project -> Project SDK -> 选择JDK 21
```

3. **Maven配置**
```
File -> Settings -> Build -> Build Tools -> Maven
- Maven home directory: /path/to/maven
- User settings file: /path/to/settings.xml
```

4. **代码风格配置**
```
File -> Settings -> Editor -> Code Style -> Java
导入项目根目录下的 .idea/codeStyles/Project.xml
```

#### Visual Studio Code配置

1. **必需插件**
```json
{
  "recommendations": [
    "vetur.vetur",
    "ms-vscode.vscode-typescript-next",
    "bradlc.vscode-tailwindcss",
    "formulahendry.auto-rename-tag",
    "christian-kohler.path-intellisense",
    "ms-vscode.vscode-json"
  ]
}
```

2. **工作区配置**
```json
{
  "typescript.preferences.quoteStyle": "single",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "eslint.validate": ["javascript", "typescript", "vue"]
}
```

## 🏗️ 项目结构详解

### 后端项目结构

```
ruoyi-geek-springboot3/
├── ruoyi-admin/                    # 启动模块
│   ├── src/main/java/
│   │   └── com/ruoyi/
│   │       ├── RuoYiApplication.java      # 启动类
│   │       └── web/                       # 控制器
│   ├── src/main/resources/
│   │   ├── application.yml                # 主配置文件
│   │   ├── application-druid.yml          # 数据库配置
│   │   ├── application-auth.yml           # 认证配置
│   │   ├── application-file.yml           # 文件配置
│   │   └── mybatis/                       # MyBatis配置
│   └── pom.xml
├── ruoyi-common/                   # 通用模块
│   ├── src/main/java/
│   │   └── com/ruoyi/common/
│   │       ├── annotation/                # 自定义注解
│   │       ├── config/                    # 全局配置
│   │       ├── core/                      # 核心组件
│   │       ├── exception/                 # 异常处理
│   │       └── utils/                     # 工具类
│   └── pom.xml
├── ruoyi-framework/                # 框架核心
│   ├── src/main/java/
│   │   └── com/ruoyi/framework/
│   │       ├── aspectj/                   # AOP切面
│   │       ├── config/                    # 框架配置
│   │       ├── datasource/                # 数据源配置
│   │       ├── interceptor/               # 拦截器
│   │       ├── security/                  # 安全框架
│   │       └── web/                       # Web配置
│   └── pom.xml
└── ruoyi-system/                   # 系统模块
    ├── src/main/java/
    │   └── com/ruoyi/system/
    │       ├── controller/                # 控制器
    │       ├── service/                   # 服务层
    │       ├── mapper/                    # 数据访问层
    │       └── domain/                    # 实体类
    └── pom.xml
```

### 前端项目结构

```
ruoyi-ui/
├── public/                         # 静态资源
├── src/
│   ├── api/                        # API接口
│   ├── assets/                     # 静态资源
│   ├── components/                 # 公共组件
│   ├── layout/                     # 布局组件
│   ├── router/                     # 路由配置
│   ├── store/                      # 状态管理
│   ├── utils/                      # 工具类
│   ├── views/                      # 页面组件
│   ├── App.vue                     # 根组件
│   └── main.js                     # 入口文件
├── package.json
└── vite.config.js                  # Vite配置
```

## 🔧 开发规范

### 代码规范

#### Java代码规范

1. **命名规范**
```java
// 类名：大驼峰命名
public class UserService {
    
    // 方法名：小驼峰命名
    public void getUserById(Long userId) {
        
    }
    
    // 常量：大写字母，下划线分隔
    public static final String DEFAULT_PASSWORD = "123456";
    
    // 变量：小驼峰命名
    private String userName;
}
```

2. **注释规范**
```java
/**
 * 用户服务类
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public class UserService {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public SysUser getUserById(Long userId) {
        // 业务逻辑
        return userMapper.selectById(userId);
    }
}
```

3. **异常处理**
```java
public class UserService {
    
    public void createUser(SysUser user) {
        try {
            // 业务逻辑
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("创建用户失败", e);
            throw new ServiceException("用户创建失败");
        }
    }
}
```

#### Vue代码规范

1. **组件命名**
```vue
<!-- 组件文件名：大驼峰命名 -->
<!-- UserList.vue -->
<template>
  <div class="user-list">
    <!-- 内容 -->
  </div>
</template>

<script setup>
// 变量名：小驼峰命名
const userList = ref([])
const queryParams = ref({})

// 方法名：小驼峰命名
const getUserList = async () => {
  // 逻辑
}
</script>
```

2. **API接口规范**
```javascript
// api/system/user.js
import request from '@/utils/request'

// 获取用户列表
export function listUser(query) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: query
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data: data
  })
}
```

### 数据库设计规范

#### 表设计规范

1. **表名规范**
```sql
-- 系统表：sys_前缀
CREATE TABLE sys_user (
    user_id     bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    user_name   varchar(30)     NOT NULL COMMENT '用户账号',
    PRIMARY KEY (user_id)
) ENGINE=InnoDB COMMENT = '用户信息表';

-- 业务表：业务前缀
CREATE TABLE order_info (
    order_id    bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    order_no    varchar(50)     NOT NULL COMMENT '订单号',
    PRIMARY KEY (order_id)
) ENGINE=InnoDB COMMENT = '订单信息表';
```

2. **字段规范**
```sql
-- 基础字段
CREATE TABLE base_entity (
    create_by     varchar(64)     NULL COMMENT '创建者',
    create_time   datetime        NULL COMMENT '创建时间',
    update_by     varchar(64)     NULL COMMENT '更新者',
    update_time   datetime        NULL COMMENT '更新时间',
    remark        varchar(500)    NULL COMMENT '备注'
);

-- 逻辑删除字段
ALTER TABLE table_name ADD COLUMN del_flag char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）';

-- 状态字段
ALTER TABLE table_name ADD COLUMN status char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）';
```

#### 索引设计规范

```sql
-- 主键索引
PRIMARY KEY (id)

-- 唯一索引
UNIQUE KEY uk_user_name (user_name)

-- 普通索引
KEY idx_dept_id (dept_id)

-- 组合索引
KEY idx_user_dept (user_id, dept_id)

-- 前缀索引
KEY idx_user_name_prefix (user_name(10))
```

## 🚀 开发实践

### 新增模块开发

#### 1. 创建模块

```bash
# 创建新模块目录
mkdir ruoyi-demo

# 创建基础结构
mkdir -p ruoyi-demo/src/main/java/com/ruoyi/demo/{controller,service,mapper,domain}
mkdir -p ruoyi-demo/src/main/resources/mapper
```

#### 2. 编写pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>com.ruoyi.geekxd</groupId>
        <artifactId>ruoyi</artifactId>
        <version>3.9.0-G</version>
    </parent>
    
    <modelVersion>4.0.0</modelVersion>
    <artifactId>ruoyi-demo</artifactId>
    
    <dependencies>
        <dependency>
            <groupId>com.ruoyi.geekxd</groupId>
            <artifactId>ruoyi-common</artifactId>
        </dependency>
    </dependencies>
</project>
```

#### 3. 创建实体类

```java
// domain/Demo.java
package com.ruoyi.demo.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 示例对象 demo
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Demo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 名称 */
    private String name;

    /** 状态 */
    private String status;
}
```

#### 4. 创建Mapper接口

```java
// mapper/DemoMapper.java
package com.ruoyi.demo.mapper;

import com.ruoyi.demo.domain.Demo;
import java.util.List;

/**
 * 示例Mapper接口
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
public interface DemoMapper {
    /**
     * 查询示例列表
     */
    List<Demo> selectDemoList(Demo demo);

    /**
     * 新增示例
     */
    int insertDemo(Demo demo);

    /**
     * 修改示例
     */
    int updateDemo(Demo demo);

    /**
     * 删除示例
     */
    int deleteDemoById(Long id);
}
```

#### 5. 创建Service接口和实现

```java
// service/IDemoService.java
package com.ruoyi.demo.service;

import com.ruoyi.demo.domain.Demo;
import java.util.List;

public interface IDemoService {
    List<Demo> selectDemoList(Demo demo);
    int insertDemo(Demo demo);
    int updateDemo(Demo demo);
    int deleteDemoById(Long id);
}

// service/impl/DemoServiceImpl.java
package com.ruoyi.demo.service.impl;

import com.ruoyi.demo.domain.Demo;
import com.ruoyi.demo.mapper.DemoMapper;
import com.ruoyi.demo.service.IDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoServiceImpl implements IDemoService {
    
    @Autowired
    private DemoMapper demoMapper;

    @Override
    public List<Demo> selectDemoList(Demo demo) {
        return demoMapper.selectDemoList(demo);
    }

    @Override
    public int insertDemo(Demo demo) {
        return demoMapper.insertDemo(demo);
    }

    @Override
    public int updateDemo(Demo demo) {
        return demoMapper.updateDemo(demo);
    }

    @Override
    public int deleteDemoById(Long id) {
        return demoMapper.deleteDemoById(id);
    }
}
```

#### 6. 创建Controller

```java
// controller/DemoController.java
package com.ruoyi.demo.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.demo.domain.Demo;
import com.ruoyi.demo.service.IDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 示例Controller
 * 
 * @author ruoyi
 * @date 2024-01-01
 */
@RestController
@RequestMapping("/demo")
public class DemoController extends BaseController {
    
    @Autowired
    private IDemoService demoService;

    /**
     * 查询示例列表
     */
    @PreAuthorize("@ss.hasPermi('demo:list')")
    @GetMapping("/list")
    public TableDataInfo list(Demo demo) {
        startPage();
        List<Demo> list = demoService.selectDemoList(demo);
        return getDataTable(list);
    }

    /**
     * 新增示例
     */
    @PreAuthorize("@ss.hasPermi('demo:add')")
    @Log(title = "示例", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Demo demo) {
        return toAjax(demoService.insertDemo(demo));
    }

    /**
     * 修改示例
     */
    @PreAuthorize("@ss.hasPermi('demo:edit')")
    @Log(title = "示例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Demo demo) {
        return toAjax(demoService.updateDemo(demo));
    }

    /**
     * 删除示例
     */
    @PreAuthorize("@ss.hasPermi('demo:remove')")
    @Log(title = "示例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(demoService.deleteDemoById(id));
    }
}
```

#### 7. 创建SQL映射文件

```xml
<!-- resources/mapper/DemoMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.demo.mapper.DemoMapper">
    
    <resultMap type="com.ruoyi.demo.domain.Demo" id="DemoResult">
        <result property="id" column="id"/>
        <result property="name" column="name"/>
        <result property="status" column="status"/>
        <result property="createBy" column="create_by"/>
        <result property="createTime" column="create_time"/>
        <result property="updateBy" column="update_by"/>
        <result property="updateTime" column="update_time"/>
        <result property="remark" column="remark"/>
    </resultMap>

    <sql id="selectDemoVo">
        select id, name, status, create_by, create_time, update_by, update_time, remark
        from demo
    </sql>

    <select id="selectDemoList" parameterType="com.ruoyi.demo.domain.Demo" resultMap="DemoResult">
        <include refid="selectDemoVo"/>
        <where>
            <if test="name != null and name != ''">
                and name like concat('%', #{name}, '%')
            </if>
            <if test="status != null and status != ''">
                and status = #{status}
            </if>
        </where>
    </select>

    <insert id="insertDemo" parameterType="com.ruoyi.demo.domain.Demo">
        insert into demo
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="name != null">name,</if>
            <if test="status != null">status,</if>
            <if test="createBy != null">create_by,</if>
            <if test="createTime != null">create_time,</if>
            <if test="remark != null">remark,</if>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
            <if test="name != null">#{name},</if>
            <if test="status != null">#{status},</if>
            <if test="createBy != null">#{createBy},</if>
            <if test="createTime != null">#{createTime},</if>
            <if test="remark != null">#{remark},</if>
        </trim>
    </insert>

    <update id="updateDemo" parameterType="com.ruoyi.demo.domain.Demo">
        update demo
        <trim prefix="SET" suffixOverrides=",">
            <if test="name != null">name = #{name},</if>
            <if test="status != null">status = #{status},</if>
            <if test="updateBy != null">update_by = #{updateBy},</if>
            <if test="updateTime != null">update_time = #{updateTime},</if>
            <if test="remark != null">remark = #{remark},</if>
        </trim>
        where id = #{id}
    </update>

    <delete id="deleteDemoById" parameterType="Long">
        delete from demo where id = #{id}
    </delete>
</mapper>
```

### 前端页面开发

#### 1. 创建API接口

```javascript
// src/api/demo/index.js
import request from '@/utils/request'

// 查询示例列表
export function listDemo(query) {
  return request({
    url: '/demo/list',
    method: 'get',
    params: query
  })
}

// 查询示例详细
export function getDemo(id) {
  return request({
    url: '/demo/' + id,
    method: 'get'
  })
}

// 新增示例
export function addDemo(data) {
  return request({
    url: '/demo',
    method: 'post',
    data: data
  })
}

// 修改示例
export function updateDemo(data) {
  return request({
    url: '/demo',
    method: 'put',
    data: data
  })
}

// 删除示例
export function delDemo(id) {
  return request({
    url: '/demo/' + id,
    method: 'delete'
  })
}
```

#### 2. 创建列表页面

```vue
<!-- src/views/demo/index.vue -->
<template>
  <div class="app-container">
    <!-- 查询条件 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="停用" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['demo:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['demo:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="demoList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="名称" align="center" prop="name" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['demo:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['demo:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_normal_disable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDemo, getDemo, delDemo, addDemo, updateDemo } from "@/api/demo";

export default {
  name: "Demo",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 示例表格数据
      demoList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        status: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询示例列表 */
    getList() {
      this.loading = true;
      listDemo(this.queryParams).then(response => {
        this.demoList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        name: null,
        status: "0",
        remark: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加示例";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getDemo(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改示例";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateDemo(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDemo(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除示例编号为"' + ids + '"的数据项？').then(function() {
        return delDemo(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
```

## 🧪 测试指南

### 单元测试

```java
// src/test/java/com/ruoyi/demo/DemoServiceTest.java
@SpringBootTest
@Transactional
@Rollback
public class DemoServiceTest {
    
    @Autowired
    private IDemoService demoService;
    
    @Test
    public void testInsertDemo() {
        Demo demo = new Demo();
        demo.setName("测试示例");
        demo.setStatus("0");
        demo.setRemark("测试备注");
        
        int result = demoService.insertDemo(demo);
        Assert.assertEquals(1, result);
    }
    
    @Test
    public void testSelectDemoList() {
        Demo demo = new Demo();
        demo.setName("测试");
        
        List<Demo> list = demoService.selectDemoList(demo);
        Assert.assertNotNull(list);
    }
}
```

### 集成测试

```java
// src/test/java/com/ruoyi/demo/DemoControllerTest.java
@SpringBootTest
@AutoConfigureMockMvc
public class DemoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testListDemo() throws Exception {
        mockMvc.perform(get("/demo/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    public void testAddDemo() throws Exception {
        String json = "{\"name\":\"测试示例\",\"status\":\"0\",\"remark\":\"测试备注\"}";
        
        mockMvc.perform(post("/demo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

## 🔍 调试技巧

### 后端调试

1. **日志调试**
```java
// 在需要调试的地方添加日志
log.debug("调试信息：{}", data);
log.info("信息日志：{}", data);
log.warn("警告日志：{}", data);
log.error("错误日志：{}", data);
```

2. **断点调试**
```java
// 在IDE中设置断点
public void debugMethod() {
    String data = "调试数据";
    // 在这里设置断点
    System.out.println(data);
}
```

3. **条件断点**
```java
// 设置条件断点，只有满足条件时才会触发
public void conditionalDebug(int i) {
    // 断点条件：i > 10
    if (i > 0) {
        System.out.println("条件满足");
    }
}
```

### 前端调试

1. **浏览器调试**
```javascript
// 在代码中添加断点
debugger;

// 控制台输出
console.log('调试信息', data);
console.error('错误信息', error);
```

2. **Vue Devtools**
```bash
# 安装Vue Devtools浏览器插件
# 在浏览器中打开开发者工具的Vue标签页
```

3. **网络调试**
```javascript
// 使用axios拦截器调试网络请求
axios.interceptors.request.use(config => {
  console.log('请求配置：', config);
  return config;
});

axios.interceptors.response.use(response => {
  console.log('响应数据：', response);
  return response;
});
```

## 📚 最佳实践

### 性能优化

1. **数据库优化**
```sql
-- 创建索引
CREATE INDEX idx_user_name ON sys_user(user_name);

-- 分页查询优化
SELECT * FROM sys_user LIMIT 10 OFFSET 0;
```

2. **缓存使用**
```java
// 使用Redis缓存
@Cacheable(value = "user", key = "#userId")
public SysUser getUserById(Long userId) {
    return userMapper.selectById(userId);
}

// 清除缓存
@CacheEvict(value = "user", key = "#userId")
public void updateUser(SysUser user) {
    userMapper.updateById(user);
}
```

3. **前端优化**
```javascript
// 使用防抖
import { debounce } from 'lodash';

const search = debounce(function(keyword) {
  // 搜索逻辑
}, 300);

// 使用节流
import { throttle } from 'lodash';

const scroll = throttle(function() {
  // 滚动逻辑
}, 100);
```

### 安全实践

1. **输入验证**
```java
// 后端参数验证
@PostMapping
public AjaxResult add(@Valid @RequestBody Demo demo) {
    // 业务逻辑
}

// 实体类验证
@NotBlank(message = "名称不能为空")
private String name;
```

2. **权限控制**
```java
// 方法级权限控制
@PreAuthorize("@ss.hasPermi('demo:add')")
public AjaxResult add(@RequestBody Demo demo) {
    // 业务逻辑
}

// 数据权限控制
@DataScope(deptAlias = "d", userAlias = "u")
public List<Demo> selectDemoList(Demo demo) {
    // 业务逻辑
}
```

3. **SQL注入防护**
```xml
<!-- 使用参数化查询 -->
<select id="selectByName" parameterType="String" resultType="Demo">
    SELECT * FROM demo WHERE name = #{name}
</select>
```

通过以上开发指南，您可以快速掌握 RuoYi-Geek 的开发模式和最佳实践，构建高质量的企业级应用。