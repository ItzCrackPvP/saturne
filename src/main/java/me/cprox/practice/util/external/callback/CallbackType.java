package me.cprox.practice.util.external.callback;

public enum CallbackType {

    WRONG_RESPONSE("License Not Found"),
    PAGE_ERROR("Site Not Found"),
    URL_ERROR("Site Not Found"),
    KEY_OUTDATED("License Not Found"),
    INCORRECT_KEY("License Not Found"),
    NOT_VALID_IP("IP Not Found"),
    INVALID_PLUGIN("Plugin Not Found"),
    VALID("License Valid");

    CallbackType(String s) {
    }
}
