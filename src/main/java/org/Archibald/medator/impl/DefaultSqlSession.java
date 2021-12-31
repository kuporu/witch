package org.Archibald.medator.impl;

import org.Archibald.medator.SqlSession;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSqlSession implements SqlSession {
    @Override
    public <T> T selectOne(String statement) {
        return null;
    }

    @Override
    public <T> T selectOne(String statement, Object... parameter) {
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement) {
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Object... parameter) {
        return null;
    }

    @Override
    public void close() {

    }

    /**
     * 向预编译SQL语句中的占位符注入方法调用者提供的参数
     * @param preparedStatement 预编译SQL语句
     * @param parameterMap 占位符 位置（下标从1开始） -> 名称，eg: 1 -> id, 2 -> name
     * @param parameter 方法调用者提供的参数，如果是简短对象（String, int）可以多个，但个数必须和parameterMap大小相同，复杂对象只能一个（**复杂对象限定个数没有实现**）
     */
    private void buildParameter(PreparedStatement preparedStatement, Map<Integer, String> parameterMap, Object... parameter) throws SQLException, IllegalAccessException {
        int parameterMapSize = parameter.length;

        boolean isSimpleObject = false;
        // 处理简单对象（***简单对象扩展设计模式目前没有实现***）
        for (int i = 1; i <= parameterMapSize; i++) {
            if (parameter[i] instanceof Long) {
                preparedStatement.setLong(i, Long.parseLong(parameter[i].toString()));
                isSimpleObject = true;
                continue;
            }
            if (parameter[i] instanceof Integer) {
                preparedStatement.setInt(i, Integer.parseInt(parameter[i].toString()));
                isSimpleObject = true;
                continue;
            }
            if (parameter[i] instanceof String) {
                preparedStatement.setString(i, parameter[i].toString());
                isSimpleObject = true;
            }
        }
        if(isSimpleObject) return;

        // 处理单个复杂对象
        // 存放复杂对象中所有属性名及其对应的值
        Map<String, Object> fieldNameToValue = new HashMap<>();
        Field[] fields = parameter.getClass().getDeclaredFields();              // 获取所有访问修饰符的属性对象
        for (Field field: fields) {                                             // 获取属性对象的名称
            String fieldName = field.getName();
            field.setAccessible(true);
            Object obj = field.get(parameter[0]);                               // 获取属性对象的名称对应的值
            fieldNameToValue.put(fieldName, obj);
        }

        for (int i = 1; i <= parameterMapSize; i++) {
            String placeholderName = parameterMap.get(i);                       // 获取预编译SQL语句占位符名称
            Object obj = fieldNameToValue.get(placeholderName);                 // 获取占位符名称对应的值

            if (obj instanceof Long) {
                preparedStatement.setLong(i, Long.parseLong(parameter[i].toString()));
                continue;
            }
            if (obj instanceof Integer) {
                preparedStatement.setInt(i, Integer.parseInt(parameter[i].toString()));
                continue;
            }
            if (obj instanceof String) {
                preparedStatement.setString(i, parameter[i].toString());
                continue;
            }
            // ***目前没有解决LocalDateTime和Date之间的关系***
            if (obj instanceof Date) {
                preparedStatement.setDate(i, (java.sql.Date)obj);
            }
        }
    }
}
