package com.kfokam.ticket.service;

import com.kfokam.ticket.dto.TicketRequest;
import com.kfokam.ticket.dto.TicketResponse;
import com.kfokam.ticket.exception.RegleDeGestionException;
import com.kfokam.ticket.exception.TicketIntrouvableException;
import com.kfokam.ticket.model.Statut;
import com.kfokam.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TicketServiceTests {

    @Autowired
    private TicketService service;

    @Autowired
    private TicketRepository repository;

    @BeforeEach
    void nettoyer() {
        repository.deleteAll();
    }

    @Test
    void creerProduitUnTicketOuvertAvecDateServeur() {
        Instant avant = Instant.now().minusSeconds(5);

        TicketResponse ticket = service.creer(new TicketRequest("Imprimante en panne", "Étage 2"));

        assertThat(ticket.id()).isNotNull();
        assertThat(ticket.titre()).isEqualTo("Imprimante en panne");
        assertThat(ticket.statut()).isEqualTo(Statut.OUVERT);
        assertThat(ticket.dateCreation()).isNotNull().isBetween(avant, Instant.now().plusSeconds(5));
    }

    @Test
    void creerAvecTitreDupliqueLeveUneRegleDeGestion() {
        service.creer(new TicketRequest("Titre déjà pris", null));

        assertThatThrownBy(() -> service.creer(new TicketRequest("Titre déjà pris", "autre")))
                .isInstanceOf(RegleDeGestionException.class)
                .hasMessageContaining("déjà utilisé");
    }

    @Test
    void creerNormaliseTitreEtDescription() {
        TicketResponse ticket = service.creer(new TicketRequest("  Titre margoté  ", "   "));

        assertThat(ticket.titre()).isEqualTo("Titre margoté");
        assertThat(ticket.description()).isNull();
    }

    @Test
    void creerAvecTitreDifferentEspaceSeulNestPasUnDoublon() {
        service.creer(new TicketRequest("Serveur hors service", null));

        assertThatCode(() -> service.creer(new TicketRequest("Serveur  hors service", null)))
                .doesNotThrowAnyException();
    }

    @Test
    void fermerPasseLeTicketDeOuvertAFerme() {
        TicketResponse cree = service.creer(new TicketRequest("Ticket à fermer", null));

        TicketResponse ferme = service.fermer(cree.id());

        assertThat(ferme.id()).isEqualTo(cree.id());
        assertThat(ferme.statut()).isEqualTo(Statut.FERME);
    }

    @Test
    void fermerUnDejaFermeLeveUneRegleDeGestion() {
        TicketResponse cree = service.creer(new TicketRequest("Fermé deux fois", null));
        service.fermer(cree.id());

        assertThatThrownBy(() -> service.fermer(cree.id()))
                .isInstanceOf(RegleDeGestionException.class)
                .hasMessageContaining("déjà fermé");
    }

    @Test
    void fermerUnTicketInconnuLeveTicketIntrouvable() {
        assertThatThrownBy(() -> service.fermer(999_999L))
                .isInstanceOf(TicketIntrouvableException.class)
                .hasMessageContaining("999999");
    }

    @Test
    void listerSansFiltreRetourneTousLesTickets() {
        service.creer(new TicketRequest("Premier ticket", null));
        service.creer(new TicketRequest("Second ticket", null));

        List<TicketResponse> tickets = service.lister(null, null, null);

        assertThat(tickets).hasSize(2);
    }

    @Test
    void listerAvecFiltreNeRetourneQueLeStatutDemande() {
        TicketResponse aFermer = service.creer(new TicketRequest("Sera fermé", null));
        service.creer(new TicketRequest("Reste ouvert", null));
        service.fermer(aFermer.id());

        List<TicketResponse> ouverts = service.lister(Statut.OUVERT, null, null);
        List<TicketResponse> fermes = service.lister(Statut.FERME, null, null);

        assertThat(ouverts).hasSize(1).allMatch(t -> t.statut() == Statut.OUVERT);
        assertThat(fermes).hasSize(1).allMatch(t -> t.statut() == Statut.FERME);
    }
}
