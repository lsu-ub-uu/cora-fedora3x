package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraTypeRestQuerySpy extends FedoraTypeRestQuery {
    public FedoraTypeRestQuerySpy(String baseUrl, String type) {
        super(baseUrl, type);
        queryForId = new HashMap<>();
        pidQueryCount = new HashMap<>();
    }
    public String badId;

    public List<String> objectUrlRequests;
    public List<String> listUrlRequests;
    public List<String> xmlPidList;
    public List<String> loadedXml;
    public int convertCalls = 0;
    private String requestedPid;
    private String possiblyPidForConverter;

    public Map<String,Integer> pidQueryCount;

    protected String factoredType;

    public Map<String,String> queryForId;
    public int queryForIdCalls = 0;
    public String queryForType;
    public int queryForTypeCalls = 0;
    public Boolean fail = false;

    @Override
    public String type() {
        return "spyType";
    }

    public void addQueryForId(String id, String query, int expectedNoCalls) {
        queryForId.put(id, query);
        pidQueryCount.put(id, expectedNoCalls);
    }

    public int getPidCountFor(String pid) {
        return pidQueryCount.get(pid);
    }

    @Override
    public String getQueryForObjectId(String id) throws FedoraReaderConverterException {
        queryForIdCalls++;
        if(badId != null && badId.equals(id)) {
            throw new FedoraReaderConverterException("Cannot create URL for " + id);
        }
        if(id == null || id.isEmpty()) {
            throw new FedoraReaderConverterException("Cannot create URL for null or empty Id");
        }
        var count = pidQueryCount.getOrDefault(id, 0);
        pidQueryCount.put(id, count - 1);
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
}
