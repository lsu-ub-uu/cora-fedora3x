package se.uu.ub.cora.fedora.reader.converter;

public interface FedoraReaderConverterFactory {
    FedoraReaderConverter factorConverter(String type) throws FedoraReaderConverterFactoryException;

    void registerConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverter) throws FedoraReaderConverterFactoryException;

    void registerTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> fedoraTypeRestQueryInterface) throws FedoraReaderConverterFactoryException;

    FedoraTypeRestQueryInterface factorTypeRestQueryInterface(String someType) throws FedoraReaderConverterFactoryException;

    Class<? extends FedoraTypeRestQueryInterface> getDefaultTypeRestQueryInterface();
    void setDefaultTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> defaultTypeRestQueryInterfaceClass);

    String getBaseUrl();
    void setBaseUrl(String baseUrl);
}
