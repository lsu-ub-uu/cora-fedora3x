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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLXPathParserSpy implements XMLXPathParser {
	public Map<String, Boolean> validXml;
	public Map<String, Integer> callCount;
	private String lastParsedXml;

	public XMLXPathParserSpy() {
		validXml = new HashMap<>();
		callCount = new HashMap<>();
	}

	public int getCallCount(String xml) {
		return callCount.get(xml);
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
	public void setupToHandleResponseXML(String xml) throws XMLXPathParserException {
		increaseCallCount(xml);
		if (validXml.containsKey(xml)) {
			if (!validXml.get(xml)) {
				throw new XMLXPathParserException("Could not parse XML");
			}
		}
		lastParsedXml = xml;
	}

	private void increaseCallCount(String xml) {
		if (callCount.containsKey(xml)) {
			callCount.put(xml, callCount.get(xml) + 1);
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
