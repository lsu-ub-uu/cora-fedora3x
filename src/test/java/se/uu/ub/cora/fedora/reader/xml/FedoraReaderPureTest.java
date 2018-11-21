package se.uu.ub.cora.fedora.reader.xml;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FedoraReaderPureTest {
    private static final String SOME_TOKEN = "someToken";
    private FedoraReaderPureFactory fedoraReaderPureFactory;

    private HttpHandlerFactorySpy httpHandlerFactorySpy;
    private HttpHandlerSpy httpHandlerSpy;
    private FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;

    private static final String SOME_TYPE = "someType";
    private static final String SOME_OBJECT_ID = "someObjectId";

    private static final String SOME_PID_QUERY = "somePidQuery:";

    private static final int DEFAULT_MAX_RESULTS = 100;

    private static final String SOME_BASE_URL = "someBaseUrl";

    private static final String SOME_TYPE_REQUEST_XML_RESPONSE = "someXmlTypeResponse:";
    private static final String SOME_PID_REQUEST_XML_RESPONSE = "someXmlPidResponse:";
    private static final String EXPECTED_OBJECT_URL = String.format("%s/objects/%s/datastreams/METADATA/content", SOME_BASE_URL, SOME_OBJECT_ID);
    private static final String EXPECTED_LIST_URL =
            String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", SOME_BASE_URL, DEFAULT_MAX_RESULTS, SOME_TYPE);

    private static final DataGroup EMPTY_FILTER = DataGroup.withNameInData("filter");

    @BeforeMethod
    void init() {
        httpHandlerSpy = new HttpHandlerSpy();
        httpHandlerFactorySpy = new HttpHandlerFactorySpy();
        httpHandlerFactorySpy.httpHandlerSpy = httpHandlerSpy;
        fedoraReaderXmlHelperSpy = new FedoraReaderXmlHelperSpy();
        fedoraReaderPureFactory = new FedoraReaderPureFactoryImp(httpHandlerFactorySpy, fedoraReaderXmlHelperSpy);
    }

    @Test
    void testFactoringAReaderShouldYieldAReader() {
        FedoraReaderPure fedoraReaderPure = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        assertNotNull(fedoraReaderPure);
    }

    @Test
    public void testThatReadingAnObjectFactoredAnHttpHandlerWithCorrectUrl() {
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(EXPECTED_OBJECT_URL), 0);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Fedora object not found: " + SOME_OBJECT_ID)
    public void testReadingAnObjectShouldThrowNotFoundIfNotFound() {
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 404);
        callCountResponse.put(0, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, callCountResponse, responseCodes, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Fedora call failed: 418")
    public void testReadingAnObjectShouldThrowIfNotOk() {
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 418);
        callCountResponse.put(0, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, callCountResponse, responseCodes, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);
    }

    @Test
    public void testReadingListWithDefaultMaxResults() {
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(EXPECTED_LIST_URL), 0);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Invalid XML")
    public void testReadingListWithBadXML() {
        fedoraReaderXmlHelperSpy.failPidExtraction = true;
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadingListWithCustomMaxResults() {
        String expectedUrl =
                String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
                        SOME_BASE_URL, 123, SOME_TYPE);
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(123);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(expectedUrl), 0);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Fedora call failed: 418")
    public void testReadingAListShouldThrowIfNotOk() {
        var failingType = "someFailingType";
        var expectedUrl = expectedListUrl(failingType);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 418);
        callCountResponse.put(0, SOME_PID_QUERY);

        httpHandlerSpy.addQueryResponse(expectedUrl, callCountResponse, responseCodes, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(failingType, EMPTY_FILTER);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Fedora object not found: someMissingType")
    public void testReadingAListShouldThrowNotFoundIfNotFound() {
        var missingType = "someMissingType";
        var expectedUrl = expectedListUrl(missingType);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 404);
        callCountResponse.put(0, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(expectedUrl, callCountResponse, responseCodes, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(missingType, EMPTY_FILTER);
    }

    @Test
    public void testReadingAListShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size());

        var pidResponseList = getExpectedResponse(pidList);
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + pidList.size());

        var pidResponseList = getExpectedResponse(pidList);
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 3 + pidList.size());

        var pidResponseList = getExpectedResponse(pidList);
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testReadingAListFromStartPositionShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int start = 2;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromStartPositionWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int start = 2;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromStartPositionWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int start = 2;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 3 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testReadingAListFromOtherStartPositionShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int start = 3;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromOtherStartPositionWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int start = 3;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromOtherStartPositionWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int start = 3;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 3 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testReadingAListLimitedByRowsPositionShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 4;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
        assertEquals(result, pidResponseList);
    }


    @Test
    public void testReadingAListWithRowLimitBeyondAvailableRowsShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 7;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size());

        var pidResponseList = getExpectedResponse(pidList);
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingLimitedByRowsWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int rows = 4;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingLimitedByRowsWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int rows = 6;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 1);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testReadingAListWithOtherRowLimitShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 5;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingWithOtherRowLimitWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int rows = 3;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 1);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingWithOtherRowLimitWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int rows = 7;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 3 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(0, rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testReadingAListFromStartLimitedByRowsPositionShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 3;
        int start = 1;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
        assertEquals(result, pidResponseList);
    }


    @Test
    public void testReadingAListFromStartWithRowLimitBeyondAvailableRowsShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 7;
        int start = 1;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromStartLimitedByRowsWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        var listCursorUrl = expectedListUrlWithCursor();

        int rows = 4;
        int start = 1;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromStartLimitedByRowsWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int rows = 6;
        int start = 1;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 3 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
        assertEquals(result, pidResponseList);
    }

    //TODO: marker

    @Test
    public void testReadingAListFromOtherStartLimitedByRowsPositionShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 1;
        int start = 2;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
        assertEquals(result, pidResponseList);
    }


    @Test
    public void testReadingAListFromOtherStartWithRowLimitBeyondAvailableRowsShouldYieldSomeStrings() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 42);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 200);
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        httpHandlerSpy.addQueryResponse(listUrl, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, false, pidList);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        int rows = 7;
        int start = 5;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size() - start);

        var pidResponseList = getExpectedResponse(pidList.subList(start, pidList.size()));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromOtherStartLimitedByRowsWithTwoPages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        var listCursorUrl = expectedListUrlWithCursor();

        int rows = 1;
        int start = 4;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));

        var result = reader.readList(SOME_TYPE, filter);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
        assertEquals(result, pidResponseList);
    }

    @Test
    public void testPagingFromOtherStartLimitedByRowsWithThreePages() {
        var pidList = getSomePidList(1, 2, 3, 4, 5, 6, 7, 8);

        createPagedHttpHandlersForReadList(pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        int rows = 5;
        int start = 2;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        var result = reader.readList(SOME_TYPE, filter);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor();
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 3 + rows);

        var pidResponseList = getExpectedResponse(pidList.subList(start, start + rows));
        assertEquals(result, pidResponseList);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Invalid start value \\(-12\\)")
    public void testPagingFromBrokenStartLimitedByRowsWithThreePages() {
        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);

        int rows = 5;
        int start = -12;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        reader.readList(SOME_TYPE, filter);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Invalid row count \\(-5\\)")
    public void testPagingFromStartLimitedByBrokenRowsWithThreePages() {
        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);

        int rows = -5;
        int start = 12;
        var filter = DataGroup.withNameInData("filter");
        filter.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows)));
        filter.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start)));
        reader.readList(SOME_TYPE, filter);
    }

    private List<String> getExpectedResponse(List<String> pidList) {
        return pidList.stream().map(pid -> SOME_PID_REQUEST_XML_RESPONSE + pid).collect(Collectors.toList());
    }

    private List<String> getSomePidList(Integer... integers) {
        return Arrays.stream(integers)
                .map(this::pidNameFromNumber)
                .collect(Collectors.toList());
    }

    private String pidNameFromNumber(int number) {
        return String.format("somePid:%05d", number);
    }

    private void createPagedHttpHandlersForReadList(List<String> pidList, int maxResults) {
        createPagedHttpHandlersForReadList(pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                maxResults, maxResults);
    }

    private void createPagedHttpHandlersForReadList(List<String> pidList, List<Integer> pidAccessCountList, int pageSize, int maxResults) {
        assert (pidList.size() == pidAccessCountList.size());
        for (int idx = 0; idx < pidList.size(); idx++) {
            createHandlerForPid(pidList.get(idx), pidAccessCountList.get(idx));
        }

        if (pidList.size() <= pageSize) {
            createResponsesForPage(pidList, maxResults);
        } else {

            var expectedNumberOfCalls = (int) Math.ceil((float) pidList.size() / (float) pageSize) - 1;

            httpHandlerSpy.addQueryResponse(expectedListUrl(FedoraReaderPureTest.SOME_TYPE, maxResults),
                    Map.of(0, SOME_TYPE_REQUEST_XML_RESPONSE),
                    Map.of(0, 200), 1);

            fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, true, pidList.subList(0, pageSize));


            int idx = expectedNumberOfCalls - 1;

            var callCountResponse = new HashMap<Integer, String>();
            var responseCodes = new HashMap<Integer, Integer>();

            for (; idx > 0; idx--) {
                responseCodes.put(idx, 200);
                callCountResponse.put(idx, SOME_TYPE_REQUEST_XML_RESPONSE + idx);
                fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE + idx,
                        true, pidList.subList(pageSize * (expectedNumberOfCalls - idx), pageSize * (expectedNumberOfCalls + 1 - idx)));
            }

            responseCodes.put(0, 200);
            callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE + 0);
            fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE + 0,
                    false, pidList.subList(pageSize * expectedNumberOfCalls, pidList.size()));

            var listCursorUrl = expectedListUrlWithCursor();

            httpHandlerSpy.addQueryResponse(listCursorUrl, callCountResponse, responseCodes, expectedNumberOfCalls);
        }
    }

    private void createResponsesForPage(List<String> pidList, int maxResults) {
        String typeQuery = expectedListUrl(FedoraReaderPureTest.SOME_TYPE, maxResults);

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
        httpHandlerSpy.addQueryResponse(expectedObjectUrl, callCountResponse, responseCodes, accessCount);
    }

    private String expectedObjectUrl(String pid) {
        return String.format("%s/objects/%s/datastreams/METADATA/content", SOME_BASE_URL, pid);
    }

    private String expectedListUrl(String type) {
        return expectedListUrl(type, DEFAULT_MAX_RESULTS);
    }

    private String expectedListUrl(String type, int maxResults) {
        return String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", SOME_BASE_URL, maxResults, type);
    }

    private String expectedListUrlWithCursor() {
        return String.format("%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", SOME_BASE_URL, FedoraReaderPureTest.SOME_TOKEN, 3, FedoraReaderPureTest.SOME_TYPE);
    }
}
