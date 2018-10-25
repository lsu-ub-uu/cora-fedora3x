package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

public class FedoraTypeRestQueryInterfaceDefault extends FedoraTypeRestQueryInterface {
    public FedoraTypeRestQueryInterfaceDefault(String baseUrl, String type) {
        super(baseUrl, type);
    }

    @Override
    public String type() {
        return "magicDefaultType";
    }

    @Override
    public String getQueryForObjectId(String id) {
        return String.format("%s/objects/%s/datastreams/METADATA/content", super.baseUrl, id);
    }

    @Override
    public String getQueryForList(DataGroup filter) {
        return String.format("%s/objects?pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", super.baseUrl, super.maxResults, super.type);
    }

    @Override
    public String getQueryForList(DataGroup filter, FedoraReaderCursor cursor) {
        return String.format("%s/objects?sessionToken=%s&pid=true&maxResults=%d&resultFormat=xml&query=pid%%7E%s:*", super.baseUrl, cursor.getToken(), super.maxResults, super.type);
    }
}
