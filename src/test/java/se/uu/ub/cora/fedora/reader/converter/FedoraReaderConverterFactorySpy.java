package se.uu.ub.cora.fedora.reader.converter;

public class FedoraReaderConverterFactorySpy implements FedoraReaderConverterFactory {
    public FedoraReaderConverterSpy fedoraReaderConverterSpy = null;
    public int factorCount = 0;
    public boolean noConverters = false;

    @Override
    public FedoraReaderConverter factorConverter(String type) throws FedoraReaderConverterFactoryException {
        factorCount++;
        if(noConverters) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }
        fedoraReaderConverterSpy.factorFor(type);
        return fedoraReaderConverterSpy;
    }

    @Override
    public void registerConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverter) {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public void registerTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> fedoraTypeRestQueryInterface) throws FedoraReaderConverterFactoryException {

    }

    @Override
    public String getBaseUrl() {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public void setBaseUrl(String baseUrl) {
    }

    @Override
    public FedoraTypeRestQueryInterface factorTypeRestQueryInterface(String someType) {
        return null;
    }

    @Override
    public Class<? extends FedoraTypeRestQueryInterface> getDefaultTypeRestQueryInterface() {
        return null;
    }

    @Override
    public void setDefaultTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> defaultTypeRestQueryInterfaceClass) {

    }

}
