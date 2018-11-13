package se.uu.ub.cora.fedora.reader.xml;

import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

public class FedoraReaderPureFactoryImp implements FedoraReaderPureFactory {
    private HttpHandlerFactory httpHandlerFactory;
    private FedoraReaderXmlHelper fedoraReaderXmlHelper;

    public FedoraReaderPureFactoryImp(HttpHandlerFactory httpHandlerFactory, FedoraReaderXmlHelper fedoraReaderXmlHelper) {
        this.httpHandlerFactory = httpHandlerFactory;
        this.fedoraReaderXmlHelper = fedoraReaderXmlHelper;
    }

    public FedoraReaderPure factor(String baseUrl) {
        return new FedoraReaderPureImp(httpHandlerFactory, fedoraReaderXmlHelper, baseUrl);
    }
}
