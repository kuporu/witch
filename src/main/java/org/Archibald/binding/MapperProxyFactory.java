/*
  通过传入接口Class对象，创建其代理对象
 */
package org.Archibald.binding;


import org.Archibald.medator.SqlSession;

import java.lang.reflect.Proxy;


public class MapperProxyFactory<T> {
    Class<T> clazz;                                         // 接口
    public MapperProxyFactory (Class<T> clazz) {
        this.clazz = clazz;
    }

    public T newInstance (SqlSession sqlSession) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{this.clazz}, new MapperProxy<>(sqlSession, clazz));
    }
}
