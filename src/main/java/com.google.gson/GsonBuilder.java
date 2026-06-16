package com.google.gson;

// Класс для настройки и создания объекта Gson
public class GsonBuilder {

    // Метод включает сериализацию полей со значением null
    public GsonBuilder serializeNulls() {
        return this;
    }

    // Метод включает форматированный вывод JSON
    public GsonBuilder setPrettyPrinting() {
        return this;
    }

    // Метод регистрирует адаптер для указанного типа данных
    public GsonBuilder registerTypeAdapter(Class<?> type, Object typeAdapter) {
        return this;
    }

    // Метод создаёт и возвращает объект Gson
    public Gson create() {
        return new Gson();
    }
}
