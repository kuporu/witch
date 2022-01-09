package org.Archibald.witch.session;

import org.Archibald.witch.mapping.SqlCommandType;

import java.util.Map;

public class SqlContext {
    private String sql;                                             // 预编译后的sql语句
    private String resultType;                                      // sql语句执行返回的包装结果类型
    private Map<Integer, String> locationAndPlaceholderName;        // 预编译后的sql语句占位符位置和名字
    private SqlCommandType sqlCommandType;                                  // CRUD类型

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public Map<Integer, String> getLocationAndPlaceholderName() {
        return locationAndPlaceholderName;
    }

    public void setLocationAndPlaceholderName(Map<Integer, String> locationAndPlaceholderName) {
        this.locationAndPlaceholderName = locationAndPlaceholderName;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }
}
