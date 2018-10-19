package se.uu.ub.cora.fedora.data;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class FedoraReaderXmlHelperTest {
    private FedoraReaderXmlHelper fedoraReaderXmlHelper;
    private XMLXPathParserFactory xmlxPathParserFactory;
    private String xmlForThreePidAndCursorAtZero;
    private String xmlEmptyWithOnlyRootResultElement;
    private String xmlWithEmptyResult;
    private String xmlWithBrokenCursorMissingToken;
    private String xmlWithBrokenCursorEmptyToken;
    private String xmlWithBrokenCursorMissingCursor;

    @BeforeMethod
    public void init() {
        xmlxPathParserFactory = new XMLXPathParserFactoryImp();
        fedoraReaderXmlHelper = new FedoraReaderXmlHelperImp();
        xmlForThreePidAndCursorAtZero = resourceToString("/xml/FedoraReadWithThreePidAndCursorAtZero.xml");
        xmlEmptyWithOnlyRootResultElement = resourceToString("/xml/FedoraReadWithNothing.xml");
        xmlWithEmptyResult = resourceToString("/xml/FedoraReadWithEmptyResult.xml");
        xmlWithBrokenCursorMissingToken = resourceToString("/xml/FedoraReadWithBrokenCursorMissingToken.xml");
        xmlWithBrokenCursorEmptyToken = resourceToString("/xml/FedoraReadWithBrokenCursorEmptyToken.xml");
        xmlWithBrokenCursorMissingCursor = resourceToString("/xml/FedoraReadWithBrokenCursorMissingCursor.xml");
    }

    private String resourceToString(String resourceFile) {
        try (var file = getClass().getResourceAsStream(resourceFile);
             var stream = new InputStreamReader(file);
             var buffered = new BufferedReader(stream)) {
                return buffered.lines().collect(Collectors.joining());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testGetPidListFromSomeData() throws XMLXPathParserException {

        List<String> expectedPidList = new ArrayList<>();
        expectedPidList.add("alvin-place:15");
        expectedPidList.add("alvin-place:679");
        expectedPidList.add("alvin-place:692");

        var actualPidList = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlForThreePidAndCursorAtZero));

        assertEquals(actualPidList.getPidList(), expectedPidList);
    }

    private XMLXPathParser getXmlXPathParser(String xmlEmptyWithOnlyRootResultElement) throws XMLXPathParserException {
        XMLXPathParser xmlxPathParser = xmlxPathParserFactory.factor();
        return xmlxPathParser.forXML(xmlEmptyWithOnlyRootResultElement);
    }

    @Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "There was no resultList in given XML")
    public void testTryGetPidListFromMissingResult() throws XMLXPathParserException {

        fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlEmptyWithOnlyRootResultElement));
    }

    @Test
    public void testGetPidListFromEmptyResult() throws XMLXPathParserException {
        var actualPidList = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlWithEmptyResult));

        assertNotNull(actualPidList);
        assertTrue(actualPidList.getPidList().isEmpty());
    }

    @Test
    public void testGetCursorFromSomeData() throws XMLXPathParserException {
        var expectedCursor = new FedoraReaderCursor("ba0a8ded8f13b71ee52155a3cbdbe34f");
        expectedCursor.setCursor("0");

        var readerPidListWithOptionalCursor = fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlForThreePidAndCursorAtZero));
        var actualCursor = readerPidListWithOptionalCursor.getCursor();
        assertNotNull(actualCursor);
        assertEquals(actualCursor.getToken(), expectedCursor.getToken());
        assertEquals(actualCursor.getCursor(), expectedCursor.getCursor());
    }

    @Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "There was no resultList in given XML")
    public void testGetNullCursorFromXmlWithoutCursor() throws XMLXPathParserException {
        fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlEmptyWithOnlyRootResultElement));
    }

    @Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "token not found in XML")
    public void testGetNullCursorFromXmlWithoutBrokenCursorMissingToken() throws XMLXPathParserException {
        fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlWithBrokenCursorMissingToken));
    }

    @Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "token not found in XML")
    public void testGetNullCursorFromXmlWithoutBrokenCursorEmptyToken() throws XMLXPathParserException {
        fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlWithBrokenCursorEmptyToken));
    }

    @Test(expectedExceptions = XMLXPathParserException.class, expectedExceptionsMessageRegExp = "cursor not found in XML")
    public void testGetNullCursorFromXmlWithoutBrokenCursorMissingCursor() throws XMLXPathParserException {
        fedoraReaderXmlHelper.extractPidListAndPossiblyCursor(getXmlXPathParser(xmlWithBrokenCursorMissingCursor));
    }

}
