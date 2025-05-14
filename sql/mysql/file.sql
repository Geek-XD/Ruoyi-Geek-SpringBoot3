DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE sys_file_info (
    file_id      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '文件主键',
    file_name    VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path    VARCHAR(500) NOT NULL COMMENT '统一逻辑路径（/开头）',
    storage_type VARCHAR(32)  NOT NULL COMMENT '存储类型（local/minio/oss）',
    file_type    VARCHAR(50)  COMMENT '文件类型/后缀',
    file_size    BIGINT       COMMENT '文件大小（字节）',
    md5          VARCHAR(64)  COMMENT '文件MD5',
    create_by    VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by    VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    del_flag     CHAR(1)      DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    PRIMARY KEY (file_id),
    UNIQUE KEY uk_file_path (file_path),
    UNIQUE KEY uk_md5 (md5)
) ENGINE=InnoDB COMMENT='文件信息表';

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query,route_name, is_frame, is_cache, menu_type, visible, `status`, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES ('文件管理', 0, 4, 'file', NULL, NULL, '',1, 0, 'M', '0', '0', NULL, 'excel', 'admin', '2024-02-15 22:40:23', '', NULL, '');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

select @fileParentId := @parentId;

-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件信息', @fileParentId, '1', 'info', 'file/info/index', 1, 0, 'C', '0', '0', 'file:info:list', 'excel', 'admin', sysdate(), '', null, '文件信息菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件信息查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'file:info:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件信息新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'file:info:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件信息修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'file:info:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件信息删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'file:info:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('文件信息导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'file:info:export',       '#', 'admin', sysdate(), '', null, '');