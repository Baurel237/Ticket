package com.kfokam.ticket.service;

import com.kfokam.ticket.dto.TicketRequest;
import com.kfokam.ticket.dto.TicketResponse;
import com.kfokam.ticket.exception.RegleDeGestionException;
import com.kfokam.ticket.exception.TicketIntrouvableException;
import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.model.Ticket;
import com.kfokam.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    // Bornes de la pagination optionnelle : évite les requêtes sans borne
    // tout en restant compatible avec le contrat simple (liste complète).
    static final int TAILLE_MAX_PAGE = 500;

    private final TicketRepository repository;
    private final Clock clock;

    @Autowired // deux constructeurs : celui-ci est celui de Spring
    public TicketService(TicketRepository repository) {
        this(repository, Clock.systemUTC());
    }

    // Overload utilisé en test pour une date de création déterministe.
    TicketService(TicketRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public TicketResponse creer(TicketRequest requete) {
        String titre = requete.titre();
        String description = normaliserDescription(requete.description());
        if (repository.existsByTitre(titre)) {
            throw new RegleDeGestionException(
                    "Le titre « " + titre + " » est déjà utilisé : deux tickets ne peuvent pas avoir le même titre.");
        }
        Ticket ticket = repository.saveAndFlush(new Ticket(titre, description, clock.instant()));
        log.info("Ticket créé : id={}, titre=\"{}\"", ticket.getId(), titre);
        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> lister(Statut statut, Integer page, Integer taille) {
        List<Ticket> tickets = (statut == null)
                ? repository.findAllByOrderByDateCreationDesc(pageable(page, taille)).getContent()
                : repository.findByStatutOrderByDateCreationDesc(statut, pageable(page, taille)).getContent();
        return tickets.stream().map(TicketResponse::from).toList();
    }

    @Transactional
    public TicketResponse fermer(Long id) {
        // Mise à jour conditionnelle : l'écriture porte elle-même la règle RG-03,
        // donc aucune fenêtre de concurrence entre lecture et écriture.
        int lignesModifiees = repository.fermerSiOuvert(id, Statut.FERME);
        if (lignesModifiees == 0) {
            Ticket dernier = repository.findById(id)
                    .orElseThrow(() -> new TicketIntrouvableException(id));
            // Le ticket existe et est forcément FERME (l'UPDATE n'a rien modifié).
            throw new RegleDeGestionException(
                    "Ce ticket est déjà fermé : un ticket fermé ne peut plus changer d'état.");
        }
        Ticket ticket = repository.findById(id).orElseThrow();
        log.info("Ticket fermé : id={}", id);
        return TicketResponse.from(ticket);
    }

    // page/taille facultatives : sans elles, la liste entière est renvoyée
    // (comportement initial inchangé). Bornes serrées pour la production.
    private static Pageable pageable(Integer page, Integer taille) {
        if (page == null && taille == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(
                page != null ? Math.max(page, 0) : 0,
                taille != null ? Math.min(Math.max(taille, 1), TAILLE_MAX_PAGE) : 50,
                Sort.by(Sort.Direction.DESC, "dateCreation"));
    }

    private static String normaliserDescription(String description) {
        return (description == null || description.isBlank()) ? null : description.trim();
    }
}
