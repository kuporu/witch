/*
 * SqlSession是用户操作直接操作的API接口，其开放有CRUD方法
 */
package org.Archibald.medator;

import java.util.List;

public interface SqlSession {
    <T> T selectOne(String statement);
    <T> T selectOne(String statement, Object... parameter);
    <T> List<T> selectList(String statement);
    <T> List<T> selectList(String statement, Object... parameter);
    <T> T getMapper(Class<T> clazz);                                // 新加入特性，传入*mapper.class使用动态代理执行其接口中的方法
    Configuration getConfiguration();
    void close();
}
