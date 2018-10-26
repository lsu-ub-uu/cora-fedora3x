package se.uu.ub.cora.fedora.reader.converter;

import java.util.Collections;
import java.util.List;

public class FedoraReadPositionFromStartConverter extends FedoraReadPositionConverterImp {
    private long start;

    public FedoraReadPositionFromStartConverter(FedoraReaderConverter fedoraReaderConverter, FedoraTypeRestQuery fedoraTypeRestQuery, long start) {
        super(fedoraReaderConverter, fedoraTypeRestQuery);
        this.start = start;
    }

    @Override
    public List<String> filterPidList(long currentPosition, List<String> pidList) {
        return filterPidListFromStart(currentPosition, pidList);
    }

    protected List<String> filterPidListFromStart(long currentPosition, List<String> pidList) {
        if(start <= currentPosition) {
            return pidList;
        }
        if(start < (currentPosition + pidList.size())) {
            return pidList.subList((int)(start - currentPosition), pidList.size());
        }
        return Collections.emptyList();
    }
}
