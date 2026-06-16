package http;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

// Класс LocalDateTimeAdapter для преобразования LocalDateTime в JSON и обратно
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    // Метод для преобразования даты и времени в строку для JSON
    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
        if (localDateTime == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(localDateTime.toString());
    }

    // Метод для преобразования строки из JSON в LocalDateTime
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        return LocalDateTime.parse(json.getAsString());
    }
}
