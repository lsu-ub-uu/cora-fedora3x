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
package se.uu.ub.cora.fedora.data;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

public class FedoraReaderXmlHelperImp implements FedoraReaderXmlHelper {
	private XMLXPathParserFactory xmlXPathParserFactory;

	@Override
	public FedoraReaderCursor getCursorIfAvailable(String xml) {
		try {
			var xmlXPathParsers = xmlXPathParserFactory.factor().forXML(xml);
			return extractCursor(xmlXPathParsers);
		} catch (XMLXPathParserException e) {
			throw new RuntimeException("malformed cursor", e);
		}
	}

	@Override
	public List<String> getPidList(String xml) {
		try {
			var xmlXPathParsers = xmlXPathParserFactory.factor().forXML(xml);
			return extractPidList(xmlXPathParsers);
		} catch (XMLXPathParserException e) {
			throw new RuntimeException("There was no resultList in given XML", e);
		}
	}

	@Override
	public void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory) {
		this.xmlXPathParserFactory = xmlXPathParserFactory;
	}

	@Override
	public XMLXPathParserFactory getXmlXPathParseFactory() {
		return xmlXPathParserFactory;
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

	private static FedoraReaderCursor extractCursor(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		FedoraReaderCursor fedoraReaderCursor = null;
		if (xmlxPathParser.hasNode("/result/listSession")) {
			fedoraReaderCursor = tryGetFedoraReaderCursor(xmlxPathParser);
		}
		return fedoraReaderCursor;
	}

	private static FedoraReaderCursor tryGetFedoraReaderCursor(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		FedoraReaderCursor fedoraReaderCursor = getFedoraReaderCursorWithToken(xmlxPathParser);
		String cursor = tryGetCursorFromXml(xmlxPathParser);
		fedoraReaderCursor.setCursor(cursor);
		return fedoraReaderCursor;
	}

	private static FedoraReaderCursor getFedoraReaderCursorWithToken(XMLXPathParser xmlxPathParser)
			throws XMLXPathParserException {
		var token = xmlxPathParser
				.getStringFromDocumentUsingXPath("/result/listSession/token/text()");
		throwIfRequiredElementNotFound(token, "token not found in XML");
		return new FedoraReaderCursor(token);
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
}
