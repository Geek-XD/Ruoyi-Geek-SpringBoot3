package com.geek.framework.mybatis;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.geek.common.core.domain.BaseEntity;
import com.github.pagehelper.PageInterceptor;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.FlexGlobalConfig.KeyConfig;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.dialect.DialectFactory;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;

@Configuration
@Import(PageInterceptor.class)
public class MybatisFlexConfig implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig flexGlobalConfig) {
        DbType.all().forEach(dbtype -> {
            DialectFactory.registerDialect(dbtype, DataScopeDialectImpl.createDialect(dbtype));
        });
        BaseEntityListener baseEntityListener = new BaseEntityListener();
        flexGlobalConfig.registerInsertListener(baseEntityListener, BaseEntity.class);
        flexGlobalConfig.registerUpdateListener(baseEntityListener, BaseEntity.class);
        flexGlobalConfig.setLogicDeleteColumn("del_flag");
        KeyConfig keyConfig = new KeyConfig();
        keyConfig.setKeyType(KeyType.Auto);
        flexGlobalConfig.setKeyConfig(keyConfig);
    }
}