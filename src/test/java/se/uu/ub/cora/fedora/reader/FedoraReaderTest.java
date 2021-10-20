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
package se.uu.ub.cora.fedora.reader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.data.DataAtomicFactory;
import se.uu.ub.cora.data.DataAtomicProvider;
import se.uu.ub.cora.data.DataGroup;
import se.uu.ub.cora.data.DataGroupProvider;
import se.uu.ub.cora.dataspies.DataAtomicFactorySpy;
import se.uu.ub.cora.dataspies.DataGroupFactorySpy;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelperSpy;
import se.uu.ub.cora.fedora.data.HttpHandlerFactorySpy;
import se.uu.ub.cora.fedora.data.HttpHandlerSpy;

public class FedoraReaderTest {
	private static final String SOME_TOKEN = "someToken";
	private static final String SOME_TYPE = "someType";
	private static final String SOME_OBJECT_ID = "someObjectId";
	private static final int DEFAULT_MAX_RESULTS = 100;
	private static final String SOME_BASE_URL = "someBaseUrl";
	private static final String SOME_TYPE_REQUEST_XML_RESPONSE = "someXmlTypeResponse:";
	private static final String SOME_PID_REQUEST_XML_RESPONSE = "someXmlPidResponse:";
	private static final String EXPECTED_OBJECT_URL = createObjectUrlWithBaseUrlAndObjectId(
			SOME_BASE_URL, SOME_OBJECT_ID);

	private static final String EXPECTED_LIST_URL = String.format(
			"%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", SOME_BASE_URL,
			DEFAULT_MAX_RESULTS, SOME_TYPE);
	private DataGroup EMPTY_FILTER;
	private HttpHandlerFactorySpy httpHandlerFactorySpy;
	private HttpHandlerSpy httpHandlerSpy;
	private FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;
	private FedoraReader reader;
	private DataGroupFactorySpy dataGroupFactory;
	private DataAtomicFactory dataAtomicFactory;
	private DataGroup filter;
	private HttpHandlerFactorySpy2 httpHandlerFactorySpy2;
	private FedoraReaderXmlHelperSpy2 fedoraReaderXmlHelperSpy2;

