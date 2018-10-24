package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.fedora.CoraLoggerSpy;
import se.uu.ub.cora.fedora.data.HttpHandlerFactorySpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactorySpy;
import se.uu.ub.cora.fedora.data.XMLXPathParserFactoryImp;

import static org.testng.Assert.assertNotNull;

public class FedoraReaderFactoryTest {
    private FedoraReaderFactory fedoraReaderFactory;

    @BeforeMethod
    public void init() {
        var httpHandlerFactory = new HttpHandlerFactorySpy();
        var fedoraReaderConverterFactory = new FedoraReaderConverterFactorySpy();
        var xmlXPathParserFactory = new XMLXPathParserFactoryImp();
        var coraLogger = new CoraLoggerSpy();
        fedoraReaderFactory = new FedoraReaderFactoryImp(fedoraReaderConverterFactory, httpHandlerFactory, xmlXPathParserFactory, "someBaseUrl", coraLogger);
    }

    @Test
    public void testGetFedoraReader() {
        assertNotNull(fedoraReaderFactory.factor());
    }

}
