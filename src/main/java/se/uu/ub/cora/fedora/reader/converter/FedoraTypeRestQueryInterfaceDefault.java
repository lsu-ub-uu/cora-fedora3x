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
        return super.baseUrl + "/fedora/" + super.type + "/" + id;
    }

    @Override
    public String getQueryForList(DataGroup filter) {
        return null;
    }

    @Override
    public String getQueryForList(DataGroup filter, FedoraReaderCursor cursor) {
        return null;
    }
}
