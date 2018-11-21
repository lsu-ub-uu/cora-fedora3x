package se.uu.ub.cora.fedora.data;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class FedoraReaderPidListWithOptionalCursorTest {
    @Test
    public void testInstantiationNoCursor() {
        List<String> pidList = new ArrayList<>();
        var queryParameters = new FedoraReaderPidListWithOptionalCursor(pidList);

        assertEquals(queryParameters.getPidList(), pidList);
        assertNull(queryParameters.getCursor());
    }

    @Test
    public void testInstantiation() {
        List<String> pidList = new ArrayList<>();
        FedoraReaderCursor cursor = new FedoraReaderCursor("Test");
        var queryParameters = new FedoraReaderPidListWithOptionalCursor(pidList, cursor);

        assertEquals(queryParameters.getPidList(), pidList);
        assertEquals(queryParameters.getCursor(), cursor);
    }
}
