package com.kfokam.ticket.controller;

import com.kfokam.ticket.dto.TicketRequest;
import com.kfokam.ticket.dto.TicketResponse;
import com.kfokam.ticket.exception.ErreurApi;
import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Gestion des tickets de support")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un ticket", description = "Le ticket est créé avec le statut OUVERT.")
    @ApiResponse(responseCode = "201", description = "Ticket créé")
    @ApiResponse(responseCode = "400", description = "Titre absent ou de plus de 100 caractères",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    @ApiResponse(responseCode = "409", description = "RG-02 : titre déjà utilisé",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    public TicketResponse creer(@Valid @RequestBody TicketRequest requete) {
        return service.creer(requete);
    }

    @GetMapping
    @Operation(summary = "Lister les tickets", description = "Filtrable par statut (paramètre facultatif).")
    @ApiResponse(responseCode = "200", description = "Liste des tickets")
    public List<TicketResponse> lister(@RequestParam(required = false) Statut statut) {
        return service.lister(statut);
    }

    @PatchMapping("/{id}/fermer")
    @Operation(summary = "Fermer un ticket", description = "Possible uniquement si le ticket est OUVERT ou EN_COURS.")
    @ApiResponse(responseCode = "200", description = "Ticket fermé")
    @ApiResponse(responseCode = "404", description = "Ticket introuvable",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    @ApiResponse(responseCode = "409", description = "RG-03 : ticket déjà FERME",
            content = @Content(schema = @Schema(implementation = ErreurApi.class)))
    public TicketResponse fermer(@PathVariable Long id) {
        return service.fermer(id);
    }
}
