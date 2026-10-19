package com.kfokam.ticket.exception;

public class TicketIntrouvableException extends RuntimeException {

    public TicketIntrouvableException(Long id) {
        super("Aucun ticket avec l'identifiant " + id + ".");
    }
}
