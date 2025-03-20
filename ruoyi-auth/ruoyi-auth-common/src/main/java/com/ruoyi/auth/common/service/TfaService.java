package com.ruoyi.auth.common.service;

import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.RegisterBody;

public interface TfaService {
    public void doBind(LoginBody loginBody);

    public void doBindVerify(LoginBody loginBody);

    public void doRegister(RegisterBody registerBody);

    public void doRegisterVerify(RegisterBody registerBody);

    public void doLogin(LoginBody loginBody);

    public String doLoginVerify(LoginBody loginBody);
}
