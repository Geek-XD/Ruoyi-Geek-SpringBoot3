package com.ruoyi.common.core.domain.model;

import com.ruoyi.common.core.domain.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户登录对象
 * 
 * @author ruoyi
 */
@Schema(title = "用户登录对象")
public class LoginBody extends BaseEntity {
    /**
     * 用户名
     */
    @Schema(title = "用户名")
    private String username;

    /**
     * 用户密码
     */
    @Schema(title = "用户密码")
    private String password;

    /**
     * 手机号码
     */
    @Schema(title = "手机号码")
    private String phonenumber;

    /**
     * 邮箱
     */
    @Schema(title = "邮箱")
    private String email;

    /**
     * 验证码
     */
    @Schema(title = "验证码")
    private String code;

    /**
     * 唯一标识
     */
    @Schema(title = "唯一标识")
    private String uuid;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
