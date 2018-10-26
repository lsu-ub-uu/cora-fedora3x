package se.uu.ub.cora.fedora.reader.converter;

public interface FedoraReaderConverterFactory {
    @Deprecated
    FedoraTypeRestQuery factorTypeRestQueryInterface(String someType) throws FedoraReaderConverterFactoryException;

    @Deprecated
    FedoraReaderConverter factorConverter(String type) throws FedoraReaderConverterFactoryException;

    void registerConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverter) throws FedoraReaderConverterFactoryException;

    void registerTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> fedoraTypeRestQueryInterface) throws FedoraReaderConverterFactoryException;


    Class<? extends FedoraTypeRestQuery> getDefaultTypeRestQueryInterface();
    void setDefaultTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> defaultTypeRestQueryInterfaceClass);

    String getBaseUrl();
    void setBaseUrl(String baseUrl);

    FedoraReadPositionConverter factor(String type) throws FedoraReaderConverterFactoryException;
    FedoraReadPositionConverter factor(String type, long start) throws FedoraReaderConverterFactoryException;
    FedoraReadPositionConverter factor(String type, long start, long stop) throws FedoraReaderConverterFactoryException;

}
