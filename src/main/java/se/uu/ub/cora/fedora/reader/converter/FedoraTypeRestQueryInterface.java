package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

public abstract class FedoraTypeRestQueryInterface {

    public FedoraTypeRestQueryInterface(String baseUrl, String type) {
        this.baseUrl = baseUrl;
        this.type = type;
    }

    public abstract String type();

    protected final String baseUrl;

    protected final String type;

    public abstract String getQueryForObjectId(String id) throws FedoraReaderConverterException;

    public abstract String getQueryForList(DataGroup filter);

    public abstract String getQueryForList(DataGroup filter, FedoraReaderCursor cursor);
}
