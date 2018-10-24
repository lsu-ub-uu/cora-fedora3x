package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FedoraTypeRestQueryInterfaceDefaultTest {
    private final static String SOME_BASE_URL = "someBaseUrl";
    private final static String SOME_TYPE = "someType";

    @Test
    void initTRQID() {
        var trqid = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_TYPE);
        assertEquals(trqid.type(), "magicDefaultType");
    }
}
