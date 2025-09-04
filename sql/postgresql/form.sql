DROP TABLE IF EXISTS form_template;

CREATE TABLE form_template (
    form_id BIGSERIAL NOT NULL PRIMARY KEY,
    form_name VARCHAR(100) NOT NULL,
    form_schema JSON,
    form_version VARCHAR(10) DEFAULT '1.0.0',
    form_status VARCHAR(2) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    del_flag CHAR(1) DEFAULT '0'
);

COMMENT ON TABLE form_template IS '表单模板表';
COMMENT ON COLUMN form_template.form_id IS '表单ID';
COMMENT ON COLUMN form_template.form_name IS '表单名称';
COMMENT ON COLUMN form_template.form_schema IS '表单JSON Schema（vForm配置）';
COMMENT ON COLUMN form_template.form_version IS '表单版本（语义化版本）';
COMMENT ON COLUMN form_template.form_status IS '发布状态（0: 草稿, 1: 已发布, 2: 已停用）';
COMMENT ON COLUMN form_template.create_by IS '创建者';
COMMENT ON COLUMN form_template.create_time IS '创建时间';
COMMENT ON COLUMN form_template.update_by IS '更新者';
COMMENT ON COLUMN form_template.update_time IS '更新时间';
COMMENT ON COLUMN form_template.remark IS '备注';
COMMENT ON COLUMN form_template.del_flag IS '删除标志（0代表存在 2代表删除）';

DROP TABLE IF EXISTS form_data;

CREATE TABLE form_data (
    data_id BIGSERIAL NOT NULL PRIMARY KEY,
    form_id BIGINT NOT NULL,
    form_version VARCHAR(10),
    data_content JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    del_flag CHAR(1) DEFAULT '0'
);

COMMENT ON TABLE form_data IS '表单数据表';
COMMENT ON COLUMN form_data.data_id IS '数据ID';
COMMENT ON COLUMN form_data.form_id IS '关联的表单ID';
COMMENT ON COLUMN form_data.form_version IS '表单版本（与模板表版本一致）';
COMMENT ON COLUMN form_data.data_content IS '表单数据内容（JSON格式）';
COMMENT ON COLUMN form_data.status IS '数据状态（draft, submitted, approved, rejected）';
COMMENT ON COLUMN form_data.create_by IS '创建者';
COMMENT ON COLUMN form_data.create_time IS '创建时间';
COMMENT ON COLUMN form_data.update_by IS '更新者';
COMMENT ON COLUMN form_data.update_time IS '更新时间';
COMMENT ON COLUMN form_data.remark IS '备注';
COMMENT ON COLUMN form_data.del_flag IS '删除标志（0代表存在 2代表删除）';

DO $$
DECLARE
    parentId INTEGER;
    fileParentId INTEGER;
BEGIN
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单管理', 0, 4, 'formManagement', NULL, NULL, '', 1, 0, 'M', '0', '0', NULL, 'form', 'admin', '2024-02-15 22:40:23', '', NULL, '')
    RETURNING menu_id INTO parentId;

    fileParentId := parentId;

    -- 菜单 SQL
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单模板', fileParentId, '1', 'formtemplate', 'form/template/index', 1, 0, 'C', '0', '0', 'form:template:list', '#', 'admin', CURRENT_TIMESTAMP, '', null, '表单模板菜单')
    RETURNING menu_id INTO parentId;

    -- 按钮 SQL
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单模板查询', parentId, '1', '#', '', 1, 0, 'F', '0', '0', 'form:template:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单模板新增', parentId, '2', '#', '', 1, 0, 'F', '0', '0', 'form:template:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单模板修改', parentId, '3', '#', '', 1, 0, 'F', '0', '0', 'form:template:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单模板删除', parentId, '4', '#', '', 1, 0, 'F', '0', '0', 'form:template:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单模板导出', parentId, '5', '#', '', 1, 0, 'F', '0', '0', 'form:template:export', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    -- 菜单 SQL
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单数据', fileParentId, '1', 'formdata', 'form/data/index', 1, 0, 'C', '0', '0', 'form:data:list', '#', 'admin', CURRENT_TIMESTAMP, '', null, '表单数据菜单')
    RETURNING menu_id INTO parentId;

    -- 按钮 SQL
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单数据查询', parentId, '1', '#', '', 1, 0, 'F', '0', '0', 'form:data:query', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单数据新增', parentId, '2', '#', '', 1, 0, 'F', '0', '0', 'form:data:add', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单数据修改', parentId, '3', '#', '', 1, 0, 'F', '0', '0', 'form:data:edit', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单数据删除', parentId, '4', '#', '', 1, 0, 'F', '0', '0', 'form:data:remove', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');

    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('表单数据导出', parentId, '5', '#', '', 1, 0, 'F', '0', '0', 'form:data:export', '#', 'admin', CURRENT_TIMESTAMP, '', null, '');
END $$;