package se.uu.ub.cora.fedora.data;

public class XMLXPathParserFactoryImp implements XMLXPathParserFactory {
    @Override
    public XMLXPathParser factor() {
        return new XMLXPathParserImp();
    }

    @Override
    public FedoraReaderXmlHelper factorHelper() {
        return new FedoraReaderXmlHelperImp();
    }
}
