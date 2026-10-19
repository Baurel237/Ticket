package com.kfokam.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Données envoyées par le client pour créer un ticket")
public record TicketRequest(
        @Schema(description = "Titre du ticket (100 caractères maximum)", example = "Imprimante en panne")
        @NotBlank(message = "Le titre est obligatoire.")
        @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères.")
        String titre,

        @Schema(description = "Description facultative (2000 caractères maximum)", example = "L'imprimante du 2e étage ne répond plus.")
        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères.")
        String description) {

    // Le titre est normalisé avant la validation : la longueur et l'unicité
    // portent sur la valeur réellement enregistrée.
    public TicketRequest {
        titre = titre == null ? null : titre.trim();
    }
}
