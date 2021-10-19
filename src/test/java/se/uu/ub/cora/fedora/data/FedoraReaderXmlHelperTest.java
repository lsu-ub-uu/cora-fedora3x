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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FedoraReaderXmlHelperTest {
	private FedoraReaderXmlHelperImp fedoraReaderXmlHelper;
	private XMLXPathParserFactory xmlXPathParserFactory;
	private String xmlForThreePidAndCursorAtZero;
	private String xmlEmptyWithOnlyRootResultElement;
	private String xmlWithEmptyResult;
	private String xmlWithBrokenCursorMissingToken;
	private String xmlWithBrokenCursorEmptyToken;
	private String xmlWithBrokenCursorMissingCursor;

	@BeforeMethod
	public void init() {
		xmlXPathParserFactory = new XMLXPathParserFactoryImp();
		fedoraReaderXmlHelper = new FedoraReaderXmlHelperImp();
		fedoraReaderXmlHelper.setXmlXPathParseFactory(xmlXPathParserFactory);
		xmlForThreePidAndCursorAtZero = resourceToString(
				"/xml/FedoraReadWithThreePidAndCursorAtZero.xml");
		xmlEmptyWithOnlyRootResultElement = resourceToString("/xml/FedoraReadWithNothing.xml");
		xmlWithEmptyResult = resourceToString("/xml/FedoraReadWithEmptyResult.xml");
		xmlWithBrokenCursorMissingToken = resourceToString(
				"/xml/FedoraReadWithBrokenCursorMissingToken.xml");
		xmlWithBrokenCursorEmptyToken = resourceToString(
				"/xml/FedoraReadWithBrokenCursorEmptyToken.xml");
		xmlWithBrokenCursorMissingCursor = resourceToString(
				"/xml/FedoraReadWithBrokenCursorMissingCursor.xml");
	}

	private String resourceToString(String resourceFile) {
		try (var file = getClass().getResourceAsStream(resourceFile);
				var stream = new InputStreamReader(file);
				var buffered = new BufferedReader(stream)) {
			return buffered.lines().collect(Collectors.joining());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Test
	public void testsSetXMLXPathParseFactory() {
		fedoraReaderXmlHelper.setXmlXPathParseFactory(xmlXPathParserFactory);
		assertEquals(fedoraReaderXmlHelper.getXmlXPathParseFactory(), xmlXPathParserFactory);
	}

	@Test
	public void testSetXMLXPathParseFactory() {
		fedoraReaderXmlHelper.setXmlXPathParseFactory(xmlXPathParserFactory);
		assertEquals(fedoraReaderXmlHelper.getXmlXPathParseFactory(), xmlXPathParserFactory);
	}

	@Test
	public void testStringGetPidListFromSomeData() {

		List<String> expectedPidList = new ArrayList<>();
		expectedPidList.add("alvin-place:15");
		expectedPidList.add("alvin-place:679");
		expectedPidList.add("alvin-place:692");

		var actualPidList = fedoraReaderXmlHelper.getPidList(xmlForThreePidAndCursorAtZero);

		assertEquals(actualPidList, expectedPidList);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "There was no resultList in given XML")
	public void testStringTryGetPidListFromMissingResult() {

		fedoraReaderXmlHelper.getPidList(xmlEmptyWithOnlyRootResultElement);
	}

	@Test
	public void testStringGetPidListFromEmptyResult() {
		var actualPidList = fedoraReaderXmlHelper.getPidList(xmlWithEmptyResult);

		assertNotNull(actualPidList);
		assertTrue(actualPidList.isEmpty());
	}

	@Test
	public void testStringGetCursorFromSomeData() {
		var expectedCursor = new FedoraReaderCursor("ba0a8ded8f13b71ee52155a3cbdbe34f");
		expectedCursor.setCursor("0");

		var actualCursor = fedoraReaderXmlHelper
				.getCursorIfAvailable(xmlForThreePidAndCursorAtZero);
		assertNotNull(actualCursor);
		assertEquals(actualCursor.getToken(), expectedCursor.getToken());
		assertEquals(actualCursor.getCursor(), expectedCursor.getCursor());
	}

	@Test
	public void testReturnNullIfCursorIsMissingFromXML() {
		FedoraReaderCursor cursor = fedoraReaderXmlHelper
				.getCursorIfAvailable(xmlEmptyWithOnlyRootResultElement);
		assertNull(cursor);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "malformed cursor")
	public void testStringGetNullCursorFromXmlWithoutBrokenCursorMissingToken() {
		fedoraReaderXmlHelper.getCursorIfAvailable(xmlWithBrokenCursorMissingToken);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "malformed cursor")
	public void testStringGetNullCursorFromXmlWithoutBrokenCursorEmptyToken() {
		fedoraReaderXmlHelper.getCursorIfAvailable(xmlWithBrokenCursorEmptyToken);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "malformed cursor")
	public void testStringGetNullCursorFromXmlWithoutBrokenCursorMissingCursor() {
		fedoraReaderXmlHelper.getCursorIfAvailable(xmlWithBrokenCursorMissingCursor);
	}

}
