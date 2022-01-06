package org.Archibald.medator.impl;

import org.Archibald.medator.Configuration;
import org.Archibald.medator.SqlSession;
import org.Archibald.medator.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {
    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }
}
