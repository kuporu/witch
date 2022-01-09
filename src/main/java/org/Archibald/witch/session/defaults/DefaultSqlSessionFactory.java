package org.Archibald.witch.session.defaults;

import org.Archibald.witch.session.Configuration;
import org.Archibald.witch.session.SqlSession;
import org.Archibald.witch.session.SqlSessionFactory;

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
