package se.uu.ub.cora.fedora.data;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;

public final class XMLXPathParserImp implements XMLXPathParser {
	private Document document;
	private XPath xpath;

	public XMLXPathParserImp() {
	}

	public XMLXPathParser forXML(String xml) throws XMLXPathParserException {
		try {
			document = createDocumentFromXML(xml);
			setupXPath();
			return this;
		} catch (Exception e) {
			throw new XMLXPathParserException("Can not readObject xml: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean hasNode(String xPath) throws XMLXPathParserException {
		try {
			String booleanXPath = String.format("boolean(%s)", xPath);
			return (Boolean) xpath.compile(booleanXPath).evaluate(document, XPathConstants.BOOLEAN);
		} catch (XPathExpressionException e) {
			throw new XMLXPathParserException("Invalid XPath", e);
		}
	}

	private void setupXPath() {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		xpath = xpathFactory.newXPath();
	}

	public Document createDocumentFromXML(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder dBuilder = createDocumentBuilder();
		return readXMLUsingBuilderAndXML(dBuilder, xml);
	}

	private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		ErrorHandler errorHandlerWithoutSystemOutPrinting = new DefaultHandler();
		dBuilder.setErrorHandler(errorHandlerWithoutSystemOutPrinting);
		return dBuilder;
	}

	private Document readXMLUsingBuilderAndXML(DocumentBuilder dBuilder, String xml)
			throws SAXException, IOException {
		StringReader reader = new StringReader(xml);
		InputSource in = new InputSource(reader);
		Document doc = dBuilder.parse(in);
		doc.getDocumentElement().normalize();
		return doc;
	}

	public String getStringFromDocumentUsingXPath(String xpathString) throws XMLXPathParserException {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			return (String) expr.evaluate(document, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new XMLXPathParserException(composeMessage(e), e);
		}
	}

	private String composeMessage(XPathExpressionException e) {
		return "Unable to use xpathString: " + e.getMessage();
	}

	public String getStringFromNodeUsingXPath(Node node, String xpathString) throws XMLXPathParserException {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			return (String) expr.evaluate(node, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new XMLXPathParserException(composeMessage(e), e);
		}
	}

	public NodeList getNodeListFromDocumentUsingXPath(String xpathString) throws XMLXPathParserException {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			return (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new XMLXPathParserException(composeMessage(e), e);
		}
	}

}