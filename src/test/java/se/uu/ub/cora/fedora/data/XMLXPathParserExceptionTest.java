package se.uu.ub.cora.fedora.data;

import org.testng.annotations.Test;

public class XMLXPathParserExceptionTest {
    @Test(expectedExceptions = XMLXPathParserException.class ,expectedExceptionsMessageRegExp = "bob")
    public void testThrow() throws XMLXPathParserException {
        throw new XMLXPathParserException("bob");
    }
}
