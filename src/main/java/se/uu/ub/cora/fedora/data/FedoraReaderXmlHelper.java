package se.uu.ub.cora.fedora.data;

public interface FedoraReaderXmlHelper {
    @Deprecated
    FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException;
    FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(String xml) throws XMLXPathParserException;

    void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory);
    XMLXPathParserFactory getXmlXPathParseFactory();
}
