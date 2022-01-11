package org.Archibald.witch.parsing;

import org.Archibald.witch.builder.BuilderException;
import org.Archibald.witch.session.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * evalNode用于解析Xpath，返回加强后的Node结点XNode
 */
public class XPathParser {
    private final Document document;
    private final XPath xpath;

    public XPathParser(InputStream inputStream) {
        this.document = this.createDocument(inputStream);
        XPathFactory factory = XPathFactory.newInstance();
        this.xpath = factory.newXPath();
    }

    public List<XNode> evalNodes(String expression) {
        return this.evalNodes(this.document, expression);
    }

    public List<XNode> evalNodes(Object root, String expression) {
        List<XNode> xNodes = new ArrayList<>();
        NodeList nodes = (NodeList)this.evaluate(expression, root, XPathConstants.NODESET);

        for(int i = 0; i < nodes.getLength(); ++i) {
            xNodes.add(new XNode(nodes.item(i), this));
        }

        return xNodes;
    }

    public XNode evalNode(String expression) {
        return this.evalNode(this.document, expression);
    }

    public XNode evalNode(Object root, String expression) {
        Node node = (Node)this.evaluate(expression, root, XPathConstants.NODE);
        return new XNode(node, this);
    }

    private Object evaluate(String expression, Object root, QName returnType) {
        try {
            return this.xpath.evaluate(expression, root, returnType);
        } catch (Exception var5) {
            throw new RuntimeException("Error evaluating XPath.  Cause: " + var5, var5);
        }
    }

    private Document createDocument(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            return doc;
        } catch (Exception e) {
            throw new BuilderException("XML解析创建Document失败", e);
        }
    }
}
