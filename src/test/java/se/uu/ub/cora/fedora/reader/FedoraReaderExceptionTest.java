package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.Test;
import se.uu.ub.cora.fedora.reader.FedoraReaderException;

public class FedoraReaderExceptionTest {

    @Test(expectedExceptions = FedoraReaderException.class ,expectedExceptionsMessageRegExp = "bob")
    public void testThrow() throws FedoraReaderException {
        throw new FedoraReaderException("bob");
    }
}
