package se.uu.ub.cora.fedora.reader.converter;

public class FedoraReaderConverterFactorySpy implements FedoraReaderConverterFactory {
    public FedoraReaderConverterSpy fedoraReaderConverterSpy = null;
    public int factorCount = 0;
    public boolean noConverters = false;

    @Override
    public FedoraReaderConverter factor(String type) throws FedoraReaderConverterFactoryException {
        factorCount++;
        if(noConverters) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }
        fedoraReaderConverterSpy.factorFor(type);
        return fedoraReaderConverterSpy;
    }

    @Override
    public void register(Class<? extends FedoraReaderConverter> fedoraReaderConverter) {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public String getBaseUrl() {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public void setBaseUrl(String baseUrl) {
    }

}
