package se.uu.ub.cora.fedora.data;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

public class XMLXPathParserFactoryTest {

    private XMLXPathParserFactory xmlxPathParserFactory;

    @BeforeMethod
    public void init() {
        xmlxPathParserFactory = new XMLXPathParserFactoryImp();
    }

    @Test
    public void testGetFXMLXPathParser() {
        assertNotNull(xmlxPathParserFactory.factor());
    }

    @Test
    public void testGetFedoraReaderXmlHelper() { assertNotNull(xmlxPathParserFactory.factorHelper());}
}
