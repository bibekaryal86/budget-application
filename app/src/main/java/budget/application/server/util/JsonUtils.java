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

  private static final ObjectMapper OBJECT_MAPPER = buildMapper();

  private JsonUtils() {}

  private static ObjectMapper buildMapper() {
    ObjectMapper objectMapper = CommonUtilities.objectMapperProvider();

    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    javaTimeModule.addDeserializer(
        LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    objectMapper.registerModule(javaTimeModule);

    SimpleModule simpleModuleUuid = new SimpleModule();
    simpleModuleUuid.addSerializer(
        UUID.class,
        new JsonSerializer<UUID>() {
          @Override
          public void serialize(
              UUID uuid, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
              throws IOException {
            jsonGenerator.writeString(uuid.toString());
          }
        });
    simpleModuleUuid.addDeserializer(
        UUID.class,
        new JsonDeserializer<UUID>() {
          @Override
          public UUID deserialize(
              JsonParser jsonParser, DeserializationContext deserializationContext)
              throws IOException {
            return UUID.fromString(jsonParser.getValueAsString());
          }
        });
    objectMapper.registerModule(simpleModuleUuid);

    return objectMapper;
  }

  public static String toJson(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("JSON serialization failed...", e);
    }
  }

  public static <T> T fromJson(String json, Class<T> type) {
    try {
      return OBJECT_MAPPER.readValue(json, type);
    } catch (IOException e) {
      throw new RuntimeException("JSON deserialization failed...", e);
    }
  }

  public static ObjectMapper mapper() {
    return OBJECT_MAPPER;
  }
}
