package com.ruoyi.file.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.file.domain.SysFileInfo;
import com.ruoyi.file.service.ISysFileInfoService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 文件信息Controller
 * 
 * @author ruoyi
 * @date 2025-04-25
 */
@RestController
@RequestMapping("/file/info")
@Tag(name = "【文件信息】管理")
public class SysFileInfoController extends BaseController
{
    @Autowired
    private ISysFileInfoService sysFileInfoService;

    /**
     * 查询文件信息列表
     */
    @Operation(summary = "查询文件信息列表")
    @PreAuthorize("@ss.hasPermi('file:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysFileInfo sysFileInfo)
    {
        startPage();
        List<SysFileInfo> list = sysFileInfoService.selectSysFileInfoList(sysFileInfo);
        return getDataTable(list);
    }

    /**
     * 导出文件信息列表
     */
    @Operation(summary = "导出文件信息列表")
    @PreAuthorize("@ss.hasPermi('file:info:export')")
    @Log(title = "文件信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysFileInfo sysFileInfo)
    {
        List<SysFileInfo> list = sysFileInfoService.selectSysFileInfoList(sysFileInfo);
        ExcelUtil<SysFileInfo> util = new ExcelUtil<SysFileInfo>(SysFileInfo.class);
        util.exportExcel(response, list, "文件信息数据");
    }

    /**
     * 获取文件信息详细信息
     */
    @Operation(summary = "获取文件信息详细信息")
    @PreAuthorize("@ss.hasPermi('file:info:query')")
    @GetMapping(value = "/{fileId}")
    public AjaxResult getInfo(@PathVariable("fileId") Long fileId)
    {
        return success(sysFileInfoService.selectSysFileInfoByFileId(fileId));
    }

    /**
     * 新增文件信息
     */
    @Operation(summary = "新增文件信息")
    @PreAuthorize("@ss.hasPermi('file:info:add')")
    @Log(title = "文件信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysFileInfo sysFileInfo)
    {
        return toAjax(sysFileInfoService.insertSysFileInfo(sysFileInfo));
    }

    /**
     * 修改文件信息
     */
    @Operation(summary = "修改文件信息")
    @PreAuthorize("@ss.hasPermi('file:info:edit')")
    @Log(title = "文件信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysFileInfo sysFileInfo)
    {
        return toAjax(sysFileInfoService.updateSysFileInfo(sysFileInfo));
    }

    /**
     * 删除文件信息
     */
    @Operation(summary = "删除文件信息")
    @PreAuthorize("@ss.hasPermi('file:info:remove')")
    @Log(title = "文件信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fileIds}")
    public AjaxResult remove(@PathVariable( name = "fileIds" ) Long[] fileIds) 
    {
        return toAjax(sysFileInfoService.deleteSysFileInfoByFileIds(fileIds));
    }
}
