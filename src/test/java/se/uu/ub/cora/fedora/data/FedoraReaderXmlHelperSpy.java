package se.uu.ub.cora.fedora.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraReaderXmlHelperSpy implements FedoraReaderXmlHelper {
    public Map<String, List<String>> pidListsForXml;
    public Map<String, Boolean> pidListHasCursor;

    public boolean failPidExtraction;

    public FedoraReaderXmlHelperSpy() {
        failPidExtraction = false;
        pidListsForXml = new HashMap<>();
        pidListHasCursor = new HashMap<>();
    }

    public void addPidListForXml(String xml, boolean hasCursor, List<String> pidList) {
        pidListsForXml.put(xml, pidList);
        pidListHasCursor.put(xml, hasCursor);
    }

    @Override
    public FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        return new FedoraReaderPidListWithOptionalCursor(extractPidList(xmlxPathParser), extractCursor(xmlxPathParser));
    }

    @Override
    public FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(String xml) throws XMLXPathParserException {
        FedoraReaderCursor fedoraReaderCursor = getFedoraReaderCursor(xml);
        ArrayList<String> pidList = getPidList(xml);
        return new FedoraReaderPidListWithOptionalCursor(pidList, fedoraReaderCursor);
    }

    public ArrayList<String> getPidList(String xml) {
        var pidList = new ArrayList<String>();
        if (pidListsForXml.containsKey(xml)) {
            pidList.addAll(pidListsForXml.get(xml));
        } else {
            System.err.println("FedoraReaderXMLHelperSpy::pidListForXml does not contain key: " + xml);

        }
        return pidList;
    }

    public FedoraReaderCursor getFedoraReaderCursor(String xml) {
        FedoraReaderCursor fedoraReaderCursor = null;
        if (pidListHasCursor.getOrDefault(xml, false)) {
            fedoraReaderCursor = new FedoraReaderCursor("someToken");
        } else {
            System.err.println("FedoraReaderXMLHelperSpy::pidListHasCursor does not contain key: " + xml);
        }
        return fedoraReaderCursor;
    }

    @Override
    public void setXmlXPathParseFactory(XMLXPathParserFactory xmlXPathParserFactory) {

    }

    @Override
    public XMLXPathParserFactory getXmlXPathParseFactory() {
        return null;
    }

    private List<String> extractPidList(XMLXPathParser xmlxPathParser) throws XMLXPathParserException {
        if (failPidExtraction) {
            throw new XMLXPathParserException("pid extraction failed");
        }
        if (xmlxPathParser instanceof XMLXPathParserSpy) {
            var xmlPathParserSpy = (XMLXPathParserSpy) xmlxPathParser;
            String lastParsedXml = xmlPathParserSpy.getLastParsedXml();
            if (pidListsForXml.containsKey(lastParsedXml)) {
                return pidListsForXml.get(lastParsedXml);
            }
        }
        return new ArrayList<>();
    }

    private FedoraReaderCursor extractCursor(XMLXPathParser xmlxPathParser) {
        if (xmlxPathParser instanceof XMLXPathParserSpy) {
            var xmlPathParserSpy = (XMLXPathParserSpy) xmlxPathParser;
            String lastParsedXml = xmlPathParserSpy.getLastParsedXml();

            if (pidListHasCursor.getOrDefault(lastParsedXml, false)) {
                return new FedoraReaderCursor(lastParsedXml);
            }
        }
        return null;
    }
}
