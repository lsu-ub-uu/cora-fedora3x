package se.uu.ub.cora.fedora.reader.converter;

public interface FedoraReaderConverterFactory {
    FedoraReaderConverter factor(String type) throws FedoraReaderConverterFactoryException;
    void register(Class<? extends FedoraReaderConverter> fedoraReaderConverter) throws FedoraReaderConverterFactoryException;
    @Deprecated
    String getBaseUrl();
    @Deprecated
    void setBaseUrl(String baseUrl);
}
