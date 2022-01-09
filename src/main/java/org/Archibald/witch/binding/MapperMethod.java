package org.Archibald.witch.binding;

import org.Archibald.witch.mapping.SqlCommandType;
import org.Archibald.witch.session.Configuration;
import org.Archibald.witch.session.SqlContext;
import org.Archibald.witch.session.SqlSession;

import java.lang.reflect.Method;
import java.util.Collection;

public class MapperMethod {
    private final MapperMethod.Command command;
    private final MapperMethod.MethodSignature methodSignature;

    public MapperMethod(Configuration configuration, Method method) {
        this.command = new Command(configuration, method);
        this.methodSignature = new MethodSignature(method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (this.command.getType()) {
            case SELECT:
                if (methodSignature.returnsMany)
                    result = sqlSession.selectList(this.command.getName(), args);
                else
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

    public static class Command {
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

    public static class MethodSignature {
        private final Class<?> returnType;
        private final boolean returnsMany;

        public MethodSignature (Method method) {
            this.returnType = method.getReturnType();
            this.returnsMany = Collection.class.isAssignableFrom(returnType);
        }
    }
}
