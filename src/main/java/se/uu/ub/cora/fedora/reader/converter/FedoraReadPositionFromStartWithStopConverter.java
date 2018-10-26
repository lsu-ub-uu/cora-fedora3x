package se.uu.ub.cora.fedora.reader.converter;

import java.util.Collections;
import java.util.List;

public class FedoraReadPositionFromStartWithStopConverter extends FedoraReadPositionFromStartConverter {
    private final long stop;

    public FedoraReadPositionFromStartWithStopConverter(FedoraReaderConverter fedoraReaderConverter, FedoraTypeRestQuery fedoraTypeRestQuery, long start, long stop) {
        super(fedoraReaderConverter, fedoraTypeRestQuery, start);
        this.stop = stop;
    }

    @Override
    public List<String> filterPidList(long currentPosition, List<String> pidList) {
        if(currentPosition > stop) {
            return Collections.emptyList();
        }
        if(currentPosition + pidList.size() > stop) {
            pidList = pidList.subList(0 ,(int) ((stop - currentPosition)));
        }

        return filterPidListFromStart(currentPosition, pidList);
    }

}
