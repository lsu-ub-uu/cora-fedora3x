package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;
import se.uu.ub.cora.fedora.data.XMLXPathParser;

public abstract class FedoraReaderConverter {

    public FedoraReaderConverter() { }

    public abstract boolean loadXml(XMLXPathParser xmlxPathParser);

    public abstract DataGroup convert() throws FedoraReaderConverterException;

    public abstract String type();

    //TODO: split into converter (loadXml, convert) and whatever (baseUrl, type, queryObjId, queryList)

    @Deprecated
    public abstract String getQueryForObjectId(String id) throws FedoraReaderConverterException;

    @Deprecated
    public abstract String getQueryForList(DataGroup filter);

    @Deprecated
    public abstract String getQueryForList(DataGroup filter, FedoraReaderCursor cursor);
}
