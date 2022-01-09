package org.Archibald.witch.binding;


import org.Archibald.witch.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MapperProxy<T> implements InvocationHandler {
    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;

    MapperProxy (SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }else {
            MapperMethod mapperMethod = new MapperMethod(sqlSession.getConfiguration(), method);
            return mapperMethod.execute(sqlSession, args);
        }
    }
}
