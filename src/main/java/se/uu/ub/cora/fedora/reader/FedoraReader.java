package se.uu.ub.cora.fedora.reader;

import se.uu.ub.cora.bookkeeper.data.DataElement;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.spider.data.SpiderReadResult;

public interface FedoraReader {
    DataElement read(String type, String id);
    SpiderReadResult readList(String type, DataGroup filter);
}
