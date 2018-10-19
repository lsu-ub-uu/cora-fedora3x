package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FedoraReaderConverterFactoryTest {
    private static final String SOME_BASE_URL = "someBaseUrl";
//    private static final Class<? extends FedoraReaderConverter> SOME_READER_CONVERTER = FedoraReaderConverterSpy.class;
    private FedoraReaderConverterFactory fedoraReaderConverterFactory;
    private static final String SOME_TYPE = "someType";

    @BeforeMethod
    public void init() {
        fedoraReaderConverterFactory = new FedoraReaderConverterFactoryImp();
        fedoraReaderConverterFactory.setBaseUrl(SOME_BASE_URL);
    }

    @Test
    public void testSetAndGetBaseUrl() {
        fedoraReaderConverterFactory.setBaseUrl("someOtherUrl");
        var actualUrl = fedoraReaderConverterFactory.getBaseUrl();
        assertEquals(actualUrl, "someOtherUrl");
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Base URL must be set")
    public void testFactorWithNullBaseUrl() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.setBaseUrl(null);
        fedoraReaderConverterFactory.factor(SOME_TYPE);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Base URL must be set")
    public void testFactorWithEmptyBaseUrl() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.setBaseUrl("\t \n\t ");
        fedoraReaderConverterFactory.factor(SOME_TYPE);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithUnavailableConstructor has no public single string constructor")
    public void testRegisterConverterWithNoPublicSingleStringConstructor() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithUnavailableConstructor.class;
        fedoraReaderConverterFactory.register(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Constructor failed for se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithBrokenConstructor")
    public void testRegisterConverterWithBrokenConstructor() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithBrokenConstructor.class;
        fedoraReaderConverterFactory.register(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithNullType has an empty or null type")
    public void testRegisterConverterWithNullType() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithNullType.class;
        fedoraReaderConverterFactory.register(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithEmptyType has an empty or null type")
    public void testRegisterConverterWithEmptyType() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithEmptyType.class;
        fedoraReaderConverterFactory.register(fedoraReaderConverterClass);
    }

    @Test
    public void testRegisterSomeReader() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.register(FedoraReaderConverterSpy.class);
        assertTrue(fedoraReaderConverterFactory.factor(FedoraReaderConverterSpy.defaultType) instanceof FedoraReaderConverterSpy);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "missingType does not have a registered converter")
    public void testFactorUnregistered() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.factor("missingType");
    }

}
