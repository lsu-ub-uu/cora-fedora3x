package se.uu.ub.cora.fedora.reader.xml;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.data.XMLXPathParserException;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import java.util.ArrayList;
import java.util.List;

public class FedoraReaderPureImp implements FedoraReaderPure {
    private static final int OK = 200;
    private static final int NOT_FOUND = 404;
    private static final int DEFAULT_MAX_RESULTS = 100;
    private HttpHandlerFactory httpHandlerFactory;
    private FedoraReaderXmlHelper fedoraReaderXmlHelper;
    private String baseUrl;
    private int maxResults;

    public FedoraReaderPureImp(HttpHandlerFactory httpHandlerFactory, FedoraReaderXmlHelper fedoraReaderXmlHelper, String baseUrl) {
        this.httpHandlerFactory = httpHandlerFactory;
        this.fedoraReaderXmlHelper = fedoraReaderXmlHelper;
        this.baseUrl = baseUrl;
        this.maxResults = DEFAULT_MAX_RESULTS;
    }

    @Override
    public void setMaxResults(int count) {
        maxResults = count;
    }

    @Override
    public String readObject(String objectId) {
        var objectUrl = String.format("%s/objects/%s/datastreams/METADATA/content", baseUrl, objectId);
        var httpHandler = httpHandlerFactory.factor(objectUrl);

        throwIfNotOk(objectId, httpHandler.getResponseCode());

        return httpHandler.getResponseText();
    }

    private void throwIfNotOk(String id, int responseCode) {
        if (responseCode != OK) {
            if (responseCode == NOT_FOUND) {
                throw new RuntimeException("Fedora object not found: " + id);
            }
            throw new RuntimeException("Fedora call failed: " + responseCode);
        }
    }

    @Override
    public List<String> readList(String type, DataGroup filter) {
        var listUrl =
                String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", baseUrl, maxResults, type);
        var start = Integer.parseInt(filter.getFirstAtomicValueWithNameInDataOrDefault("start", "0"));
        if (start < 0) {
            throw new RuntimeException(String.format("Invalid start value (%d)", start));
        }
        Integer rows = null;
        if (filter.containsChildWithNameInData("rows")) {
            var rowCount = Integer.parseInt(filter.getFirstAtomicValueWithNameInData("rows"));
            if (rowCount < 0) {
                throw new RuntimeException(String.format("Invalid row count (%d)", rowCount));
            }
            rows = rowCount;
        }
        return getObjectXmlList(type, listUrl, start, rows);
    }

    private List<String> getObjectXmlList(String type, String pidListUrl, int start, Integer rows) {
        var httpHandler = httpHandlerFactory.factor(pidListUrl);
        throwIfNotOk(type, httpHandler.getResponseCode());
        try {
            var result = new ArrayList<String>();
            var somePossibleCursorAndPidList = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(httpHandler.getResponseText());
            for (var pid : somePossibleCursorAndPidList.getPidList()) {
                if (start <= 0) {
                    if (rows != null) {
                        if (rows > 0) {
                            result.add(readObject(pid));
                            rows--;
                        } else {
                            break;
                        }
                    } else {
                        result.add(readObject(pid));
                    }
                } else {
                    start--;
                }
            }
            if (somePossibleCursorAndPidList.getCursor() != null && (rows == null || rows > 0)) {
                String nextPageInCursor = String.format("%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", baseUrl, somePossibleCursorAndPidList.getCursor().getToken(), maxResults, type);
                result.addAll(getObjectXmlList(type, nextPageInCursor, start, rows));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Invalid XML", e);
        }
    }
}
