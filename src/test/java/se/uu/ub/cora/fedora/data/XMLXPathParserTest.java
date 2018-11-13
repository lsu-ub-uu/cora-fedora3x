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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.*;

public class XMLXPathParserTest {
	private XMLXPathParserFactory xmlxPathParserFactory = new XMLXPathParserFactoryImp();
	private XMLXPathParser xmlxPathParser;

	@BeforeMethod
	public void init() {
		xmlxPathParser = xmlxPathParserFactory.factor();
	}

	@Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "Can not readObject xml: XML document structures must start and end within the same entity.")
	public void testBadXmlShouldThrow() throws Exception {
		xmlxPathParser.forXML("<pid>");
	}


	@Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "Unable to use xpathString: javax.xml.transform.TransformerException: Extra illegal tokens: 'not'")
	public void testMalformedXPath() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		parser.getStringFromDocumentUsingXPath("/broken/xpath/string not");
	}

	@Test
	public void testGoodXPath() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		var result = parser.getStringFromDocumentUsingXPath("/");
		assertNotNull(result);
	}

	@Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "Unable to use xpathString: javax.xml.transform.TransformerException: Extra illegal tokens: 'not'")
	public void testMalformedXPathForNodeList() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		parser.getNodeListFromDocumentUsingXPath("/broken/xpath/string not");
	}

	@Test
	public void testGoodXPathForNodeList() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		var result = parser.getNodeListFromDocumentUsingXPath("/");
		assertNotNull(result);
	}

	@Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "Unable to use xpathString: javax.xml.transform.TransformerException: Extra illegal tokens: 'not'")
	public void testMalformedNodeXPath() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		parser.getStringFromNodeUsingXPath(null, "/broken/xpath/string not");
	}

	@Test
	public void testGoodNodeXPath() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		Node node = new Node() {
			@Override
			public String getNodeName() {
				return null;
			}

			@Override
			public String getNodeValue() throws DOMException {
				return null;
			}

			@Override
			public void setNodeValue(String s) throws DOMException {

			}

			@Override
			public short getNodeType() {
				return 0;
			}

			@Override
			public Node getParentNode() {
				return null;
			}

			@Override
			public NodeList getChildNodes() {
				return null;
			}

			@Override
			public Node getFirstChild() {
				return null;
			}

			@Override
			public Node getLastChild() {
				return null;
			}

			@Override
			public Node getPreviousSibling() {
				return null;
			}

			@Override
			public Node getNextSibling() {
				return null;
			}

			@Override
			public NamedNodeMap getAttributes() {
				return null;
			}

			@Override
			public Document getOwnerDocument() {
				return null;
			}

			@Override
			public Node insertBefore(Node node, Node node1) throws DOMException {
				return null;
			}

			@Override
			public Node replaceChild(Node node, Node node1) throws DOMException {
				return null;
			}

			@Override
			public Node removeChild(Node node) throws DOMException {
				return null;
			}

			@Override
			public Node appendChild(Node node) throws DOMException {
				return null;
			}

			@Override
			public boolean hasChildNodes() {
				return false;
			}

			@Override
			public Node cloneNode(boolean b) {
				return null;
			}

			@Override
			public void normalize() {

			}

			@Override
			public boolean isSupported(String s, String s1) {
				return false;
			}

			@Override
			public String getNamespaceURI() {
				return null;
			}

			@Override
			public String getPrefix() {
				return null;
			}

			@Override
			public void setPrefix(String s) throws DOMException {

			}

			@Override
			public String getLocalName() {
				return null;
			}

			@Override
			public boolean hasAttributes() {
				return false;
			}

			@Override
			public String getBaseURI() {
				return null;
			}

			@Override
			public short compareDocumentPosition(Node node) throws DOMException {
				return 0;
			}

			@Override
			public String getTextContent() throws DOMException {
				return null;
			}

			@Override
			public void setTextContent(String s) throws DOMException {

			}

			@Override
			public boolean isSameNode(Node node) {
				return false;
			}

			@Override
			public String lookupPrefix(String s) {
				return null;
			}

			@Override
			public boolean isDefaultNamespace(String s) {
				return false;
			}

			@Override
			public String lookupNamespaceURI(String s) {
				return null;
			}

			@Override
			public boolean isEqualNode(Node node) {
				return false;
			}

			@Override
			public Object getFeature(String s, String s1) {
				return null;
			}

			@Override
			public Object setUserData(String s, Object o, UserDataHandler userDataHandler) {
				return null;
			}

			@Override
			public Object getUserData(String s) {
				return null;
			}
		};

		assertEquals(parser.getStringFromNodeUsingXPath(node, "/"), "");
	}

	@Test
	public void testNodeIsPresent() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<bob></bob>");
		assertTrue(parser.hasNode("/bob"));
	}

	@Test
	public void testNodeIsNotPresentAsRoot() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<bob><alice></alice></bob>");
		assertFalse(parser.hasNode("/alice"));
	}

	@Test
	public void testNodeIsPresentSomeWhere() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<bob><alice></alice></bob>");
		assertTrue(parser.hasNode("//alice"));
	}

	@Test
	public void testNodeIsNotPresentAtAll() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<bob></bob>");
		assertFalse(parser.hasNode("//alice"));
	}


	@Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "Invalid XPath")
	public void testMalformedXPathQueryToHasNode() throws Exception {
		XMLXPathParser parser = xmlxPathParser.forXML("<pid></pid>");
		parser.hasNode("this is not a valid XPath");
	}

}
