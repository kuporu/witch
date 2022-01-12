package org.Archibald.witch.builder.xml;

import org.Archibald.witch.builder.BaseBuilder;
import org.Archibald.witch.builder.BuilderException;
import org.Archibald.witch.parsing.XNode;
import org.Archibald.witch.parsing.XPathParser;
import org.Archibald.witch.session.Configuration;
import org.Archibald.witch.session.Resource;
import org.Archibald.witch.session.MappedStatement;

import java.io.InputStream;
import java.sql.DriverManager;
import java.util.*;

public class XMLConfigBuilder extends BaseBuilder {
    private final XPathParser xPathParser;
    public XMLConfigBuilder (InputStream inputStream) {
        super(new Configuration());
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
        this.dataSource(context.evalNode("environments"));                                                              // 获取数据库配置信息
        this.getConnection();                                                                                           // 获取数据库连接
        this.getMapperSqlContext(context.evalNode("mappers"), configuration);                                           // 获取sql上下文信息
        return configuration;
    }

    /**
     * 除了sql预编译，向 mapperRegistry 中注入动态代理对象
     * @param context   /configuration结点
     * @param configuration 配置类对象
     */
    private void getMapperSqlContext (XNode context, Configuration configuration) {
        Map<String, MappedStatement> map = new HashMap<>();
        Iterator<XNode> iterator = context.evalNodes("mapper").iterator();
        try {
            while (iterator.hasNext()) {
                XNode next = iterator.next();
                String source = next.getStringAttribute("resource");
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(this.configuration, Resource.getInputStream(source));
                xmlMapperBuilder.parse();
            }
        } catch (Exception e) {
            throw new BuilderException("", e);
        }
    }

    /**
     * 配置数据库连接并返回
     */
    private void getConnection () {
        Properties dataSource = this.configuration.getDataSource();
        try {
            Class.forName((String) dataSource.get("driver"));
            this.configuration.setConnection(DriverManager.getConnection((String) dataSource.get("url"), (String) dataSource.get("username"), (String) dataSource.get("password")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库配置信息
     */
    private void dataSource (XNode context) {
        Properties properties = new Properties();
        XNode xNode = context.evalNode("environment/dataSource");
        Properties childrenAsProperties = xNode.getChildrenAsProperties();
        properties.put("driver", childrenAsProperties.getProperty("driver"));
        properties.put("url", childrenAsProperties.getProperty("url"));
        properties.put("username", childrenAsProperties.getProperty("username"));
        properties.put("password", childrenAsProperties.getProperty("password"));

        this.configuration.setDataSource(properties);
    }
}
