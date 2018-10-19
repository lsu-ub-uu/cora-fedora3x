package se.uu.ub.cora.fedora.reader;

import org.testng.Assert;
import org.testng.annotations.Test;

import se.uu.ub.cora.bookkeeper.data.DataElement;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.HttpHandlerSpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterSpy;
import se.uu.ub.cora.fedora.data.XMLXPathParserFactorySpy;
import se.uu.ub.cora.fedora.data.XMLXPathParserSpy;
import se.uu.ub.cora.spider.data.SpiderReadResult;

import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FedoraReaderEmptyFilterTest extends FedoraReaderTestBase {

    private static final String SOME_OBJECT_ID = "someObjectId";
    private static final DataGroup EMPTY_FILTER = DataGroup.withNameInData("filter");

    @Test
    public void testFactoringAReaderShouldYieldAReader() {
        FedoraReader reader = fedoraReaderFactory.factor();
        assertNotNull(reader);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestFedoraConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertFedoraReaderConverterSpyWasCreatedFor(SOME_TYPE);
    }

    private void assertFedoraReaderConverterSpyWasCreatedFor(String type) {
        assertTrue(fedoraConverterSpies.containsKey(type));
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestFedoraConverterEvenTwice() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
        reader.read("someOtherType", SOME_OBJECT_ID);

        assertFedoraReaderConverterSpyWasCreatedFor(SOME_TYPE);
        assertFedoraReaderConverterSpyWasCreatedFor("someOtherType");
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldRequestUrlForObjectIdFromFedoraConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).objectUrlRequests.size(), 1);
        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).objectUrlRequests.get(0), SOME_OBJECT_ID);
    }

    private FedoraReaderConverterSpy getFedoraConverterSpyForType(String type) {
        return fedoraConverterSpies.get(type);
    }

    @Test
    public void testReadingAnObjectWithIdAndTypeShouldProduceExpectedUrlInFirstCallToTheOnlyFactoredHttpHandlerSpy() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        Assert.assertEquals(getHttpHandlerFactorySpy().factoredHttpHandlers, 1);

        var expectedUrl = converterURLQuery(SOME_OBJECT_ID);
        assertTrue(getHttpHandlerFactorySpy().urlCalls.containsKey(expectedUrl));
    }

    private String converterURLQuery(String value) {
        return "Converter URL for (" + SOME_BASE_URL + "," + value + ")";
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "Cannot create URL for someObjectId")
    public void testReadingAnObjectWithBadId() throws FedoraReaderException {
        getFedoraReaderConverterFactorySpy().badId = SOME_OBJECT_ID;
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }


    @Test
    public void testReadingAnObjectShouldFactorAnXMLPathParserInTheSpy() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(getXmlXPathParserFactorySpy().factorCallCount, 1);
    }

    private XMLXPathParserFactorySpy getXmlXPathParserFactorySpy() {
        return (XMLXPathParserFactorySpy) xmlxPathParserFactory;
    }

    @Test
    public void testReadingAnObjectShouldTryToParseSomeXML() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
        assertEquals(getXmlXPathParserSpy().parsedXml.size(), 1);
        assertEquals(getXmlXPathParserSpy().getLastParsedXml(), SOME_GOOD_RESPONSE_XML);
    }

    private XMLXPathParserSpy getXmlXPathParserSpy() {
        return getXmlXPathParserFactorySpy().parserSpy;
    }

    @Test
    public void testReadingAnObjectShouldTryToConvertReceivedXML() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(fedoraConverterSpies.size(), 1);
        assertTrue(fedoraConverterSpies.containsKey(SOME_TYPE));
        assertEquals(fedoraConverterSpies.get(SOME_TYPE).xmlxPathParser, getXmlXPathParserSpy());
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "XML cannot be converted to someType")
    public void testReadingAnObjectShouldThrowIfItFailsToConvertReceivedXML() throws FedoraReaderException {
        getXmlXPathParserSpy().uselessXml = true;
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "someType does not have a registered converter")
    public void testReadObjectWithNoRegisteredConverterShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        blacklistConverterType(SOME_TYPE);

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }

    private void blacklistConverterType(String someType) {
        getFedoraReaderConverterFactorySpy().blacklistedConverters.add(someType);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "Could not parse XML")
    public void testReadingObjectWithBadXmlShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        getXmlXPathParserSpy().invalidXml= true;

        reader.read(SOME_TYPE, SOME_OBJECT_ID);
    }

    @Test
    public void testReadingConvertedDataElement() throws FedoraReaderException {
        DataGroup someDataElement = DataGroup.withNameInData(SOME_OBJECT_ID + "/someValue");
        getFedoraReaderConverterFactorySpy().conversionResult = someDataElement;

        FedoraReader reader = fedoraReaderFactory.factor();

        DataElement readResult = reader.read(SOME_TYPE, SOME_OBJECT_ID);

        assertEquals(someDataElement, readResult);
    }

    @Test
    public void testReadListShouldFactorAConverterForItsType() throws FedoraReaderException {
        var reader = fedoraReaderFactory.factor();
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertFedoraReaderConverterSpyWasCreatedFor(SOME_TYPE);
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "someType does not have a registered converter")
    public void testReadListShouldThrowIfItCannotFactorAConverterForItsType() throws FedoraReaderException {
        var reader = fedoraReaderFactory.factor();

        blacklistConverterType(SOME_TYPE);

        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadListWithTypeShouldProduceExpectedUrlInFirstCallToHttpHandlerFactorySpy() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        Assert.assertEquals(getHttpHandlerFactorySpy().factoredHttpHandlers, 1);

        var expectedUrl = converterURLQuery(SOME_TYPE);
        assertTrue(getHttpHandlerFactorySpy().urlCalls.containsKey(expectedUrl));
    }

    @Test
    public void testReadListShouldFactorAnXMLParser() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(getXmlXPathParserFactorySpy().factorCallCount, 1);
    }

    @Test
    public void testReadListShouldSendSomeXMLToTheXMLParser() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(SOME_GOOD_RESPONSE_XML, getXmlXPathParserSpy().parsedXml.get(0));
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "Could not parse XML")
    public void testReadListShouldSendSomeXMLToTheXMLParserAndIfItsBadTheParserShouldThrow() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        getXmlXPathParserSpy().invalidXml = true;

        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadListConverterShouldGetXMLToParsePidListFrom() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(1,2,3);
        getXmlXPathParserSpy().xmlPidList = somePidList;
        createHttpHandlersForPidCalls(somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).xmlPidList, somePidList);
        verifyHttpHandlerWasCalledForEveryPid(somePidList);
    }

    private List<String> getSomePidList(Integer... integers) {
        List<String> xmlPidSet = new ArrayList<>();
        for(var integer : integers) {
            xmlPidSet.add("somePid:0000" + integer);
        }
        return xmlPidSet;
    }

    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "pid extraction failed")
    public void testReadListConverterShouldGetXMLToParsePidListFromAndThrowIfPidListIsBad() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        getXmlXPathParserSpy().xmlPidList = null;

        reader.readList(SOME_TYPE, EMPTY_FILTER);
    }

    @Test
    public void testReadListFindingAPidShouldYieldMoreCallsForUrlsToConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(1);
        getXmlXPathParserSpy().xmlPidList = somePidList;
        createHttpHandlersForPidCalls(somePidList);

        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).listUrlRequests.size(), 1);
        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).objectUrlRequests.size(), 1);
        verifyHttpHandlerWasCalledForEveryPid(somePidList);
    }

    @Test
    public void testReadListFindingSeveralPidShouldYieldYetMoreCallsForUrlsToConverter() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        getXmlXPathParserSpy().xmlPidList = somePidList;
        createHttpHandlersForPidCalls(somePidList);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).listUrlRequests.size(), 1);
        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).objectUrlRequests.size(), 5);
        verifyHttpHandlerWasCalledForEveryPid(somePidList);
    }

    @Test
    public void testReadListFindingSeveralPidShouldYieldYetMoreCallsForUrlsToHttpFactory() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        var pidCount = somePidList.size();
        getXmlXPathParserSpy().xmlPidList = somePidList;
        createHttpHandlersForPidCalls(somePidList);
        reader.readList(SOME_TYPE, EMPTY_FILTER);

        Assert.assertEquals(getHttpHandlerFactorySpy().factoredHttpHandlers, 1 + pidCount);
        assertEquals(getXmlXPathParserFactorySpy().factorCallCount, 1 + pidCount);
        for(var idx = 0; idx < pidCount; idx++) {
            verifyHttpHandlerCalledForCorrectUrl(somePidList, idx);
            assertEquals(getXmlXPathParserSpy().parsedXml.get(idx), SOME_GOOD_RESPONSE_XML);
            assertEquals(fedoraConverterSpies.get(SOME_TYPE).loadedXml.get(idx), SOME_GOOD_RESPONSE_XML);
        }
        assertEquals(getFedoraConverterSpyForType(SOME_TYPE).convertCalls, pidCount);
        verifyHttpHandlerWasCalledForEveryPid(somePidList);
    }

    private void verifyHttpHandlerCalledForCorrectUrl(List<String> listOfIntegers, int idx) {
        var expectedUrl = converterURLQuery(String.valueOf(listOfIntegers.get(idx)));
        assertTrue(getHttpHandlerFactorySpy().urlCalls.containsKey(expectedUrl));
    }

    @Test
    public void testReadListFindingAPidShouldYieldASpiderResultListWithAnElement() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(1);
        getXmlXPathParserSpy().xmlPidList = somePidList;
        createHttpHandlersForPidCalls(somePidList);

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), 1);
        assertEquals(result.totalNumberOfMatches, 1);
        verifyHttpHandlerWasCalledForEveryPid(somePidList);
    }

    @Test
    public void testReadListFindingAFewPidShouldYieldASpiderResultListWithAFewElements() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        createHttpHandlersForPidCalls(somePidList);

        getXmlXPathParserSpy().xmlPidList = somePidList;

        SpiderReadResult result = reader.readList(SOME_TYPE, EMPTY_FILTER);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), somePidList.size());
        assertEquals(result.totalNumberOfMatches, somePidList.size());
        verifyHttpHandlerWasCalledForEveryPid(somePidList);
    }

    private void createHttpHandlersForPidCalls(List<String> pidList) {
        for(var pid : pidList) {
            HttpHandlerSpy httpHandlerSpy = new HttpHandlerSpy();
            httpHandlerSpy.responseText = SOME_GOOD_RESPONSE_XML;
            getHttpHandlerFactorySpy().urlHandlers.put("Converter URL for (someBaseUrl," + pid + ")", httpHandlerSpy);
        }
    }

    private void verifyHttpHandlerWasCalledForEveryPid(List<String> pidList) {
        for(var pid : pidList) {
            assertTrue(getHttpHandlerFactorySpy().getSpyFor("Converter URL for (someBaseUrl," + pid + ")").wasCalled);
        }
    }

}
