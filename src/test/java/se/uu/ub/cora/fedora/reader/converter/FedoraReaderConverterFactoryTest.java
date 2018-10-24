package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;
import se.uu.ub.cora.fedora.data.FedoraReaderCursor;

import static org.testng.Assert.*;

public class FedoraReaderConverterFactoryTest {
    private static final String SOME_BASE_URL = "someBaseUrl";
    private static final String SOME_ID = "someId";
    private static final String SOME_MISSING_TYPE = "missingType";
    private FedoraReaderConverterFactory fedoraReaderConverterFactory;
    private static final String SOME_TYPE = "someType";

    @BeforeMethod
    public void init() {
        fedoraReaderConverterFactory = new FedoraReaderConverterFactoryImp();
        fedoraReaderConverterFactory.setBaseUrl(SOME_BASE_URL);
        fedoraReaderConverterFactory.setDefaultTypeRestQueryInterface(FedoraTypeRestQueryInterfaceDefault.class);
    }

    @Test
    public void testSetAndGetBaseUrl() {
        fedoraReaderConverterFactory.setBaseUrl("someOtherUrl");
        var actualUrl = fedoraReaderConverterFactory.getBaseUrl();
        assertEquals(actualUrl, "someOtherUrl");
    }

    @Test
    public void testSetAndGetDefaultTypeRestQueryInterface() {
        fedoraReaderConverterFactory.setDefaultTypeRestQueryInterface(FedoraTypeRestQueryInterfaceDefault.class);
        var actual = fedoraReaderConverterFactory.getDefaultTypeRestQueryInterface();
        assertEquals(actual, FedoraTypeRestQueryInterfaceDefault.class);
    }


    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Base URL must be set")
    public void testFactorWithNullBaseUrl() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.setBaseUrl(null);
        fedoraReaderConverterFactory.factorConverter(SOME_TYPE);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Base URL must be set")
    public void testFactorWithEmptyBaseUrl() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.setBaseUrl("\t \n\t ");
        fedoraReaderConverterFactory.factorConverter(SOME_TYPE);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithUnavailableConstructor has no public default constructor")
    public void testRegisterConverterWithNoPublicDefaultConstructor() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithUnavailableConstructor.class;
        fedoraReaderConverterFactory.registerConverter(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Constructor failed for se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithBrokenConstructor")
    public void testRegisterConverterWithBrokenConstructor() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithBrokenConstructor.class;
        fedoraReaderConverterFactory.registerConverter(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithNullType has an empty or null type")
    public void testRegisterConverterWithNullType() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithNullType.class;
        fedoraReaderConverterFactory.registerConverter(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraReaderConverterWithEmptyType has an empty or null type")
    public void testRegisterConverterWithEmptyType() throws FedoraReaderConverterFactoryException {
        Class<? extends FedoraReaderConverter> fedoraReaderConverterClass = FedoraReaderConverterWithEmptyType.class;
        fedoraReaderConverterFactory.registerConverter(fedoraReaderConverterClass);
    }

    @Test
    public void testRegisterSomeReader() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.registerConverter(FedoraReaderConverterSpy.class);
        assertTrue(fedoraReaderConverterFactory.factorConverter(FedoraReaderConverterSpy.defaultType) instanceof FedoraReaderConverterSpy);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "missingType does not have a registered converter")
    public void testFactorUnregistered() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.factorConverter("missingType");
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraTypeRestQueryInterfaceWithUnavailableConstructor has no public single string constructor")
    public void testRegisterTypeRestQueryInterfaceWithNoPublicSingleStringConstructor() throws FedoraReaderConverterFactoryException {
        var fedoraReaderConverterClass = FedoraTypeRestQueryInterfaceWithUnavailableConstructor.class;
        fedoraReaderConverterFactory.registerTypeRestQueryInterface(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Constructor failed for se.uu.ub.cora.fedora.reader.converter.FedoraTypeRestQueryInterfaceWithBrokenConstructor")
    public void testRegisterTypeRestQueryInterfaceWithBrokenConstructor() throws FedoraReaderConverterFactoryException {
        var fedoraReaderConverterClass = FedoraTypeRestQueryInterfaceWithBrokenConstructor.class;
        fedoraReaderConverterFactory.registerTypeRestQueryInterface(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraTypeRestQueryInterfaceNullType has an empty or null type")
    public void testRegisterTypeRestQueryInterfaceWithNullType() throws FedoraReaderConverterFactoryException {
        var fedoraTypeRestQueryInterfaceNullType = FedoraTypeRestQueryInterfaceNullType.class;
        fedoraReaderConverterFactory.registerTypeRestQueryInterface(fedoraTypeRestQueryInterfaceNullType);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "se.uu.ub.cora.fedora.reader.converter.FedoraTypeRestQueryInterfaceWithEmptyType has an empty or null type")
    public void testRegisterTypeRestQueryInterfaceWithEmptyType() throws FedoraReaderConverterFactoryException {
        var fedoraReaderConverterClass = FedoraTypeRestQueryInterfaceWithEmptyType.class;
        fedoraReaderConverterFactory.registerTypeRestQueryInterface(fedoraReaderConverterClass);
    }

    @Test(expectedExceptions = FedoraReaderConverterFactoryException.class, expectedExceptionsMessageRegExp = "Base URL must be set")
    public void testFactorTypeRestQueryInterfaceWithNullBaseUrl() throws FedoraReaderConverterFactoryException {
        fedoraReaderConverterFactory.setBaseUrl(null);
        fedoraReaderConverterFactory.factorTypeRestQueryInterface(SOME_TYPE);
    }

    @Test
    public void testFactorUnregisteredTypeRestQueryInterface() throws FedoraReaderConverterFactoryException, FedoraReaderConverterException {
        var typeRestQueryInterface = fedoraReaderConverterFactory.factorTypeRestQueryInterface(SOME_MISSING_TYPE);
        var defaultTypeRestQueryInterface = new FedoraTypeRestQueryInterfaceDefault(SOME_BASE_URL, SOME_MISSING_TYPE);
        assertEquals(typeRestQueryInterface.getQueryForObjectId(SOME_ID), defaultTypeRestQueryInterface.getQueryForObjectId(SOME_ID));
    }

    @Test
    public void testRegisterAndFactorTypeRestQueryInterface() throws FedoraReaderConverterFactoryException {
     //   var someTypeRestQueryInterface = new FedoraTypeRestQueryInterfaceSpy(SOME_BASE_URL, SOME_TYPE);
        fedoraReaderConverterFactory.registerTypeRestQueryInterface(FedoraTypeRestQueryInterfaceSpy.class);
        var actual = fedoraReaderConverterFactory.factorTypeRestQueryInterface(SOME_TYPE);
        assertNotNull(actual);
    }

}
