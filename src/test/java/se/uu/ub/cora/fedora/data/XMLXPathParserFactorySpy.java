package se.uu.ub.cora.fedora.data;

import java.util.ArrayList;
import java.util.List;

public class XMLXPathParserFactorySpy implements XMLXPathParserFactory {
    public XMLXPathParserSpy parserSpy;

    public int factorCallCount = 0;

    public XMLXPathParserFactorySpy() {
        parserSpy = new XMLXPathParserSpy();
    }

    @Override
    public XMLXPathParser factor() {
        factorCallCount++;
        return parserSpy;
    }

    @Override
    public FedoraReaderXmlHelper factorHelper() {
        return new FedoraReaderXmlHelperSpy();
    }
}
