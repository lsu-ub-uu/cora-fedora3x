package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.BeforeMethod;
import se.uu.ub.cora.fedora.CoraLogger;
import se.uu.ub.cora.fedora.CoraLoggerSpy;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactorySpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterSpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FedoraReaderTestBase {
    static final String SOME_TYPE = "someType";
    static final String SOME_OBJECT_ID = "someObjectId";

    static final String SOME_TYPE_QUERY = "someTypeQuery:";
    static final String SOME_PID_QUERY = "somePidQuery:";

    private static final String SOME_BASE_URL = "someBaseUrl";
    static final String SOME_TYPE_REQUEST_XML_RESPONSE = "someXmlTypeResponse:";
    static final String SOME_PID_REQUEST_XML_RESPONSE = "someXmlPidResponse:";

    FedoraReaderFactory fedoraReaderFactory;
    CoraLogger coraLogger;
    FedoraReaderConverterFactorySpy fedoraReaderConverterFactorySpy;
    FedoraReaderConverterSpy fedoraReaderConverterSpy;

    XMLXPathParserFactorySpy xmlxPathParserFactorySpy;
    XMLXPathParserSpy xmlxPathParserSpy;
    FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;

    HttpHandlerFactorySpy httpHandlerFactorySpy;
    HttpHandlerSpy httpHandlerSpy;


    @BeforeMethod
    public void init() {
        coraLogger = new CoraLoggerSpy();
        initiateDefaultFedoraReaderConverterFactory();
        initiateDefaultHttpHandlerFactory();

        xmlxPathParserFactorySpy = new XMLXPathParserFactorySpy();
        xmlxPathParserSpy = xmlxPathParserFactorySpy.parserSpy;
        fedoraReaderXmlHelperSpy = xmlxPathParserFactorySpy.helperSpy;
        XMLXPathParserFactory xmlxPathParserFactory = xmlxPathParserFactorySpy;
        HttpHandlerFactory httpHandlerFactory = httpHandlerFactorySpy;
        FedoraReaderConverterFactory fedoraReaderConverterFactory = fedoraReaderConverterFactorySpy;
        fedoraReaderFactory = new FedoraReaderFactoryImp(fedoraReaderConverterFactory, httpHandlerFactory, xmlxPathParserFactory, SOME_BASE_URL, coraLogger);
    }

    private void initiateDefaultFedoraReaderConverterFactory() {
        fedoraReaderConverterFactorySpy = new FedoraReaderConverterFactorySpy();
        fedoraReaderConverterSpy = new FedoraReaderConverterSpy();
        fedoraReaderConverterFactorySpy.fedoraReaderConverterSpy = fedoraReaderConverterSpy;
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

    private String pidNameFromNumber(int number) {
        return String.format("somePid:%05d", number);
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, List<Integer> pidAccessCountList, int pageSize) {
        String typeQuery = SOME_TYPE_QUERY + type;
        fedoraReaderConverterSpy.queryForType = typeQuery;

        assert(pidList.size() == pidAccessCountList.size());
        for(int idx = 0; idx < pidList.size(); idx++) {
            createHandlerForPid(pidList.get(idx), pidAccessCountList.get(idx));
        }

        if (pidList.size() < pageSize) {
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
        fedoraReaderConverterSpy.addQueryForId(pid, pid);
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
