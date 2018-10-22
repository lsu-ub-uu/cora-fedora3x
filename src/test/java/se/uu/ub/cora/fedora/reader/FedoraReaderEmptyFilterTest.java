package se.uu.ub.cora.fedora.reader;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterSpy;
import se.uu.ub.cora.spider.data.SpiderReadResult;

import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FedoraReaderEmptyFilterTest extends FedoraReaderTestBase {

    private static final DataGroup EMPTY_FILTER = DataGroup.withNameInData("filter");

    @BeforeMethod
    public void initEmptyFilterTest() {
    }

    @Test
    public void testFactoringAReaderShouldYieldAReader() {
        FedoraReader reader = fedoraReaderFactory.factor();
        assertNotNull(reader);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestFedoraConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(fedoraReaderConverterSpy.getTypeCountFor(SOME_TYPE), 1);
    }


    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestFedoraConverterEvenTwice() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
        reader.read("someOtherType", SOME_OBJECT_ID);

        assertEquals(fedoraReaderConverterSpy.getTypeCountFor(SOME_TYPE), 1);
        assertEquals(fedoraReaderConverterSpy.getTypeCountFor("someOtherType"), 1);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestUrlForObjectIdFromFedoraConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(fedoraReaderConverterSpy.queryForIdCalls, 1);
        assertEquals(fedoraReaderConverterSpy.getPidCountFor(SOME_OBJECT_ID), 1);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldProduceExpectedUrlInFirstCallToTheOnlyFactoredHttpHandler() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);


        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertTrue(httpHandlerSpy.urlCalls.containsKey(SOME_PID_QUERY));
        assertEquals(httpHandlerSpy.getUrlCountCallFor(SOME_PID_QUERY), 1);
    }

    private String converterURLQuery(String value) {
        return "Converter URL for (" + SOME_BASE_URL + "," + value + ")";
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "Cannot create URL for someObjectId")
    public void testReadingAnObjectWithBadId() throws FedoraReaderException {
        FedoraReaderConverterSpy fedoraReaderConverterSpy = new FedoraReaderConverterSpy("bob");
        fedoraReaderConverterSpy.badId = SOME_OBJECT_ID;

        fedoraReaderConverterFactorySpy.fedoraReaderConverterSpy = fedoraReaderConverterSpy;
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }


    @Test
    public void testReadingAnObjectShouldFactorAnXMLPathParserInTheSpy() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(xmlxPathParserFactorySpy.factorCallCount, 1);
    }

    @Test
    public void testReadingAnObjectShouldTryToParseSomeXML() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(SOME_PID_QUERY, SOME_PID_REQUEST_XML_RESPONSE);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertTrue(xmlxPathParserSpy.wasAllXmlCalledAtLeastOnce());
        assertEquals(xmlxPathParserSpy.getLastParsedXml(), SOME_PID_REQUEST_XML_RESPONSE);
    }

    @Test
    public void testReadingAnObjectShouldTryToConvertReceivedXML() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(SOME_PID_QUERY, SOME_PID_REQUEST_XML_RESPONSE);
        xmlxPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE);

        DataGroup expected = DataGroup.withNameInData("someDataGroup");
        fedoraReaderConverterSpy.conversionResultForPid.put(SOME_PID_REQUEST_XML_RESPONSE, expected);

        var actual = reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(fedoraReaderConverterSpy.convertCalls, 1);
        assertEquals(actual, expected);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "XML cannot be converted to someType")
    public void testReadingAnObjectShouldThrowIfItFailsToConvertReceivedXML() throws FedoraReaderException {
        fedoraReaderConverterSpy.uselessXml = true;

        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "someType does not have a registered converter")
    public void testReadObjectWithNoRegisteredConverterShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterFactorySpy.noConverters = true;

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "Could not parse XML")
    public void testReadingObjectWithXmlThatCannotBeParsedShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY);
        httpHandlerSpy.addQueryResponse(SOME_PID_QUERY, SOME_PID_REQUEST_XML_RESPONSE);
        xmlxPathParserSpy.addInvalidXml(SOME_PID_REQUEST_XML_RESPONSE);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }

    @Test
    public void testReadListShouldFactorAConverterForItsType() throws FedoraReaderException {
        var reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraReaderConverterSpy.getTypeCountFor(SOME_TYPE), 1);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "someType does not have a registered converter")
    public void testReadListShouldThrowIfItCannotFactorAConverterForItsType() throws FedoraReaderException {
        var reader = fedoraReaderFactory.factor();

        fedoraReaderConverterFactorySpy.noConverters = true;

        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadListWithTypeShouldProduceExpectedUrlInFirstCallToHttpHandlerSpy() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.queryForType = SOME_TYPE_QUERY;
        httpHandlerSpy.addQueryResponse(SOME_TYPE_QUERY, SOME_TYPE_REQUEST_XML_RESPONSE);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        Assert.assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);

        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }

    @Test
    public void testReadListShouldFactorAnXMLParser() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(xmlxPathParserFactorySpy.factorCallCount, 1);
    }

    @Test
    public void testReadListShouldSendSomeXMLToTheXMLParser() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.queryForType = SOME_TYPE_QUERY;
        httpHandlerSpy.addQueryResponse(SOME_TYPE_QUERY, SOME_TYPE_REQUEST_XML_RESPONSE);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(xmlxPathParserFactorySpy.factorCallCount, 1);
        assertEquals(xmlxPathParserSpy.getLastParsedXml(), SOME_TYPE_REQUEST_XML_RESPONSE);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "Could not parse XML")
    public void testReadListShouldSendSomeXMLToTheXMLParserAndIfItsBadTheParserShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterSpy.queryForType = SOME_TYPE_QUERY;
        httpHandlerSpy.addQueryResponse(SOME_TYPE_QUERY, SOME_TYPE_REQUEST_XML_RESPONSE);

        xmlxPathParserSpy.addInvalidXml(SOME_TYPE_REQUEST_XML_RESPONSE);

        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadListConverterShouldGetXMLToParsePidListFrom() throws FedoraReaderException {
        List<String> somePidList = getSomePidList(1, 2, 3);

        createHttpHandlersForReadList(SOME_TYPE, somePidList);

        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraReaderConverterSpy.loadedXml.size(), 3);

        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }

    private void createHttpHandlersForReadList(String type, List<String> pidList) {
        String typeQuery = SOME_TYPE_QUERY + type;
        fedoraReaderConverterSpy.queryForType = typeQuery;
        String typeResponseXml = SOME_TYPE_REQUEST_XML_RESPONSE + type;
        httpHandlerSpy.addQueryResponse(typeQuery, typeResponseXml);
        xmlxPathParserSpy.addXml(typeResponseXml);
        fedoraReaderXmlHelperSpy.addPidListForXml(typeResponseXml, pidList);

        for (var pid : pidList) {
            String pidQuery = SOME_PID_QUERY + pid;
            fedoraReaderConverterSpy.addQueryForId(pid, pidQuery);
            String pidResponseXml = SOME_PID_REQUEST_XML_RESPONSE + pid;
            httpHandlerSpy.addQueryResponse(pidQuery, pidResponseXml);
            xmlxPathParserSpy.addXml(pidResponseXml);
        }
    }

    private List<String> getSomePidList(Integer... integers) {
        List<String> xmlPidSet = new ArrayList<>();
        for (var integer : integers) {
            xmlPidSet.add("somePid:0000" + integer);
        }
        return xmlPidSet;
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "pid extraction failed")
    public void testReadListConverterShouldGetXMLToParsePidListFromAndThrowIfPidListIsBad() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderXmlHelperSpy.failPidExtraction = true;

        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadListFindingAPidShouldYieldMoreCallsForUrlsToConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        String somePid = "somePid";

        List<String> somePidList = new ArrayList<>();
        somePidList.add(somePid);

        createHttpHandlersForReadList(SOME_TYPE, somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraReaderConverterSpy.queryForTypeCalls, 1);

        assertEquals(fedoraReaderConverterSpy.getPidCountFor(somePid), 1);
        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }

    @Test
    public void testReadListFindingSeveralPidShouldYieldYetMoreCallsForUrlsToConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createHttpHandlersForReadList(SOME_TYPE, somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);


        assertEquals(fedoraReaderConverterSpy.queryForTypeCalls, 1);

        for (var somePid : somePidList) {
            assertEquals(fedoraReaderConverterSpy.getPidCountFor(somePid), 1);
        }

        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }

    @Test
    public void testReadListFindingSeveralPidShouldYieldYetMoreCallsForUrlsToHttpFactory() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        var pidCount = somePidList.size();

        createHttpHandlersForReadList(SOME_TYPE, somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        Assert.assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidCount);
        assertEquals(xmlxPathParserFactorySpy.factorCallCount, 1 + pidCount);
        assertTrue(httpHandlerSpy.allWasCalledOnce());
        assertEquals(fedoraReaderConverterSpy.convertCalls, pidCount);
    }

    @Test
    public void testReadListFindingAPidShouldYieldASpiderResultListWithAnElement() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(1);

        createHttpHandlersForReadList(SOME_TYPE, somePidList);

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), 1);
        assertEquals(result.totalNumberOfMatches, 1);
        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }

    @Test
    public void testReadListFindingAFewPidShouldYieldASpiderResultListWithAFewElements() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createHttpHandlersForReadList(SOME_TYPE, somePidList);

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), somePidList.size());
        assertEquals(result.totalNumberOfMatches, somePidList.size());
        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }


    @Test
    public void testReadListWithCursor() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, pageSize);


        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), somePidList.size());
        assertEquals(result.totalNumberOfMatches, somePidList.size());
        assertTrue(httpHandlerSpy.allWasCalledOnce());
    }

    private void createHandlerForPid(String pid) {
        httpHandlerSpy.addQueryResponse(pid, SOME_PID_REQUEST_XML_RESPONSE + pid);
        xmlxPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE + pid);
    }

    private String getTypeRequestQueryWithCursor(String type, int cursor) {
        return SOME_TYPE_REQUEST_XML_RESPONSE + type + ":cursor:" + cursor;
    }

    private void createPagedHttpHandlersForReadList(String type, List<String> pidList, int pageSize) {
        String typeQuery = SOME_TYPE_QUERY + type;
        fedoraReaderConverterSpy.queryForType = typeQuery;

        pidList.forEach(this::createHandlerForPid);

        int pidCount = pidList.size();
        if (pidCount < pageSize) {
            httpHandlerSpy.addQueryResponse(typeQuery, SOME_TYPE_REQUEST_XML_RESPONSE + type);
            xmlxPathParserSpy.addXml(SOME_TYPE_REQUEST_XML_RESPONSE + type);
            fedoraReaderXmlHelperSpy.addPidListForXml(SOME_TYPE_REQUEST_XML_RESPONSE + type, pidList);
        } else {
            String typeRequestQueryWithCursor = getTypeRequestQueryWithCursor(type, 0);
            httpHandlerSpy.addQueryResponse(typeQuery, typeRequestQueryWithCursor);
            xmlxPathParserSpy.addXml(typeRequestQueryWithCursor);
            fedoraReaderXmlHelperSpy.addPidListForXml(typeRequestQueryWithCursor, pidList.subList(0,pageSize));
            int idx = 1;
            for (; (idx + 1) * pageSize < pidCount; idx++) {
                String xmlResponse = getTypeRequestQueryWithCursor(type, idx);
                httpHandlerSpy.addQueryResponse(getTypeRequestQueryWithCursor(type, idx - 1),
                        xmlResponse);
                xmlxPathParserSpy.addXml(xmlResponse);
                fedoraReaderXmlHelperSpy.addPidListForXml(xmlResponse, pidList.subList(pageSize * idx, pageSize * idx + 1));
            }
            String finalXmlResponse = "finalXmlResponse";
            httpHandlerSpy.addQueryResponse(getTypeRequestQueryWithCursor(type, idx - 1), finalXmlResponse);
            xmlxPathParserSpy.addXml(finalXmlResponse);
            fedoraReaderXmlHelperSpy.addPidListForXml(finalXmlResponse, pidList.subList(pageSize * idx, pidList.size()));
        }
    }
}
