package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;

public class FedoraReaderConverterWithNullType extends FedoraReaderConverter {

    public FedoraReaderConverterWithNullType() {
        super();
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
