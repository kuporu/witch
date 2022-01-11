package org.Archibald.witch.builder.xml;

import org.Archibald.witch.builder.BuilderException;
import org.Archibald.witch.mapping.SqlCommandType;
import org.Archibald.witch.parsing.XNode;
import org.Archibald.witch.parsing.XPathParser;
import org.Archibald.witch.session.Configuration;
import org.Archibald.witch.session.Resource;
import org.Archibald.witch.session.SqlContext;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLConfigBuilder {
    private final XPathParser xPathParser;
    public XMLConfigBuilder (InputStream inputStream) {
        this.xPathParser = new XPathParser(inputStream);
    }

    /**
     * 通过对XML文件的解析，返回Configuration对象
     * @return 返回configuration对象
     */
    public Configuration parse () {
        Configuration configuration = parseConfiguration(this.xPathParser.evalNode("/configuration"));
        return configuration;
    }

    private Configuration parseConfiguration(XNode context) {                                                           // 从xml文件中生成配置类对象
        Configuration configuration = new Configuration();
        configuration.setDataSource(this.dataSource(context.evalNode("environments")));                                 // 获取数据库配置信息
        configuration.setConnection(this.getConnection(configuration));                                                 // 获取数据库连接
        configuration.setMapperSqlContext(this.getMapperSqlContext(context.evalNode("mappers"), configuration));        // 获取sql上下文信息
        return configuration;
    }

    /**
     * 除了sql预编译，向 mapperRegistry 中注入动态代理对象
     * @param context   /configuration结点
     * @param configuration 配置类对象
     * @return 返回sqlContext对象
     */
    private Map<String, SqlContext> getMapperSqlContext (XNode context, Configuration configuration) {
        Map<String, SqlContext> map = new HashMap<>();
        Iterator<XNode> iterator = context.evalNodes("mapper").iterator();
        String regEx = "(#\\{(.*?)})";
        Pattern pattern = Pattern.compile(regEx);
        try {
            while (iterator.hasNext()) {
                XNode next = iterator.next();
                String source = next.getStringAttribute("resource");
                XPathParser XMLMapperParser = new XPathParser(Resource.getInputStream(source));
                XNode mapperRoot = XMLMapperParser.evalNode("/mapper");                   // *mapper.xml
                String namespace = mapperRoot.getStringAttribute("namespace");
                configuration.setMapper(Class.forName(namespace));                                  // 将namespace中的Class注入MapperRegistry中统一管理
                List<XNode> xNodes = mapperRoot.evalNodes("select|insert|update|delete");
                for (XNode child : xNodes) {
                    String key = namespace + "." + child.getStringAttribute("id");
                    String sql = child.getBody();
                    Matcher matcher = pattern.matcher(sql);
                    int idx = 1;                                                                    // 记录占位符位置（从1开始）
                    Map<Integer, String> locationAndPlaceHolderName = new HashMap<>();
                    while (matcher.find()) {
                        String group1 = matcher.group(1);
                        String group2 = matcher.group(2);                                           // 记录占位符名称
                        sql = sql.replace(group1, "?");                                  // 将#{...}占位符换为?
                        locationAndPlaceHolderName.put(idx, group2);
                        idx++;
                    }

                    SqlContext sqlContext = new SqlContext();                                       // 构建sqlContext对象
                    sqlContext.setSql(sql);
                    sqlContext.setResultType(child.getStringAttribute("resultType"));
                    sqlContext.setLocationAndPlaceholderName(locationAndPlaceHolderName);
                    sqlContext.setSqlCommandType(SqlCommandType.valueOf(child.getName().toUpperCase()));                // 获取CRUD类型

                    map.put(key, sqlContext);
                }
            }
            return map;
        } catch (Exception e) {
            throw new BuilderException("", e);
        }
    }

    /**
     * 配置数据库连接并返回
     * @param configuration 已获取的数据库配置信息
     * @return 数据库连接
     */
    private Connection getConnection (Configuration configuration) {
        Properties dataSource = configuration.getDataSource();
        try {
            Class.forName((String) dataSource.get("driver"));
            return DriverManager.getConnection((String) dataSource.get("url"), (String) dataSource.get("username"), (String) dataSource.get("password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取数据库配置信息
     * @return 数据库配置信息key:name -> value:value
     */
    private Properties dataSource (XNode context) {
        Properties properties = new Properties();
        XNode xNode = context.evalNode("environment/dataSource");
        Properties childrenAsProperties = xNode.getChildrenAsProperties();
        properties.put("driver", childrenAsProperties.getProperty("driver"));
        properties.put("url", childrenAsProperties.getProperty("url"));
        properties.put("username", childrenAsProperties.getProperty("username"));
        properties.put("password", childrenAsProperties.getProperty("password"));

        return properties;
    }
}
