package budget.application.server.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.bibekaryal86.shdsvc.helpers.CommonUtilities;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class JsonUtils {

  private static final ObjectMapper mapper = buildMapper();

  private JsonUtils() {}

  private static ObjectMapper buildMapper() {
    ObjectMapper m = CommonUtilities.objectMapperProvider();

    m.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    JavaTimeModule timeModule = new JavaTimeModule();
    timeModule.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    timeModule.addDeserializer(
        LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    m.registerModule(timeModule);

    SimpleModule uuidModule = new SimpleModule();
    uuidModule.addSerializer(
        UUID.class,
        new JsonSerializer<UUID>() {
          @Override
          public void serialize(UUID value, JsonGenerator gen, SerializerProvider serializers)
              throws IOException {
            gen.writeString(value.toString());
          }
        });
    uuidModule.addDeserializer(
        UUID.class,
        new JsonDeserializer<UUID>() {
          @Override
          public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return UUID.fromString(p.getValueAsString());
          }
        });
    m.registerModule(uuidModule);

    return m;
  }

  public static String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON serialization failed...", e);
    }
  }

  public static <T> T fromJson(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    } catch (IOException e) {
      throw new RuntimeException("JSON deserialization failed...", e);
    }
  }

  public static ObjectMapper mapper() {
    return mapper;
  }
}
