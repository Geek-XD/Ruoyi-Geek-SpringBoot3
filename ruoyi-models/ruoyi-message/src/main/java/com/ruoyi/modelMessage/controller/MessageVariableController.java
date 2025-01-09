package com.ruoyi.modelMessage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.modelMessage.domain.MessageVariable;
import com.ruoyi.modelMessage.service.IMessageVariableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 变量管理Controller
 * 
 * @author ruoyi
 * @date 2024-12-31
 */
@RestController
@RequestMapping("/modelMessage/variable")
@Tag(name = "【变量管理】管理")
public class MessageVariableController extends BaseController
{
    @Autowired
    private IMessageVariableService messageVariableService;

    /**
     * 查询变量管理列表
     */
    @Operation(summary = "查询变量管理列表")
    //@PreAuthorize("@ss.hasPermi('modelMessage:variable:list')")
    @GetMapping("/list")
    public TableDataInfo list(MessageVariable messageVariable)
    {
        startPage();
        List<MessageVariable> list = messageVariableService.selectMessageVariableList(messageVariable);
        return getDataTable(list);
    }

    /**
     * 导出变量管理列表
     */
    @Operation(summary = "导出变量管理列表")
    @PreAuthorize("@ss.hasPermi('modelMessage:variable:export')")
    @Log(title = "变量管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MessageVariable messageVariable)
    {
        List<MessageVariable> list = messageVariableService.selectMessageVariableList(messageVariable);
        ExcelUtil<MessageVariable> util = new ExcelUtil<MessageVariable>(MessageVariable.class);
        util.exportExcel(response, list, "变量管理数据");
    }

    /**
     * 获取变量管理详细信息
     */
    @Operation(summary = "获取变量管理详细信息")
    //@PreAuthorize("@ss.hasPermi('modelMessage:variable:query')")
    @GetMapping(value = "/{variableId}")
    public AjaxResult getInfo(@PathVariable("variableId") Long variableId)
    {
        return success(messageVariableService.selectMessageVariableByVariableId(variableId));
    }

    /**
     * 新增变量管理
     */
    @Operation(summary = "新增变量管理")
    //@PreAuthorize("@ss.hasPermi('modelMessage:variable:add')")
    @Log(title = "变量管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MessageVariable messageVariable)
    {
        return toAjax(messageVariableService.insertMessageVariable(messageVariable));
    }

    /**
     * 修改变量管理
     */
    @Operation(summary = "修改变量管理")
    //@PreAuthorize("@ss.hasPermi('modelMessage:variable:edit')")
    @Log(title = "变量管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MessageVariable messageVariable)
    {
        return toAjax(messageVariableService.updateMessageVariable(messageVariable));
    }

    /**
     * 删除变量管理
     */
    @Operation(summary = "删除变量管理")
    //@PreAuthorize("@ss.hasPermi('modelMessage:variable:remove')")
    @Log(title = "变量管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{variableIds}")
    public AjaxResult remove(@PathVariable( name = "variableIds" ) Long[] variableIds) 
    {
        for (Long variableId : variableIds) {
            // 获取变量信息
            MessageVariable variable = messageVariableService.selectMessageVariableByVariableId(variableId);
            if (variable == null) {
                throw new ServiceException("变量不存在！");
            }
            // 检查变量是否被模板使用
            if (messageVariableService.selectTemplateByVariableId(variable.getVariableName())) {
                throw new ServiceException("该变量已被模版使用，不能删除！");
            }
         }
        return toAjax(messageVariableService.deleteMessageVariableByVariableIds(variableIds));
    }

    /**
     * 查询角色信息
     */
    @Operation(summary = "查询变量")
    @GetMapping("/selectMessageVariable")
    @Anonymous
    public AjaxResult selectMessageVariable() {
       return success(messageVariableService.selectMessageVariable());
    }

    /**
     * 根据变量类型生成不同的变量内容
     */
    @Operation(summary = "根据变量类型生成不同变量内容")
    @GetMapping("/generate")
    public AjaxResult generateVariableContent(@RequestParam String variableType) {
        try {
            Object content = messageVariableService.generateVariableContent(variableType);
            return AjaxResult.success(content);
        } catch (Exception e) {
            return AjaxResult.error("生成变量内容失败！");
        }
    }
}
