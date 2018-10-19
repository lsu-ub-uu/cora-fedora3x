package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;
import se.uu.ub.cora.fedora.data.XMLXPathParserSpy;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverter;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterException;

import java.util.ArrayList;
import java.util.List;

public class FedoraReaderConverterSpy extends FedoraReaderConverter {
    public static final String defaultType = "someSpyType";
    public String type;
    public XMLXPathParser xmlxPathParser;
    public DataGroup conversionResult;
    public String badId;
    public List<String> objectUrlRequests;
    public List<String> listUrlRequests;
    public List<String> xmlPidList;
    public List<String> loadedXml;
    public int convertCalls = 0;
    private String requestedPid;

    public FedoraReaderConverterSpy(String baseUrl) {
        super(baseUrl);
        objectUrlRequests = new ArrayList<>();
        listUrlRequests = new ArrayList<>();
        loadedXml = new ArrayList<>();
        xmlPidList = new ArrayList<>();
        requestedPid = null;
    }

    @Override
    public String getQueryForObjectId(String id) throws FedoraReaderConverterException {
        if(badId.equals(id)) {
            throw new FedoraReaderConverterException("Cannot create URL for " + id);
        }
        requestedPid = id;
        objectUrlRequests.add(id);
        return "Converter URL for (" + baseUrl + "," + id + ")";
    }

    @Override
    public String getQueryForList(DataGroup filter) {
        listUrlRequests.add(type);
        try {
            return "Converter URL for (" + baseUrl + "," + type + ")";
        } catch (Exception e) {}
        return null;
    }

    @Override
    public boolean loadXml(XMLXPathParser xmlxPathParser) {
        if(xmlxPathParser instanceof  XMLXPathParserSpy){
            var xmlParserSpy = (XMLXPathParserSpy) xmlxPathParser;
            loadedXml.add(xmlParserSpy.getLastParsedXml());
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
        if(xmlxPathParser instanceof XMLXPathParserSpy && ((XMLXPathParserSpy)xmlxPathParser).uselessXml) {
            throw new FedoraReaderConverterException("XML cannot be converted to " + type);
        }
        if(conversionResult == null) {
            return DataGroup.withNameInData(type + "/" + loadedXml.get(convertCalls - 1));
        }
        return  conversionResult;
    }

    @Override
    public String type() {
        if(type == null) {
            return defaultType;
        }
        return type;
    }
}
