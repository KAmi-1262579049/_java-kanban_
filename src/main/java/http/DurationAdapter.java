package http;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Duration;

// Класс DurationAdapter для преобразования Duration в JSON и обратно
public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    // Метод для преобразования продолжительности в количество минут для JSON
    @Override
    public JsonElement serialize(Duration duration, Type typeOfSrc, JsonSerializationContext context) {
        if (duration == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(duration.toMinutes());
    }

    // Метод для преобразования количества минут из JSON в Duration
    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json == null || json.isJsonNull()) {
            return Duration.ZERO;
        }
        return Duration.ofMinutes(json.getAsLong());
    }
}
