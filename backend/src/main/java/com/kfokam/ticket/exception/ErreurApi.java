package com.kfokam.ticket.exception;

import java.time.LocalDateTime;

public record ErreurApi(int status, String erreur, String message, LocalDateTime horodatage) {

    public static ErreurApi of(int status, String erreur, String message) {
        return new ErreurApi(status, erreur, message, LocalDateTime.now());
    }
}
