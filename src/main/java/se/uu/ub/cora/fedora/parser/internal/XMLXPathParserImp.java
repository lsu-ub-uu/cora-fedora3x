/*
 * Copyright 2018 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.fedora.parser.internal;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import se.uu.ub.cora.fedora.parser.XMLXPathParser;
import se.uu.ub.cora.fedora.parser.XMLXPathParserException;

public final class XMLXPathParserImp implements XMLXPathParser {
	private Document document;
	private XPath xpath;

	@Override
	public void setupToHandleResponseXML(String xml) throws XMLXPathParserException {
		try {
			document = createDocumentFromXML(xml);
			setupXPath();
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

	@Override
	public String getStringFromDocumentUsingXPath(String xpathString)
			throws XMLXPathParserException {
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

	@Override
	public String getStringFromNodeUsingXPath(Node node, String xpathString)
			throws XMLXPathParserException {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			return (String) expr.evaluate(node, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new XMLXPathParserException(composeMessage(e), e);
		}
	}

	@Override
	public NodeList getNodeListFromDocumentUsingXPath(String xpathString)
			throws XMLXPathParserException {
		try {
			XPathExpression expr = xpath.compile(xpathString);
			return (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new XMLXPathParserException(composeMessage(e), e);
		}
	}

}