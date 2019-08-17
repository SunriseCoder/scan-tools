package utils;

import java.io.File;
import java.io.IOException;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class JSONUtils {

    public static <T> T loadFromDisk(File file, TypeReference<T> typeReference) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
        T result = mapper.readValue(file, typeReference);
        return result;
    }

    public static void saveToDisk(Object object, File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        mapper.setTimeZone(TimeZone.getDefault());
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
    }
}
