package se.uu.ub.cora.fedora.reader;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.data.SpiderReadResult;

import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

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

        assertEquals(fedoraReadPositionConverterSpy.getTypeCountFor(SOME_TYPE), 1);
    }


    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestFedoraConverterEvenTwice() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
        reader.read("someOtherType", SOME_OBJECT_ID);

        assertEquals(fedoraReadPositionConverterSpy.getTypeCountFor(SOME_TYPE), 1);
        assertEquals(fedoraReadPositionConverterSpy.getTypeCountFor("someOtherType"), 1);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestUrlForObjectIdFromFedoraConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        fedoraTypeRestQuerySpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY, 1);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(fedoraTypeRestQuerySpy.queryForIdCalls, 1);
        assertEquals(fedoraTypeRestQuerySpy.getPidCountFor(SOME_OBJECT_ID), 0);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldProduceExpectedUrlInFirstCallToTheOnlyFactoredHttpHandler() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraTypeRestQuerySpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY, 1);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);


        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);
        assertTrue(httpHandlerSpy.urlCalls.containsKey(SOME_PID_QUERY));
        assertEquals(httpHandlerSpy.getUrlCountCallFor(SOME_PID_QUERY), 0);
    }

    @Test
    public void testReadingAnObjectWithBadId() throws FedoraReaderException {
        fedoraTypeRestQuerySpy.badId = SOME_OBJECT_ID;
        FedoraReader reader = fedoraReaderFactory.factor();

        var result =  reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertNull(result);

        assertLogHasSingleMessage("Cannot create URL for someObjectId");
    }

    @Test
    public void testReadingAnObjectWithBadIdAndNoLogger() throws FedoraReaderException {
        fedoraTypeRestQuerySpy.badId = SOME_OBJECT_ID;
        coraLogger = null;
        FedoraReader reader = fedoraReaderFactory.factor();

        var result =  reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertNull(result);
    }

    @Test
    public void testLoggerWasPassedOnToFedoraReadPositionConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        reader.read(SOME_TYPE, SOME_OBJECT_ID);
        assertEquals(fedoraReadPositionConverterSpy.logger, coraLogger);
    }


    @Test
    public void testReadingAnObjectShouldFactorAnXMLPathParserInTheSpy() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(xmlXPathParserFactorySpy.factorCallCount, 1);
    }

    @Test
    public void testReadingAnObjectShouldTryToParseSomeXML() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraTypeRestQuerySpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY, 1);
        httpHandlerSpy.addQueryResponse(SOME_PID_QUERY, SOME_PID_REQUEST_XML_RESPONSE,1);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertTrue(xmlxPathParserSpy.wasAllXmlCalledAtLeastOnce());
        assertEquals(xmlxPathParserSpy.getLastParsedXml(), SOME_PID_REQUEST_XML_RESPONSE);
    }

    @Test
    public void testReadingAnObjectShouldTryToConvertReceivedXML() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraTypeRestQuerySpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY, 1);
        httpHandlerSpy.addQueryResponse(SOME_PID_QUERY, SOME_PID_REQUEST_XML_RESPONSE,1);
        xmlxPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE);

        DataGroup expected = DataGroup.withNameInData("someDataGroup");
        fedoraReaderConverterSpy.conversionResultForPid.put(SOME_PID_REQUEST_XML_RESPONSE, expected);

        var actual = reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(fedoraReaderConverterSpy.convertCalls, 1);
        assertEquals(actual, expected);
    }

    @Test
    public void testReadingAnObjectShouldThrowIfItFailsToConvertReceivedXML() throws FedoraReaderException {

        fedoraTypeRestQuerySpy.addQueryForId(SOME_OBJECT_ID, SOME_OBJECT_ID, 1);
        httpHandlerSpy.addQueryResponse(SOME_OBJECT_ID, SOME_PID_REQUEST_XML_RESPONSE + SOME_OBJECT_ID, 1);
        xmlxPathParserSpy.addXml(SOME_PID_REQUEST_XML_RESPONSE + SOME_OBJECT_ID);

        fedoraReaderConverterSpy.uselessXml = true;

        FedoraReader reader = fedoraReaderFactory.factor();

        assertNull(reader.read(SOME_TYPE, SOME_OBJECT_ID));
        assertLogHasSingleMessage("XML cannot be converted to someType");
    }

    @Test
    public void testReadObjectWithNoRegisteredConverterShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderConverterFactorySpy.noConverters = true;

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
        assertLogHasSingleMessage("someType does not have a registered converter");
    }

    @Test
    public void testReadingObjectWithXmlThatCannotBeParsedShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraTypeRestQuerySpy.addQueryForId(SOME_OBJECT_ID, SOME_PID_QUERY, 1);
        httpHandlerSpy.addQueryResponse(SOME_PID_QUERY, SOME_PID_REQUEST_XML_RESPONSE,1);
        xmlxPathParserSpy.addInvalidXml(SOME_PID_REQUEST_XML_RESPONSE);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertLogHasSingleMessage("Could not parse XML");
    }

    @Test
    public void testReadListShouldFactorAConverterForItsType() throws FedoraReaderException {
        var reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraReadPositionConverterSpy.getTypeCountFor(SOME_TYPE), 1);
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

        fedoraTypeRestQuerySpy.queryForType = SOME_TYPE_QUERY;
        httpHandlerSpy.addQueryResponse(SOME_TYPE_QUERY, SOME_TYPE_REQUEST_XML_RESPONSE, 1);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        Assert.assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1);

        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testReadListShouldFactorAnXMLParser() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(xmlXPathParserFactorySpy.factorCallCount, 1);
    }

    @Test
    public void testReadListShouldSendSomeXMLToTheXMLParser() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraTypeRestQuerySpy.queryForType = SOME_TYPE_QUERY;
        httpHandlerSpy.addQueryResponse(SOME_TYPE_QUERY, SOME_TYPE_REQUEST_XML_RESPONSE, 1);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(xmlXPathParserFactorySpy.factorCallCount, 1);
        assertEquals(xmlxPathParserSpy.getLastParsedXml(), SOME_TYPE_REQUEST_XML_RESPONSE);
    }

    @Test
    public void testReadListShouldSendSomeXMLToTheXMLParserAndIfItsBadAnErrorShouldBeLogged() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraTypeRestQuerySpy.queryForType = SOME_TYPE_QUERY;
        httpHandlerSpy.addQueryResponse(SOME_TYPE_QUERY, SOME_TYPE_REQUEST_XML_RESPONSE, 1);

        xmlxPathParserSpy.addInvalidXml(SOME_TYPE_REQUEST_XML_RESPONSE);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertLogHasSingleMessage("Could not parse XML");
    }

    @Test
    public void testReadListConverterShouldGetXMLToParsePidListFrom() throws FedoraReaderException {
        List<String> somePidList = getSomePidList(1, 2, 3);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraReaderConverterSpy.loadedXml.size(), 3);

        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testReadListConverterShouldGetXMLToParsePidListFromAndThrowIfPidListIsBad() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        fedoraReaderXmlHelperSpy.failPidExtraction = true;

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertLogHasSingleMessage("pid extraction failed");
    }

    @Test
    public void testReadListFindingAPidShouldYieldMoreCallsForUrlsToConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        String somePid = "somePid";

        List<String> somePidList = new ArrayList<>();
        somePidList.add(somePid);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraTypeRestQuerySpy.queryForTypeCalls, 1);

        assertEquals(fedoraTypeRestQuerySpy.getPidCountFor(somePid), 0);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testReadListFindingSeveralPidShouldYieldYetMoreCallsForUrlsToConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);


        assertEquals(fedoraTypeRestQuerySpy.queryForTypeCalls, 1);

        for (var somePid : somePidList) {
            assertEquals(fedoraTypeRestQuerySpy.getPidCountFor(somePid), 0);
        }

        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testReadListShouldLogAnErrorIfTheConverterFailsForAPid() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        fedoraReaderConverterSpy.failForPidInList.add(
            httpHandlerSpy.urlResponse.get(somePidList.get(2)));

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraTypeRestQuerySpy.queryForTypeCalls, 1);

        for (var somePid : somePidList) {
            assertEquals(fedoraTypeRestQuerySpy.getPidCountFor(somePid), 0);
        }

        assertTrue(httpHandlerSpy.allCallsAccountedFor());
        assertLogHasSingleMessage("XML cannot be converted to someType");
    }

    @Test
    public void testReadListShouldLogAnErrorIfTheConverterFailsForAPidButNotWithNoLogger() throws FedoraReaderException {
        coraLogger = null;
        fedoraReadPositionConverterSpy.logger = null;
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        fedoraReaderConverterSpy.failForPidInList.add(
                httpHandlerSpy.urlResponse.get(somePidList.get(2)));

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(fedoraTypeRestQuerySpy.queryForTypeCalls, 1);

        for (var somePid : somePidList) {
            assertEquals(fedoraTypeRestQuerySpy.getPidCountFor(somePid), 0);
        }

        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testReadListFindingSeveralPidShouldYieldYetMoreCallsForUrlsToHttpFactory() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        var pidCount = somePidList.size();

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(httpHandlerFactorySpy.factoredHttpHandlers, 1 + pidCount);
        assertEquals(xmlXPathParserFactorySpy.factorCallCount, 1 + pidCount);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
        assertEquals(fedoraReaderConverterSpy.convertCalls, pidCount);
    }

    @Test
    public void testReadListFindingAPidShouldYieldASpiderResultListWithAnElement() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(1);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), 1);
        assertEquals(result.totalNumberOfMatches, 1);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testReadListFindingAFewPidShouldYieldASpiderResultListWithAFewElements() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList);

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), somePidList.size());
        assertEquals(result.totalNumberOfMatches, somePidList.size());
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
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
        int totalSize = somePidList.size();
        assertEquals(result.listOfDataGroups.size(), totalSize);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testReadListWithCursorAndSeveralPages() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> somePidListAccessCounts = somePidList.stream().map(itm -> 1).collect(Collectors.toList());

        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, somePidListAccessCounts, pageSize);

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        int totalSize = somePidList.size();
        assertEquals(result.listOfDataGroups.size(), totalSize);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }



}