	@BeforeMethod
	void init() {
		dataGroupFactory = new DataGroupFactorySpy();
		DataGroupProvider.setDataGroupFactory(dataGroupFactory);
		dataAtomicFactory = new DataAtomicFactorySpy();
		DataAtomicProvider.setDataAtomicFactory(dataAtomicFactory);

		EMPTY_FILTER = dataGroupFactory.factorUsingNameInData("filter");
		httpHandlerSpy = new HttpHandlerSpy();
		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		httpHandlerFactorySpy.httpHandlerSpy = httpHandlerSpy;
		fedoraReaderXmlHelperSpy = new FedoraReaderXmlHelperSpy();
		reader = FedoraReaderImp.usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
				httpHandlerFactorySpy, fedoraReaderXmlHelperSpy, SOME_BASE_URL);
		filter = dataGroupFactory.factorUsingNameInData("filter");

	}

	@Test
	public void testThatReadingAnObjectFactoredAnHttpHandlerWithCorrectUrl() {
		reader.readObject(SOME_OBJECT_ID);

		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 1);
		assertEquals(httpHandlerSpy.urlCall.pop(), EXPECTED_OBJECT_URL);
		assertTrue(httpHandlerSpy.urlCall.isEmpty());
	}

	@Test
	public void testThatReadingSomeOtherObjectFromOtherUrlFactoredAnHttpHandlerWithCorrectUrl() {
		String otherBaseUrl = "someOtherBaseUrl";
		String otherObjectId = "someOtherObjectId";
		reader = FedoraReaderImp.usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
				httpHandlerFactorySpy, fedoraReaderXmlHelperSpy, otherBaseUrl);

		reader.readObject(otherObjectId);

		assertEquals(httpHandlerSpy.urlCall.pop(),
				createObjectUrlWithBaseUrlAndObjectId(otherBaseUrl, otherObjectId));
	}

	private static String createObjectUrlWithBaseUrlAndObjectId(String baseUrl, String objectId) {
		return String.format("%s/objects/%s/datastreams/METADATA/content", baseUrl, objectId);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Fedora object not found: " + SOME_OBJECT_ID)
	public void testReadingAnObjectShouldThrowNotFoundIfNotFound() {
		httpHandlerSpy.urlCallResponseCode.push(404);

		reader.readObject(SOME_OBJECT_ID);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Fedora call failed: 418")
	public void testReadingAnObjectShouldThrowTheHttpResponseCodeIfNotOk() {
		httpHandlerSpy.urlCallResponseCode.push(418);

		reader.readObject(SOME_OBJECT_ID);
	}

	@Test
	public void testReadingListWithDefaultMaxResults() {
		int noOfCallsForPidList = 1;
		int noOfCallsOneForEachPid = DEFAULT_MAX_RESULTS;
		int totalNoOfCalls = noOfCallsForPidList + noOfCallsOneForEachPid;

		reader.readList(SOME_TYPE, EMPTY_FILTER);

		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, totalNoOfCalls);
		assertEquals(httpHandlerSpy.urlCall.elementAt(0), EXPECTED_LIST_URL);
		assertEquals(httpHandlerSpy.urlCall.elementAt(1),
				"someBaseUrl/objects/generatedPid:0/datastreams/METADATA/content");
		assertEquals(httpHandlerSpy.urlCall.elementAt(100),
				"someBaseUrl/objects/generatedPid:99/datastreams/METADATA/content");
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "FedoraReader: Bad XML:.+")
	public void testReadingListWithBadXML() {
		fedoraReaderXmlHelperSpy.failPidExtraction = true;
		reader.readList(SOME_TYPE, EMPTY_FILTER);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "FedoraReader: Bad XML:.+")
	public void testReadingFromStartRowsListWithBadXML() {
		fedoraReaderXmlHelperSpy.failPidExtraction = true;

		int rows = 1;
		int start = 4;

		filterAddRows(rows);
		filterAddStart(start);

		reader.readList(SOME_TYPE, filter);
	}

	private void filterAddStart(int start) {
		filter.addChild(
				dataAtomicFactory.factorUsingNameInDataAndValue("start", String.valueOf(start)));
	}

	private void filterAddRows(int rows) {
		filter.addChild(
				dataAtomicFactory.factorUsingNameInDataAndValue("rows", String.valueOf(rows)));
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Invalid XML")
	public void testReadingRowsListWithBadXML() {
		fedoraReaderXmlHelperSpy.failPidExtraction = true;

		int rows = 1;

		filterAddRows(rows);

		reader.readList(SOME_TYPE, filter);
	}

	@Test
	public void testReadingListWithCustomMaxResults() {
		String expectedUrl = String.format(
				"%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				SOME_BASE_URL, 123, SOME_TYPE);
		reader.setMaxResults(123);
		var results = reader.readList(SOME_TYPE, EMPTY_FILTER);

		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 124);
		assertEquals(httpHandlerSpy.urlCall.elementAt(0), expectedUrl);
		assertEquals(results.size(), 123);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "FedoraReader: Fedora call failed: 418")
	public void testReadingAListShouldThrowIfNotOk() {
		var failingType = "someFailingType";
		httpHandlerSpy.urlCallResponseCode.push(418);

		reader.readList(failingType, EMPTY_FILTER);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Fedora call failed: 418")
	public void testReadingAListWithRowLimitShouldThrowIfNotOk() {
		var failingType = "someFailingType";

		httpHandlerSpy.urlCallResponseCode.push(418);

		filterAddRows(42);

		reader.readList(failingType, filter);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Fedora call failed: 418")
	public void testReadingAListWithStartShouldThrowIfNotOk() {
		var failingType = "someFailingType";

		httpHandlerSpy.urlCallResponseCode.push(418);

		filterAddRows(42);

		reader.readList(failingType, filter);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "FedoraReader: Fedora object not found: someMissingType")
	public void testReadingAListShouldThrowNotFoundIfNotFound() {
		String missingType = "someMissingType";
		httpHandlerSpy.urlCallResponseCode.push(404);

		reader.readList(missingType, EMPTY_FILTER);
	}

	@Test
	public void testReadingAListShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		String listUrl = expectedListUrl(SOME_TYPE, 42);
		httpHandlerSpy.urlCallResponseCode.push(200);
		httpHandlerSpy.urlCallResponseText.push(listUrl + " xml");

		reader.setMaxResults(42);

		List<String> result = reader.readList(SOME_TYPE, EMPTY_FILTER);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 6);

		var pidResponseList = getExpectedResponse(pidList);
		assertEquals(result, pidResponseList);
	}

	private List<String> getExpectedResponse(List<String> pidList) {

		return pidList.stream()
				.map(pid -> String.format(
						"someBaseUrl/objects/%s/datastreams/METADATA/content xml response", pid))
				.collect(Collectors.toList());
	}

	private List<String> getSomePidList(Integer... integers) {
		return Arrays.stream(integers).map(this::pidNameFromNumber).collect(Collectors.toList());
	}

	private String pidNameFromNumber(int number) {
		return String.format("somePid:%05d", number);
	}

	private String expectedListUrl(String type, int maxResults) {
		return String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				SOME_BASE_URL, maxResults, type);
	}

	@Test
	public void testPagingWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + pidList.size());

		var pidResponseList = getExpectedResponse(pidList);
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 3 + pidList.size());

		var pidResponseList = getExpectedResponse(pidList);
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListFromStartPositionShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		reader.setMaxResults(42);

		int start = 2;
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		var firstListUrl = expectedListUrl(SOME_TYPE, 2);
		assertEquals(httpHandlerSpy.urlCall.elementAt(0), firstListUrl);
		var listCursorUrl = expectedListUrlWithCursor(42);
		assertEquals(httpHandlerSpy.urlCall.elementAt(1), listCursorUrl);

		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	private String expectedListUrlWithCursor(int maxResults) {
		return String.format(
				"%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				SOME_BASE_URL, FedoraReaderTest.SOME_TOKEN, maxResults, FedoraReaderTest.SOME_TYPE);
	}

	@Test
	public void testPagingFromStartPositionWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int start = 2;

		filterAddStart(start);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromStartPositionWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int start = 2;

		filterAddStart(start);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 3 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListFromOtherStartPositionShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		reader.setMaxResults(42);

		int start = 3;

		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromOtherStartPositionWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int start = 3;

		filterAddStart(start);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromOtherStartPositionWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int start = 3;

		filterAddStart(start);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 3 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListLimitedByRowsPositionShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 42);
		Map<Integer, String> callCountResponse = new HashMap<>();
		Map<Integer, Integer> responseCodes = new HashMap<>();
		responseCodes.put(0, 200);
		callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
		fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

		reader.setMaxResults(42);

		int rows = 4;

		filterAddRows(rows);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 1 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
		assertEquals(result, pidResponseList);
	}

	private void createPagedHttpHandlersForReadList(List<String> pidList, int maxResults) {
		createPagedHttpHandlersForReadList(pidList,
				pidList.stream().map(itm -> 1).collect(Collectors.toList()), maxResults,
				maxResults);
	}

	private void createPagedHttpHandlersForReadList(List<String> pidList,
			List<Integer> pidAccessCountList, int pageSize, int maxResults) {
		assert (pidList.size() == pidAccessCountList.size());
		for (int idx = 0; idx < pidList.size(); idx++) {
			createHandlerForPid(pidList.get(idx), pidAccessCountList.get(idx));
		}

		if (pidList.size() <= pageSize) {
			createResponsesForPage(pidList, maxResults);
		} else {

			var expectedNumberOfCalls = (int) Math.ceil((float) pidList.size() / (float) pageSize)
					- 1;

			httpHandlerSpy.addQueryResponse(expectedListUrl(FedoraReaderTest.SOME_TYPE, maxResults),
					Map.of(0, SOME_TYPE_REQUEST_XML_RESPONSE), Map.of(0, 200), 1);

			fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, true,
					pidList.subList(0, pageSize));

			int idx = expectedNumberOfCalls - 1;

			var callCountResponse = new HashMap<Integer, String>();
			var responseCodes = new HashMap<Integer, Integer>();

			for (; idx > 0; idx--) {
				responseCodes.put(idx, 200);
				callCountResponse.put(idx, SOME_TYPE_REQUEST_XML_RESPONSE + idx);
				fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE + idx,
						true, pidList.subList(pageSize * (expectedNumberOfCalls - idx),
								pageSize * (expectedNumberOfCalls + 1 - idx)));
			}

			responseCodes.put(0, 200);
			callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE + 0);
			fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE + 0, false,
					pidList.subList(pageSize * expectedNumberOfCalls, pidList.size()));

			var listCursorUrl = expectedListUrlWithCursor();

			httpHandlerSpy.addQueryResponse(listCursorUrl, callCountResponse, responseCodes,
					expectedNumberOfCalls);
		}
	}

	private void createResponsesForPage(List<String> pidList, int maxResults) {
		String typeQuery = expectedListUrl(FedoraReaderTest.SOME_TYPE, maxResults);

		Map<Integer, String> callCountResponse = new HashMap<>();
		Map<Integer, Integer> responseCodes = new HashMap<>();
		callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);
		responseCodes.put(0, 200);
		httpHandlerSpy.addQueryResponse(typeQuery, callCountResponse, responseCodes, 1);
		fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);
	}

	private void createHandlerForPid(String pid, int accessCount) {
		Map<Integer, String> callCountResponse = new HashMap<>();
		Map<Integer, Integer> responseCodes = new HashMap<>();
		for (int i = 0; i < accessCount; i++) {
			callCountResponse.put(i, SOME_PID_REQUEST_XML_RESPONSE + pid);
			responseCodes.put(i, 200);
		}

		String expectedObjectUrl = expectedObjectUrl(pid);
		httpHandlerSpy.addQueryResponse(expectedObjectUrl, callCountResponse, responseCodes,
				accessCount);
	}

	private String expectedObjectUrl(String pid) {
		return String.format("%s/objects/%s/datastreams/METADATA/content", SOME_BASE_URL, pid);
	}

	private String expectedListUrlWithCursor() {
		return expectedListUrlWithCursor(3);
	}

	@Test
	public void testReadingAListWithRowLimitBeyondAvailableRowsShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 42);
		Map<Integer, String> callCountResponse = new HashMap<>();
		Map<Integer, Integer> responseCodes = new HashMap<>();
		responseCodes.put(0, 200);
		callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
		fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

		reader.setMaxResults(42);

		int rows = 7;

		filterAddRows(rows);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 1 + pidList.size());

		var pidResponseList = getExpectedResponse(pidList);
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingLimitedByRowsWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int rows = 4;

		filterAddRows(rows);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingLimitedByRowsWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int rows = 6;

		filterAddRows(rows);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.urlCall.elementAt(0), listUrl);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.urlCall.elementAt(4), listCursorUrl);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListWithOtherRowLimitShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 42);
		Map<Integer, String> callCountResponse = new HashMap<>();
		Map<Integer, Integer> responseCodes = new HashMap<>();
		responseCodes.put(0, 200);
		callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
		fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

		reader.setMaxResults(42);

		int rows = 5;

		filterAddRows(rows);

		var result = reader.readList(SOME_TYPE, filter);
		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);

		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 1 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingWithOtherRowLimitWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		reader.setMaxResults(3);

		int rows = 3;

		filterAddRows(rows);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 1 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingWithOtherRowLimitWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int rows = 7;

		filterAddRows(rows);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 3 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListFromStartLimitedByRowsPositionShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		reader.setMaxResults(42);

		int rows = 3;
		int start = 1;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);
		var listUrl = expectedListUrlWithCursor(42);
		assertEquals(httpHandlerSpy.urlCall.elementAt(1), listUrl);

		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 5);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListFromStartWithRowLimitBeyondAvailableRowsShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		reader.setMaxResults(42);

		int rows = 7;
		int start = 1;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 6);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromStartLimitedByRowsWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);
		fedoraReaderXmlHelperSpy.pidList = pidList;

		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		var listCursorUrl = expectedListUrlWithCursor();

		int rows = 4;
		int start = 1;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 7);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromStartLimitedByRowsWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;
		createPagedHttpHandlersForReadList(pidList, 3);

		reader.setMaxResults(3);

		int rows = 6;
		int start = 1;

		filterAddRows(rows);
		filterAddStart(start);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 3);
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 3 + rows);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListFromOtherStartLimitedByRowsPositionShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		createPagedHttpHandlersForReadList(pidList, 42);
		Map<Integer, String> callCountResponse = new HashMap<>();
		Map<Integer, Integer> responseCodes = new HashMap<>();
		responseCodes.put(0, 200);
		callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
		fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

		reader.setMaxResults(42);

		int rows = 1;
		int start = 2;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 3);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testReadingAListFromOtherStartWithRowLimitBeyondAvailableRowsShouldYieldSomeStrings() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		var listUrl = expectedListUrl(SOME_TYPE, 42);

		reader.setMaxResults(42);

		int rows = 7;
		int start = 5;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
		assertEquals(httpHandlerFactorySpy.noOfFactoredHttpHandlers, 2 + pidList.size() - start);

		var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromOtherStartLimitedByRowsWithTwoPages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		reader.setMaxResults(3);

		var listUrl = expectedListUrl(SOME_TYPE, 4);
		var listCursorUrl = expectedListUrlWithCursor();

		int rows = 1;
		int start = 4;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);
		assertEquals(httpHandlerSpy.urlCall.elementAt(1), listCursorUrl);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromOtherStartLimitedByRowsWithTwoPagesAndHardPageSizeLimit() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7);

		fedoraReaderXmlHelperSpy.hardLimitOnMaxResults = 2;
		fedoraReaderXmlHelperSpy.pidList = pidList;

		reader.setMaxResults(3);

		var listUrl = expectedListUrl(SOME_TYPE, 5);
		var listCursorUrl = expectedListUrlWithCursor(3);

		int rows = 1;
		int start = 5;

		filterAddRows(rows);
		filterAddStart(start);

		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);
		assertEquals(httpHandlerSpy.urlCall.elementAt(1), listCursorUrl);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testPagingFromOtherStartLimitedByRowsWithThreePages() {
		var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

		fedoraReaderXmlHelperSpy.pidList = pidList;

		reader.setMaxResults(3);

		int rows = 5;
		int start = 2;

		filterAddRows(rows);
		filterAddStart(start);
		var result = reader.readList(SOME_TYPE, filter);

		var listUrl = expectedListUrl(SOME_TYPE, 2);
		assertEquals(httpHandlerSpy.urlCall.firstElement(), listUrl);

		var listCursorUrl = expectedListUrlWithCursor();
		assertEquals(httpHandlerSpy.urlCall.elementAt(1), listCursorUrl);

		var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
		assertEquals(result, pidResponseList);
	}

	@Test
	public void testAskingForZeroRowsShouldYieldEmptyResponse() {
		filterAddRows(0);
		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(result, List.of());
	}

	@Test
	public void testAskingForOneRowShouldYieldOneRow() {
		filterAddRows(1);
		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(result.size(), 1);
	}

	@Test
	public void testAskingForSomethingAfterTheEndOfThePidListShouldYieldAnEmptyList() {
		fedoraReaderXmlHelperSpy.pidList = List.of();

		filterAddStart(1);
		var result = reader.readList(SOME_TYPE, filter);

		assertEquals(result, List.of());
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Invalid start value \\(-12\\)")
	public void testPagingFromBrokenStartLimitedByRowsWithThreePages() {
		int rows = 5;
		int start = -12;

		filterAddRows(rows);
		filterAddStart(start);
		reader.readList(SOME_TYPE, filter);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Invalid row count \\(-1\\)")
	public void testPagingFromStartLimitedByBrokenRowsWithStartThreePages() {
		int rows = -1;

		filterAddRows(rows);
		reader.readList(SOME_TYPE, filter);
	}

	@Test
	public void testReadPidsForTypeOnlyOneSetOfResults() throws Exception {
		setUpBetterSpies();

		String type = "someType";

		List<String> listOfPids = reader.readPidsForType(type);

		assertNotNull(listOfPids);

		String urlToListPids = String.format(
				"%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
				SOME_BASE_URL, Integer.MAX_VALUE, SOME_TYPE);

		httpHandlerFactorySpy2.MCR.assertParameters("factor", 0, urlToListPids);
		HttpHandlerSpy2 handlerSpy = (HttpHandlerSpy2) httpHandlerFactorySpy2.MCR
				.getReturnValue("factor", 0);
		handlerSpy.MCR.assertMethodWasCalled("getResponseCode");
		String resultFromHttpHandler = (String) handlerSpy.MCR.getReturnValue("getResponseText", 0);
		fedoraReaderXmlHelperSpy2.MCR.assertParameters("getPidList", 0, resultFromHttpHandler);
		fedoraReaderXmlHelperSpy2.MCR.assertParameters("getSessionIfAvailable", 0,
				resultFromHttpHandler);
		assertEquals(listOfPids, fedoraReaderXmlHelperSpy2.MCR.getReturnValue("getPidList", 0));
	}

	public void setUpBetterSpies() {
		httpHandlerFactorySpy2 = new HttpHandlerFactorySpy2();
		fedoraReaderXmlHelperSpy2 = new FedoraReaderXmlHelperSpy2();
		reader = FedoraReaderImp.usingHttpHandlerFactoryAndFedoraReaderXmlHelperAndBaseUrl(
				httpHandlerFactorySpy2, fedoraReaderXmlHelperSpy2, SOME_BASE_URL);
	}

}
