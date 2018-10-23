package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;
import se.uu.ub.cora.fedora.data.XMLXPathParser;
import se.uu.ub.cora.fedora.data.XMLXPathParserSpy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraReaderConverterSpy extends FedoraReaderConverter {
    public static final String defaultType = "someSpyType";

    public boolean uselessXml;

    public Map<String,Integer> factorTypeCount;

    private String lastFactorType;

    public XMLXPathParser xmlxPathParser;
    public Map<String, DataGroup> conversionResultForPid;
    public String badId;
    public List<String> objectUrlRequests;
    public List<String> listUrlRequests;
    public List<String> xmlPidList;
    public List<String> loadedXml;
    public int convertCalls = 0;
    private String requestedPid;
    private String possiblyPidForConverter;

    public Map<String,Integer> pidQueryCount;

    public Map<String,String> queryForId;
    public int queryForIdCalls = 0;
    public String queryForType;
    public int queryForTypeCalls = 0;


    public void factorFor(String type) {
        int count = 0;
        if(factorTypeCount.containsKey(type)) {
            count = factorTypeCount.get(type);
        }
        factorTypeCount.put(type, count + 1);
        lastFactorType = type;
    }

    public void addQueryForId(String id, String query) {
        queryForId.put(id, query);
    }

    public FedoraReaderConverterSpy(String baseUrl) {
        super(baseUrl);

        queryForId = new HashMap<>();
        factorTypeCount = new HashMap<>();
        objectUrlRequests = new ArrayList<>();
        listUrlRequests = new ArrayList<>();
        loadedXml = new ArrayList<>();
        xmlPidList = new ArrayList<>();
        requestedPid = null;
        uselessXml = false;
        conversionResultForPid = new HashMap<>();
        pidQueryCount = new HashMap<>();

    }

    public int getTypeCountFor(String type) {
        return factorTypeCount.get(type);
    }


    public int getPidCountFor(String pid) {
        return pidQueryCount.get(pid);
    }

    @Override
    public String getQueryForObjectId(String id) throws FedoraReaderConverterException {
        queryForIdCalls++;
        var count = 0;
        if(pidQueryCount.containsKey(id)) {
            count = pidQueryCount.get(id);
        }
        pidQueryCount.put(id, count + 1);
        if(badId != null && badId.equals(id)) {
            throw new FedoraReaderConverterException("Cannot create URL for " + id);
        }
        return queryForId.get(id);
    }

    @Override
    public String getQueryForList(DataGroup filter) {
        queryForTypeCalls++;
        return queryForType;
    }

    @Override
    public String getQueryForList(DataGroup filter, FedoraReaderCursor cursor) {
        return cursor.getToken();
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
        if(uselessXml) {
            throw new FedoraReaderConverterException("XML cannot be converted to " + lastFactorType);
        }
        if(conversionResultForPid.containsKey(possiblyPidForConverter)) {
            return conversionResultForPid.get(possiblyPidForConverter);
        }
        return null;
    }

    @Override
    public String type() {
        if(lastFactorType == null) {
            return defaultType;
        }
        return lastFactorType;
    }
}
