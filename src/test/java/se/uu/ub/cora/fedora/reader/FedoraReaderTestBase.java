package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.BeforeMethod;
import se.uu.ub.cora.fedora.data.*;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactorySpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterSpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderTestBase {
    static final String SOME_TYPE = "someType";
    static final String SOME_OBJECT_ID = "someObjectId";

    static final String SOME_TYPE_QUERY = "someTypeQuery:";
    static final String SOME_PID_QUERY = "somePidQuery:";

    static final String SOME_BASE_URL = "someBaseUrl";
    static final String SOME_TYPE_REQUEST_XML_RESPONSE = "someXmlTypeResponse:";
    static final String SOME_PID_REQUEST_XML_RESPONSE = "someXmlPidResponse:";

    FedoraReaderFactory fedoraReaderFactory;

    FedoraReaderConverterFactorySpy fedoraReaderConverterFactorySpy;
    FedoraReaderConverterSpy fedoraReaderConverterSpy;

    XMLXPathParserFactorySpy xmlxPathParserFactorySpy;
    XMLXPathParserSpy xmlxPathParserSpy;
    FedoraReaderXmlHelperSpy fedoraReaderXmlHelperSpy;

    HttpHandlerFactorySpy httpHandlerFactorySpy;
    HttpHandlerSpy httpHandlerSpy;


    @BeforeMethod
    public void init() {
        initiateDefaultFedoraReaderConverterFactory();
        initiateDefaultHttpHandlerFactory();

        xmlxPathParserFactorySpy = new XMLXPathParserFactorySpy();
        xmlxPathParserSpy = xmlxPathParserFactorySpy.parserSpy;
        fedoraReaderXmlHelperSpy = xmlxPathParserFactorySpy.helperSpy;
        XMLXPathParserFactory xmlxPathParserFactory = xmlxPathParserFactorySpy;
        HttpHandlerFactory httpHandlerFactory = httpHandlerFactorySpy;
        FedoraReaderConverterFactory fedoraReaderConverterFactory = fedoraReaderConverterFactorySpy;
        fedoraReaderFactory = new FedoraReaderFactoryImp(fedoraReaderConverterFactory, httpHandlerFactory, xmlxPathParserFactory, SOME_BASE_URL);
    }

    private void initiateDefaultFedoraReaderConverterFactory() {
        fedoraReaderConverterFactorySpy = new FedoraReaderConverterFactorySpy();
        fedoraReaderConverterSpy = new FedoraReaderConverterSpy(SOME_BASE_URL);
        fedoraReaderConverterFactorySpy.fedoraReaderConverterSpy = fedoraReaderConverterSpy;
    }

    private void initiateDefaultHttpHandlerFactory() {
        httpHandlerFactorySpy = new HttpHandlerFactorySpy();
        httpHandlerSpy = new HttpHandlerSpy();
        httpHandlerFactorySpy.httpHandlerSpy = httpHandlerSpy;
    }
}
