package se.uu.ub.cora.fedora.reader.xml;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelperSpy;
import se.uu.ub.cora.fedora.data.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import static org.testng.Assert.assertNotNull;

public class FedoraReaderPureFactoryTest {

    private FedoraReaderPureFactory fedoraReaderPureFactory;

    @BeforeMethod
    public void init() {
        HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactorySpy();
        FedoraReaderXmlHelper fedoraReaderXmlHelper = new FedoraReaderXmlHelperSpy();
        fedoraReaderPureFactory = new FedoraReaderPureFactoryImp(httpHandlerFactory, fedoraReaderXmlHelper);
    }

    @Test
    public void testGetFedoraReader() {
        assertNotNull(fedoraReaderPureFactory.factor("someBaseUrl"));
    }
}
