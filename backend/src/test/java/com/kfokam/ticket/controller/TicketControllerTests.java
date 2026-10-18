package com.kfokam.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfokam.ticket.repository.TicketRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository repository;

    @BeforeEach
    void nettoyer() {
        repository.deleteAll();
    }

    private String json(String titre, String description) throws Exception {
        return objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("titre", titre);
            put("description", description);
        }});
    }

    // --- POST /api/tickets ---

    @Test
    void postCreeUnTicket201AvecEnTeteLocation() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Imprimante en panne", "Étage 2")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.matchesPattern("/api/tickets/\\d+")))
                .andExpect(jsonPath("$.titre").value("Imprimante en panne"))
                .andExpect(jsonPath("$.statut").value("OUVERT"))
                .andExpect(jsonPath("$.dateCreation").isNotEmpty());
    }

    @Test
    void postSansTitreRenvoie400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("   ", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Le titre est obligatoire."))
                .andExpect(jsonPath("$.horodatage").isNotEmpty());
    }

    @Test
    void postAvecTitreDePlusDe100CaracteresRenvoie400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("a".repeat(101), null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le titre ne doit pas dépasser 100 caractères."));
    }

    @Test
    void postAvecTitreDe100CaracteresEstAccepte() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("a".repeat(100), null)))
                .andExpect(status().isCreated());
    }

    @Test
    void postAvecTitreDupliqueRenvoie409() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Doublon impossible", null)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Doublon impossible", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erreur").value("Règle de gestion violée"))
                .andExpect(jsonPath("$.message").value(
                        "Le titre « Doublon impossible » est déjà utilisé : deux tickets ne peuvent pas avoir le même titre."));
    }

    @Test
    void postAvecCorpsNonJsonRenvoie400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ceci n'est pas du json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Le corps de la requête est absent ou n'est pas un JSON valide."));
    }

    // --- GET /api/tickets ---

    @Test
    void getRenvoie200AvecLaListeComplete() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content(json("Ticket A", null)));
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content(json("Ticket B", null)));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAvecFiltreStatutNeRetourneQueCeStatut() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content(json("À fermer", null)));
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content(json("Ouvert tout le temps", null)));

        Long id = repository.findByStatutOrderByDateCreationDesc(
                com.kfokam.ticket.model.Statut.OUVERT).get(0).getId();
        mockMvc.perform(patch("/api/tickets/" + id + "/fermer"));

        mockMvc.perform(get("/api/tickets").param("statut", "FERME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].statut").value("FERME"));

        mockMvc.perform(get("/api/tickets").param("statut", "OUVERT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].statut").value("OUVERT"));
    }

    @Test
    void getAvecStatutInvalideRenvoie400AvecValeursAcceptees() throws Exception {
        mockMvc.perform(get("/api/tickets").param("statut", "INCONNU"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Valeur invalide pour le paramètre « statut ». Valeurs acceptées : OUVERT, EN_COURS, FERME."));
    }

    // --- PATCH /api/tickets/{id}/fermer ---

    @Test
    void patchFermeUnTicket200() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content(json("À fermer", null)));
        Long id = repository.findAll().get(0).getId();

        mockMvc.perform(patch("/api/tickets/" + id + "/fermer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("FERME"));
    }

    @Test
    void patchSurTicketInconnuRenvoie404() throws Exception {
        mockMvc.perform(patch("/api/tickets/999999/fermer"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erreur").value("Ticket introuvable"))
                .andExpect(jsonPath("$.message").value("Aucun ticket avec l'identifiant 999999."));
    }

    @Test
    void patchSurTicketDejaFermeRenvoie409() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content(json("Fermé deux fois", null)));
        Long id = repository.findAll().get(0).getId();
        mockMvc.perform(patch("/api/tickets/" + id + "/fermer")).andExpect(status().isOk());

        mockMvc.perform(patch("/api/tickets/" + id + "/fermer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Ce ticket est déjà fermé : un ticket fermé ne peut plus changer d'état."));
    }

    @Test
    void patchAvecIdNonNumeriqueRenvoie400() throws Exception {
        mockMvc.perform(patch("/api/tickets/abc/fermer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valeur invalide pour le paramètre « id »."));
    }
}
