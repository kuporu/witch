package org.Archibald.binding;

import org.Archibald.mapping.SqlCommandType;
import org.Archibald.medator.Configuration;
import org.Archibald.medator.SqlContext;
import org.Archibald.medator.SqlSession;

import java.lang.reflect.Method;

public class MapperMethod {
    private final MapperMethod.Command command;

    public MapperMethod(Configuration configuration, Method method) {
        this.command = new Command(configuration, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (this.command.getType()) {
            case SELECT:
                result = sqlSession.selectOne(this.command.getName(), args);
                break;
            case INSERT:
                result = null;
                break;
            case DELETE:
                result = null;
                break;
            case UPDATE:
                result = null;
                break;
            default:
                throw new RuntimeException();
        }
        if (result == null) {
            throw new RuntimeException();
        }

        return result;
    }

    class Command {
        private final String name;
        private final SqlCommandType type;

        public Command (Configuration configuration, Method method) {
            this.name = method.getDeclaringClass().getName() + "." + method.getName();
            SqlContext sqlContext = configuration.getMapperSqlContext().get(name);
            this.type = sqlContext.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }
}
