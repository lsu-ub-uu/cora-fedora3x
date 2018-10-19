package se.uu.ub.cora.fedora.data;

import java.util.List;

public interface FedoraReaderXmlHelper {
    FedoraReaderPidListWithOptionalCursor extractPidListAndPossiblyCursor(XMLXPathParser xmlxPathParser) throws XMLXPathParserException;
}
