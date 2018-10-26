package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;

public class FedoraReaderConverterWithBrokenConstructor extends FedoraReaderConverter {

    public FedoraReaderConverterWithBrokenConstructor() {
        super();
        throw new RuntimeException("FedoraReaderConverterWithBrokenConstructor exploded");
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
