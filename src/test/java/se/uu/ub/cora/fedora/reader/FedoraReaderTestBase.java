package se.uu.ub.cora.fedora.reader;

import org.testng.annotations.BeforeMethod;
import se.uu.ub.cora.fedora.data.HttpHandlerFactorySpy;
import se.uu.ub.cora.fedora.data.HttpHandlerSpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactorySpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterSpy;
import se.uu.ub.cora.fedora.data.XMLXPathParserFactory;
import se.uu.ub.cora.fedora.data.XMLXPathParserFactorySpy;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import java.util.Map;

public class FedoraReaderTestBase {
    static final String SOME_TYPE = "someType";
    static final String SOME_BASE_URL = "someBaseUrl";
    static final String SOME_GOOD_RESPONSE_XML = "someGoodResponseXML";

    FedoraReaderFactory fedoraReaderFactory;
    FedoraReaderConverterFactory fedoraReaderConverterFactory;
    HttpHandlerFactory httpHandlerFactory;
    XMLXPathParserFactory xmlxPathParserFactory;
    Map<String, FedoraReaderConverterSpy> fedoraConverterSpies;

    @BeforeMethod
    public void init() {
        var httpHandlerFactorySpy = new HttpHandlerFactorySpy();
        httpHandlerFactorySpy.urlHandlers.put("Converter URL for (someBaseUrl,someType)", getHttpHandlerSpy(SOME_GOOD_RESPONSE_XML));
        httpHandlerFactorySpy.urlHandlers.put("Converter URL for (someBaseUrl,someObjectId)", getHttpHandlerSpy(SOME_GOOD_RESPONSE_XML));
        httpHandlerFactory = httpHandlerFactorySpy;


        ((HttpHandlerFactorySpy) httpHandlerFactory).responseText = SOME_GOOD_RESPONSE_XML;
        fedoraReaderConverterFactory = new FedoraReaderConverterFactorySpy();
        fedoraReaderConverterFactory.setBaseUrl(SOME_BASE_URL);
        xmlxPathParserFactory = new XMLXPathParserFactorySpy();
        fedoraReaderFactory = new FedoraReaderFactoryImp(fedoraReaderConverterFactory, httpHandlerFactory, xmlxPathParserFactory, SOME_BASE_URL);
        fedoraConverterSpies = getFedoraReaderConverterFactorySpy().spies;
    }

    private HttpHandlerSpy getHttpHandlerSpy(String response) {
        HttpHandlerSpy defaultHttpForSomeType = new HttpHandlerSpy();
        defaultHttpForSomeType.responseText = response;
        return defaultHttpForSomeType;
    }

    HttpHandlerFactorySpy getHttpHandlerFactorySpy() {
        return (HttpHandlerFactorySpy) httpHandlerFactory;
    }

    FedoraReaderConverterFactorySpy getFedoraReaderConverterFactorySpy() {
        return (FedoraReaderConverterFactorySpy) fedoraReaderConverterFactory;
    }
}
