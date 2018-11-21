package se.uu.ub.cora.fedora.reader.xml;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderXmlHelper;
import se.uu.ub.cora.fedora.data.XMLXPathParserException;
import se.uu.ub.cora.fedora.reader.FedoraReaderException;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public String readObject(String objectId) throws FedoraReaderException {
        var objectUrl = String.format("%s/objects/%s/datastreams/METADATA/content", baseUrl, objectId);
        var httpHandler = httpHandlerFactory.factor(objectUrl);

        throwIfNotOk(objectId, httpHandler.getResponseCode());

        return httpHandler.getResponseText();
    }

    private void throwIfNotOk(String type, int responseCode) throws FedoraReaderException {
        if (responseCode != OK) {
            if (responseCode == NOT_FOUND) {
                throw new FedoraReaderException("404: " + type + " not found.");
            }
            throw new FedoraReaderException(responseCode + ": failed ...");
        }
    }

    @Override
    public List<String> readList(String type, DataGroup filter) throws FedoraReaderException {
        var listUrl =
                String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", baseUrl, maxResults, type);
        var start = Integer.parseInt(filter.getFirstAtomicValueWithNameInDataOrDefault("start", "0"));
        if (start < 0) {
            throw new RuntimeException(String.format("Invalid start value (%d)", start));
        }
        Optional<Integer> rows = Optional.empty();
        if (filter.containsChildWithNameInData("rows")) {
            var rowCount = Integer.parseInt(filter.getFirstAtomicValueWithNameInData("rows"));
            if (rowCount < 0) {
                throw new RuntimeException(String.format("Invalid row count (%d)", rowCount));
            }
            rows = Optional.of(rowCount);
        }
        return getObjectXmlList(type, listUrl, start, rows);
    }

    private List<String> getObjectXmlList(String type, String listUrl, int start, Optional<Integer> rows) throws FedoraReaderException {
        var httpHandler = httpHandlerFactory.factor(listUrl);
        var result = new ArrayList<String>();
        throwIfNotOk(type, httpHandler.getResponseCode());
        try {
            var somePossibleCursorAndPidList = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(httpHandler.getResponseText());
            for (var pid : somePossibleCursorAndPidList.getPidList()) {
                if (start <= 0) {
                    if (rows.isPresent()) {
                        if (rows.get() > 0) {
                            result.add(readObject(pid));
                            rows = Optional.of(rows.get() - 1);
                        }
                    } else {
                        result.add(readObject(pid));
                    }
                } else {
                    start--;
                }
            }

            if (somePossibleCursorAndPidList.getCursor() != null) {
                listUrl = String.format("%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", baseUrl, somePossibleCursorAndPidList.getCursor().getToken(), maxResults, type);
            } else {
                listUrl = null;
            }
        } catch (XMLXPathParserException e) {
            throw new RuntimeException("Invalid XML", e);
        }

        if (listUrl != null) {
            result.addAll(getObjectXmlList(type, listUrl, start, rows));
        }

        return result;
    }
}
