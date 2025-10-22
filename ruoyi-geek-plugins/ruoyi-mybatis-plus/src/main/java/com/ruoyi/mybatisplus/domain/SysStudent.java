package com.ruoyi.mybatisplus.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 学生信息对象 sys_student
 * 
 * @author ruoyi
 */
@TableName(value = "sys_student")
public class SysStudent implements Serializable
{
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /** 编号 */
    @TableId(type = IdType.AUTO)
    private Long studentId;

    /** 学生名称 */
    @Excel(name = "学生名称")
    private String studentName;

    /** 年龄 */
    @Excel(name = "年龄")
    private Integer studentAge;

    /** 爱好（0代码 1音乐 2电影） */
    @Excel(name = "爱好", readConverterExp = "0=代码,1=音乐,2=电影")
    private String studentHobby;

    /** 性别（0男 1女 2未知） */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String studentSex;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String studentStatus;

    /** 生日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生日", width = 30, dateFormat = "yyyy-MM-dd")
    private Date studentBirthday;

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }
    public void setStudentName(String studentName) 
    {
        this.studentName = studentName;
    }

    public String getStudentName() 
    {
        return studentName;
    }
    public void setStudentAge(Integer studentAge) 
    {
        this.studentAge = studentAge;
    }

    public Integer getStudentAge() 
    {
        return studentAge;
    }
    public void setStudentHobby(String studentHobby) 
    {
        this.studentHobby = studentHobby;
    }

    public String getStudentHobby() 
    {
        return studentHobby;
    }
    public void setStudentSex(String studentSex) 
    {
        this.studentSex = studentSex;
    }

    public String getStudentSex() 
    {
        return studentSex;
    }
    public void setStudentStatus(String studentStatus) 
    {
        this.studentStatus = studentStatus;
    }

    public String getStudentStatus() 
    {
        return studentStatus;
    }
    public void setStudentBirthday(Date studentBirthday) 
    {
        this.studentBirthday = studentBirthday;
    }

    public Date getStudentBirthday() 
    {
        return studentBirthday;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("studentId", getStudentId())
            .append("studentName", getStudentName())
            .append("studentAge", getStudentAge())
            .append("studentHobby", getStudentHobby())
            .append("studentSex", getStudentSex())
            .append("studentStatus", getStudentStatus())
            .append("studentBirthday", getStudentBirthday())
            .toString();
    }
}