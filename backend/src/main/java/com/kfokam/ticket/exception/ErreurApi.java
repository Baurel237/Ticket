package com.kfokam.ticket.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record ErreurApi(int status, String erreur, String message,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Instant horodatage) {

    public static ErreurApi of(int status, String erreur, String message) {
        return new ErreurApi(status, erreur, message, Instant.now());
    }
}
