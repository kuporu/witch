package org.Archibald.witch.parsing;

import org.w3c.dom.CharacterData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class XNode {
    private final Properties attributes;        // 标签中的所有属性
    private final Node node;                    // Node接口
    private final XPathParser xPathParser;      // 用于除根结点外的其他结点的Xpath解析
    private final String body;
    private final String name;

    public XNode(Node node, XPathParser xPathParser) {
        this.node = node;
        this.xPathParser = xPathParser;
        this.attributes = this.parseAttributes(node);
        this.body = this.parseBody(node);
        this.name = node.getNodeName();
    }

    public String getBody() {
        return this.body;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 通过调用注入的XPathParse解析Xpath（expression）并返回XNode加强结点(单个)
     * @param expression XPath表达式
     * @return XNode结点
     */
    public XNode evalNode(String expression) {
        return this.xPathParser.evalNode(this.node, expression);
    }

    /**
     *  通过调用注入的XPathParse解析Xpath（expression）并返回XNode加强结点(多个)
     * @param expression XPath表达式
     * @return XNode结点
     */
    public List<XNode> evalNodes(String expression) {
        return this.xPathParser.evalNodes(this.node, expression);
    }

    /**
     * 获取当前结点下的所有子节点的attributes值，拼接为Properties并返回
     * @return properties对象
     */
    public Properties getChildrenAsProperties() {
        Properties properties = new Properties();
        Iterator<XNode> iterator = this.getChildren().iterator();

        while (iterator.hasNext()) {
            XNode childNode = iterator.next();
            String name = (String) childNode.attributes.get("name");
            String value = (String) childNode.attributes.get("value");
            if (name != null && value != null)
                properties.put(name, value);
        }

        return properties;
    }

    /**
     * 获取当前结点下的孩子结点，包装为加强结点集合并放回
     */
    public List<XNode> getChildren() {
        NodeList childNodes = this.node.getChildNodes();
        List<XNode> childXNodes = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            childXNodes.add(new XNode(item, this.xPathParser));
        }

        return childXNodes;
    }

    /**
     * 获取当前结点自身的属性值
     * @param n
     * @return
     */
    private Properties parseAttributes(Node n) {
        Properties attribute = new Properties();
        NamedNodeMap attributes = n.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                attribute.put(item.getNodeName(), item.getNodeValue());
            }
        }
        return attribute;
    }

    /**
     * 通过结点的属性名获取结点的属性值
     * @param name 属性名
     * @return 属性值
     */
    public String getStringAttribute(String name) {
        return this.attributes.getProperty(name);
    }

    private String parseBody(Node node) {
        String data = this.getBodyData(node);
        if (data == null) {
            NodeList children = node.getChildNodes();

            for(int i = 0; i < children.getLength(); ++i) {
                Node child = children.item(i);
                data = this.getBodyData(child);
                if (data != null) {
                    break;
                }
            }
        }

        return data;
    }

    private String getBodyData(Node child) {
        if (child.getNodeType() != 4 && child.getNodeType() != 3) {
            return null;
        } else {
            String data = ((CharacterData)child).getData();
            return data;
        }
    }
}
