package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.HttpHandlerSpy;

import java.util.Optional;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class FedoraReaderPaginationFilterTest extends FedoraReaderTestBase {
    private final static String SOME_XML_PID_LIST = "someXmlPidList";
    private static final String REQUEST_TYPE_LIST = "Converter URL for (someBaseUrl,someType)";

    @Test
    public void testFilterWithStartAsSomeValue() throws FedoraReaderException {
//        FedoraReader reader = fedoraReaderFactory.factor();
//        httpHandlerSpy.addQueryResponse(REQUEST_TYPE_LIST, SOME_XML_PID_LIST);
//
//        HttpHandlerSpy objectHttpHandlerSpy = new HttpHandlerSpy();
//        httpHandlerFactorySpy.urlHandlers.put(pidRequestUrl("someObjectId"), objectHttpHandlerSpy);
//
//        setTotalNumberOfItemsForType(SOME_TYPE, 5);
//
//        var start = 3;
//        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());
//
//        var result = reader.readList(SOME_TYPE, filter);

//        var calledHandlers = httpHandlerSpy.urlHandlers.keySet();
//        assertEquals(calledHandlers, Set.of());

//        assertEquals(result.totalNumberOfMatches, 5);
//        assertEquals(result.listOfDataGroups, 3);
    }


//    @Test
//    public void testFilterWithStartAsSomeOtherValue() throws FedoraReaderException {
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        setTotalNumberOfItemsForType(SOME_TYPE, 5);
//
//        var start = 1;
//        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());
//
//        var result = reader.readList(SOME_TYPE, filter);
//
//        assertEquals(result.totalNumberOfMatches, 5);
//        assertEquals(result.listOfDataGroups, 5);
//    }


    private DataGroup createMinimumFilterWithStartAndRows(Optional<Integer> start, Optional<Integer> rows) {
        DataGroup searchData = DataGroup.withNameInData("filter");
        if(start.isPresent()) {
            searchData.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(start.get())));
        }
        if(rows.isPresent()) {
            searchData.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(rows.get())));
        }
        return searchData;
    }
//    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "XML cannot be converted to someType")
//    public void testReadListShouldSendSomeXMLToTheXMLParserAndIfItsUselessTheConverterShouldThrow() throws FedoraReaderException {
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        xmlxPathParserFactorySpy().uselessXml = true;
//
//        reader.readList(SOME_TYPE, EMPTY_FILTER);
//    }

    @Test
    public void testReadListShouldRegisterAsACallToHttpHandler() throws FedoraReaderException {
        var reader = fedoraReaderFactory.factor();

//        reader.readList(SOME_TYPE, EMPTY_FILTER);

//        assertEquals(getFactoredHttpHandlerSpies().size(), 1);

//        String actualFirstUrl = getActualUrlFromHttpHandlerFactorySpyForCall(0);
//        var expectedFirstUrl = getReadListUrlForCountAndType(DEFAULT_NUMBER_OF_OBJECTS_TO_READ, SOME_TYPE);
//        assertEquals(actualFirstUrl, expectedFirstUrl);

//        String actualSecondUrl = getActualUrlFromHttpHandlerFactorySpyForCall(2);
//        var expectedSecondUrl = SOME_BASE_URL + "objects/" + SOME_OBJECT_ID + "/datastreams/METADATA/content";
//        assertEquals(actualSecondUrl, expectedSecondUrl);
    }


//    @Test
//    public void testReadingAnObjectShouldLandSomeGoodXmlInTheOnlyConverterSpy() throws FedoraReaderException {
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        setupHttpHandlerCallResponse(SOME_TYPE_REQUEST_XML_RESPONSE);
//
//        reader.read(SOME_TYPE, SOME_OBJECT_ID);
//
//        assertEquals(fedoraConverterSpies.size(), 1);
//        assertTrue(fedoraConverterSpies.containsKey(SOME_TYPE));
//        assertEquals(fedoraConverterSpies.get(SOME_TYPE).convertedXml, SOME_TYPE_REQUEST_XML_RESPONSE);
//    }
//
//    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "someUnavailableType does not have a registered converter")
//    public void testReadObjectWithNoRegisteredConverterShouldThrow() throws FedoraReaderException {
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        blacklistConverterType(SOME_UNAVAILABLE_TYPE);
//
//        reader.read(SOME_UNAVAILABLE_TYPE, SOME_OBJECT_ID);
//    }
//
//    @Test(expectedExceptions = FedoraReaderException.class, expectedExceptionsMessageRegExp = "someBadXml cannot be converted")
//    public void testReadingObjectWithXmlThatCannotBeParsedShouldThrow() throws FedoraReaderException {
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        setupConverterToReceiveBadXml();
//
//        reader.read(SOME_TYPE, SOME_OBJECT_ID);
//    }
//
//    @Test
//    public void testReadingConvertedDataElement() throws FedoraReaderException {
//        DataElement someDataElement = DataAtomic.withNameInDataAndValue(SOME_OBJECT_ID, "someValue");
//        getFedoraReaderConverterFactorySpy().conversionResultForPid = someDataElement;
//
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        DataElement readResult = reader.read(SOME_TYPE, SOME_OBJECT_ID);
//
//        assertEquals(someDataElement, readResult);
//    }
//
//
//    @Test
//    public void testReadList() throws FedoraReaderException {
//        DataElement someDataElement = DataAtomic.withNameInDataAndValue(SOME_OBJECT_ID, "someValue");
//        getFedoraReaderConverterFactorySpy().conversionResultForPid = someDataElement;
//
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        DataElement readResult = reader.read(SOME_TYPE, SOME_OBJECT_ID);
//
//        assertEquals(someDataElement, readResult);
//    }
//
//
//    @Test
//    public void testReadListWithSomeData() throws FedoraReaderException {
//        DataElement someDataElement = DataAtomic.withNameInDataAndValue(SOME_OBJECT_ID, "someValue");
//        getFedoraReaderConverterFactorySpy().conversionResultForPid = someDataElement;
//
//        FedoraReader reader = fedoraReaderFactory.factor();
//
//        DataElement readResult = reader.read(SOME_TYPE, SOME_OBJECT_ID);
//
//        assertEquals(someDataElement, readResult);
//    }

}
