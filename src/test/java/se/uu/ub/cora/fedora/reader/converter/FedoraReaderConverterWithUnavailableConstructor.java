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
