package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.XMLXPathParser;

public abstract class FedoraReaderConverter {
    public abstract boolean loadXml(XMLXPathParser xmlxPathParser);

    public abstract DataGroup convert() throws FedoraReaderConverterException;

//TODO: split into converter (loadXml, convert) and whatever (baseUrl, type, queryObjId, queryList)

    public FedoraReaderConverter(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public abstract String type();

    protected final String baseUrl;

    public abstract String getQueryForObjectId(String id) throws FedoraReaderConverterException;

    public abstract String getQueryForList(DataGroup filter);


}
