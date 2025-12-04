package com.ruoyi.framework.mybatis;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;

public class BaseEntityListener implements InsertListener, UpdateListener {

    @Override
    public void onUpdate(Object arg0) {
        if (!(arg0 instanceof BaseEntity)) {
            return;
        }
        BaseEntity entity = (BaseEntity) arg0;
        entity.setUpdateTime(DateUtils.getNowDate());
        if (!SecurityUtils.isAnonymous()) {
            entity.setUpdateBy(null);
        } else {
            entity.setUpdateBy(SecurityUtils.getUsername());
        }
    }

    @Override
    public void onInsert(Object arg0) {
        if (!(arg0 instanceof BaseEntity)) {
            return;
        }
        BaseEntity entity = (BaseEntity) arg0;
        entity.setCreateTime(DateUtils.getNowDate());
        if (SecurityUtils.isAnonymous()) {
            entity.setCreateBy(null);
        } else {
            entity.setCreateBy(SecurityUtils.getUsername());
        }
    }

}
