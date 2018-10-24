package se.uu.ub.cora.fedora;

import java.util.ArrayList;
import java.util.List;

public class CoraLoggerSpy implements CoraLogger {
    private List<String> log = new ArrayList<>();
    public List<String> getLog() { return log; }
    @Override
    public void write(String logMessage) {
        log.add(logMessage);
    }
}
