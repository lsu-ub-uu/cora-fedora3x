package se.uu.ub.cora.fedora.data;

public class XMLXPathParserFactorySpy implements XMLXPathParserFactory {
    public XMLXPathParserSpy parserSpy;
    public FedoraReaderXmlHelperSpy helperSpy;

    public int factorCallCount = 0;
    public int factorHelperCallCount = 0;

    public XMLXPathParserFactorySpy(){
        parserSpy = new XMLXPathParserSpy();
        helperSpy = new FedoraReaderXmlHelperSpy();
    }

    @Override
    public XMLXPathParser factor() {
        factorCallCount++;
        return parserSpy;
    }

    @Override
    public FedoraReaderXmlHelper factorHelper() {
        factorHelperCallCount++;
        return helperSpy;
    }
}
