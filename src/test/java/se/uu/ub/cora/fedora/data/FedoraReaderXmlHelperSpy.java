package se.uu.ub.cora.fedora.data;

import java.util.List;
import java.util.stream.Collectors;

public class FedoraReaderXmlHelperSpy implements FedoraReaderXmlHelper {
    @Override
    public FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        return new FedoraReaderPidListWithOptionalCursor(extractPidList(xmlxPathParser), extractCursor(xmlxPathParser));
    }

    private List<String> extractPidList(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        if(xmlxPathParser instanceof XMLXPathParserSpy) {
            var xmlPathParserSpy = (XMLXPathParserSpy) xmlxPathParser;
            if(xmlPathParserSpy.xmlPidList == null) {

                throw new XMLXPathParserException("pid extraction failed");
            }
            return xmlPathParserSpy.xmlPidList.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return null;
    }

    private FedoraReaderCursor extractCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        return null;
    }
}
