package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ObjectMapperSingleton {

  private static ObjectMapper instance;

  private ObjectMapperSingleton() {
    if (instance != null) {
      throw new IllegalStateException("Приватный констуктор!");
    }
  }

  public static ObjectMapper getInstance() {
    if (instance == null) {
      instance = new ObjectMapper();
      JavaTimeModule javaTimeModule = new JavaTimeModule();
      javaTimeModule.addSerializer(LocalDate.class,
          new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      instance.registerModule(javaTimeModule);
    }
    return instance;
  }
}
