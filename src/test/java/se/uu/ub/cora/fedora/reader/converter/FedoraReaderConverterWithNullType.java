package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverter;
import se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterException;

public class FedoraReaderConverterWithNullType extends FedoraReaderConverter {

    public FedoraReaderConverterWithNullType(String baseUrl) {
        super(baseUrl);
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
    public boolean loadXml(XMLXPathParser xmlxPathParser) {
        return false;
    }

    @Override
    public DataGroup convert() throws FedoraReaderConverterException {
        return null;
    }

    @Override
    public String type() {
        return null;
    }
}
