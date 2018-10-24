package se.uu.ub.cora.fedora.reader.converter;

import se.uu.ub.cora.bookkeeper.data.DataGroup;

import java.util.function.Function;

public class FedoraReadPositionConverter {
    public static Function<Long, Function<String, DataGroup>> convertFromStart(long start, Function<String, DataGroup> converter) {
        return index -> {
            if (index >= start) {
                return converter;
            }
            return null;
        };
    }

    public static Function<Long, Function<String, DataGroup>> convertFromStartToStop(long start, long stop, Function<String, DataGroup> converter) {
        return index -> {
            if (index < stop) {
                return convertFromStart(start, converter).apply(index);
            }
            return null;
        };
    }
}
