package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.BeforeMethod;

import se.uu.ub.cora.fedora.CoraLoggerSpy;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.fedora.reader.converter.*;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class FedoraReaderTestBase {
    static final String SOME_TYPE = "someType";
    static final String SOME_OBJECT_ID = "someObjectId";

    static final String SOME_TYPE_QUERY = "someTypeQuery:";
    static final String SOME_PID_QUERY = "somePidQuery:";

    private static final String SOME_BASE_URL = "someBaseUrl";
    static final String SOME_TYPE_REQUEST_XML_RESPONSE = "someXmlTypeResponse:";
    static final String SOME_PID_REQUEST_XML_RESPONSE = "someXmlPidResponse:";

    FedoraReaderFactory fedoraReaderFactory;
    CoraLoggerSpy coraLogger;
    FedoraReaderConverterFactorySpy fedoraReaderConverterFactorySpy;
    FedoraReaderConverterSpy fedoraReaderConverterSpy;
    FedoraReadPositionConverterSpy fedoraReadPositionConverterSpy;
    FedoraTypeRestQuerySpy fedoraTypeRestQuerySpy;

    XMLXPathParserFactorySpy xmlXPathParserFactorySpy;
    XMLXPathParserSpy xmlxPathParserSpy;
    FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;

    HttpHandlerFactorySpy httpHandlerFactorySpy;
    HttpHandlerSpy httpHandlerSpy;


    @BeforeMethod
    public void init() {
        coraLogger = new CoraLoggerSpy();
        initiateDefaultFedoraReaderConverterFactory();
        initiateDefaultHttpHandlerFactory();

        xmlXPathParserFactorySpy = new XMLXPathParserFactorySpy();
        xmlxPathParserSpy = xmlXPathParserFactorySpy.parserSpy;
        fedoraReaderXmlHelperSpy = xmlXPathParserFactorySpy.helperSpy;
        XMLXPathParserFactory xmlxPathParserFactory = xmlXPathParserFactorySpy;
        HttpHandlerFactory httpHandlerFactory = httpHandlerFactorySpy;
        FedoraReaderConverterFactory fedoraReaderConverterFactory = fedoraReaderConverterFactorySpy;

        fedoraReaderFactory = new FedoraReaderFactoryImp(fedoraReaderConverterFactory,
                httpHandlerFactory, xmlxPathParserFactory, SOME_BASE_URL, coraLogger);
    }

    private void initiateDefaultFedoraReaderConverterFactory() {
        fedoraReadPositionConverterSpy = new FedoraReadPositionConverterSpy();

        fedoraTypeRestQuerySpy = new FedoraTypeRestQuerySpy(SOME_BASE_URL, SOME_TYPE);
        fedoraReadPositionConverterSpy.fedoraTypeRestQuerySpy = fedoraTypeRestQuerySpy;

        fedoraReaderConverterSpy = new FedoraReaderConverterSpy();
        fedoraReadPositionConverterSpy.fedoraReaderConverterSpy = fedoraReaderConverterSpy;

        fedoraReaderConverterFactorySpy = new FedoraReaderConverterFactorySpy();
        fedoraReaderConverterFactorySpy.fedoraReadPositionConverterSpy = fedoraReadPositionConverterSpy;
        fedoraReaderConverterFactorySpy.fedoraReadPositionConverterSpy.fedoraReaderConverterSpy = fedoraReaderConverterSpy;
    }

    private void initiateDefaultHttpHandlerFactory() {
        httpHandlerFactorySpy = new HttpHandlerFactorySpy();
        httpHandlerSpy = new HttpHandlerSpy();
        httpHandlerFactorySpy.httpHandlerSpy = httpHandlerSpy;
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

    void assertLogHasSingleMessage(String message) {
        assertEquals(coraLogger.getLog().size(), 1);
        assertEquals(coraLogger.getLog().get(0), message);
    }

    private String pidNameFromNumber(int number) {
        return String.format("somePid:%05d", number);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList) {
        createPagedHttpHandlersForReadList(type, pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                pidList.size());
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, int pageSize) {
        createPagedHttpHandlersForReadList(type, pidList,
                pidList.stream().map(itm -> 1).collect(Collectors.toList()),
                pageSize);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, List<Integer> pidAccessCountList, int pageSize) {
        String typeQuery = SOME_TYPE_QUERY + type;
        fedoraTypeRestQuerySpy.queryForType = typeQuery;

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
        xmlxPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE + pid);
        fedoraTypeRestQuerySpy.addQueryForId(pid, pid, accessCount);
    }

    private String getTypeRequestQueryWithCursor(String type, int cursor) {
        return SOME_TYPE_REQUEST_XML_RESPONSE + type + ":cursor:" + cursor;
    }

    private void setupHandlersForRequestWithPidResult(String httpRequestUrl, String httpXmlResponse, int expectedNumberOfCalls, boolean hasCursor, List<String> pidList) {
        httpHandlerSpy.addQueryResponse(httpRequestUrl, httpXmlResponse, expectedNumberOfCalls);
        xmlxPathParserSpy.addXml(httpXmlResponse);
        fedoraReaderXmlHelperSpy.addPidListForXml(httpXmlResponse, hasCursor, pidList);
    }


}
