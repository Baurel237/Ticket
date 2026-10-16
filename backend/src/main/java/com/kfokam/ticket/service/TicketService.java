package com.kfokam.ticket.service;

import com.kfokam.ticket.dto.TicketRequest;
import com.kfokam.ticket.dto.TicketResponse;
import com.kfokam.ticket.exception.RegleDeGestionException;
import com.kfokam.ticket.exception.TicketIntrouvableException;
import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.model.Ticket;
import com.kfokam.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TicketResponse creer(TicketRequest requete) {
        String titre = requete.titre().trim();
        if (repository.existsByTitre(titre)) {
            throw new RegleDeGestionException(
                    "RG-02 : un ticket avec le titre « " + titre + " » existe déjà.");
        }
        Ticket ticket = repository.saveAndFlush(new Ticket(titre, requete.description()));
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
                    "RG-03 : le ticket " + id + " est déjà FERME, il ne peut plus changer d'état.");
        }
        ticket.setStatut(Statut.FERME);
        return TicketResponse.from(ticket);
    }
}
