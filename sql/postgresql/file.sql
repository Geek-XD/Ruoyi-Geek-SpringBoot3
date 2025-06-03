-- ----------------------------
-- 1、sys_file_info 表
-- ----------------------------
DROP TABLE IF EXISTS sys_file_info CASCADE;

CREATE TABLE sys_file_info (
    file_id BIGSERIAL NOT NULL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    storage_type VARCHAR(32) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    md5 VARCHAR(64),
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP,
    remark VARCHAR(255),
    del_flag CHAR(1) DEFAULT '0'
);

-- 添加唯一约束
ALTER TABLE sys_file_info ADD CONSTRAINT uk_file_path UNIQUE (file_path);
-- ALTER TABLE sys_file_info ADD CONSTRAINT uk_md5 UNIQUE (md5);

-- 添加表和列注释
COMMENT ON TABLE sys_file_info IS '文件信息表';
COMMENT ON COLUMN sys_file_info.file_id IS '文件主键';
COMMENT ON COLUMN sys_file_info.file_name IS '原始文件名';
COMMENT ON COLUMN sys_file_info.file_path IS '统一逻辑路径（/开头）';
COMMENT ON COLUMN sys_file_info.storage_type IS '存储类型（local/minio/oss）';
COMMENT ON COLUMN sys_file_info.file_type IS '文件类型/后缀';
COMMENT ON COLUMN sys_file_info.file_size IS '文件大小（字节）';
COMMENT ON COLUMN sys_file_info.md5 IS '文件MD5';
COMMENT ON COLUMN sys_file_info.create_by IS '创建者';
COMMENT ON COLUMN sys_file_info.create_time IS '创建时间';
COMMENT ON COLUMN sys_file_info.update_by IS '更新者';
COMMENT ON COLUMN sys_file_info.update_time IS '更新时间';
COMMENT ON COLUMN sys_file_info.remark IS '备注';
COMMENT ON COLUMN sys_file_info.del_flag IS '删除标志（0代表存在 2代表删除）';

-- ----------------------------
-- 菜单 SQL
-- ----------------------------
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('文件管理', 1, 1, 'file', 'system/file/index', 1, 0, 'C', '0', '0', 'system:file:list', 'excel', 'admin', CURRENT_TIMESTAMP, '', NULL, '文件管理菜单');

-- 文件管理菜单ID
DO $$
DECLARE
    parentId INTEGER;
BEGIN
    SELECT LASTVAL() INTO parentId;
    
    -- 按钮 SQL
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('文件信息查询', parentId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:file:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('文件信息新增', parentId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:file:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('文件信息修改', parentId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:file:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('文件信息删除', parentId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:file:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('文件信息导出', parentId, 5, '#', '', 1, 0, 'F', '0', '0', 'system:file:export', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
END $$;