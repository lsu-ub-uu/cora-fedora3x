package se.uu.ub.cora.fedora.data;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface XMLXPathParser {
    XMLXPathParser forXML(String xml) throws XMLXPathParserException;

    boolean hasNode(String xPath) throws XMLXPathParserException;

    String getStringFromDocumentUsingXPath(String xpathString) throws XMLXPathParserException;

    String getStringFromNodeUsingXPath(Node node, String xpathString) throws XMLXPathParserException;

    NodeList getNodeListFromDocumentUsingXPath(String xpathString) throws XMLXPathParserException;
}
