package org.Archibald.medator;

import java.sql.Connection;
import java.util.Map;

public class Configuration {
    protected Connection connection;                            // 数据库连接
    protected Map<String, String> dataSource;                   // 数据库连接所需要的信息（如：数据库名称，用户名，密码）
    protected Map<String, SqlContext> mapperSqlContext;         // 根据mapper.xml配置文件属性封装为SqlContext对象，其索引key为namespace + id

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Map<String, String> getDataSource() {
        return dataSource;
    }

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, SqlContext> getMapperSqlContext() {
        return mapperSqlContext;
    }

    public void setMapperSqlContext(Map<String, SqlContext> mapperSqlContext) {
        this.mapperSqlContext = mapperSqlContext;
    }
}
