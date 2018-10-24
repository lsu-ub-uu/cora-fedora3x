package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;
import se.uu.ub.cora.fedora.data.XMLXPathParser;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverter;

public class FedoraReaderConverterWithUnavailableConstructor extends FedoraReaderConverter {

    private FedoraReaderConverterWithUnavailableConstructor() {
        super();
    }

    @Override
    public String getQueryForObjectId(String id) {
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

    @Override
    public boolean loadXml(XMLXPathParser xmlxPathParser) {
        return false;
    }

    @Override
    public DataGroup convert() {
        return null;
    }

    @Override
    public String type() {
        return null;
    }
}
