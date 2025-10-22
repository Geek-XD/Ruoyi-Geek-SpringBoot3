package com.ruoyi.atomikos.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;

import jakarta.annotation.PreDestroy;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

/**
 * JTA 事务配置
 *
 * @author ruoyi
 */
@Configuration
@ConditionalOnProperty(name = "atomikos.enabled", havingValue = "true")
public class AtomikosConfig {
    @Bean(name = "userTransaction")
    public UserTransaction userTransaction() throws Throwable {
        UserTransaction userTransaction = new UserTransactionImp();
        // 设置事务超时时间为10000毫秒
        userTransaction.setTransactionTimeout(10000);
        return userTransaction;
    }

    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    public TransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        // 设置是否强制关闭事务管理器为false
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    @Bean(name = "transactionManager")
    @DependsOn({ "userTransaction", "atomikosTransactionManager" })
    public PlatformTransactionManager transactionManager() throws Throwable {
        UserTransaction userTransaction = userTransaction();
        TransactionManager atomikosTransactionManager = atomikosTransactionManager();
        return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
    }

    private List<AtomikosDataSourceBean> atomikosDataSourceBeans = new ArrayList<>();

    public List<AtomikosDataSourceBean> getAtomikosDataSourceBeans() {
        return atomikosDataSourceBeans;
    }

    @PreDestroy
    public void destroy() {
        for (AtomikosDataSourceBean aDataSourceBean : this.atomikosDataSourceBeans) {
            aDataSourceBean.close();
        }
    }
}