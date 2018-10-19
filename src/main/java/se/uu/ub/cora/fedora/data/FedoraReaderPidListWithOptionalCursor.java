package se.uu.ub.cora.fedora.data;

import java.util.List;

public class FedoraReaderPidListWithOptionalCursor {

    private final List<String> pidList;
    private final FedoraReaderCursor cursor;

    public FedoraReaderPidListWithOptionalCursor(List<String> pidList, FedoraReaderCursor cursor) {
        this.pidList = pidList;
        this.cursor = cursor;
    }

    public FedoraReaderPidListWithOptionalCursor(List<String> pidList) {
        this.pidList = pidList;
        this.cursor = null;
    }

    public List<String> getPidList() {
        return pidList;
    }

    public FedoraReaderCursor getCursor() {
        return cursor;
    }
}
