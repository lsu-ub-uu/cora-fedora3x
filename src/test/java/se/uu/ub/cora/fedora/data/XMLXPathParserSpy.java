package se.uu.ub.cora.fedora.data;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class XMLXPathParserSpy implements XMLXPathParser {
    public List<String> parsedXml;
    public Boolean invalidXml;
    public Boolean uselessXml;
    public Boolean problematicPidList;
    public List<String> xmlPidList;
    private String lastParsedXml;

    public XMLXPathParserSpy() {
        parsedXml = new ArrayList<>();
        xmlPidList = new ArrayList<>();
        uselessXml = false;
        invalidXml = false;
        problematicPidList = false;
    }

    public String getLastParsedXml() {
        return lastParsedXml;
    }

    @Override
    public XMLXPathParser forXML(String xml) throws XMLXPathParserException {
        if(invalidXml) {
            throw new XMLXPathParserException("Could not parse XML");
        }
        parsedXml.add(xml);
        lastParsedXml = xml;
        return this;
    }

    @Override
    public boolean hasNode(String xPath) throws XMLXPathParserException {
        return false;
    }

    @Override
    public String getStringFromDocumentUsingXPath(String xpathString) throws XMLXPathParserException {
        return null;
    }

    @Override
    public String getStringFromNodeUsingXPath(Node node, String xpathString) throws XMLXPathParserException {
        return null;
    }

    @Override
    public NodeList getNodeListFromDocumentUsingXPath(String xpathString) throws XMLXPathParserException {
        return null;
    }
}
