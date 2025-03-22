package com.ruoyi.modelMessage.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.modelMessage.domain.MessageSystem;
import com.ruoyi.modelMessage.domain.MessageTemplate;
import com.ruoyi.modelMessage.enums.SendMode;
import com.ruoyi.modelMessage.mapper.MessageSystemMapper;
import com.ruoyi.modelMessage.service.IMessageSystemService;
import com.ruoyi.modelMessage.utils.MessageSystemUtils;
import com.ruoyi.tfa.email.utils.EmailUtil;
import com.ruoyi.tfa.phone.config.DySmsConfig;
import com.ruoyi.tfa.phone.domain.DySmsTemplate;
import com.ruoyi.tfa.phone.utils.DySmsUtil;

/**
 * 消息管理Service业务层处理
 * 
 */
@Service
public class MessageSystemServiceImpl implements IMessageSystemService {

    private static final Logger log = LoggerFactory.getLogger(MessageSystemServiceImpl.class);

    @Autowired
    private DySmsConfig dySmsConfig;

    @Autowired
    private MessageSystemMapper messageSystemMapper;

    @Autowired
    private MessageSystemUtils messageSystemUtils;

    /**
     * 查询消息管理
     * 
     * @param messageId 消息管理主键
     * @return 消息管理
     */
    @Override
    public MessageSystem selectMessageSystemByMessageId(Long messageId) {
        return messageSystemMapper.selectMessageSystemByMessageId(messageId);
    }

    /**
     * 查询消息管理列表
     * 
     * @param messageSystem 消息管理
     * @return 消息管理列表
     */
    @Override
    public List<MessageSystem> selectMessageSystemList(MessageSystem messageSystem) {
        return messageSystemMapper.selectMessageSystemList(messageSystem);
    }

    /**
     * 新增消息管理
     * 
     * @param messageSystem 消息管理
     * @return 结果
     */
    @Override
    public int insertMessageSystem(MessageSystem messageSystem) {
        messageSystem.setMessageStatus("0"); // 默认发送信息为未读状态
        messageSystem.setCreateBy(SecurityUtils.getUsername());
        messageSystem.setUpdateBy(SecurityUtils.getUsername());
        messageSystem.setCreateTime(DateUtils.getNowDate());
        messageSystem.setUpdateTime(DateUtils.getNowDate());
        return messageSystemMapper.insertMessageSystem(messageSystem);
    }

    /**
     * 修改消息管理
     * 
     * @param messageSystem 消息管理
     * @return 结果
     */
    @Override
    public int updateMessageSystem(MessageSystem messageSystem) {
        messageSystem.setUpdateTime(DateUtils.getNowDate());
        return messageSystemMapper.updateMessageSystem(messageSystem);
    }

    /**
     * 批量删除消息管理
     * 
     * @param messageIds 需要删除的消息管理主键
     * @return 结果
     */
    @Override
    public int deleteMessageSystemByMessageIds(Long[] messageIds) {
        return messageSystemMapper.deleteMessageSystemByMessageIds(messageIds);
    }

    /**
     * 删除消息管理信息
     * 
     * @param messageId 消息管理主键
     * @return 结果
     */
    @Override
    public int deleteMessageSystemByMessageId(Long messageId) {
        return messageSystemMapper.deleteMessageSystemByMessageId(messageId);
    }

    // 查询系统资源用户信息
    @Override
    public List<SysUser> selectUser() {
        return messageSystemMapper.selectUser();
    }

    // 收件人为本人的话点击信息详情的时候然后把状态未读信息修改为已读
    @Override
    public int updateState(Long messageId) {
        int result = messageSystemMapper.updateState(messageId, SecurityUtils.getUsername());
        return result;
    }

    // 根据发送方式过滤用户 (短信或邮箱)
    @Override
    public List<SysUser> getUsersFilteredBySendMode(String filterType) {
        return messageSystemMapper.selectUserBySendMode(filterType);
    }

    // 查询角色信息 然后根据角色把消息发给某角色
    @Override
    public List<SysRole> selectRole() {
        return messageSystemMapper.selectRole();
    }

    // 查询部门信息 然后根据部门把消息发给某部门
    @Override
    public List<SysDept> selectDept() {
        return messageSystemMapper.selectDept();
    }

    /**
     * 根据角色ID获取对应用户信息。
     *
     * @param roleId 角色ID
     * @return 符合条件的用户列表
     */
    @Override
    public List<SysUser> selectUsersByRoleId(Long roleId) {
        List<SysUser> roleList = messageSystemMapper.selectUsersByRoleId(roleId);
        return roleList;
    }

