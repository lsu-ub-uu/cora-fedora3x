package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import static org.testng.Assert.assertEquals;

public class FedoraTypeRestQueryDefaultTest {
    private final static String SOME_BASE_URL = "someBaseUrl";
    private final static String SOME_TYPE = "someType";
    private final static String SOME_ID = "someId";
    private final static DataGroup EMPTY_FILTER = DataGroup.withNameInData("filter");

    @Test
    void testSetAndGetMaxResult() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        var maxResult = 7;
        fedoraTypeRequestQuery.setMaxResults(maxResult);
        assertEquals(fedoraTypeRequestQuery.getMaxResults(), maxResult);
    }

    @Test
    void testGetQueryForObjectId() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        assertEquals(fedoraTypeRequestQuery.type(), "magicDefaultType");
        assertEquals(fedoraTypeRequestQuery.getQueryForObjectId(SOME_ID),
                SOME_BASE_URL + "/objects/" + SOME_ID + "/datastreams/METADATA/content");
    }

    @Test
    void testGetQueryForList() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        var expectedUrl = SOME_BASE_URL +
                "/objects?pid=true&maxResults=100&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(fedoraTypeRequestQuery.type(), "magicDefaultType");
        assertEquals(fedoraTypeRequestQuery.getQueryForList(EMPTY_FILTER), expectedUrl);
    }

    @Test
    void testGetQueryForListWithMaxResult() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        var maxResult = 7;
        fedoraTypeRequestQuery.setMaxResults(maxResult);
        var expectedUrl = SOME_BASE_URL +
                "/objects?pid=true&maxResults=7&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(fedoraTypeRequestQuery.type(), "magicDefaultType");
        assertEquals(fedoraTypeRequestQuery.getQueryForList(EMPTY_FILTER), expectedUrl);
    }

    @Test
    void testGetQueryForListWithCursor() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        var cursor = new FedoraReaderCursor("someToken");
        var expectedUrl = SOME_BASE_URL +
                "/objects?sessionToken=someToken&pid=true&maxResults=100&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(fedoraTypeRequestQuery.type(), "magicDefaultType");
        assertEquals(fedoraTypeRequestQuery.getQueryForList(EMPTY_FILTER, cursor), expectedUrl);
    }

    @Test
    void testGetQueryForListWithCursorWithMaxResult() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        var cursor = new FedoraReaderCursor("someToken");
        var maxResult = 7;
        fedoraTypeRequestQuery.setMaxResults(maxResult);
        var expectedUrl = SOME_BASE_URL +
                "/objects?sessionToken=someToken&pid=true&maxResults=7&resultFormat=xml&query=pid%7E" +
                SOME_TYPE + ":*";
        assertEquals(fedoraTypeRequestQuery.type(), "magicDefaultType");
        assertEquals(fedoraTypeRequestQuery.getQueryForList(EMPTY_FILTER, cursor), expectedUrl);
    }

    @Test(expectedExceptions = NullPointerException.class)
    void testGetQueryForListWithNullCursor() {
        var fedoraTypeRequestQuery = new FedoraTypeRestQueryDefault(SOME_BASE_URL, SOME_TYPE);
        fedoraTypeRequestQuery.getQueryForList(EMPTY_FILTER, null);
    }
}
