package se.uu.ub.cora.fedora.data;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class XMLXPathParserSpy implements XMLXPathParser {
    public Map<String, Boolean> validXml;
    public Map<String, Integer> callCount;
    private String lastParsedXml;

    public int getCallCount(String xml) {
        return callCount.get(xml);
    }

    public XMLXPathParserSpy() {
        validXml = new HashMap<>();
        callCount = new HashMap<>();
    }

    public void removeXml(String xml) {
        validXml.remove(xml);
        callCount.remove(xml);
    }

    public void addXml(String xml) {
        validXml.put(xml, true);
        callCount.put(xml, 0);
    }

    public void addInvalidXml(String xml) {
        validXml.put(xml, false);
        callCount.put(xml, 0);
    }

    public boolean wasAllXmlCalledAtLeastOnce() {
        return !callCount.values().contains(0);
    }

    public String getLastParsedXml() {
        return lastParsedXml;
    }

    @Override
    public XMLXPathParser forXML(String xml) throws XMLXPathParserException {
        increaseCallCount(xml);
        if(validXml.containsKey(xml)) {
            if (!validXml.get(xml)) {
                throw new XMLXPathParserException("Could not parse XML");
            }
        }
        lastParsedXml = xml;
        return this;
    }

    private void increaseCallCount(String xml) {
        if(callCount.containsKey(xml)) {
            callCount.put(xml, callCount.get(xml) +1);
        } else {
            callCount.put(xml, 1);
        }
    }

    @Override
    public boolean hasNode(String xPath) {
        return false;
    }

    @Override
    public String getStringFromDocumentUsingXPath(String xpathString) {
        return null;
    }

    @Override
    public String getStringFromNodeUsingXPath(Node node, String xpathString) {
        return null;
    }

    @Override
    public NodeList getNodeListFromDocumentUsingXPath(String xpathString) {
        return null;
    }
}
