package org.Archibald.witch.builder.xml;

import org.Archibald.witch.builder.BaseBuilder;
import org.Archibald.witch.builder.BuilderException;
import org.Archibald.witch.mapping.SqlCommandType;
import org.Archibald.witch.parsing.XNode;
import org.Archibald.witch.parsing.XPathParser;
import org.Archibald.witch.session.Configuration;
import org.Archibald.witch.session.MappedStatement;
import org.Archibald.witch.session.Resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLMapperBuilder extends BaseBuilder {
    private final XPathParser XMLMapperParser;

    public XMLMapperBuilder(Configuration configuration, InputStream inputStream) {
        super(configuration);
        this.XMLMapperParser = new XPathParser(inputStream);
    }

    public void parse () {
        try {
            String regEx = "(#\\{(.*?)})";
            Pattern pattern = Pattern.compile(regEx);
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

                MappedStatement mappedStatement = new MappedStatement();                                            // 构建sqlContext对象
                mappedStatement.setSql(sql);
                mappedStatement.setResultType(child.getStringAttribute("resultType"));
                mappedStatement.setLocationAndPlaceholderName(locationAndPlaceHolderName);
                mappedStatement.setSqlCommandType(SqlCommandType.valueOf(child.getName().toUpperCase()));           // 获取CRUD类型
                mappedStatement.setId(key);

                this.configuration.addDataMapperSqlContext(mappedStatement);
            }
        }catch (Exception e) {
            throw new BuilderException("构建Mapper配置文件时错误", e);
        }
    }
}
