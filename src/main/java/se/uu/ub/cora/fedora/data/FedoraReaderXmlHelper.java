package se.uu.ub.cora.fedora.data;

import java.util.List;

public interface FedoraReaderXmlHelper {
    FedoraReaderCursor getCursorIfAvailable(String xml);
    List<String> getPidList(String xml);
    void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory);
    XMLXPathParserFactory getXmlXPathParseFactory();
}
