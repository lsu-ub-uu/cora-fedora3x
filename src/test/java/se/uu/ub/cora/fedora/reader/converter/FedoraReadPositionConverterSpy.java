package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.CoraLogger;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraReadPositionConverterSpy implements FedoraReadPositionConverter {

    public long start;
    public long stop;

    public Map<String,Integer> factorTypeCount;

    private String lastFactorType;

    public FedoraReadPositionConverterSpy() {
        factorTypeCount = new HashMap<>();
        start = 0;
        stop = 0;
    }
    public FedoraReaderConverterSpy fedoraReaderConverterSpy;
    public FedoraTypeRestQuerySpy fedoraTypeRestQuerySpy;

    public CoraLogger logger;

    public void factorFor(String type) {
        int count = 0;
        if(factorTypeCount.containsKey(type)) {
            count = factorTypeCount.get(type);
        }
        factorTypeCount.put(type, count + 1);
        lastFactorType = type;
        fedoraReaderConverterSpy.factoredType = type;
        fedoraTypeRestQuerySpy.factoredType = type;
    }

    public int getTypeCountFor(String type) {
        return factorTypeCount.get(type);
    }

    @Override
    public void setLogger(CoraLogger logger) {

        this.logger = logger;
    }

    @Override
    public List<String> filterPidList(long currentPosition, List<String> pidList) {
        if(stop > 0) {
            if (currentPosition > stop) {
                return Collections.emptyList();
            }
            if (currentPosition + pidList.size() > stop) {
                pidList = pidList.subList(0, (int) ((stop - currentPosition)));
            }
        }
        if(start <= currentPosition) {
            return pidList;
        }
        if(start < (currentPosition + pidList.size())) {
            return pidList.subList((int)(start - currentPosition), pidList.size());
        }
        return Collections.emptyList();
    }

    @Override
    public FedoraReaderConverter getConverter() {
        return fedoraReaderConverterSpy;
    }

    @Override
    public String getQueryForObjectId(String id) {
        try {
            return fedoraTypeRestQuerySpy.getQueryForObjectId(id);
        } catch (FedoraReaderConverterException e) {
            logger.write(e.getMessage());
        }
        return null;
    }

    @Override
    public String getQueryForList(DataGroup filter) {
        return fedoraTypeRestQuerySpy.getQueryForList(filter);
    }

    @Override
    public String getQueryForList(DataGroup filter, FedoraReaderCursor cursor) {
        return fedoraTypeRestQuerySpy.getQueryForList(filter, cursor);
    }
}
