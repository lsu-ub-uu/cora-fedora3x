package se.uu.ub.cora.fedora.reader.xml;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.fedora.reader.FedoraReaderException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FedoraReaderPureTest {
    private static final String SOME_TOKEN = "someToken";
    FedoraReaderPureFactory fedoraReaderPureFactory;

    private HttpHandlerFactorySpy httpHandlerFactorySpy;
    private HttpHandlerSpy httpHandlerSpy;
    private FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;
    private XMLXPathParserFactorySpy xmlXPathParserFactorySpy;
    private XMLXPathParserSpy xmlXPathParserSpy;

    private static final String SOME_TYPE = "someType";
    private static final String SOME_OBJECT_ID = "someObjectId";

    private static final String SOME_TYPE_QUERY = "someTypeQuery:";
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
    public void testThatReadingAnObjectFactoredAnHttpHandlerWithCorrectUrl() throws FedoraReaderException {
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(EXPECTED_OBJECT_URL), 0);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "404: " + SOME_OBJECT_ID + " not found.")
    public void testReadingAnObjectShouldThrowNotFoundIfNotFound() throws FedoraReaderException {
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 404);
        callCountResponse.put(0, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, callCountResponse, responseCodes, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "418: failed ...")
    public void testReadingAnObjectShouldThrowIfNotOk() throws FedoraReaderException {
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        responseCodes.put(0, 418);
        callCountResponse.put(0, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, callCountResponse, responseCodes, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);
    }

    @Test
    public void testReadingListWithDefaultMaxResults() throws FedoraReaderException {
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(EXPECTED_LIST_URL), 0);
    }

    @Test
    public void testReadingListWithCustomMaxResults() throws FedoraReaderException {
        String expectedUrl =
                String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",
                        SOME_BASE_URL, 123, SOME_TYPE);
        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(123);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(expectedUrl), 0);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "418: failed ...")
    public void testReadingAListShouldThrowIfNotOk() throws FedoraReaderException {
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


    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "404: someMissingType not found.")
    public void testReadingAListShouldThrowNotFoundIfNotFound() throws FedoraReaderException {
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
    public void testReadingAListShouldYieldSomeStrings() throws FedoraReaderException {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(SOME_TYPE, pidList, 42);
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
    public void testPaginering() throws FedoraReaderException {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(SOME_TYPE, pidList, 3);


        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(3);

        var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        var listUrl = expectedListUrl(SOME_TYPE, 3);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        var listCursorUrl = expectedListUrlWithCursor(SOME_TYPE, 3, SOME_TOKEN);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(listCursorUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 2 + pidList.size());

        var pidResponseList = getExpectedResponse(pidList);
        assertEquals(result, pidResponseList);
    }

    private List<String> getExpectedResponse(List<String> pidList) {
        return pidList.stream().map(pid -> SOME_PID_REQUEST_XML_RESPONSE + pid).collect(Collectors.toList());
    }


    List<String> getSomePidList(Integer... integers) {
        return Arrays.stream(integers)
                .map(this::pidNameFromNumber)
                .collect(Collectors.toList());
    }

    List<Integer> getSomePidAccessCountList(Integer... integers) {
        return Arrays.stream(integers)
                .collect(Collectors.toList());
    }

    private String pidNameFromNumber(int number) {
        return String.format("somePid:%05d", number);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList) {
        createPagedHttpHandlersForReadList(type, pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                pidList.size(), DEFAULT_MAX_RESULTS);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, int maxResults) {
        createPagedHttpHandlersForReadList(type, pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                maxResults, maxResults);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, int pageSize, int maxResults) {
        createPagedHttpHandlersForReadList(type, pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                pageSize, maxResults);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, List<Integer> pidAccessCountList, int pageSize, int maxResults) {


        assert (pidList.size() == pidAccessCountList.size());
        for (int idx = 0; idx < pidList.size(); idx++) {
            createHandlerForPid(pidList.get(idx), pidAccessCountList.get(idx));
        }

        if (pidList.size() <= pageSize) {
            createResponsesForPage(type, pidList, maxResults, false);
        } else {
            createResponsesForPage(type, pidList.subList(0, pageSize), maxResults, true);

            var expectedNumberOfCalls = (int) Math.ceil((float) pidList.size() / (float) pageSize) - 1;
            var listCursorUrl = expectedListUrlWithCursor(SOME_TYPE, 3, SOME_TOKEN);
            var callCountResponse = new HashMap<Integer, String>();
            var responseCodes = new HashMap<Integer, Integer>();
            responseCodes.put(0, 200);
            callCountResponse.put(0, "bob");
            int idx = 1;
            fedoraReaderXmlHelperSpy.addPidListForXml("bob", false, pidList.subList(pageSize * idx, pidList.size()));


            httpHandlerSpy.addQueryResponse(listCursorUrl, callCountResponse, responseCodes, expectedNumberOfCalls);



           /* setupHandlersForRequestWithPidResult(
                    typeQuery,
                    getTypeRequestQueryWithCursor(type, 0, maxResults),
                    1, true,
                    pidList.subList(0, pageSize));

            int idx = 1;
            for (; (idx + 1) * pageSize < pidList.size(); idx++) {
                setupHandlersForRequestWithPidResult(
                        getTypeRequestQueryWithCursor(type, idx - 1, maxResults),
                        getTypeRequestQueryWithCursor(type, idx, maxResults),
                        1, true,
                        pidList.subList(pageSize * idx, pageSize * (idx + 1)));
            }

            setupHandlersForRequestWithPidResult(
                    getTypeRequestQueryWithCursor(type, idx - 1, maxResults),
                    "finalXmlResponse",
                    1, false,
                    pidList.subList(pageSize * idx, pidList.size()));*/
        }
    }

    private void createResponsesForPage(String type, List<String> pidList, int maxResults, boolean hasCursor) {
        String typeQuery = expectedListUrl(type, maxResults);
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();
        callCountResponse.put(0, SOME_TYPE_REQUEST_XML_RESPONSE);
        responseCodes.put(0, 200);
        httpHandlerSpy.addQueryResponse(typeQuery, callCountResponse, responseCodes, 1);
        fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE, hasCursor, pidList);
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

    private String getTypeRequestQueryWithCursor(String type, int cursor, int maxResults) {
        return expectedListUrlWithCursor(type, maxResults, "someToken");
    }

    private void setupHandlersForRequestWithPidResult(String httpRequestUrl, String httpXmlResponse, int expectedNumberOfCalls, boolean hasCursor, List<String> pidList) {
        Map<Integer, String> callCountResponse = new HashMap<>();
        Map<Integer, Integer> responseCodes = new HashMap<>();


        for (int i = 0; i < expectedNumberOfCalls; i++) {
            callCountResponse.put(i, SOME_PID_REQUEST_XML_RESPONSE + pidList.get(i));
            responseCodes.put(0, 200);
        }


        httpHandlerSpy.addQueryResponse(httpRequestUrl, callCountResponse, responseCodes, expectedNumberOfCalls);
        fedoraReaderXmlHelperSpy.addPidListForXml(httpXmlResponse, hasCursor, pidList);
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

    private String expectedListUrlWithCursor(String type, int maxResults, String token) {
        //listUrl=String.format("%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*",baseUrl, somePossibleCursorAndPidList.getCursor().getToken(),maxResults, type);
        return String.format("%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", SOME_BASE_URL, token, maxResults, type);
    }
}
