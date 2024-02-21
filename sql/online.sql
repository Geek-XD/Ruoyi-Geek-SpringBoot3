DROP TABLE IF EXISTS online_mb;

CREATE TABLE online_mb (
    mb_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    tag varchar(255) NULL COMMENT '标签名',
    tag_id varchar(255) NULL COMMENT '标签id',
    parameterType varchar(255) NULL COMMENT '参数类型',
    resultMap varchar(255) NULL COMMENT '结果类型',
    sql varchar(255) NULL COMMENT 'sql语句',
    path varchar(255) NULL COMMENT '请求路径',
    method varchar(255) NULL COMMENT '请求方式',
    resultType varchar(255) NULL COMMENT '响应类型',
    actuator varchar(255) NULL COMMENT '执行器',
    PRIMARY KEY (mb_id)
) ENGINE = InnoDB  COMMENT = '在线接口';

-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('mybatis在线接口', '3', '1', 'mb', 'tool/online/mb/index', 1, 0, 'C', '0', '0', 'online:mb:list', 'code', 'admin', sysdate(), '', null, 'mybatis在线接口菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('mybatis在线接口查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'online:mb:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('mybatis在线接口新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'online:mb:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('mybatis在线接口修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'online:mb:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('mybatis在线接口删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'online:mb:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('mybatis在线接口导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'online:mb:export',       '#', 'admin', sysdate(), '', null, '');


INSERT INTO sys_dict_type ( dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES ( '请求方式', 'online_api_method', '0', 'admin', '2024-02-21 18:22:03', 'admin', '2024-02-21 18:22:13', NULL);
INSERT INTO sys_dict_type ( dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES ( '标签名', 'online_api_tag', '0', 'admin', '2024-02-21 18:22:29', '', NULL, NULL);
INSERT INTO sys_dict_type ( dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES ( '响应类型', 'online_api_result', '0', 'admin', '2024-02-21 18:22:46', '', NULL, NULL);
INSERT INTO sys_dict_type ( dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES ( '执行器', 'online_api_actuator', '0', 'admin', '2024-02-21 18:23:03', '', NULL, NULL);


INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'POST', 'POST', 'online_api_method', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:23:23', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'GET', 'GET', 'online_api_method', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:23:30', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'PUT', 'PUT', 'online_api_method', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:23:37', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'DELETE', 'DELETE', 'online_api_method', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:23:49', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'select', 'select', 'online_api_tag', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:24:06', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'update', 'update', 'online_api_tag', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:24:12', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'insert', 'insert', 'online_api_tag', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:24:18', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'delete', 'delete', 'online_api_tag', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:24:26', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'selectList', 'selectList', 'online_api_actuator', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:25:00', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'insert', 'insert', 'online_api_actuator', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:25:05', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'selectOne', 'selectOne', 'online_api_actuator', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:25:11', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'update', 'update', 'online_api_actuator', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:25:16', '', NULL, NULL);
INSERT INTO sys_dict_data ( dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES ( 0, 'delete', 'delete', 'online_api_actuator', NULL, 'default', 'N', '0', 'admin', '2024-02-21 18:25:21', '', NULL, NULL);