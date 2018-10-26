package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;
import se.uu.ub.cora.fedora.data.XMLXPathParserSpy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraReaderConverterSpy extends FedoraReaderConverter {
    public static final String defaultType = "someSpyType";

    public boolean uselessXml;

    public XMLXPathParser xmlxPathParser;
    public Map<String, DataGroup> conversionResultForPid;

    public List<String> failForPidInList;
    public List<String> xmlPidList;
    public List<String> loadedXml;
    public int convertCalls = 0;
    private String requestedPid;
    private String possiblyPidForConverter;

    protected String factoredType;

    public FedoraReaderConverterSpy() {
        super();

        uselessXml = false;
        loadedXml = new ArrayList<>();
        xmlPidList = new ArrayList<>();
        conversionResultForPid = new HashMap<>();
        failForPidInList = new ArrayList<>();
        requestedPid = null;

    }

    @Override
    public boolean loadXml(XMLXPathParser xmlxPathParser) {
        if(xmlxPathParser instanceof  XMLXPathParserSpy){
            var xmlParserSpy = (XMLXPathParserSpy) xmlxPathParser;
            String lastParsedXml = xmlParserSpy.getLastParsedXml();
            loadedXml.add(lastParsedXml);
            possiblyPidForConverter = lastParsedXml;
            if(requestedPid != null) {
                xmlPidList.add(requestedPid);
                requestedPid = null;
            }
        }
        this.xmlxPathParser = xmlxPathParser;
        return false;
    }

    @Override
    public DataGroup convert() throws FedoraReaderConverterException {
        convertCalls += 1;
        if(uselessXml || failForPidInList.contains(possiblyPidForConverter)) {
            throw new FedoraReaderConverterException("XML cannot be converted to " + type());
        }
        if(conversionResultForPid.containsKey(possiblyPidForConverter)) {
            return conversionResultForPid.get(possiblyPidForConverter);
        }
        return null;
    }

    @Override
    public String type() {
        if(factoredType == null) {
            return defaultType;
        }
        return factoredType;
    }
}
