package com.kfokam.ticket.repository;

import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByTitre(String titre);

    List<Ticket> findByStatutOrderByDateCreationDesc(Statut statut);

    List<Ticket> findAllByOrderByDateCreationDesc();
}
