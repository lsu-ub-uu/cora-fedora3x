package se.uu.ub.cora.fedora.reader.xml;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

import java.util.List;

public interface FedoraReaderPure {
    String readObject(String objectId);
    List<String> readList(String type, DataGroup filter);

    void setMaxResults(int count);
}
