package se.uu.ub.cora.fedora.reader.converter;

public class FedoraReaderConverterFactorySpy implements FedoraReaderConverterFactory {
    public FedoraReadPositionConverterSpy fedoraReadPositionConverterSpy = null;
    public int factorCount = 0;
    public boolean noConverters = false;

    @Override
    public FedoraReadPositionConverter factor(String type) throws FedoraReaderConverterFactoryException {
        factorCount++;
        if(noConverters) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }
        fedoraReadPositionConverterSpy.factorFor(type);
        return fedoraReadPositionConverterSpy;
    }

    @Override
    public FedoraReadPositionConverter factor(String type, long start) throws FedoraReaderConverterFactoryException {
        factorCount++;
        if(noConverters) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }
        fedoraReadPositionConverterSpy.factorFor(type);
        fedoraReadPositionConverterSpy.start = start;
        return fedoraReadPositionConverterSpy;
    }

    @Override
    public FedoraReadPositionConverter factor(String type, long start, long stop) throws FedoraReaderConverterFactoryException {
        factorCount++;
        if(noConverters) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }
        fedoraReadPositionConverterSpy.factorFor(type);
        fedoraReadPositionConverterSpy.start = start;
        fedoraReadPositionConverterSpy.stop = stop;
        return fedoraReadPositionConverterSpy;
    }

    @Override
    public FedoraReaderConverter factorConverter(String type) throws FedoraReaderConverterFactoryException {
        return null;
    }

    @Override
    public void registerConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverter) {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public void registerTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> fedoraTypeRestQueryInterface) throws FedoraReaderConverterFactoryException {

    }

    @Override
    public String getBaseUrl() {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public void setBaseUrl(String baseUrl) {
    }

    @Override
    public FedoraTypeRestQuery factorTypeRestQueryInterface(String someType) {
        return null;
    }

    @Override
    public Class<? extends FedoraTypeRestQuery> getDefaultTypeRestQueryInterface() {
        return null;
    }

    @Override
    public void setDefaultTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> defaultTypeRestQueryInterfaceClass) {

    }

}
