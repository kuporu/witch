/*
*   用于存储 Class 和 MapperProxyFactory 的对应关系 (map)
 */

package org.Archibald.witch.binding;


import org.Archibald.witch.session.SqlSession;

import java.util.HashMap;
import java.util.Map;

public class MapperRegistry {
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public <T> T getMappers(Class<T> clazz, SqlSession sqlSession) {                                // 泛型方法，获取代理对象
        MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory) this.knownMappers.get(clazz);
        T obj = (T)mapperProxyFactory.newInstance(sqlSession);
        return obj;
    }

    public <T> void setMappers(Class<T> type) {
        this.knownMappers.put(type, new MapperProxyFactory<>(type));
    }
}
