package se.uu.ub.cora.fedora.data;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FedoraReaderCursorTest {

    public static final String SOME_CURSOR_TOKEN = "someCursorToken";
    public static final String SOME_CURSOR_POSITION = "someCursor";

    @Test
    public void initFedoraReaderCursor() {
        FedoraReaderCursor fedoraReaderCursor = new FedoraReaderCursor(SOME_CURSOR_TOKEN);
        assertNotNull(fedoraReaderCursor);
        assertEquals(fedoraReaderCursor.getToken(), SOME_CURSOR_TOKEN);
    }

    @Test
    public void testReadCursor() {
        FedoraReaderCursor fedoraReaderCursor = new FedoraReaderCursor(SOME_CURSOR_TOKEN);
        fedoraReaderCursor.setCursor(SOME_CURSOR_POSITION);
        assertEquals(fedoraReaderCursor.getCursor(), SOME_CURSOR_POSITION);
    }
}
