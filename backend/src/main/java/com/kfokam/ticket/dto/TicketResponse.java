package com.kfokam.ticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.model.Ticket;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String titre,
        String description,
        Statut statut,
        // ISO-8601 avec fuseau explicite : le navigateur interprète correctement
        // la date quel que soit son fuseau.
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Instant dateCreation) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitre(),
                ticket.getDescription(),
                ticket.getStatut(),
                ticket.getDateCreation());
    }
}
