package com.ruoyi.mybatisplus.service.impl;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mybatisplus.domain.SysStudent;
import com.ruoyi.mybatisplus.mapper.SysStudentMapper;
import com.ruoyi.mybatisplus.service.ISysStudentService;

/**
 * 学生信息Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysStudentServiceImpl extends ServiceImpl<SysStudentMapper, SysStudent> implements ISysStudentService
{
    @Override
    public List<SysStudent> queryList(SysStudent sysStudent)
    {
        // 注意：mybatis-plus lambda 模式不支持 eclipse 的编译器
        // LambdaQueryWrapper<SysStudent> queryWrapper = Wrappers.lambdaQuery();
        // queryWrapper.eq(SysStudent::getStudentName, sysStudent.getStudentName());
        QueryWrapper<SysStudent> queryWrapper = Wrappers.query();
        if (StringUtils.isNotEmpty(sysStudent.getStudentName()))
        {
            queryWrapper.eq("student_name", sysStudent.getStudentName());
        }
        if (StringUtils.isNotNull(sysStudent.getStudentAge()))
        {
            queryWrapper.eq("student_age", sysStudent.getStudentAge());
        }
        if (StringUtils.isNotEmpty(sysStudent.getStudentHobby()))
        {
            queryWrapper.eq("student_hobby", sysStudent.getStudentHobby());
        }
        return this.list(queryWrapper);
    }
}