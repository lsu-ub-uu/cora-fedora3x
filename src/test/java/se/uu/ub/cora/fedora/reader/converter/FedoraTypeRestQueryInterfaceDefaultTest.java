package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import static org.testng.Assert.assertEquals;

public class FedoraTypeRestQueryInterfaceDefaultTest {
    private final static String SOME_BASE_URL = "someBaseUrl";
    private final static String SOME_TYPE = "someType";
    private final static String SOME_ID = "someId";
    private final static DataGroup EMPTY_FILTER = DataGroup.withNameInData("filter");

    @Test
    void testSetAndGetMaxResult() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        var maxResult = 7;
        trqid.setMaxResults(maxResult);
        assertEquals(trqid.getMaxResults(), maxResult);
    }

    @Test
    void testGetQueryForObjectId() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        assertEquals(trqid.type(), "magicDefaultType");
        assertEquals(trqid.getQueryForObjectId(SOME_ID),
                SOME_BASE_URL + "/objects/" + SOME_ID + "/datastreams/METADATA/content");
    }

    @Test
    void testGetQueryForList() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        var expectedUrl = SOME_BASE_URL +
                "/objects?pid=true&maxResults=100&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(trqid.type(), "magicDefaultType");
        assertEquals(trqid.getQueryForList(EMPTY_FILTER), expectedUrl);
    }

    @Test
    void testGetQueryForListWithMaxResult() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        var maxResult = 7;
        trqid.setMaxResults(maxResult);
        var expectedUrl = SOME_BASE_URL +
                "/objects?pid=true&maxResults=7&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(trqid.type(), "magicDefaultType");
        assertEquals(trqid.getQueryForList(EMPTY_FILTER), expectedUrl);
    }

    @Test
    void testGetQueryForListWithCursor() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        var cursor = new FedoraReaderCursor("someToken");
        var expectedUrl = SOME_BASE_URL +
                "/objects?sessionToken=someToken&pid=true&maxResults=100&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(trqid.type(), "magicDefaultType");
        assertEquals(trqid.getQueryForList(EMPTY_FILTER, cursor), expectedUrl);
    }

    @Test
    void testGetQueryForListWithCursorWithMaxResult() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        var cursor = new FedoraReaderCursor("someToken");
        var maxResult = 7;
        trqid.setMaxResults(maxResult);
        var expectedUrl = SOME_BASE_URL +
                "/objects?sessionToken=someToken&pid=true&maxResults=7&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(trqid.type(), "magicDefaultType");
        assertEquals(trqid.getQueryForList(EMPTY_FILTER, cursor), expectedUrl);
    }

    @Test(expectedExceptions = NullPointerException.class)
    void testGetQueryForListWithNullCursor() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        trqid.getQueryForList(EMPTY_FILTER, null);
    }
}
