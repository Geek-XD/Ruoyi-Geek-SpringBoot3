-- ----------------------------
-- 1、oauth_user 表
-- ----------------------------
DROP TABLE IF EXISTS oauth_user CASCADE;

CREATE TABLE oauth_user (
    id bigserial NOT NULL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    source VARCHAR(255) NOT NULL,
    access_token VARCHAR(255) NOT NULL,
    expire_in INT,
    refresh_token VARCHAR(255),
    open_id VARCHAR(255),
    uid VARCHAR(255),
    access_code VARCHAR(255),
    union_id VARCHAR(255),
    scope VARCHAR(255),
    token_type VARCHAR(255),
    id_token VARCHAR(255),
    mac_algorithm VARCHAR(255),
    mac_key VARCHAR(255),
    code VARCHAR(255),
    oauth_token VARCHAR(255),
    oauth_token_secret VARCHAR(255)
);

COMMENT ON TABLE oauth_user IS '第三方登录';
COMMENT ON COLUMN oauth_user.id IS '主键';
COMMENT ON COLUMN oauth_user.uuid IS '第三方系统的唯一ID，详细解释请参考：名词解释';
COMMENT ON COLUMN oauth_user.user_id IS '用户ID';
COMMENT ON COLUMN oauth_user.source IS '第三方用户来源，可选值：GITHUB、GITEE、QQ，更多请参考：AuthDefaultSource.java(opens new window)';
COMMENT ON COLUMN oauth_user.access_token IS '用户的授权令牌';
COMMENT ON COLUMN oauth_user.expire_in IS '第三方用户的授权令牌的有效期，部分平台可能没有';
COMMENT ON COLUMN oauth_user.refresh_token IS '刷新令牌，部分平台可能没有';
COMMENT ON COLUMN oauth_user.open_id IS '第三方用户的 open id，部分平台可能没有';
COMMENT ON COLUMN oauth_user.uid IS '第三方用户的 ID，部分平台可能没有';
COMMENT ON COLUMN oauth_user.access_code IS '个别平台的授权信息，部分平台可能没有';
COMMENT ON COLUMN oauth_user.union_id IS '第三方用户的 union id，部分平台可能没有';
COMMENT ON COLUMN oauth_user.scope IS '第三方用户授予的权限，部分平台可能没有';
COMMENT ON COLUMN oauth_user.token_type IS '个别平台的授权信息，部分平台可能没有';
COMMENT ON COLUMN oauth_user.id_token IS 'id token，部分平台可能没有';
COMMENT ON COLUMN oauth_user.mac_algorithm IS '小米平台用户的附带属性，部分平台可能没有';
COMMENT ON COLUMN oauth_user.mac_key IS '小米平台用户的附带属性，部分平台可能没有';
COMMENT ON COLUMN oauth_user.code IS '用户的授权code，部分平台可能没有';
COMMENT ON COLUMN oauth_user.oauth_token IS 'Twitter平台用户的附带属性，部分平台可能没有';
COMMENT ON COLUMN oauth_user.oauth_token_secret IS 'Twitter平台用户的附带属性，部分平台可能没有';

-- ----------------------------
-- 菜单 SQL
-- ----------------------------
SELECT setval('sys_menu_menu_id_seq', max(menu_id)) FROM sys_menu WHERE menu_id < 100;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('第三方认证', 1, 1, 'oauth', 'system/oauth/index', '', 1, 0, 'C', '0', '0', 'system:oauth:list', 'checkbox', 'admin', CURRENT_TIMESTAMP, '', NULL, '第三方认证菜单');

-- 按钮父菜单ID
DO $$
DECLARE
    parentId INTEGER;
BEGIN
    SELECT LASTVAL() INTO parentId;
    
    -- 按钮 SQL
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('第三方认证查询', parentId, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:oauth:query', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('第三方认证新增', parentId, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:oauth:add', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('第三方认证修改', parentId, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:oauth:edit', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('第三方认证删除', parentId, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:oauth:remove', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
    
    INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
    VALUES ('第三方认证导出', parentId, 5, '#', '', '', 1, 0, 'F', '0', '0', 'system:oauth:export', '#', 'admin', CURRENT_TIMESTAMP, '', NULL, '');
END $$;