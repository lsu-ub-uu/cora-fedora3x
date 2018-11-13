package se.uu.ub.cora.fedora.reader.xml;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.reader.FedoraReaderException;

import java.util.List;

public interface FedoraReaderPure {
    String readObject(String objectId) throws FedoraReaderException;
    List<String> readList(String type, DataGroup filter) throws FedoraReaderException;

    void setMaxResults(int count);
}
