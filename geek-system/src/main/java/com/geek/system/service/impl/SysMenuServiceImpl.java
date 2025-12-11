package com.geek.system.service.impl;

import static com.geek.common.core.domain.entity.table.SysMenuTableDef.*;
import static com.geek.system.domain.table.SysRoleMenuTableDef.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geek.common.constant.Constants;
import com.geek.common.constant.UserConstants;
import com.geek.common.core.domain.TreeSelect;
import com.geek.common.core.domain.entity.SysMenu;
import com.geek.common.core.domain.entity.SysRole;
import com.geek.common.core.domain.entity.SysUser;
import com.geek.common.utils.SecurityUtils;
import com.geek.common.utils.StringUtils;
import com.geek.system.domain.SysRoleMenu;
import com.geek.system.domain.SysUserRole;
import com.geek.system.domain.vo.MetaVo;
import com.geek.system.domain.vo.RouterVo;
import com.geek.system.mapper.SysMenuMapper;
import com.geek.system.mapper.SysRoleMapper;
import com.geek.system.service.ISysMenuService;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

/**
 * 菜单 业务层处理
 * 
 * @author geek
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 根据用户查询系统菜单列表
     * 
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(Long userId) {
        return selectMenuList(new SysMenu(), userId);
    }

    /**
     * 查询系统菜单列表
     * 
     * @param menu 菜单信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId) {
        QueryChain<SysMenu> query = this.queryChain()
                .like(SysMenu::getMenuName, menu.getMenuName())
                .eq(SysMenu::getVisible, menu.getVisible())
                .eq(SysMenu::getStatus, menu.getStatus())
                .orderBy(SysMenu::getParentId, true)
                .orderBy(SysMenu::getOrderNum, true);
        // 管理员显示所有菜单信息
        if (!SysUser.isAdmin(userId)) {
            query.leftJoin(SysRoleMenu.class)
                    .on(SysMenu::getMenuId, SysRoleMenu::getMenuId)
                    .leftJoin(SysUserRole.class)
                    .on(SysRoleMenu::getRoleId, SysUserRole::getRoleId)
                    .leftJoin(SysRole.class)
                    .on(SysUserRole::getRoleId, SysRole::getRoleId)
                    .eq(SysUserRole::getUserId, userId);
        }
        return query.list();
    }

    /**
     * 根据用户ID查询权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        List<String> perms = this.queryChain()
                .select(QueryMethods.distinct(SysMenu::getPerms))
                .leftJoin(SysRoleMenu.class)
                .on(SysMenu::getMenuId, SysRoleMenu::getMenuId)
                .leftJoin(SysUserRole.class)
                .on(SysRoleMenu::getRoleId, SysUserRole::getRoleId)
                .leftJoin(SysRole.class)
                .on(SysUserRole::getRoleId, SysRole::getRoleId)
                .eq(SysMenu::getStatus, QueryMethods.string("0"))
                .eq(SysRole::getStatus, QueryMethods.string("0"))
                .eq(SysUserRole::getUserId, userId)
                .listAs(String.class);

        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId) {
        List<String> perms = QueryChain.of(SysMenu.class)
                .select(QueryMethods.distinct(SysMenu::getPerms))
                .leftJoin(SysRoleMenu.class)
                .on(SysRoleMenu::getMenuId, SysMenu::getMenuId)
                .eq(SysMenu::getStatus, QueryMethods.string("0"))
                .eq(SysRoleMenu::getRoleId, roleId)
                .listAs(String.class);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据用户ID查询菜单
     * 
     * @param userId 用户名称
     * @return 菜单列表
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId) {

        QueryChain<SysMenu> qc = this.queryChain();
        if (!SecurityUtils.isAdmin(userId)) {
            qc.leftJoin(SysRoleMenu.class)
                    .on(SysRoleMenu::getMenuId, SysMenu::getMenuId)
                    .leftJoin(SysUserRole.class)
                    .on(SysRoleMenu::getRoleId, SysUserRole::getRoleId)
                    .leftJoin(SysRole.class)
                    .on(SysUserRole::getRoleId, SysRole::getRoleId)
                    .leftJoin(SysUser.class)
                    .on(SysUserRole::getUserId, SysUser::getUserId)
                    .eq(SysUser::getUserId, userId)
                    .eq(SysRole::getStatus, "0");
        }
        List<SysMenu> menus = qc.in(SysMenu::getMenuType, Arrays.asList("M", "C"))
                .eq(SysMenu::getStatus, "0")
                .orderBy(SysMenu::getParentId, true)
                .orderBy(SysMenu::getOrderNum, true)
                .list();
        return getChildPerms(menus, 0);
    }

    /**
     * 根据角色ID查询菜单树信息
     * 
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        SysRole role = LogicDeleteManager.execWithoutLogicDelete(() -> roleMapper.baseQueryChain()
                .eq(SysRole::getRoleId, roleId)
                .one());
        QueryChain<SysMenu> queryChain = this.queryChain()
                .from(SYS_MENU)
                .select(SYS_MENU.MENU_ID)
                .leftJoin(SYS_ROLE_MENU)
                .on(SYS_MENU.MENU_ID.eq(SYS_ROLE_MENU.MENU_ID))
                .where(SYS_ROLE_MENU.ROLE_ID.eq(roleId));
        if (role.isMenuCheckStrictly()) {
            queryChain.and(SYS_MENU.MENU_ID.notIn(
                    QueryWrapper.create()
                            .from(SYS_MENU)
                            .select(SYS_MENU.PARENT_ID)
                            .innerJoin(SYS_ROLE_MENU)
                            .on(SYS_MENU.MENU_ID.eq(SYS_ROLE_MENU.MENU_ID)
                                    .and(SYS_ROLE_MENU.ROLE_ID.eq(roleId)))));
        }
        queryChain.orderBy(SYS_MENU.PARENT_ID.asc(), SYS_MENU.ORDER_NUM.asc());
        return queryChain.listAs(Long.class);
    }

    /**
     * 构建前端路由所需要的菜单
     * 
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()),
                    menu.getPath()));
            List<SysMenu> cMenus = menu.getChildren();
            if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (isMenuFrame(menu)) {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(getRouteName(menu.getRouteName(), menu.getPath()));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(),
                        StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().intValue() == 0 && isInnerLink(menu)) {
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(getRouteName(menu.getRouteName(), routerPath));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要树结构
     * 
     * @param menus 菜单列表
     * @return 树结构列表
     */
    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        List<Long> tempList = menus.stream().map(SysMenu::getMenuId).collect(Collectors.toList());
        for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext();) {
            SysMenu menu = (SysMenu) iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.getParentId())) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus) {
        List<SysMenu> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 是否存在菜单子节点
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return this.queryChain()
                .eq(SysMenu::getParentId, menuId)
                .exists();
    }

    /**
     * 查询菜单使用数量
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        return QueryChain.of(SysRoleMenu.class)
                .eq(SysRoleMenu::getMenuId, menuId)
                .exists();
    }

    /**
     * 校验菜单名称是否唯一
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenu menu) {
        return !this.queryChain()
                .eq(SysMenu::getMenuName, menu.getMenuName())
                .eq(SysMenu::getParentId, menu.getParentId())
                .ne(SysMenu::getMenuId, menu.getMenuId())
                .exists();
    }

    /**
     * 获取路由名称
     * 
     * @param menu 菜单信息
     * @return 路由名称
     */
    public String getRouteName(SysMenu menu) {
        // 非外链并且是一级目录（类型为目录）
        if (isMenuFrame(menu)) {
            return StringUtils.EMPTY;
        }
        return getRouteName(menu.getRouteName(), menu.getPath());
    }

    /**
     * 获取路由名称，如没有配置路由名称则取路由地址
     * 
     * @param routerName 路由名称
     * @param path       路由地址
     * @return 路由名称（驼峰格式）
     */
    public String getRouteName(String name, String path) {
        String routerName = StringUtils.isNotEmpty(name) ? name : path;
        return StringUtils.capitalize(routerName);
    }

    /**
     * 获取路由地址
     * 
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = menu.getPath();
        // 内链打开外网方式
        if (menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        if (0 == menu.getParentId().intValue() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame())) {
            routerPath = "/" + menu.getPath();
        }
        // 非外链并且是一级目录（类型为菜单）
        else if (isMenuFrame(menu)) {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     * 
     * @param menu 菜单信息
     * @return 组件信息
     */
    public String getComponent(SysMenu menu) {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
            component = menu.getComponent();
        } else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0
                && isInnerLink(menu)) {
            component = UserConstants.INNER_LINK;
        } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为菜单内部跳转
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isMenuFrame(SysMenu menu) {
        return menu.getParentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    /**
     * 是否为内链组件
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isInnerLink(SysMenu menu) {
        return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.ishttp(menu.getPath());
    }

    /**
     * 是否为parent_view组件
     * 
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(SysMenu menu) {
        return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 根据父节点的ID获取所有子节点
     * 
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext();) {
            SysMenu t = (SysMenu) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     * 
     * @param list 分类表
     * @param t    子节点
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 内链域名特殊字符替换
     * 
     * @return 替换后的内链域名
     */
    public String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(path, new String[] { Constants.HTTP, Constants.HTTPS, Constants.WWW, "." },
                new String[] { "", "", "", "/" });
    }
}
