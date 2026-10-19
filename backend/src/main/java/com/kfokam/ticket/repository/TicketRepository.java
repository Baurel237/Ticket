package com.kfokam.ticket.repository;

import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByTitre(String titre);

    List<Ticket> findByStatutOrderByDateCreationDesc(Statut statut);

    List<Ticket> findAllByOrderByDateCreationDesc();

    Page<Ticket> findByStatutOrderByDateCreationDesc(Statut statut, Pageable pageable);

    Page<Ticket> findAllByOrderByDateCreationDesc(Pageable pageable);

    /**
     * RG-03 : ferme le ticket uniquement s'il n'est pas déjà FERME.
     * Renvoie le nombre de lignes modifiées : 0 signifie « introuvable ou
     * déjà fermé ». La condition est évaluée dans la même requête SQL que
     * l'écriture : deux fermetures concurrentes ne peuvent pas passer
     * toutes les deux (la deuxième modifie 0 ligne).
     */
    @Modifying
    @Query("UPDATE Ticket t SET t.statut = :ferme WHERE t.id = :id AND t.statut <> :ferme")
    int fermerSiOuvert(@Param("id") Long id, @Param("ferme") Statut ferme);
}
