package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

public abstract class FedoraTypeRestQuery {

    public FedoraTypeRestQuery(String baseUrl, String type) {
        this.baseUrl = baseUrl;
        this.type = type;
    }

    public abstract String type();

    protected final String baseUrl;

    protected final String type;

    protected int maxResults = 100;

    public int getMaxResults() { return maxResults; }

    public void setMaxResults(int maxResults) { this.maxResults = maxResults; }

    public abstract String getQueryForObjectId(String id) throws FedoraReaderConverterException;

    public abstract String getQueryForList(DataGroup filter);

    public abstract String getQueryForList(DataGroup filter, FedoraReaderCursor cursor);
}
