package se.uu.ub.cora.fedora.reader;

import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterFactory;
import se.uu.ub.cora.fedora.data.XMLXPathParserFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderFactoryImp implements FedoraReaderFactory {
    private FedoraReaderConverterFactory fedoraReaderConverterFactory;
    private HttpHandlerFactory httpHandlerFactory;
    private XMLXPathParserFactory xmlXPathParserFactory;

    public FedoraReaderFactoryImp(FedoraReaderConverterFactory fedoraReaderConverterFactory, HttpHandlerFactory httpHandlerFactory, XMLXPathParserFactory xmlXPathParserFactory, String baseUrl) {
        this.fedoraReaderConverterFactory = fedoraReaderConverterFactory;
        this.fedoraReaderConverterFactory.setBaseUrl(baseUrl);
        this.httpHandlerFactory = httpHandlerFactory;
        this.xmlXPathParserFactory = xmlXPathParserFactory;
    }

    public FedoraReader factor() {
        return new FedoraReaderImp(fedoraReaderConverterFactory, httpHandlerFactory, xmlXPathParserFactory);
    }
}
