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
package se.uu.ub.cora.fedora.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

public class FedoraReaderXmlHelperImp implements FedoraReaderXmlHelper {
	private XMLXPathParserFactory xmlXPathParserFactory;

	public FedoraReaderXmlHelperImp(XMLXPathParserFactory xmlXPathParserFactory) {
		this.xmlXPathParserFactory = xmlXPathParserFactory;
	}

	@Override
	public ListSession getSession(String responseXML) {
		try {
			XMLXPathParser xmlXPathParser = xmlXPathParserFactory.factor();
			xmlXPathParser.setupToHandleResponseXML(responseXML);
			return extractListSession(xmlXPathParser);
		} catch (XMLXPathParserException e) {
			throw new RuntimeException("malformed cursor", e);
		}
	}

	private static ListSession extractListSession(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		ListSession fedoraListSession = ListSession.createListSessionNoMoreResults();
		if (xmlxPathParser.hasNode("/result/listSession")) {
			fedoraListSession = tryGetFedoraListSession(xmlxPathParser);
		}
		return fedoraListSession;
	}

	private static ListSession tryGetFedoraListSession(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		ListSession fedoraListSession = getFedoraListSessionWithToken(xmlxPathParser);
		String cursor = tryGetCursorFromXml(xmlxPathParser);
		fedoraListSession.setCursor(cursor);
		return fedoraListSession;
	}

	private static ListSession getFedoraListSessionWithToken(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		var token = xmlxPathParser
				.getStringFromDocumentUsingXPath("/result/listSession/token/text()");
		throwIfRequiredElementNotFound(token, "token not found in XML");
		return ListSession.createListSessionUsingToken(token);
	}

	private static void throwIfRequiredElementNotFound(String element, String message)
			throws XMLXPathParserException {
		if (element.isEmpty()) {
			throw new XMLXPathParserException(message);
		}
	}

	private static String tryGetCursorFromXml(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		var cursor = xmlxPathParser
				.getStringFromDocumentUsingXPath("/result/listSession/cursor/text()");
		throwIfRequiredElementNotFound(cursor, "cursor not found in XML");
		return cursor;
	}

	@Override
	public List<String> getPidList(String responseXML) {
		try {
			XMLXPathParser xmlXPathParser = xmlXPathParserFactory.factor();
			xmlXPathParser.setupToHandleResponseXML(responseXML);
			return extractPidList(xmlXPathParser);
		} catch (XMLXPathParserException e) {
			throw new RuntimeException("There was no resultList in given XML", e);
		}
	}

	private static List<String> extractPidList(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		throwIfResultListIsMissing(xmlxPathParser);
		var nodeList = xmlxPathParser
				.getNodeListFromDocumentUsingXPath("/result/resultList/objectFields/pid/text()");
		return getPidListFromNodeList(nodeList);
	}

	private static void throwIfResultListIsMissing(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		if (!xmlxPathParser.hasNode("/result/resultList")) {
			throw new XMLXPathParserException("There was no resultList in given XML");
		}
	}

	private static List<String> getPidListFromNodeList(NodeList nodeList) {
		var result = new ArrayList<String>();
		for (int idx = 0; idx < nodeList.getLength(); idx++) {
			var node = nodeList.item(idx);
			result.add(node.getNodeValue());
		}
		return result;
	}

	public XMLXPathParserFactory getXmlXPathParseFactory() {
		return xmlXPathParserFactory;
	}

}
