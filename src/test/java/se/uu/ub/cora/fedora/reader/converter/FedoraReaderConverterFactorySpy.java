package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FedoraReaderConverterFactorySpy implements FedoraReaderConverterFactory {
    public Map<String,FedoraReaderConverterSpy> spies = new HashMap<>();
    public List<String> blacklistedConverters = new ArrayList<>();
    public String badId = "";
    public DataGroup conversionResult = null;
    public String baseUrl = "someDefaultSpyBaseUrl";

    @Override
    public FedoraReaderConverter factor(String type) throws FedoraReaderConverterFactoryException {
        if(blacklistedConverters.contains(type)) {
            throw new FedoraReaderConverterFactoryException(type + " does not have a registered converter");
        }

        FedoraReaderConverterSpy fedoraReaderConverterSpy = new FedoraReaderConverterSpy(baseUrl);
        fedoraReaderConverterSpy.badId = badId;
        fedoraReaderConverterSpy.conversionResult = conversionResult;
        fedoraReaderConverterSpy.type = type;

        spies.put(type, fedoraReaderConverterSpy);
        return fedoraReaderConverterSpy;
    }

    @Override
    public void register(Class<? extends FedoraReaderConverter> fedoraReaderConverter) {
        throw new RuntimeException("not implemented in spy factory");
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
