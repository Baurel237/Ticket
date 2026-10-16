package com.kfokam.ticket.dto;

import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.model.Ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String titre,
        String description,
        Statut statut,
        LocalDateTime dateCreation) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitre(),
                ticket.getDescription(),
                ticket.getStatut(),
                ticket.getDateCreation());
    }
}
