package me.cprox.practice.util.external.json;

import com.google.gson.JsonObject;

public interface JsonSerializer<T> {

    JsonObject serialize(T t);

}
