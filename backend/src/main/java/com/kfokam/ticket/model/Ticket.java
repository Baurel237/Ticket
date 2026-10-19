package com.kfokam.ticket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Les contraintes sont répliquées sur l'entité (renforce le DTO) : toute
    // création d'un Ticket hors du POST est elle aussi bornée.
    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères.")
    @Column(nullable = false, length = 100, unique = true)
    private String titre;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères.")
    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    protected Ticket() {
    }

    public Ticket(String titre, String description, Instant dateCreation) {
        this.titre = titre;
        this.description = description;
        this.statut = Statut.OUVERT;
        this.dateCreation = dateCreation;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public Statut getStatut() {
        return statut;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }
}
