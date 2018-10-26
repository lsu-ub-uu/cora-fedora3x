package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;

public abstract class FedoraReaderConverter {

    public FedoraReaderConverter() { }

    public abstract boolean loadXml(XMLXPathParser xmlxPathParser);

    public abstract DataGroup convert() throws FedoraReaderConverterException;

    public abstract String type();
}
