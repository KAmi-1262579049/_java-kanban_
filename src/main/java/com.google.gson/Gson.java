package com.google.gson;

import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Класс Gson для преобразования задач в JSON и обратно
public class Gson {

    // Метод преобразует объект в JSON-строку
    public String toJson(Object object) {
        if (object == null) {
            return "null";
        }
        if (object instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
        }
        if (object instanceof Subtask subtask) {
            return taskToJson(subtask, true);
        }
        if (object instanceof Epic epic) {
            return taskToJson(epic, false);
        }
        if (object instanceof Task task) {
            return taskToJson(task, false);
        }
        if (object instanceof String string) {
            return quote(string);
        }
        return String.valueOf(object);
    }

    // Метод преобразует JSON-строку в объект нужного класса
    public <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null || json.isBlank() || "null".equals(json.trim())) {
            return null;
        }
        Map<String, String> fields = parseObject(json.trim());
        if (classOfT == Task.class) {
            return classOfT.cast(createTask(fields));
        }
        if (classOfT == Subtask.class) {
            return classOfT.cast(createSubtask(fields));
        }
        if (classOfT == Epic.class) {
            return classOfT.cast(createEpic(fields));
        }
        throw new IllegalArgumentException("Unsupported class: " + classOfT.getName());
    }

    // Метод преобразует задачу, подзадачу или эпик в JSON
    private String taskToJson(Task task, boolean withEpicId) {
        StringBuilder builder = new StringBuilder("{");
        builder.append(field("id", task.getId())).append(',');
        builder.append(field("name", task.getName())).append(',');
        builder.append(field("description", task.getDescription())).append(',');
        builder.append(field("status", task.getStatus().name())).append(',');
        builder.append(field("type", task.getType().name())).append(',');
        builder.append(field("duration", task.getDuration().toMinutes())).append(',');
        builder.append(field("startTime", task.getStartTime() == null ? null : task.getStartTime().toString()));
        if (withEpicId) {
            builder.append(',').append(field("epicId", ((Subtask) task).getEpicId()));
        }
        builder.append('}');
        return builder.toString();
    }

    // Метод создаёт JSON-поле со строковым значением
    private String field(String name, String value) {
        return quote(name) + ":" + quote(value);
    }

    // Метод создаёт JSON-поле с числовым значением
    private String field(String name, long value) {
        return quote(name) + ":" + value;
    }

    // Метод добавляет кавычки к строке и экранирует специальные символы
    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    // Метод создаёт обычную задачу из полей JSON
    private Task createTask(Map<String, String> fields) {
        Task task = new Task(getInt(fields, "id"), getString(fields, "name"), getString(fields, "description"),
                getStatus(fields), getDuration(fields), getStartTime(fields));
        return task;
    }

    // Метод создаёт эпик из полей JSON
    private Epic createEpic(Map<String, String> fields) {
        Epic epic = new Epic(getInt(fields, "id"), getString(fields, "name"), getString(fields, "description"));
        epic.setStatus(getStatus(fields));
        return epic;
    }

    // Метод создаёт подзадачу из полей JSON
    private Subtask createSubtask(Map<String, String> fields) {
        return new Subtask(getInt(fields, "id"), getString(fields, "name"), getString(fields, "description"),
                getStatus(fields), getInt(fields, "epicId"), getDuration(fields), getStartTime(fields));
    }

    // Метод получает статус задачи из полей JSON
    private TaskStatus getStatus(Map<String, String> fields) {
        String status = fields.get("status");
        return status == null || status.isBlank() ? TaskStatus.NEW : TaskStatus.valueOf(status);
    }

    // Метод получает продолжительность задачи из полей JSON
    private Duration getDuration(Map<String, String> fields) {
        String duration = fields.get("duration");
        if (duration == null || duration.isBlank()) {
            return Duration.ZERO;
        }
        return Duration.ofMinutes(Long.parseLong(duration));
    }

    // Метод получает дату и время начала задачи из полей JSON
    private LocalDateTime getStartTime(Map<String, String> fields) {
        String startTime = fields.get("startTime");
        if (startTime == null || startTime.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(startTime);
    }

    // Метод получает целое число из полей JSON
    private int getInt(Map<String, String> fields, String name) {
        String value = fields.get(name);
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    // Метод получает строку из полей JSON
    private String getString(Map<String, String> fields, String name) {
        String value = fields.get(name);
        return value == null ? "" : value;
    }

    // Метод разбирает JSON-объект и возвращает карту полей
    private Map<String, String> parseObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        String body = json.substring(1, json.length() - 1);
        int index = 0;
        while (index < body.length()) {
            int keyStart = body.indexOf('"', index);
            if (keyStart < 0) {
                break;
            }
            int keyEnd = findStringEnd(body, keyStart + 1);
            String key = unquote(body.substring(keyStart + 1, keyEnd));
            int colon = body.indexOf(':', keyEnd);
            int valueStart = colon + 1;
            while (valueStart < body.length() && Character.isWhitespace(body.charAt(valueStart))) {
                valueStart++;
            }
            String value;
            if (valueStart < body.length() && body.charAt(valueStart) == '"') {
                int valueEnd = findStringEnd(body, valueStart + 1);
                value = unquote(body.substring(valueStart + 1, valueEnd));
                index = valueEnd + 1;
            } else {
                int valueEnd = body.indexOf(',', valueStart);
                if (valueEnd < 0) {
                    valueEnd = body.length();
                }
                value = body.substring(valueStart, valueEnd).trim();
                if ("null".equals(value)) {
                    value = null;
                }
                index = valueEnd;
            }
            result.put(key, value);
            index++;
        }
        return result;
    }

    // Метод ищет конец строки внутри JSON с учётом экранирования
    private int findStringEnd(String value, int start) {
        boolean escaped = false;
        for (int i = start; i < value.length(); i++) {
            char symbol = value.charAt(i);
            if (symbol == '"' && !escaped) {
                return i;
            }
            escaped = symbol == '\\' && !escaped;
            if (symbol != '\\') {
                escaped = false;
            }
        }
        throw new IllegalArgumentException("Invalid json string");
    }

    // Метод убирает экранирование из JSON-строки
    private String unquote(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
