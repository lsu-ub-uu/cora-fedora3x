package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.CoraLoggerSpy;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FedoraReadPositionConverterTest {
    public static final String URL_REQUEST_FAILED = "";
    private final String SOME_BASE_URL = "baseUrl";
    private final String SOME_TYPE = "someType";
    private final String SOME_ID = "someId";
    private final DataGroup EMPTY_FILTER = DataGroup.withNameInData("filter");
    private List<String> data;
    private CoraLoggerSpy logger = new CoraLoggerSpy();
    private FedoraReaderConverterSpy fedoraReaderConverterSpy = new FedoraReaderConverterSpy();
    private FedoraTypeRestQuerySpy fedoraTypeRestQuerySpy = new FedoraTypeRestQuerySpy(SOME_BASE_URL, SOME_TYPE);

    private FedoraReadPositionConverter converter;

    @BeforeMethod
    void init() {
        data = new ArrayList<>();
        data.add("a");
        data.add("b");
        data.add("c");
        data.add("d");
        data.add("e");
        fedoraTypeRestQuerySpy.fail = false;
        converter = new FedoraReadPositionConverterImp(fedoraReaderConverterSpy, fedoraTypeRestQuerySpy);
    }

    @Test
    void testCreateFedoraReadPositionConverter() {
        assertNotNull(converter);
    }

    @Test
    void testGetConverter() {
        assertEquals(converter.getConverter(), fedoraReaderConverterSpy);
    }

    @Test
    void testGetQueryForObjectId() {
        fedoraTypeRestQuerySpy.addQueryForId(SOME_ID, SOME_ID, 1);
        assertEquals(converter.getQueryForObjectId(SOME_ID), SOME_ID);
    }

    @Test
    void testGetQueryForNullObjectId() {
        fedoraTypeRestQuerySpy.fail = true;
        var url = converter.getQueryForObjectId(null);
        assertEquals(logger.getLog().size(), 0);
        assertEquals(url, URL_REQUEST_FAILED);
    }

    @Test
    void testGetQueryForNullObjectIdAndLogError() {
        fedoraTypeRestQuerySpy.fail = true;
        converter.setLogger(logger);
        var url = converter.getQueryForObjectId(null);
        assertEquals(logger.getLog().size(), 1);
        assertEquals(logger.getLog().get(0), "Cannot create URL for null or empty Id");
        assertEquals(url, URL_REQUEST_FAILED);
    }

    @Test
    void testGetQueryForList() {
        fedoraTypeRestQuerySpy.queryForType = "someTypeUrl";
        assertEquals(converter.getQueryForList(EMPTY_FILTER), "someTypeUrl");
    }

    @Test(expectedExceptions = NullPointerException.class)
    void testGetQueryForListWithNullCursor() {
        converter.getQueryForList(EMPTY_FILTER, null);
    }

    @Test
    void testGetQueryForListWithCursor() {
        assertEquals(converter.getQueryForList(EMPTY_FILTER, new FedoraReaderCursor("someToken")), "someToken");
    }

    @Test
    void testConvertAll() {
        var result = converter.filterPidList(3, data);
        assertEquals(result,data);
    }

    @Test
    void testConvertAllWithFromStartConverter() {
        converter = new FedoraReadPositionFromStartConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 42);
        var result = converter.filterPidList(42, data);
        assertEquals(result,data);
    }

    @Test
    void testStartAfterFirstElementWithFromStartConverter() {
        converter = new FedoraReadPositionFromStartConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 45);
        var result = converter.filterPidList(42, data);
        assertEquals(result,data.subList(3,5));
    }

    @Test
    void testConvertNothingWithFromStartConverter() {
        converter = new FedoraReadPositionFromStartConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 45);
        var result = converter.filterPidList(3, data);
        assertEquals(result,List.of());
    }

    @Test
    void testConvertAllWithFromStartWithStopConverter() {
        converter = new FedoraReadPositionFromStartWithStopConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 42, 1337);
        var result = converter.filterPidList(42, data);
        assertEquals(result,data);
    }

    @Test
    void testStartAfterFirstElementWithFromStartWithStopConverter() {
        converter = new FedoraReadPositionFromStartWithStopConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 45, 1337);
        var result = converter.filterPidList(42, data);
        assertEquals(result,data.subList(3,5));
    }

    @Test
    void testConvertNothingFromBeforeStartWithFromStartWithStopConverter() {
        converter = new FedoraReadPositionFromStartWithStopConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 45, 1337);
        var result = converter.filterPidList(3, data);
        assertEquals(result,List.of());
    }

    @Test
    void testConvertAllFromStartToStopWithFromStartWithStopConverter() {
        converter = new FedoraReadPositionFromStartWithStopConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 42, 1337);
        var result = converter.filterPidList(1334, data);
        assertEquals(result,data.subList(0, 3));
    }

    @Test
    void testConvertFromStartToStopWithFromStartWithStopConverter() {
        converter = new FedoraReadPositionFromStartWithStopConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 45, 46);
        var result = converter.filterPidList(43, data);
        assertEquals(result,data.subList(2,3));
    }

    @Test
    void testConvertNothingFromAfterStopWithFromStartWithStopConverter() {
        converter = new FedoraReadPositionFromStartWithStopConverter(fedoraReaderConverterSpy,fedoraTypeRestQuerySpy, 45, 1337);
        var result = converter.filterPidList(6017, data);
        assertEquals(result,List.of());
    }

}
