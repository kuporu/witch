package org.Archibald.witch.session.defaults;

import org.Archibald.witch.session.Configuration;
import org.Archibald.witch.session.MappedStatement;
import org.Archibald.witch.session.SqlSession;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class DefaultSqlSession implements SqlSession {
    private final Connection connection;                                                                              // sql连接
    private final Map<String, MappedStatement> statement2SqlContext;                                                       // statement转化为sql上下文
    private final Configuration configuration;

    /**
     * 装配 SqlSession 时 Configuration 就已经组装好了
     * @param configuration 配置类
     */
    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
        this.connection = configuration.getConnection();
        this.statement2SqlContext = configuration.getMapperSqlContext();
    }

    @Override
    // 泛型的用法1：返回值的类型不需要在编译时指定，可以延迟到方法调用时才指定。
    public <T> T selectOne(String statement) {
        try {
            return (T) selectList(statement).get(0);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T selectOne(String statement, Object... parameter) {
        if (parameter == null)                                                                                  // 没有携带参数的调用无参方法
            return selectOne(statement);
        MappedStatement mappedStatement = statement2SqlContext.get(statement);
        String sql = mappedStatement.getSql();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            buildParameter(preparedStatement, mappedStatement.getLocationAndPlaceholderName(), parameter);           // sql预编译配置参数
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> objects = resultSet2Obj(resultSet, Class.forName(mappedStatement.getResultType()));
            return objects.get(0);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement) {
        MappedStatement mappedStatement = statement2SqlContext.get(statement);
        String sql = mappedStatement.getSql();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, Class.forName(mappedStatement.getResultType()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> List<T> selectList(String statement, Object... parameter) {
        if (parameter == null)
            return selectList(statement);
        MappedStatement mappedStatement = statement2SqlContext.get(statement);
        String sql = mappedStatement.getSql();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            buildParameter(preparedStatement, mappedStatement.getLocationAndPlaceholderName(), parameter);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, Class.forName(mappedStatement.getResultType()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T getMapper(Class<T> clazz) {
        return configuration.getMapper(clazz, this);
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
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
            if (parameter[i - 1] instanceof Long) {
                preparedStatement.setLong(i, Long.parseLong(parameter[i - 1].toString()));
                isSimpleObject = true;
                continue;
            }
            if (parameter[i - 1] instanceof Integer) {
                preparedStatement.setInt(i, Integer.parseInt(parameter[i - 1].toString()));
                isSimpleObject = true;
                continue;
            }
            if (parameter[i - 1] instanceof String) {
                preparedStatement.setString(i, parameter[i - 1].toString());
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

    /**
     *  将数据库查询结果 resultSet 封装为 clazz （List）对象并返回
     * @param resultSet sql查询返回数据
     * @param clazz 返回对象类型
     * @param <T> **泛型，暂时不知道怎么用，用在什么地方**
     * @return sql查询结果包装的对象
     */

    // *** ?的用法还没有搞清楚***
    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
        List<T> res = new LinkedList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();               // 获取sql表中元数据（包含字段名）
            int columnCount = metaData.getColumnCount();                        // 获取字段名长度
            while (resultSet.next()) {                                          // 查看resultSet中的多行数据
                T instance = (T) clazz.newInstance();                          // 构建clazz返回对象
                for (int i = 1; i <= columnCount; i++) {                        // 遍历列名
                    String columnName = metaData.getColumnName(i);              // 获取属性名
                    Object columnValue = resultSet.getObject(i);                // 获取属性值
                                                                                // 获取clazz类方法名
                    String objectMethodName = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                                                                                // 获取返回对象方法名
                    if (columnValue == null)                                    // 如果对应字段值为空，则跳过赋值阶段
                        continue;
                    Method method = clazz.getMethod(objectMethodName, columnValue.getClass());
                    method.invoke(instance, columnValue);                       // 执行对象set方法
                }
                res.add(instance);
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return res;
    }
}
