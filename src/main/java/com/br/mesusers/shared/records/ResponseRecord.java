package com.br.mesusers.shared.records;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseRecord<T>(
    boolean success,
    int statusCode,
    String message,
    T data
) {
    public static <T> ResponseRecord<T> success(T data) {
        return new ResponseRecord<>(true, 200, "Operação realizada com sucesso", data);
    }
    public static <T> ResponseRecord<T> success(String message, T data) {
        return new ResponseRecord<>(true, 200, message, data);
    }
    public static <T> ResponseRecord<T> error(int statusCode, String message) {
        return new ResponseRecord<>(false, statusCode, message, null);
    }
    public static <T> ResponseRecord<T> error(int statusCode, String message, T data) {
        return new ResponseRecord<>(false, statusCode, message, data);
    }
}