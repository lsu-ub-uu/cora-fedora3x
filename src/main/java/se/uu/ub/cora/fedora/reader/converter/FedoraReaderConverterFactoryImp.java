package se.uu.ub.cora.fedora.reader.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FedoraReaderConverterFactoryImp implements FedoraReaderConverterFactory {
    private Map<String, Class<? extends FedoraReaderConverter>> loadableConverter = new HashMap<>();
    private Map<String, Class<? extends FedoraTypeRestQuery>> loadableTypeRestQueryInterface = new HashMap<>();
    private Class<? extends FedoraTypeRestQuery> defaultTypeRestQueryInterfaceClass = FedoraTypeRestQueryDefault.class;
    private String baseUrl;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public FedoraReadPositionConverter factor(String type) throws FedoraReaderConverterFactoryException {
        return new FedoraReadPositionConverterImp(factorConverter(type), factorTypeRestQueryInterface(type));
    }

    @Override
    public FedoraReadPositionConverter factor(String type, long start) throws FedoraReaderConverterFactoryException {
        return new FedoraReadPositionFromStartConverter(factorConverter(type), factorTypeRestQueryInterface(type), start);
    }

    @Override
    public FedoraReadPositionConverter factor(String type, long start, long stop) throws FedoraReaderConverterFactoryException {
        return new FedoraReadPositionFromStartWithStopConverter(factorConverter(type), factorTypeRestQueryInterface(type), start, stop);
    }

    @Override
    public void registerConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws FedoraReaderConverterFactoryException {
        FedoraReaderConverter fedoraReaderConverter = tryInstantiatingConverter(fedoraReaderConverterClass);
        loadableConverter.put(fedoraReaderConverter.type(), fedoraReaderConverterClass);
    }

    @Override
    public void registerTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> fedoraTypeRestQueryInterfaceClass) throws FedoraReaderConverterFactoryException {
        FedoraTypeRestQuery fedoraTypeRestQuery = tryInstantiatingTypeRestQueryInterface(fedoraTypeRestQueryInterfaceClass, "");
        loadableTypeRestQueryInterface.put(fedoraTypeRestQuery.type(), fedoraTypeRestQueryInterfaceClass);
    }

    private FedoraTypeRestQuery tryInstantiatingTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> fedoraTypeRestQueryInterfaceClass, String type) throws FedoraReaderConverterFactoryException {
        try {
            return tryGetValidFedoraTypeRestQueryInterface(fedoraTypeRestQueryInterfaceClass, type);
        } catch (NoSuchMethodException e) {
            throw new FedoraReaderConverterFactoryException(fedoraTypeRestQueryInterfaceClass.getName() + " has no public single string constructor", e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new FedoraReaderConverterFactoryException("Constructor failed for " + fedoraTypeRestQueryInterfaceClass.getName(), e);
        }
    }

    private FedoraTypeRestQuery tryGetValidFedoraTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> fedoraTypeRestQueryInterfaceClass, String type) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, FedoraReaderConverterFactoryException {
        FedoraTypeRestQuery fedoraTypeRestQuery = getFedoraTypeRestQueryInterfaceFromClass(fedoraTypeRestQueryInterfaceClass, type);
        validateTypeRestQueryInterfaceType(fedoraTypeRestQuery);
        return fedoraTypeRestQuery;
    }

    private void validateTypeRestQueryInterfaceType(FedoraTypeRestQuery fedoraReaderConverter) throws FedoraReaderConverterFactoryException {
        if(stringIsNotNullOrBlank(fedoraReaderConverter.type())) {
            throw new FedoraReaderConverterFactoryException(fedoraReaderConverter.getClass().getName() + " has an empty or null type");
        }
    }

    private FedoraTypeRestQuery getFedoraTypeRestQueryInterfaceFromClass(Class<? extends FedoraTypeRestQuery> fedoraTypeRestQueryInterfaceClass, String type) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        var constructor = fedoraTypeRestQueryInterfaceClass.getConstructor(String.class, String.class);
        return constructor.newInstance(baseUrl, type);
    }

    private FedoraReaderConverter tryInstantiatingConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws FedoraReaderConverterFactoryException {
        try {
            return tryGetValidFedoraReaderConverter(fedoraReaderConverterClass);
        } catch (NoSuchMethodException e) {
            throw new FedoraReaderConverterFactoryException(fedoraReaderConverterClass.getName() + " has no public default constructor", e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new FedoraReaderConverterFactoryException("Constructor failed for " + fedoraReaderConverterClass.getName(), e);
        }
    }

    private FedoraReaderConverter tryGetValidFedoraReaderConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, FedoraReaderConverterFactoryException {
        FedoraReaderConverter fedoraReaderConverter = getFedoraReaderConverterFromClass(fedoraReaderConverterClass);
        validateConverterType(fedoraReaderConverter);
        return fedoraReaderConverter;
    }

    private FedoraReaderConverter getFedoraReaderConverterFromClass(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        var constructor = fedoraReaderConverterClass.getConstructor();
        return constructor.newInstance();
    }

    private void validateConverterType(FedoraReaderConverter fedoraReaderConverter) throws FedoraReaderConverterFactoryException {
        if(stringIsNotNullOrBlank(fedoraReaderConverter.type())) {
            throw new FedoraReaderConverterFactoryException(fedoraReaderConverter.getClass().getName() + " has an empty or null type");
        }
    }

    private boolean stringIsNotNullOrBlank(String type) {
        return type == null || type.trim().length() == 0;
    }

    @Override
    public FedoraReaderConverter factorConverter(String type) throws FedoraReaderConverterFactoryException {
        checkBaseUrlAndExistenceOfConverterForType(type);
        return tryInstantiatingConverter(loadableConverter.get(type));
    }

    @Override
    public FedoraTypeRestQuery factorTypeRestQueryInterface(String type) throws FedoraReaderConverterFactoryException {
        if(stringIsNotNullOrBlank(baseUrl)) {
            throw new FedoraReaderConverterFactoryException("Base URL must be set");
        }
        var loadable = loadableTypeRestQueryInterface.getOrDefault(type,
                defaultTypeRestQueryInterfaceClass);
        return tryInstantiatingTypeRestQueryInterface(loadable, type);
    }

    @Override
    public Class<? extends FedoraTypeRestQuery> getDefaultTypeRestQueryInterface() {
        return defaultTypeRestQueryInterfaceClass;
    }

    @Override
    public void setDefaultTypeRestQueryInterface(Class<? extends FedoraTypeRestQuery> defaultTypeRestQueryInterfaceClass) {
        this.defaultTypeRestQueryInterfaceClass = defaultTypeRestQueryInterfaceClass;
    }

    private void checkBaseUrlAndExistenceOfConverterForType(String type) throws FedoraReaderConverterFactoryException {
        if(stringIsNotNullOrBlank(baseUrl)) {
            throw new FedoraReaderConverterFactoryException("Base URL must be set");
        }
        if(!loadableConverter.containsKey(type)) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }
    }

}
