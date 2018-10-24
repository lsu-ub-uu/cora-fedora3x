package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataAtomic;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.data.SpiderReadResult;

import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FedoraReaderPaginationFilterTest extends FedoraReaderTestBase {
    private final static String SOME_XML_PID_LIST = "someXmlPidList";
    private static final String REQUEST_TYPE_LIST = "Converter URL for (someBaseUrl,someType)";

    @Test
    public void testFilterWithStartAsSomeValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,1,1,1);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), totalSize - start + 1);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testFilterWithStartAsSomeOtherValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,1,1,1,1);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), totalSize - start + 1);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testFilterWithRowsAsSomeValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(1,1,0,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var rows = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.empty(), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testFilterWithRowsAsSomeOtherValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(1,1,1,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var rows = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.empty(), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testFilterWithStartAndRowsAsValues() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,1,1,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 3;
        var rows = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testFilterWithStartAndRowsAsOtherValues() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,1,1,1,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 2;
        var rows = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultFilterWithStartAsSomeValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,1,1,1);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), totalSize - start + 1);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testPagedResultFilterWithStartAsSomeOtherValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,1,1,1,1);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), totalSize - start + 1);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testPagedResultFilterWithRowsAsSomeValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(1,1,0,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var rows = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.empty(), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultFilterWithRowsAsSomeOtherValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(1,1,1,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var rows = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.empty(), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultFilterWithStartAndRowsAsValues() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,1,1,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 3;
        var rows = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultFilterWithStartAndRowsAsOtherValues() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 3;
        List<String> somePidList = getSomePidList(2, 3, 5, 7, 11);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,1,1,1,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 2;
        var rows = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultWithMorePagesFilterWithStartAsSomeValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,1,1,1, 1,1,1,1,1, 1,1,1);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 3;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), totalSize - start + 1);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testPagedResultWithMorePagesFilterWithStartAsSomeOtherValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,0,0,0, 1,1,1,1,1, 1,1,1);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 6;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.empty());

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), totalSize - start + 1);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }


    @Test
    public void testPagedResultWithMorePagesFilterWithRowsAsSomeValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(1,1,0,0,0, 0,0,0,0,0, 0,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var rows = 2;
        var filter = createMinimumFilterWithStartAndRows(Optional.empty(), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultWithMorePagesFilterWithRowsAsSomeOtherValue() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(1,1,1,1,1, 1,1,0,0,0, 0,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var rows = 7;
        var filter = createMinimumFilterWithStartAndRows(Optional.empty(), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultWithMorePagesFilterWithStartAndRowsAsValues() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,0,1,1, 1,1,0,0,0, 0,0,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 4;
        var rows = 4;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    @Test
    public void testPagedResultWithMorePagesFilterWithStartAndRowsAsOtherValues() throws FedoraReaderException {
        FedoraReader reader = fedoraReaderFactory.factor();

        int pageSize = 5;
        List<String> somePidList = getSomePidList(
                2, 3, 5, 7, 11,
                13, 17, 19, 23, 27,
                29, 31, 37);
        List<Integer> shouldBeAccessed = getSomePidAccessCountList(0,0,0,0,0, 0,0,1,1,1, 1,1,0);
        int totalSize = somePidList.size();
        createPagedHttpHandlersForReadList(SOME_TYPE, somePidList, shouldBeAccessed, pageSize);

        var start = 8;
        var rows = 5;
        var filter = createMinimumFilterWithStartAndRows(Optional.of(start), Optional.of(rows));

        SpiderReadResult result = reader.readList(SOME_TYPE, filter);

        assertNotNull(result);
        assertNotNull(result.listOfDataGroups);
        assertEquals(result.listOfDataGroups.size(), rows);
        assertEquals(result.totalNumberOfMatches, totalSize);
        assertTrue(httpHandlerSpy.allCallsAccountedFor());
    }

    private DataGroup createMinimumFilterWithStartAndRows(Optional<Integer> start, Optional<Integer> rows) {
        DataGroup searchData = DataGroup.withNameInData("filter");
        start.ifPresent(integer ->
                searchData.addChild(DataAtomic.withNameInDataAndValue("start", String.valueOf(integer))));
        rows.ifPresent(integer ->
                searchData.addChild(DataAtomic.withNameInDataAndValue("rows", String.valueOf(integer))));
        return searchData;
    }
}
