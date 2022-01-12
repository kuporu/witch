package org.Archibald.witch.session;

import org.Archibald.witch.binding.MapperRegistry;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
    protected Connection connection;                            // 数据库连接
    protected Properties dataSource;                            // 数据库连接所需要的信息（如：数据库名称，用户名，密码）
    protected Map<String, MappedStatement> mapperSqlContext;    // 根据mapper.xml配置文件属性封装为SqlContext对象，其索引key为namespace + id
    protected final MapperRegistry mapperRegistry;              // mapper方法动态代理管理器

    public Configuration () {
        this.mapperRegistry = new MapperRegistry();
        this.mapperSqlContext = new HashMap<>();
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Properties getDataSource() {
        return dataSource;
    }

    public void setDataSource(Properties dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 向mapperSqlContext单例容器中注入ms
     * @param ms ms对象
     */
    public void addDataMapperSqlContext(MappedStatement ms) {
        this.mapperSqlContext.put(ms.getId(), ms);
    }

    public Map<String, MappedStatement> getMapperSqlContext() {
        return mapperSqlContext;
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMappers(type, sqlSession);
    }

    public <T> void setMapper(Class<T> type) {
        this.mapperRegistry.setMappers(type);
    }
}
