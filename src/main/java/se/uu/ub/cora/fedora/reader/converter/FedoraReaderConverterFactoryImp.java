package se.uu.ub.cora.fedora.reader.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FedoraReaderConverterFactoryImp implements FedoraReaderConverterFactory {
    private Map<String, Class<? extends FedoraReaderConverter>> loadableConverter = new HashMap<>();
    private Map<String, Class<? extends FedoraTypeRestQueryInterface>> loadableTypeRestQueryInterface = new HashMap<>();
    private Class<? extends FedoraTypeRestQueryInterface> defaultTypeRestQueryInterfaceClass = FedoraTypeRestQueryInterfaceDefault.class;
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
    public void registerConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws FedoraReaderConverterFactoryException {
        FedoraReaderConverter fedoraReaderConverter = tryInstantiatingConverter(fedoraReaderConverterClass);
        loadableConverter.put(fedoraReaderConverter.type(), fedoraReaderConverterClass);
    }

    @Override
    public void registerTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> fedoraTypeRestQueryInterfaceClass) throws FedoraReaderConverterFactoryException {
        FedoraTypeRestQueryInterface fedoraTypeRestQueryInterface = tryInstantiatingTypeRestQueryInterface(fedoraTypeRestQueryInterfaceClass, "");
        loadableTypeRestQueryInterface.put(fedoraTypeRestQueryInterface.type(), fedoraTypeRestQueryInterfaceClass);
    }

    private FedoraTypeRestQueryInterface tryInstantiatingTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> fedoraTypeRestQueryInterfaceClass, String type) throws FedoraReaderConverterFactoryException {
        try {
            return tryGetValidFedoraTypeRestQueryInterface(fedoraTypeRestQueryInterfaceClass, type);
        } catch (NoSuchMethodException e) {
            throw new FedoraReaderConverterFactoryException(fedoraTypeRestQueryInterfaceClass.getName() + " has no public single string constructor", e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new FedoraReaderConverterFactoryException("Constructor failed for " + fedoraTypeRestQueryInterfaceClass.getName(), e);
        }
    }

    private FedoraTypeRestQueryInterface tryGetValidFedoraTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> fedoraTypeRestQueryInterfaceClass, String type) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, FedoraReaderConverterFactoryException {
        FedoraTypeRestQueryInterface fedoraTypeRestQueryInterface = getFedoraTypeRestQueryInterfaceFromClass(fedoraTypeRestQueryInterfaceClass, type);
        validateTypeRestQueryInterfaceType(fedoraTypeRestQueryInterface);
        return fedoraTypeRestQueryInterface;
    }

    private void validateTypeRestQueryInterfaceType(FedoraTypeRestQueryInterface fedoraReaderConverter) throws FedoraReaderConverterFactoryException {
        if(stringIsNotNullOrBlank(fedoraReaderConverter.type())) {
            throw new FedoraReaderConverterFactoryException(fedoraReaderConverter.getClass().getName() + " has an empty or null type");
        }
    }

    private FedoraTypeRestQueryInterface getFedoraTypeRestQueryInterfaceFromClass(Class<? extends FedoraTypeRestQueryInterface> fedoraTypeRestQueryInterfaceClass, String type) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
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
    public FedoraTypeRestQueryInterface factorTypeRestQueryInterface(String type) throws FedoraReaderConverterFactoryException {
        if(stringIsNotNullOrBlank(baseUrl)) {
            throw new FedoraReaderConverterFactoryException("Base URL must be set");
        }
        var loadable = loadableTypeRestQueryInterface.getOrDefault(type,
                defaultTypeRestQueryInterfaceClass);
        return tryInstantiatingTypeRestQueryInterface(loadable, type);
    }

    @Override
    public Class<? extends FedoraTypeRestQueryInterface> getDefaultTypeRestQueryInterface() {
        return defaultTypeRestQueryInterfaceClass;
    }

    @Override
    public void setDefaultTypeRestQueryInterface(Class<? extends FedoraTypeRestQueryInterface> defaultTypeRestQueryInterfaceClass) {
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
