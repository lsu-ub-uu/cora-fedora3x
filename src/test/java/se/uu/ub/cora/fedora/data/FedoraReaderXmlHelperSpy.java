package se.uu.ub.cora.fedora.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraReaderXmlHelperSpy implements FedoraReaderXmlHelper {
    public Map<String, List<String>> pidListsForXml;
    public boolean failPidExtraction;

    FedoraReaderXmlHelperSpy() {
        failPidExtraction = false;
        pidListsForXml = new HashMap<>();
    }

    public void addPidListForXml(String xml, List<String> pidList) {
        pidListsForXml.put(xml, pidList);
    }

    @Override
    public FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        return new FedoraReaderPidListWithOptionalCursor(extractPidList(xmlxPathParser), extractCursor(xmlxPathParser));
    }

    private List<String> extractPidList(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        if(failPidExtraction) {
            throw new XMLXPathParserException("pid extraction failed");
        }
        if(xmlxPathParser instanceof XMLXPathParserSpy) {
            var xmlPathParserSpy = (XMLXPathParserSpy) xmlxPathParser;
            String lastParsedXml = xmlPathParserSpy.getLastParsedXml();
            if(pidListsForXml.containsKey(lastParsedXml)) {
                return pidListsForXml.get(lastParsedXml);
            }
        }
        return new ArrayList<>();
    }

    private FedoraReaderCursor extractCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        return null;
    }
}
