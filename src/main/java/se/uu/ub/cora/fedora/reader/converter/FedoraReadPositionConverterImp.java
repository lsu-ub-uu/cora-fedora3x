package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.CoraLogger;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import java.util.List;

public class FedoraReadPositionConverterImp implements FedoraReadPositionConverter {
    private final FedoraReaderConverter fedoraReaderConverter;
    private final FedoraTypeRestQuery fedoraTypeRestQuery;
    private CoraLogger logger;

    public FedoraReadPositionConverterImp(FedoraReaderConverter fedoraReaderConverter,
                                          FedoraTypeRestQuery fedoraTypeRestQuery) {
        this.fedoraReaderConverter = fedoraReaderConverter;
        this.fedoraTypeRestQuery = fedoraTypeRestQuery;
    }

    @Override
    public void setLogger(CoraLogger logger) {
        this.logger = logger;
    }

    @Override
    public List<String> filterPidList(long currentPosition, List<String> pidList) {
        return pidList;
    }

    @Override
    public FedoraReaderConverter getConverter() {
        return fedoraReaderConverter;
    }

    @Override
    public String getQueryForObjectId(String id) {
        try {
            return fedoraTypeRestQuery.getQueryForObjectId(id);
        } catch (FedoraReaderConverterException e) {
            log(e.getMessage());
        }
        return "";
    }

    private void log(String message) {
        if(logger != null) {
            logger.write(message);
        }
    }

    @Override
    public String getQueryForList(DataGroup filter) {
        return fedoraTypeRestQuery.getQueryForList(filter);
    }

    @Override
    public String getQueryForList(DataGroup filter, FedoraReaderCursor cursor) {
        return fedoraTypeRestQuery.getQueryForList(filter, cursor);
    }
}
