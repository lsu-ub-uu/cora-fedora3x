package se.uu.ub.cora.fedora.reader.converter;

import org.testng.annotations.Test;
import se.uu.ub.cora.bookkeeper.data.DataGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;

public class FedoraReadPositionConverterTest {
    @Test
    void testConvertAll() {
        List<String> data = new ArrayList<>();
        data.add("a");
        data.add("b");
        data.add("c");
        data.add("d");
        data.add("e");

        Function<String, DataGroup> nullConverter = str -> null;

        Function<Long, Function<String, DataGroup>> converter =
                FedoraReadPositionConverter.convertFromStart(0, nullConverter);

        List<DataGroup> result = runConverterOnList(data, converter);

        assertEquals(result.size(), data.size());
    }

    @Test
    void testConvertFromStart() {
        List<String> data = new ArrayList<>();
        data.add("a");
        data.add("b");
        data.add("c");
        data.add("d");
        data.add("e");

        Function<String, DataGroup> nullConverter = str -> null;

        Function<Long, Function<String, DataGroup>> converter =
                FedoraReadPositionConverter.convertFromStart(2, nullConverter);

        List<DataGroup> result = runConverterOnList(data, converter);

        assertEquals(result.size(), 3);
    }


    @Test
    void testConvertFromStartToStop() {
        List<String> data = new ArrayList<>();
        data.add("a");
        data.add("b");
        data.add("c");
        data.add("d");
        data.add("e");

        Function<String, DataGroup> nullConverter = str -> null;

        Function<Long, Function<String, DataGroup>> converter =
                FedoraReadPositionConverter.convertFromStartToStop(2, 4, nullConverter);

        List<DataGroup> result = runConverterOnList(data, converter);

        assertEquals(result.size(), 2);
    }

    private List<DataGroup> runConverterOnList(List<String> data, Function<Long, Function<String, DataGroup>> converter) {
        Long pos = 0L;
        List<DataGroup> result = new ArrayList<>();
        for (var elem : data) {
            var converterForElem = converter.apply(pos);
            if (converterForElem != null) {
                result.add(converterForElem.apply(elem));
            }
            pos++;
        }
        return result;
    }

}
