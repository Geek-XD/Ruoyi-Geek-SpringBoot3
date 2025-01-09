package com.ruoyi.modelMessage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.ruoyi.modelMessage.domain.MessageSystem;
import com.ruoyi.modelMessage.domain.MessageTemplate;
import com.ruoyi.modelMessage.domain.vo.SysDeptVo;
import com.ruoyi.modelMessage.domain.vo.SysRoleVo;
import com.ruoyi.modelMessage.domain.vo.SysUserVo;

/**
 * 消息管理Mapper接口
 * 
 * @author ruoyi
 * @date 2024-12-21
 */
public interface MessageSystemMapper 
{
    /**
     * 查询消息管理
     * 
     * @param messageId 消息管理主键
     * @return 消息管理
     */
    public MessageSystem selectMessageSystemByMessageId(Long messageId);

    /**
     * 查询消息管理列表
     * 
     * @param messageSystem 消息管理
     * @return 消息管理集合
     */
    public List<MessageSystem> selectMessageSystemList(MessageSystem messageSystem);

    /**
     * 新增消息管理
     * 
     * @param messageSystem 消息管理
     * @return 结果
     */
    public int insertMessageSystem(MessageSystem messageSystem);

    /**
     * 修改消息管理
     * 
     * @param messageSystem 消息管理
     * @return 结果
     */
    public int updateMessageSystem(MessageSystem messageSystem);

    /**
     * 删除消息管理
     * 
     * @param messageId 消息管理主键
     * @return 结果
     */
    public int deleteMessageSystemByMessageId(Long messageId);

    /**
     * 批量删除消息管理
     * 
     * @param messageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMessageSystemByMessageIds(Long[] messageIds);

    //查询平台系统资源收件人用户信息
    @Select("SELECT * FROM `sys_user`")
    public List<SysUserVo> selectUser();

    //查询角色信息 然后根据角色把消息发给某角色
    @Select("SELECT * FROM `sys_role`")
    public List<SysRoleVo> selectRole();

    //查询部门信息 然后根据部门把消息发给某部门
    @Select("SELECT * FROM `sys_dept`")
    public List<SysDeptVo> selectDept();

    // 根据发送方式过滤用户 (短信或邮箱)
    @Select("<script>" +
        "SELECT * FROM `sys_user`" +
        "<where>" +
            "<if test='filterType == \"phone\"'>AND phonenumber IS NOT NULL AND phonenumber != ''</if>" +
            "<if test='filterType == \"email\"'>AND email IS NOT NULL AND email != ''</if>" +
        "</where>" +
        "</script>")
    List<SysUserVo> selectUserBySendMode(String filterType);

    //将信息状态未读信息变为已读
    @Update("update message_system set message_status = 1 where message_id = #{messageId} and message_recipient = #{userName}")
    public int updateState(Long messageId, String userName);

    
     /**
     * 根据部门ID获取所有符合条件的用户信息。
     *
     * @param deptId 部门ID
     * @return 用户信息列表
     */
    @Select("SELECT u.* FROM sys_user u JOIN sys_dept d ON u.dept_id = d.dept_id WHERE d.dept_id = #{deptId}")
    public List<SysUserVo> getUserNameByDeptId(Long roleId);

    /**
     * 根据角色ID查询用户列表。
     *
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Select("SELECT u.* FROM sys_user u JOIN sys_user_role ur ON u.user_id = ur.user_id WHERE ur.role_id = #{roleId}")
    public List<SysUserVo> selectUsersByRoleId(Long roleId);

    //查询模版签名
    @Select("SELECT template_id,template_code,template_variable FROM `message_template`")
    public List<MessageTemplate> selecTemplates();

    //批量发送信息
    public int batchInsertMessageSystem(List<MessageSystem> messageSystemList);
}
