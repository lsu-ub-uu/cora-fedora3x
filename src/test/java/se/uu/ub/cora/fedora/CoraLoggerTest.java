package se.uu.ub.cora.fedora;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CoraLoggerTest {
    @Test
    void initLogger() {
        CoraLogger logger = new CoraLogger() {
            @Override
            public void write(String logMessage) {
                assertEquals(logMessage, "bob");
            }
        };
        logger.write("bob");
    }

}
