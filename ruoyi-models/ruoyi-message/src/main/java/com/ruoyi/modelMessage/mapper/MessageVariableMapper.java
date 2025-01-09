package com.ruoyi.modelMessage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.ruoyi.modelMessage.domain.MessageVariable;

/**
 * 变量管理Mapper接口
 * 
 * @author ruoyi
 * @date 2024-12-31
 */
public interface MessageVariableMapper 
{
    /**
     * 查询变量管理
     * 
     * @param variableId 变量管理主键
     * @return 变量管理
     */
    public MessageVariable selectMessageVariableByVariableId(Long variableId);

    /**
     * 查询变量管理列表
     * 
     * @param messageVariable 变量管理
     * @return 变量管理集合
     */
    public List<MessageVariable> selectMessageVariableList(MessageVariable messageVariable);

    /**
     * 新增变量管理
     * 
     * @param messageVariable 变量管理
     * @return 结果
     */
    public int insertMessageVariable(MessageVariable messageVariable);

    /**
     * 修改变量管理
     * 
     * @param messageVariable 变量管理
     * @return 结果
     */
    public int updateMessageVariable(MessageVariable messageVariable);

    /**
     * 删除变量管理
     * 
     * @param variableId 变量管理主键
     * @return 结果
     */
    public int deleteMessageVariableByVariableId(Long variableId);

    /**
     * 批量删除变量管理
     * 
     * @param variableIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMessageVariableByVariableIds(Long[] variableIds);

    //删除变量之前检查一下有没有模版使用
    @Select("SELECT COUNT(*) > 0 FROM message_template WHERE template_variable = #{templateVariable}")
    public boolean selectTemplateByVariableId(String templateVariable);

    //查询变量
    @Select("SELECT * FROM message_variable")
    public List<MessageVariable> selectMessageVariable();

    //查询在使用模版签名时用到了那些变量一一赋值
    public List<MessageVariable> selectMessageVariables(List<String> variableNames);
}
