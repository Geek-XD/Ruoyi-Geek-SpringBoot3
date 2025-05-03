-- ----------------------------
-- 18、代码生成业务表
-- ----------------------------
DROP TABLE IF EXISTS gen_table;
CREATE TABLE gen_table (
  table_id            bigserial PRIMARY KEY,
  table_name          varchar(200)    DEFAULT '' ,
  table_comment       varchar(500)    NOT NULL ,
  table_alias         varchar(200)    NOT NULL ,
  have_sub_column     char(1)         DEFAULT '0',
  sub_table_name      varchar(64)     DEFAULT NULL,
  sub_table_fk_name   varchar(64)     DEFAULT NULL,
  class_name          varchar(100)    DEFAULT '' ,
  tpl_category        varchar(200)    DEFAULT 'crud',
  tpl_web_type        varchar(200)    DEFAULT 'element-plus',
  package_name        varchar(100)    DEFAULT NULL,
  module_name         varchar(30)     DEFAULT NULL,
  business_name       varchar(30)     DEFAULT NULL,
  function_name       varchar(50)     DEFAULT NULL,
  function_author     varchar(50)     DEFAULT NULL,
  gen_type            char(1)         DEFAULT '0',
  gen_path            varchar(200)    DEFAULT '/' ,
  options             varchar(1000)   DEFAULT NULL,
  create_by           varchar(64)     DEFAULT '' ,
  create_time         timestamp       DEFAULT CURRENT_TIMESTAMP,
  update_by           varchar(64)     DEFAULT '' ,
  update_time         timestamp       DEFAULT CURRENT_TIMESTAMP,
  remark              varchar(500)    DEFAULT NULL
);

COMMENT ON TABLE gen_table IS '代码生成业务表';
COMMENT ON COLUMN gen_table.table_id IS '编号';
COMMENT ON COLUMN gen_table.table_name IS '表名称';
COMMENT ON COLUMN gen_table.table_alias IS '表别名';
COMMENT ON COLUMN gen_table.table_comment IS '表描述';
COMMENT ON COLUMN gen_table.have_sub_column IS '是否含有关联字段';
COMMENT ON COLUMN gen_table.sub_table_name IS '关联子表的表名';
COMMENT ON COLUMN gen_table.sub_table_fk_name IS '子表关联的外键名';
COMMENT ON COLUMN gen_table.class_name IS '实体类名称';
COMMENT ON COLUMN gen_table.tpl_category IS '使用的模板（crud单表操作 tree树表操作）';
COMMENT ON COLUMN gen_table.tpl_web_type IS '使用的模板类型';
COMMENT ON COLUMN gen_table.package_name IS '生成包路径';
COMMENT ON COLUMN gen_table.module_name IS '生成模块名';
COMMENT ON COLUMN gen_table.business_name IS '生成业务名';
COMMENT ON COLUMN gen_table.function_name IS '生成功能名';
COMMENT ON COLUMN gen_table.function_author IS '生成功能作者';
COMMENT ON COLUMN gen_table.gen_type IS '生成代码方式（0zip压缩包 1自定义路径）';
COMMENT ON COLUMN gen_table.gen_path IS '生成路径（不填默认项目路径）';
COMMENT ON COLUMN gen_table.options IS '其它生成选项';
COMMENT ON COLUMN gen_table.create_by IS '创建者';
COMMENT ON COLUMN gen_table.create_time IS '创建时间';
COMMENT ON COLUMN gen_table.update_by IS '更新者';
COMMENT ON COLUMN gen_table.update_time IS '更新时间';
COMMENT ON COLUMN gen_table.remark IS '备注';

