package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.CoraLogger;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import java.util.List;

public interface FedoraReadPositionConverter {
    void setLogger(CoraLogger logger);
    List<String> filterPidList(long currentPosition, List<String> pidList);
    FedoraReaderConverter getConverter();
    String getQueryForObjectId(String id);
    String getQueryForList(DataGroup filter);
    String getQueryForList(DataGroup filter, FedoraReaderCursor cursor);
}
