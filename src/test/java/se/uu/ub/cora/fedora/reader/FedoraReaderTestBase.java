package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.BeforeMethod;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactorySpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterSpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import java.util.List;

public class FedoraReaderTestBase {
    static final String SOME_TYPE = "someType";
    static final String SOME_OBJECT_ID = "someObjectId";

    static final String SOME_TYPE_QUERY = "someTypeQuery:";
    static final String SOME_PID_QUERY = "somePidQuery:";

    private static final String SOME_BASE_URL = "someBaseUrl";
    static final String SOME_TYPE_REQUEST_XML_RESPONSE = "someXmlTypeResponse:";
    static final String SOME_PID_REQUEST_XML_RESPONSE = "someXmlPidResponse:";

    FedoraReaderFactory fedoraReaderFactory;

    FedoraReaderConverterFactorySpy fedoraReaderConverterFactorySpy;
    FedoraReaderConverterSpy fedoraReaderConverterSpy;

    XMLXPathParserFactorySpy xmlxPathParserFactorySpy;
    XMLXPathParserSpy xmlxPathParserSpy;
    FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;

    HttpHandlerFactorySpy httpHandlerFactorySpy;
    HttpHandlerSpy httpHandlerSpy;


    @BeforeMethod
    public void init() {
        initiateDefaultFedoraReaderConverterFactory();
        initiateDefaultHttpHandlerFactory();

        xmlxPathParserFactorySpy = new XMLXPathParserFactorySpy();
        xmlxPathParserSpy = xmlxPathParserFactorySpy.parserSpy;
        fedoraReaderXmlHelperSpy = xmlxPathParserFactorySpy.helperSpy;
        XMLXPathParserFactory xmlxPathParserFactory = xmlxPathParserFactorySpy;
        HttpHandlerFactory httpHandlerFactory = httpHandlerFactorySpy;
        FedoraReaderConverterFactory fedoraReaderConverterFactory = fedoraReaderConverterFactorySpy;
        fedoraReaderFactory = new FedoraReaderFactoryImp(fedoraReaderConverterFactory, httpHandlerFactory, xmlxPathParserFactory, SOME_BASE_URL);
    }

    private void initiateDefaultFedoraReaderConverterFactory() {
        fedoraReaderConverterFactorySpy = new FedoraReaderConverterFactorySpy();
        fedoraReaderConverterSpy = new FedoraReaderConverterSpy(SOME_BASE_URL);
        fedoraReaderConverterFactorySpy.fedoraReaderConverterSpy = fedoraReaderConverterSpy;
    }

    private void initiateDefaultHttpHandlerFactory() {
        httpHandlerFactorySpy = new HttpHandlerFactorySpy();
        httpHandlerSpy = new HttpHandlerSpy();
        httpHandlerFactorySpy.httpHandlerSpy = httpHandlerSpy;
    }

    void createPagedHttpHandlersForReadList(String type, List<String> pidList, List<Boolean> livePage, int pageSize) {
        String typeQuery = SOME_TYPE_QUERY + type;
        fedoraReaderConverterSpy.queryForType = typeQuery;

        int pidCount = pidList.size();
        if (pidCount < pageSize) {
            if(livePage.get(0)) {
                pidList.forEach(this::createExpectedHandlerForPid);
            } else {
                pidList.forEach(this::createUnexpectedHandlerForPid);
            }

            setupHandlersForRequestWithPidResult(
                    typeQuery,
                    SOME_TYPE_REQUEST_XML_RESPONSE + type,
                    1,false,
                    pidList);
        } else {
            List<String> livePidList = pidList.subList(0, pageSize);
            if(livePage.get(0)) {
                livePidList.forEach(this::createExpectedHandlerForPid);
            } else {
                livePidList.forEach(this::createUnexpectedHandlerForPid);
            }
            setupHandlersForRequestWithPidResult(
                    typeQuery,
                    getTypeRequestQueryWithCursor(type, 0),
                    1,true,
                    livePidList);

            int idx = 1;
            for (; (idx + 1) * pageSize < pidCount; idx++) {
                List<String> pidList1 = pidList.subList(pageSize * idx, pageSize * idx + 1);
                if(livePage.get(idx)) {
                    pidList1.forEach(this::createExpectedHandlerForPid);
                } else {
                    pidList1.forEach(this::createUnexpectedHandlerForPid);
                }
                setupHandlersForRequestWithPidResult(
                        getTypeRequestQueryWithCursor(type, idx - 1),
                        getTypeRequestQueryWithCursor(type, idx),
                        1,true,
                        pidList1);
            }
            List<String> pidList1 = pidList.subList(pageSize * idx, pidList.size());
            if(livePage.get(idx)) {
                pidList1.forEach(this::createExpectedHandlerForPid);
            } else {
                pidList1.forEach(this::createUnexpectedHandlerForPid);
            }
            pidList1.forEach(this::createUnexpectedHandlerForPid);

            setupHandlersForRequestWithPidResult(
                    getTypeRequestQueryWithCursor(type, idx - 1),
                    "finalXmlResponse",
                    1,false,
                    pidList1);
        }
    }

    private void createExpectedHandlerForPid(String pid) {
        httpHandlerSpy.addQueryResponse(pid, SOME_PID_REQUEST_XML_RESPONSE + pid, 1);
        xmlxPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE + pid);
        fedoraReaderConverterSpy.addQueryForId(pid, pid);
    }

    private void createUnexpectedHandlerForPid(String pid) {
        httpHandlerSpy.addQueryResponse(pid, SOME_PID_REQUEST_XML_RESPONSE + pid, 0);
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