-- ----------------------------
-- 19、代码生成业务表字段
-- ----------------------------
DROP TABLE IF EXISTS gen_table_column;
CREATE TABLE gen_table_column (
  column_id       bigserial PRIMARY KEY,
  table_id        bigint        DEFAULT NULL,
  column_name     varchar(200)    DEFAULT NULL,
  column_comment  varchar(500)    DEFAULT NULL,
  column_type     varchar(100)    DEFAULT NULL,
  java_type       varchar(500)    DEFAULT NULL,
  java_field      varchar(200)    DEFAULT NULL,
  is_pk           char(1)         DEFAULT NULL,
  is_increment    char(1)         DEFAULT NULL,
  is_required     char(1)         DEFAULT NULL,
  is_insert       char(1)         DEFAULT NULL,
  is_edit         char(1)         DEFAULT NULL,
  is_list         char(1)         DEFAULT NULL,
  is_query        char(1)         DEFAULT NULL,
  query_type      varchar(200)    DEFAULT 'EQ',
  html_type       varchar(200)    DEFAULT NULL,
  dict_type       varchar(200)    DEFAULT '' ,
  sort            int             DEFAULT NULL,
  sub_column_table_name   varchar(200)    DEFAULT NULL,
  sub_column_fk_name      varchar(200)    DEFAULT NULL,
  sub_column_name         varchar(200)    DEFAULT NULL,
  sub_column_java_field   varchar(200)    DEFAULT NULL,
  sub_column_java_type    varchar(255)    DEFAULT NULL,
  create_by       varchar(64)     DEFAULT '' ,
  create_time     timestamp       DEFAULT CURRENT_TIMESTAMP,
  update_by       varchar(64)     DEFAULT '' ,
  update_time     timestamp       DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE gen_table_column IS '代码生成业务表字段';
COMMENT ON COLUMN gen_table_column.column_id IS '编号';
COMMENT ON COLUMN gen_table_column.table_id IS '归属表编号';
COMMENT ON COLUMN gen_table_column.column_name IS '列名称';
COMMENT ON COLUMN gen_table_column.column_comment IS '列描述';
COMMENT ON COLUMN gen_table_column.column_type IS '列类型';
COMMENT ON COLUMN gen_table_column.java_type IS 'JAVA类型';
COMMENT ON COLUMN gen_table_column.java_field IS 'JAVA字段名';
COMMENT ON COLUMN gen_table_column.is_pk IS '是否主键（1是）';
COMMENT ON COLUMN gen_table_column.is_increment IS '是否自增（1是）';
COMMENT ON COLUMN gen_table_column.is_required IS '是否必填（1是）';
COMMENT ON COLUMN gen_table_column.is_insert IS '是否为插入字段（1是）';
COMMENT ON COLUMN gen_table_column.is_edit IS '是否编辑字段（1是）';
COMMENT ON COLUMN gen_table_column.is_list IS '是否列表字段（1是）';
COMMENT ON COLUMN gen_table_column.is_query IS '是否查询字段（1是）';
COMMENT ON COLUMN gen_table_column.query_type IS '查询方式（等于、不等于、大于、小于、范围）';
COMMENT ON COLUMN gen_table_column.html_type IS '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）';
COMMENT ON COLUMN gen_table_column.dict_type IS '字典类型';
COMMENT ON COLUMN gen_table_column.sort IS '排序';
COMMENT ON COLUMN gen_table_column.sub_column_table_name IS '关联表名称';
COMMENT ON COLUMN gen_table_column.sub_column_fk_name IS '关联字段名称';
COMMENT ON COLUMN gen_table_column.sub_column_name IS '映射字段名称';
COMMENT ON COLUMN gen_table_column.sub_column_java_field IS '映射字段JAVA字段名';
COMMENT ON COLUMN gen_table_column.sub_column_java_type IS '映射字段JAVA类型';
COMMENT ON COLUMN gen_table_column.create_by IS '创建者';
COMMENT ON COLUMN gen_table_column.create_time IS '创建时间';
COMMENT ON COLUMN gen_table_column.update_by IS '更新者';
COMMENT ON COLUMN gen_table_column.update_time IS '更新时间';

DROP TABLE IF EXISTS gen_join_table;
CREATE TABLE gen_join_table (
  table_id            bigserial,
  left_table_id       bigint          DEFAULT NULL,
  right_table_id      bigint          DEFAULT NULL,
  left_table_alias    varchar(200)    DEFAULT NULL,
  right_table_alias   varchar(200)    DEFAULT NULL,
  left_table_fk       varchar(200)    DEFAULT NULL,
  right_table_fk      varchar(200)    DEFAULT NULL,
  join_type           varchar(200)    DEFAULT NULL,
  join_columns        varchar(500)    DEFAULT NULL,
  order_num           varchar(64)     DEFAULT NULL,
  new_table_id        bigint          DEFAULT NULL,
  PRIMARY KEY (table_id, right_table_id, left_table_id)
);

COMMENT ON TABLE gen_join_table IS '代码生成关联表';
COMMENT ON COLUMN gen_join_table.table_id IS '表编号';
COMMENT ON COLUMN gen_join_table.left_table_id IS '左表名称';
COMMENT ON COLUMN gen_join_table.right_table_id IS '右表编号';
COMMENT ON COLUMN gen_join_table.left_table_alias IS '左表别名';
COMMENT ON COLUMN gen_join_table.right_table_alias IS '右表别名';
COMMENT ON COLUMN gen_join_table.left_table_fk IS '左表关联键';
COMMENT ON COLUMN gen_join_table.right_table_fk IS '右表关联键';
COMMENT ON COLUMN gen_join_table.join_type IS '关联类型';
COMMENT ON COLUMN gen_join_table.join_columns IS '关联字段';
COMMENT ON COLUMN gen_join_table.order_num IS '序号';
COMMENT ON COLUMN gen_join_table.new_table_id IS '新表编号';

-- 插入菜单数据
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', CURRENT_TIMESTAMP, '', NULL, '代码生成菜单');

-- 代码生成按钮
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1055, '生成查询', 116, 1, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1056, '生成修改', 116, 2, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1057, '生成删除', 116, 3, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1058, '导入代码', 116, 4, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1059, '预览代码', 116, 5, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (1060, '生成代码', 116, 6, '#', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');