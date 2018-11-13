package se.uu.ub.cora.fedora.reader.xml;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.fedora.reader.FedoraReaderException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FedoraReaderPureTest {
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
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, null, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertEquals(httpHandlerSpy.getUrlCountCallFor(EXPECTED_OBJECT_URL), 0);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "404: " + SOME_OBJECT_ID + " not found.")
    public void testReadingAnObjectShouldThrowNotFoundIfNotFound() throws FedoraReaderException {
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, null, 404, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "418: failed ...")
    public void testReadingAnObjectShouldThrowIfNotOk() throws FedoraReaderException {
        httpHandlerSpy.addQueryResponse(EXPECTED_OBJECT_URL, null, 418, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readObject(SOME_OBJECT_ID);
    }

    @Test
    public void testReadingListWithDefaultMaxResults() throws FedoraReaderException {
        httpHandlerSpy.addQueryResponse(EXPECTED_LIST_URL, null, 1);

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
        httpHandlerSpy.addQueryResponse(expectedUrl, null, 1);

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
        httpHandlerSpy.addQueryResponse(expectedUrl, null, 418, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(failingType, EMPTY_FILTER);
    }


    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "404: someMissingType not found.")
    public void testReadingAListShouldThrowNotFoundIfNotFound() throws FedoraReaderException {
        var missingType = "someMissingType";
        var expectedUrl = expectedListUrl(missingType);
        httpHandlerSpy.addQueryResponse(expectedUrl, null, 404, 1);

        FedoraReaderPure reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.readList(missingType, EMPTY_FILTER);
    }

    @Test
    public void testReadingAListShouldYieldSomeStrings() throws FedoraReaderException {
        var pidList = getSomePidList(1, 2, 3, 4, 5);

        createPagedHttpHandlersForReadList(SOME_TYPE, pidList, 42);

        var listUrl = expectedListUrl(SOME_TYPE, 42);

        var reader = fedoraReaderPureFactory.factor(SOME_BASE_URL);
        reader.setMaxResults(42);

        var result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerSpy.getUrlCountCallFor(listUrl), 0);
        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidList.size());


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
                pidList.size(), maxResults);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, int pageSize, int maxResults) {
        createPagedHttpHandlersForReadList(type, pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                pageSize, maxResults);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, List<Integer> pidAccessCountList, int pageSize, int maxResults) {
        String typeQuery = expectedListUrl(type, maxResults);

        assert(pidList.size() == pidAccessCountList.size());
        for(int idx = 0; idx < pidList.size(); idx++) {
            createHandlerForPid(pidList.get(idx), pidAccessCountList.get(idx));
        }

        if (pidList.size() <= pageSize) {
            setupHandlersForRequestWithPidResult(
                    typeQuery,
                    SOME_TYPE_REQUEST_XML_RESPONSE + type,
                    1,false,
                    pidList);
        } else {
            setupHandlersForRequestWithPidResult(
                    typeQuery,
                    getTypeRequestQueryWithCursor(type, 0),
                    1,true,
                    pidList.subList(0, pageSize));

            int idx = 1;
            for (; (idx + 1) * pageSize < pidList.size(); idx++) {
                setupHandlersForRequestWithPidResult(
                        getTypeRequestQueryWithCursor(type, idx - 1),
                        getTypeRequestQueryWithCursor(type, idx),
                        1,true,
                        pidList.subList(pageSize * idx, pageSize * (idx + 1)));
            }

            setupHandlersForRequestWithPidResult(
                    getTypeRequestQueryWithCursor(type, idx - 1),
                    "finalXmlResponse",
                    1,false,
                    pidList.subList(pageSize * idx, pidList.size()));
        }
    }

    private void createHandlerForPid(String pid, int accessCount) {
        httpHandlerSpy.addQueryResponse(pid, SOME_PID_REQUEST_XML_RESPONSE + pid, accessCount);
    //    xmlXPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE + pid);
        //fedoraTypeRestQuerySpy.addQueryForId(pid, pid, accessCount);
    }

    private String getTypeRequestQueryWithCursor(String type, int cursor) {
        return SOME_TYPE_REQUEST_XML_RESPONSE + type + ":cursor:" + cursor;
    }

    private void setupHandlersForRequestWithPidResult(String httpRequestUrl, String httpXmlResponse, int expectedNumberOfCalls, boolean hasCursor, List<String> pidList) {
        httpHandlerSpy.addQueryResponse(httpRequestUrl, httpXmlResponse, expectedNumberOfCalls);
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
}
