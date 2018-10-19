package se.uu.ub.cora.fedora.reader.converter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FedoraReaderConverterFactoryImp implements FedoraReaderConverterFactory {
    private Map<String, Class<? extends FedoraReaderConverter>> loadableConverter = new HashMap<>();
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
    public void register(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws FedoraReaderConverterFactoryException {
        FedoraReaderConverter fedoraReaderConverter = tryInstantiatingConverter(fedoraReaderConverterClass);
        loadableConverter.put(fedoraReaderConverter.type(), fedoraReaderConverterClass);
    }

    private FedoraReaderConverter tryInstantiatingConverter(Class<? extends FedoraReaderConverter> fedoraReaderConverterClass) throws FedoraReaderConverterFactoryException {
        try {
            return tryGetValidFedoraReaderConverter(fedoraReaderConverterClass);
        } catch (NoSuchMethodException e) {
            throw new FedoraReaderConverterFactoryException(fedoraReaderConverterClass.getName() + " has no public single string constructor", e);
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
        var constructor = fedoraReaderConverterClass.getConstructor(String.class);
        return constructor.newInstance(baseUrl);
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
    public FedoraReaderConverter factor(String type) throws FedoraReaderConverterFactoryException {
        checkBaseUrlAndExistenceOfConverterForType(type);
        return tryInstantiatingConverter(loadableConverter.get(type));
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
