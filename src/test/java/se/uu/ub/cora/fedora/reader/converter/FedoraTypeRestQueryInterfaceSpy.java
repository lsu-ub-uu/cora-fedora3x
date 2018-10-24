package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

public class FedoraTypeRestQueryInterfaceSpy extends FedoraTypeRestQueryInterface {
    public FedoraTypeRestQueryInterfaceSpy(String baseUrl, String type) {
        super(baseUrl, type);
    }

    @Override
    public String type() {
        return "spyType";
    }

    @Override
    public String getQueryForObjectId(String id) throws FedoraReaderConverterException {
        return null;
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
