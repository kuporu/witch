/*
 * 需要解析 xml 配置文件和 *Mapper.xml配置文件
 * 加载datasource数据生成数据库连接，解析xml中sql语句，服务于sqlSession处理sql预编译
 * 利用组装好的配置文件对象（Configuration）
 * 生成SqlSessionFactory对象
 */
package org.Archibald.witch.session;

import org.Archibald.witch.builder.xml.XMLConfigBuilder;
import org.Archibald.witch.session.defaults.DefaultSqlSessionFactory;
import org.dom4j.DocumentException;

import java.io.Reader;

public class SqlSessionFactoryBuilder {

    public DefaultSqlSessionFactory build(String resourceName) throws DocumentException {
        Reader reader = Resource.getResourceAsReader(resourceName);                                 // 通过相对路径名获取文件Reader
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        Configuration configuration = xmlConfigBuilder.parse();

        return new DefaultSqlSessionFactory(configuration);
    }
}