    /**
     * 根据角色ID获取用户ID列表。
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @Override
    public List<SysUser> getUserNameByDeptId(Long deptId) {
        List<SysUser> depts= messageSystemMapper.getUserNameByDeptId(deptId);
        return depts;
    }

    // 查询模版签名
    @Override
    public List<MessageTemplate> selecTemplates() {
        List<MessageTemplate> templates= messageSystemMapper.selecTemplates();
        return templates;
    }

    /**
     * 批量发送信息
     */
    @Override
    public int batchInsertMessageSystem(List<MessageSystem> messageSystemList) {
        String username = SecurityUtils.getUsername();
        Date nowDate = DateUtils.getNowDate();
        for (MessageSystem messageSystem : messageSystemList) {
            messageSystem.setMessageStatus("0"); // 默认发送信息为未读状态
            messageSystem.setCreateBy(username);
            messageSystem.setUpdateBy(username);
            messageSystem.setCreateTime(nowDate);
            messageSystem.setUpdateTime(nowDate);
        }
        int result = messageSystemMapper.batchInsertMessageSystem(messageSystemList);
        if ( result <= 0) {
            throw new ServiceException("消息发送失败！");
        }
        return result;
    }

    /**
     * 根据 MessageSystem 对象的 sendMode 属性处理消息的发送方式
     */
    @Override
    public void processMessageSystem(MessageSystem messageSystem) {
        if (messageSystem == null || messageSystem.getSendMode() == null) {
            throw new ServiceException("无效的消息系统对象或发送方式！");
        }
        String sendModeStr = messageSystem.getSendMode();
        SendMode sendMode;
        try {
            sendMode = SendMode.fromCode(sendModeStr);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("类型转换失败: " + sendModeStr);
        }
        switch (sendMode) {
            case PHONE:
                sendNotificationMessage(messageSystem);
                break;
            case EMAIL:
                handleEmailNotification(messageSystem);
                break;
            case PLATFORM:
                sendPlatformMessage(messageSystem);
                break;
            default:
                throw new ServiceException("未知的发送方式！");
        }
    }

    /**
     * 发送平台消息
     */
    public void sendPlatformMessage(MessageSystem messageSystem) {
        String notificationContent = messageSystem.getMessageContent();
        try {
            String filledMessage = notificationContent.startsWith("SMS_") ?
            messageSystemUtils.processTemplateMessage(messageSystem, notificationContent) :
            notificationContent; // 是自定义输入，使用用户输入的内容
            log.info("平台内容: {}", filledMessage);
            messageSystem.setMessageContent(filledMessage);
        } catch (Exception e) {
            log.error("发送平台消息时发生异常: ", e);
            throw new ServiceException("发送平台消息异常：" + e.getMessage());
        }
    }

    /**
     * 发送邮件通知
     */
    public void handleEmailNotification(MessageSystem messageSystem) {
        String email = messageSystem.getCode();
        if (StringUtils.isEmpty(email)) {
            throw new ServiceException("邮箱地址不能为空！");
        }
        try {
            String filledMessage = messageSystem.getMessageContent().startsWith("SMS_") ?
            messageSystemUtils.processTemplateMessage(messageSystem, messageSystem.getMessageContent()) :
            messageSystem.getMessageContent(); // 是自定义输入，则直接使用用户提供的内容
            log.info("邮件内容: {}", filledMessage);
            messageSystem.setMessageContent(filledMessage);
            EmailUtil.sendMessage(email, "通知", filledMessage);
        } catch (Exception e) {
            log.error("发送邮件时发生异常: ", e);
            throw new ServiceException("发送通知信息异常：" + e.getMessage());
        }
    }

    /**
     * 发送短信通知
     */
    @SuppressWarnings("unchecked")
    public void sendNotificationMessage(MessageSystem messageSystem) {
        String phone = messageSystem.getCode();
        if (StringUtils.isEmpty(phone)) {
            throw new ServiceException("手机号码为空！");
        }
        try {
            // 解析并处理模板消息
            Map<String, Object> parsedParams = messageSystemUtils.parseInput(messageSystem.getMessageContent());
            String templateCode = (String) parsedParams.get("templateCode");
            DySmsTemplate dySmsTemplate = null;
            if (templateCode != null) {
                dySmsTemplate = dySmsConfig.getTemplate().get(templateCode);
                Map<String, String> params = (Map<String, String>) parsedParams.get("params");
                // 提取模板中的变量名并填充内置变量
                List<String> variableNames = (List<String>) parsedParams.get("variableNames");
                messageSystemUtils.fillBuiltInVariables(params, messageSystem, variableNames);
                String filledMessage = messageSystemUtils.fillTemplate((String) parsedParams.get("templateContent"), params);
                messageSystem.setMessageContent(filledMessage);
                JSONObject templateParamJson = new JSONObject(params);
                DySmsUtil.sendSms(dySmsTemplate, templateParamJson, phone);
            } else {
                DySmsUtil.sendSms(null, null, phone);
            }
        } catch (Exception e) {
            log.error("发送短信时发生异常: ", e);
            throw new ServiceException("发送短信异常：" + e.getMessage());
        }
    }
}
