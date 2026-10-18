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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TicketResponse creer(TicketRequest requete) {
        String titre = requete.titre();
        String description = normaliserDescription(requete.description());
        if (repository.existsByTitre(titre)) {
            throw new RegleDeGestionException(
                    "Le titre « " + titre + " » est déjà utilisé : deux tickets ne peuvent pas avoir le même titre.");
        }
        Ticket ticket = repository.saveAndFlush(new Ticket(titre, description));
        log.info("Ticket créé : id={}, titre=\"{}\"", ticket.getId(), titre);
        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> lister(Statut statut) {
        List<Ticket> tickets = (statut == null)
                ? repository.findAllByOrderByDateCreationDesc()
                : repository.findByStatutOrderByDateCreationDesc(statut);
        return tickets.stream().map(TicketResponse::from).toList();
    }

    @Transactional
    public TicketResponse fermer(Long id) {
        Ticket ticket = repository.findById(id).orElseThrow(() -> new TicketIntrouvableException(id));
        if (ticket.getStatut() == Statut.FERME) {
            throw new RegleDeGestionException(
                    "Ce ticket est déjà fermé : un ticket fermé ne peut plus changer d'état.");
        }
        ticket.setStatut(Statut.FERME);
        log.info("Ticket fermé : id={}", id);
        return TicketResponse.from(ticket);
    }

    private static String normaliserDescription(String description) {
        return (description == null || description.isBlank()) ? null : description.trim();
    }
}
