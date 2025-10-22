package com.ruoyi.mybatisplus.service;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.mybatisplus.domain.SysStudent;

/**
 * 学生信息Service接口
 * 
 * @author ruoyi
 */
public interface ISysStudentService extends IService<SysStudent>
{
    /**
     * 查询学生信息列表
     * 
     * @param sysStudent 学生信息
     * @return 学生信息集合
     */
    public List<SysStudent> queryList(SysStudent sysStudent);
}